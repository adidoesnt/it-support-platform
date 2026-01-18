package com.adityabanerjee.worker.sqs;

import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.annotation.Transactional;

import com.adityabanerjee.worker.workflowRuns.WorkflowRun;
import com.adityabanerjee.worker.workflowRuns.WorkflowRunRepository;
import com.adityabanerjee.worker.workflowRuns.WorkflowStatus;
import com.adityabanerjee.worker.workflowRuns.WorkflowStep;

import java.math.BigInteger;
import java.util.Optional;

@Service
public class WorkflowProcessor {
    private final WorkflowRunRepository workflowRunRepository;

    public WorkflowProcessor(WorkflowRunRepository workflowRunRepository) {
        this.workflowRunRepository = workflowRunRepository;
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

            if (workflowRunStatus != WorkflowStatus.PENDING) {
                System.out.println(String.format(
                        "[WARNING] Workflow run %s invalid status, expected %s but got %s", workflowRunId,
                        WorkflowStatus.PENDING, workflowRunStatus));
                // The workflow run is not in the pending status -> nothing to do
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
            System.out.println(String.format("Error processing payload validation for workflow run %s: %s",
                    workflowRunId, e.getMessage()));
            System.out.println(String.format(
                    "Rolling back transaction for error processing payload validation for workflow run %s: %s",
                    workflowRunId, e.getMessage()));
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return new StepResult(true, false);
        }
    }

    private StepResult processPayloadValidation(WorkflowRun workflowRun) {
        try {
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
        } catch (Exception e) {
            System.out.println(String.format("Error processing payload validation for workflow run %s: %s",
                    workflowRun.id(), e.getMessage()));
            return new StepResult(true, false);
        }
    }

    private StepResult processIncidentClassification(WorkflowRun workflowRun) {
        try {
            System.out
                    .println(String.format("Processing incident classification for workflow run %s", workflowRun.id()));

            // TODO: Implement incident classification

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
        } catch (Exception e) {
            System.out.println(String.format("Error processing incident classification for workflow run %s: %s",
                    workflowRun.id(), e.getMessage()));
            return new StepResult(true, false);
        }
    }

    private StepResult processTicketCreation(WorkflowRun workflowRun) {
        try {
            System.out.println(String.format("Processing ticket creation for workflow run %s", workflowRun.id()));

            // TODO: Implement ticket creation

            // Update the workflow status to completed
            WorkflowRun updatedStepWorkflowRun = new WorkflowRun(
                    workflowRun.id(),
                    workflowRun.incidentId(),
                    workflowRun.currentStep(),
                    WorkflowStatus.COMPLETED, // Only update the status
                    workflowRun.createdAt(),
                    workflowRun.updatedAt());
            workflowRunRepository.save(updatedStepWorkflowRun);

            return new StepResult(true, true);
        } catch (Exception e) {
            System.out.println(String.format("Error processing ticket creation for workflow run %s: %s",
                    workflowRun.id(), e.getMessage()));
            return new StepResult(true, false);
        }
    }
}
