package com.adityabanerjee.api.metrics;

public enum Metric {
    INCIDENTS_RECEIVED("incidents_received_total");
    

    private final String value;

    Metric(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
