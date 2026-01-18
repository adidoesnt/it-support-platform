package com.adityabanerjee.worker.classifier;

import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;
import java.math.BigInteger;

public interface IncidentClassificationRepository extends ListCrudRepository<IncidentClassification, BigInteger> {
    Optional<IncidentClassification> findByWorkflowRunId(BigInteger workflowRunId);
}
