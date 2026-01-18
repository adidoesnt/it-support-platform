package com.adityabanerjee.worker.classifier;

public interface IncidentClassifier {
    ClassificationResult classifyIncident(String incidentDescription);
}
