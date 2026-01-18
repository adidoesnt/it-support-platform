package com.adityabanerjee.worker.workflowRuns;

import java.math.BigInteger;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;

@Table("workflow_runs")
public record WorkflowRun(
        @Id BigInteger id,
        BigInteger incidentId,
        WorkflowStep currentStep,
        WorkflowStatus status,
        @CreatedDate LocalDateTime createdAt,
        @LastModifiedDate LocalDateTime updatedAt) {
}
