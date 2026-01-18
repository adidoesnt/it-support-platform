package com.adityabanerjee.worker.sqs;

import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.annotation.Transactional;

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

    public WorkflowProcessor(WorkflowRunRepository workflowRunRepository) {
        this.workflowRunRepository = workflowRunRepository;
    }

    @Transactional
    public boolean processWorkflowRunById(BigInteger workflowRunId) {
        try {
            Optional<WorkflowRun> workflowRunOpt = workflowRunRepository.findById(workflowRunId);

            if (workflowRunOpt.isEmpty()) {
                System.out.println(String.format("[WARNING] Workflow run %s not found, skipping", workflowRunId));
                // The workflow run is not found -> nothing to do
                // Just delete the message
                return true;
            }

            WorkflowRun workflowRun = workflowRunOpt.get();
            WorkflowStatus workflowRunStatus = workflowRun.status();

            if (workflowRunStatus != WorkflowStatus.PENDING) {
                System.out.println(String.format(
                        "[WARNING] Workflow run %s invalid status, expected %s but got %s", workflowRunId,
                        WorkflowStatus.PENDING, workflowRunStatus));
                // The workflow run is not in the pending status -> nothing to do
                // Just delete the message
                return true;
            }

            WorkflowStep workflowRunStep = workflowRun.currentStep();
            switch (workflowRunStep) {
                case PAYLOAD_VALIDATION:
                    return processPayloadValidation(workflowRun);
                case INCIDENT_CLASSIFICATION:
                    return processIncidentClassification(workflowRun);
                case TICKET_CREATION:
                    return processTicketCreation(workflowRun);
                default:
                    System.out.println(String.format(
                            "[WARNING] Workflow run %s invalid step, expected %s but got %s", workflowRunId,
                            WorkflowStep.PAYLOAD_VALIDATION, workflowRunStep));
                    // The workflow run is not in the payload validation step -> nothing to do
                    // Just delete the message
                    return true;
            }
        } catch (Exception e) {
            System.out.println(String.format("Error processing payload validation for workflow run %s: %s",
                    workflowRunId, e.getMessage()));
            System.out.println(String.format(
                    "Rolling back transaction for error processing payload validation for workflow run %s: %s",
                    workflowRunId, e.getMessage()));
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return false;
        }
    }

    private boolean processPayloadValidation(WorkflowRun workflowRun) {
        try {
            return true;
        } catch (Exception e) {
            System.out.println(String.format("Error processing payload validation for workflow run %s: %s",
                    workflowRun.id(), e.getMessage()));
            return false;
        }
    }

    private boolean processIncidentClassification(WorkflowRun workflowRun) {
        try {
            return true;
        } catch (Exception e) {
            System.out.println(String.format("Error processing incident classification for workflow run %s: %s",
                    workflowRun.id(), e.getMessage()));
            return false;
        }
    }

    private boolean processTicketCreation(WorkflowRun workflowRun) {
        try {
            return true;
        } catch (Exception e) {
            System.out.println(String.format("Error processing ticket creation for workflow run %s: %s",
                    workflowRun.id(), e.getMessage()));
            return false;
        }
    }
}
