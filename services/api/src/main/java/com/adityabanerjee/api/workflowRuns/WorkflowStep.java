package com.adityabanerjee.api.workflowRuns;

public enum WorkflowStep {
    PAYLOAD_VALIDATION("payload_validation"),
    INCIDENT_CLASSIFICATION("incident_classification"),
    TICKET_CREATION("ticket_creation");

    private final String value;

    WorkflowStep(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}