package com.adityabanerjee.api.incidents;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

import java.math.BigInteger;
import java.util.Optional;

import com.adityabanerjee.api.incidents.responses.CreateIncidentResponse;
import com.adityabanerjee.api.idempotencyKeys.IdempotencyKeyRepository;
import com.adityabanerjee.api.idempotencyKeys.IdempotencyKey;
import com.adityabanerjee.api.workflowRuns.WorkflowRunRepository;
import com.adityabanerjee.api.workflowRuns.WorkflowRun;
import com.adityabanerjee.api.workflowRuns.WorkflowStep;
import com.adityabanerjee.api.workflowRuns.WorkflowStatus;
import com.adityabanerjee.api.metrics.Metric;

import jakarta.servlet.http.HttpServletRequest;
import com.adityabanerjee.api.sqs.WorkflowEnqueuer;

@RestController
@RequestMapping("/incidents")
public class IncidentController {
    private final IncidentRepository incidentRepository;
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final WorkflowRunRepository workflowRunRepository;
    private final WorkflowEnqueuer workflowEnqueuer;
    private final Counter incidentsReceived;

    public IncidentController(IncidentRepository incidentRepository,
            IdempotencyKeyRepository idempotencyKeyRepository,
            WorkflowRunRepository workflowRunRepository,
            WorkflowEnqueuer workflowEnqueuer,
            MeterRegistry meterRegistry) {
        this.incidentRepository = incidentRepository;
        this.idempotencyKeyRepository = idempotencyKeyRepository;
        this.workflowRunRepository = workflowRunRepository;
        this.workflowEnqueuer = workflowEnqueuer;
        this.incidentsReceived = Counter
                .builder(Metric.INCIDENTS_RECEIVED.getValue())
                .description("Total reported incidents")
                .register(meterRegistry);
    }

    @PostMapping
    @Transactional
    public ResponseEntity<CreateIncidentResponse> createIncident(@RequestBody Incident incident,
            HttpServletRequest request) {
        // Increment the incidents received counter
        incidentsReceived.increment();

        // Check for idempotency (using the idempotency key header)
        String idempotencyKey = request.getHeader("Idempotency-Key");
        System.out.println(String.format("Idempotency key: %s", idempotencyKey));

        if (idempotencyKey == null) {
            System.out.println("Idempotency key is missing");
            return ResponseEntity.badRequest().body(new CreateIncidentResponse(null));
        }

        Optional<IdempotencyKey> idempotencyKeyEntity = idempotencyKeyRepository.findById(idempotencyKey);
        System.out
                .println(String.format("Existing idempotency key entity: %s",
                        idempotencyKeyEntity.map(IdempotencyKey::workflowRunId).orElse(null)));
        if (idempotencyKeyEntity.isPresent()) {
            BigInteger workflowRunId = idempotencyKeyEntity.get().workflowRunId();
            System.out.println(String.format("Idempotency key %s already , returning existing workflow run id %s",
                    idempotencyKey, workflowRunId));
            return ResponseEntity.ok(new CreateIncidentResponse(workflowRunId));
        }

        // Create a new incident
        Incident savedIncident = incidentRepository.save(incident);
        System.out.println(String.format("Saved incident with id %s", savedIncident.id()));

        // Create a new workflow and associate it with the idempotency key
        WorkflowRun workflowRun = new WorkflowRun(
                null,
                savedIncident.id(),
                WorkflowStep.PAYLOAD_VALIDATION,
                WorkflowStatus.PENDING,
                null,
                null);
        WorkflowRun savedWorkflowRun = workflowRunRepository.save(workflowRun);
        System.out.println(String.format("Saved workflow run with id %s", savedWorkflowRun.id()));

        // Insert the idempotency key and associate it with the workflow run
        try {
            IdempotencyKey savedIdempotencyKey = idempotencyKeyRepository
                    .save(new IdempotencyKey(idempotencyKey, savedWorkflowRun.id(), null, null));
            System.out.println(String.format("Saved idempotency key with id %s", savedIdempotencyKey.key()));
        } catch (DuplicateKeyException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            System.out.println(
                    String.format("Duplicate idempotency key %s found, rolling back transaction", idempotencyKey));

            return idempotencyKeyRepository.findById(idempotencyKey)
                    .map(key -> ResponseEntity.ok(new CreateIncidentResponse(key.workflowRunId())))
                    .orElseThrow();
        }

        // Enqueue only after the transaction commits to avoid race conditions
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    workflowEnqueuer.enqueueWorkflow(savedWorkflowRun.id());
                } catch (Exception e) {
                    System.out.println(String.format(
                            "Failed to enqueue workflow run %s after commit: %s",
                            savedWorkflowRun.id(),
                            e.getMessage()));
                }
            }
        });

        return ResponseEntity.ok(new CreateIncidentResponse(savedWorkflowRun.id()));
    }
}