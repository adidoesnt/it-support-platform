package com.adityabanerjee.worker.workflowRuns;

import org.springframework.data.repository.ListCrudRepository;

import java.math.BigInteger;

public interface WorkflowRunRepository extends ListCrudRepository<WorkflowRun, BigInteger> {
}
