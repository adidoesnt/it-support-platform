package com.adityabanerjee.worker.sqs;

import java.math.BigInteger;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.adityabanerjee.worker.workflowRuns.WorkflowRun;
import com.adityabanerjee.worker.workflowRuns.WorkflowRunRepository;
import com.adityabanerjee.worker.workflowRuns.WorkflowStatus;

@Service
public class WorkflowFailureService {
    private final WorkflowRunRepository workflowRunRepository;

    public WorkflowFailureService(WorkflowRunRepository workflowRunRepository) {
        this.workflowRunRepository = workflowRunRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markWorkflowRunAsFailed(BigInteger workflowRunId) {
        WorkflowRun workflowRun = workflowRunRepository.findById(workflowRunId)
                .orElseThrow(() -> new RuntimeException("Workflow run not found"));

        WorkflowRun updatedWorkflowRun = new WorkflowRun(
                workflowRun.id(),
                workflowRun.incidentId(),
                workflowRun.currentStep(),
                WorkflowStatus.FAILED,
                workflowRun.createdAt(),
                workflowRun.updatedAt());
        workflowRunRepository.save(updatedWorkflowRun);
    }
}
