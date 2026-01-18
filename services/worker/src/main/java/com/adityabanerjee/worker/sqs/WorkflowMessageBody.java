package com.adityabanerjee.worker.sqs;

import java.math.BigInteger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public record WorkflowMessageBody(BigInteger workflowRunId) {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public String toJson() throws JsonProcessingException {
        return objectMapper.writeValueAsString(this);
    }
}
