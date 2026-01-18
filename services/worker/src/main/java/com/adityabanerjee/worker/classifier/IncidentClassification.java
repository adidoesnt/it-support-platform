package com.adityabanerjee.worker.classifier;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.math.BigInteger;

@Table("incident_classifications")
public record IncidentClassification(
    @Id BigInteger id,
    BigInteger workflowRunId,
    BigInteger incidentId,
    String category,
    String priority,
    String summary,
    String modelProvider,
    String modelName,
    String rawResponse,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
