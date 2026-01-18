package com.adityabanerjee.worker.classifier;

public enum IncidentCategory {
    ACCESS("access"),
    NETWORK("network"),
    HARDWARE("hardware"),
    SOFTWARE("software"),
    SECURITY("security"),
    DATA("data"),
    OTHER("other");

    private final String value;

    IncidentCategory(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
