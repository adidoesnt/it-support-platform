package com.adityabanerjee.worker.classifier;

public enum IncidentPriority {
    P1("p1"),
    P2("p2"),
    P3("p3");

    private final String value;

    IncidentPriority(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
