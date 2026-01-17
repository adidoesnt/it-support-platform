package com.adityabanerjee.api.sqs;

import java.math.BigInteger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

public record WorkflowMessageBody(BigInteger workflowRunId) {
    public String toJson() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert WorkflowMessageBody to JSON", e);
        }
    }
}
