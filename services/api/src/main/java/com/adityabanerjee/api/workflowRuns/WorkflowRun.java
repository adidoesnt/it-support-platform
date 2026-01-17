package com.adityabanerjee.api.workflowRuns;

import java.math.BigInteger;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;

public record WorkflowRun(
        @Id BigInteger id,
        BigInteger incidentId,
        WorkflowStep currentStep,
        WorkflowStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

}
