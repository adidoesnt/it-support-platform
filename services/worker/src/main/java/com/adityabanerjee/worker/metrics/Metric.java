package com.adityabanerjee.worker.metrics;

public enum Metric {
    WORKFLOW_STEP_FAILURES("workflow_step_failure_total"),
    WORKFLOW_STEP_SUCCESS("workflow_step_success_total"),
    WORKFLOW_STEP_DURATION("workflow_step_duration_seconds");

    private final String value;

    Metric(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
