package com.adityabanerjee.worker.sqs;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adityabanerjee.worker.classifier.IncidentClassification;
import com.adityabanerjee.worker.classifier.IncidentClassificationRepository;
import com.adityabanerjee.worker.classifier.IncidentClassifier;
import com.adityabanerjee.worker.classifier.ClassificationResult;
import com.adityabanerjee.worker.incidents.Incident;
import com.adityabanerjee.worker.incidents.IncidentRepository;
import com.adityabanerjee.worker.tickets.Ticket;
import com.adityabanerjee.worker.tickets.TicketStatus;
import com.adityabanerjee.worker.tickets.TicketRepository;
import com.adityabanerjee.worker.workflowRuns.WorkflowRun;
import com.adityabanerjee.worker.workflowRuns.WorkflowRunRepository;
import com.adityabanerjee.worker.workflowRuns.WorkflowStatus;
import com.adityabanerjee.worker.workflowRuns.WorkflowStep;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class WorkflowProcessor {
    private final WorkflowRunRepository workflowRunRepository;
    private final WorkflowFailureService workflowFailureService;
    private final IncidentClassificationRepository incidentClassificationRepository;
    private final IncidentClassifier incidentClassifier;
    private final IncidentRepository incidentRepository;
    private final TicketRepository ticketRepository;

    public WorkflowProcessor(WorkflowRunRepository workflowRunRepository,
            IncidentClassificationRepository incidentClassificationRepository,
            IncidentClassifier incidentClassifier,
            IncidentRepository incidentRepository,
            WorkflowFailureService workflowFailureService,
            TicketRepository ticketRepository) {
        this.workflowRunRepository = workflowRunRepository;
        this.incidentClassificationRepository = incidentClassificationRepository;
        this.incidentClassifier = incidentClassifier;
        this.workflowFailureService = workflowFailureService;
        this.incidentRepository = incidentRepository;
        this.ticketRepository = ticketRepository;
    }

    @Transactional
    public StepResult processWorkflowRunById(BigInteger workflowRunId) {
        try {
            Optional<WorkflowRun> workflowRunOpt = workflowRunRepository.findById(workflowRunId);

            if (workflowRunOpt.isEmpty()) {
                System.out.println(String.format("[WARNING] Workflow run %s not found, skipping", workflowRunId));
                // The workflow run is not found -> nothing to do
                // Just delete the message
                return new StepResult(true, false);
            }

            WorkflowRun workflowRun = workflowRunOpt.get();
            WorkflowStatus workflowRunStatus = workflowRun.status();

            if (workflowRunStatus == WorkflowStatus.COMPLETED || workflowRunStatus == WorkflowStatus.FAILED) {
                System.out.println(String.format(
                        "[WARNING] Workflow run %s already completed or failed, skipping", workflowRunId));
                // The workflow run is already completed or failed -> nothing to do
                // Just delete the message
                return new StepResult(true, false);
            }

            WorkflowStep workflowRunStep = workflowRun.currentStep();
            StepResult stepResult = switch (workflowRunStep) {
                case PAYLOAD_VALIDATION -> processPayloadValidation(workflowRun);
                case INCIDENT_CLASSIFICATION -> processIncidentClassification(workflowRun);
                case TICKET_CREATION -> processTicketCreation(workflowRun);
                default -> {
                    System.out.println(String.format(
                            "[WARNING] Workflow run %s invalid step, expected %s but got %s", workflowRunId,
                            WorkflowStep.PAYLOAD_VALIDATION, workflowRunStep));
                    // The workflow run is not in the payload validation step -> nothing to do
                    // Just delete the message
                    yield new StepResult(true, false);
                }
            };

            return stepResult;
        } catch (Exception e) {
            System.out.println(String.format("Error processing message for workflow run %s: %s",
                    workflowRunId, e.getMessage()));

            try {
                workflowFailureService.markWorkflowRunAsFailed(workflowRunId);
            } catch (Exception ignored) {
                System.out.println(String.format("Error marking workflow run %s as failed: %s", workflowRunId,
                        ignored.getMessage()));
            }

            // Re-throw so that "Transactional" rolls back the transaction
            throw e;
        }
    }

    private StepResult processPayloadValidation(WorkflowRun workflowRun) {
        System.out.println(String.format("Processing payload validation for workflow run %s", workflowRun.id()));

        // Update the workflow run status to in progress
        WorkflowRun updatedStatusWorkflowRun = new WorkflowRun(
                workflowRun.id(),
                workflowRun.incidentId(),
                workflowRun.currentStep(),
                WorkflowStatus.IN_PROGRESS, // Only update the status
                workflowRun.createdAt(),
                workflowRun.updatedAt());
        workflowRunRepository.save(updatedStatusWorkflowRun);

        // TODO: Implement payload validation
        // For now, we only have the workflow ID and we get the corresponding data from
        // the DB
        // As such, there is nothing to do here

        // Update the workflow run status to in progress and step to incident
        // classification
        WorkflowRun updatedStepWorkflowRun = new WorkflowRun(
                updatedStatusWorkflowRun.id(),
                updatedStatusWorkflowRun.incidentId(),
                WorkflowStep.INCIDENT_CLASSIFICATION, // Only update the step, keep the status
                updatedStatusWorkflowRun.status(),
                updatedStatusWorkflowRun.createdAt(),
                updatedStatusWorkflowRun.updatedAt());
        workflowRunRepository.save(updatedStepWorkflowRun);

        return new StepResult(true, true);
    }

    private StepResult processIncidentClassification(WorkflowRun workflowRun) {
        System.out
                .println(String.format("Processing incident classification for workflow run %s", workflowRun.id()));

        // Check for an existing incident classification
        Optional<IncidentClassification> incidentClassificationOpt = incidentClassificationRepository
                .findByWorkflowRunId(workflowRun.id());
        if (incidentClassificationOpt.isPresent()) {
            System.out.println(String.format("Incident classification already exists for workflow run %s, skipping",
                    workflowRun.id()));
            // The incident classification already exists -> nothing to do
            // Just delete the message
            return new StepResult(true, false);
        }

        // Classify the incident
        Incident incident = incidentRepository.findById(workflowRun.incidentId()).orElseThrow(
                () -> new RuntimeException(String.format("Incident %s not found", workflowRun.incidentId())));
        ClassificationResult classificationResult = incidentClassifier.classifyIncident(incident.description());

        // Save the incident classification
        IncidentClassification incidentClassification = new IncidentClassification(
                null,
                workflowRun.id(),
                workflowRun.incidentId(),
                classificationResult.category(),
                classificationResult.priority(),
                classificationResult.summary(),
                null, // TODO: Add model provider
                null, // TODO: Add model name
                null, // TODO: Add raw response
                LocalDateTime.now(),
                LocalDateTime.now());
        incidentClassificationRepository.save(incidentClassification);

        // Update the workflow step to ticket creation
        WorkflowRun updatedStepWorkflowRun = new WorkflowRun(
                workflowRun.id(),
                workflowRun.incidentId(),
                WorkflowStep.TICKET_CREATION,
                workflowRun.status(),
                workflowRun.createdAt(),
                workflowRun.updatedAt());
        workflowRunRepository.save(updatedStepWorkflowRun);

        return new StepResult(true, true);
    }

    private StepResult processTicketCreation(WorkflowRun workflowRun) {
        System.out.println(String.format("Processing ticket creation for workflow run %s", workflowRun.id()));

        Incident incident = incidentRepository.findById(workflowRun.incidentId()).orElseThrow(
                () -> new RuntimeException(String.format("Incident %s not found", workflowRun.incidentId())));
        IncidentClassification incidentClassification = incidentClassificationRepository
                .findByWorkflowRunId(workflowRun.id()).orElseThrow(
                        () -> new RuntimeException(
                                String.format("Incident classification %s not found", workflowRun.id())));

        String ticketTitle = String.format("[%s] [%s] %s", incidentClassification.priority(),
                incidentClassification.category(), incidentClassification.summary());
        Ticket ticket = new Ticket(
                null,
                workflowRun.incidentId(),
                workflowRun.id(),
                ticketTitle,
                incident.description(),
                TicketStatus.OPEN,
                LocalDateTime.now(),
                LocalDateTime.now());
        ticketRepository.save(ticket);

        // Update the workflow status to completed
        WorkflowRun updatedStepWorkflowRun = new WorkflowRun(
                workflowRun.id(),
                workflowRun.incidentId(),
                workflowRun.currentStep(),
                WorkflowStatus.COMPLETED, // Only update the status
                workflowRun.createdAt(),
                workflowRun.updatedAt());
        workflowRunRepository.save(updatedStepWorkflowRun);

        // This is the last step, so we don't enqueue the next step
        return new StepResult(true, false);
    }
}
