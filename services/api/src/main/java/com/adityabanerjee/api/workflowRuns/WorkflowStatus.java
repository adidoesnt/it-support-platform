package com.adityabanerjee.api.workflowRuns;

public enum WorkflowStatus {
    PENDING("pending"),
    IN_PROGRESS("in_progress"),
    COMPLETED("completed"),
    FAILED("failed");

    private final String value;

    WorkflowStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}