package com.adityabanerjee.api.incidents.responses;

import java.math.BigInteger;

public class CreateIncidentResponse {
    private final BigInteger workflowRunId;

    public CreateIncidentResponse(BigInteger workflowRunId) {
        this.workflowRunId = workflowRunId;
    }

    public BigInteger getWorkflowRunId() {
        return workflowRunId;
    }
}
