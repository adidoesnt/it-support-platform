package com.adityabanerjee.api.sqs;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.math.BigInteger;

@Component
public class WorkflowEnqueuer {
    private final QueueResolver queueResolver;
    private final SqsClient sqsClient;

    public WorkflowEnqueuer(QueueResolver queueResolver, SqsClient sqsClient) {
        this.queueResolver = queueResolver;
        this.sqsClient = sqsClient;
    }

    public void enqueueWorkflow(BigInteger workflowRunId) throws JsonProcessingException {
        String queueUrl = queueResolver.getQueueUrl();
        System.out.println(String.format("Fetched queue URL: %s", queueUrl));

        WorkflowMessageBody workflowMessageBody = new WorkflowMessageBody(workflowRunId);
        String workflowMessageBodyJson = workflowMessageBody.toJson();

        SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(workflowMessageBodyJson)
                .build();
        System.out
                .println(String.format("Sending message to queue %s with body %s", queueUrl, workflowMessageBodyJson));

        SendMessageResponse sendMessageResponse = sqsClient.sendMessage(sendMessageRequest);
        System.out.println(String.format("Enqueued workflow run %s to queue %s with message id %s", workflowRunId,
                queueUrl, sendMessageResponse.messageId()));
    }
}
