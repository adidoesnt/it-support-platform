package com.adityabanerjee.worker.sqs;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;

@Component
public class WorkflowConsumer implements SmartLifecycle {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean isRunning = false;

    private final QueueResolver queueResolver;
    private final SqsClient sqsClient;
    private final WorkflowProcessor workflowProcessor;

    private final int maxMessages;
    private final int waitTimeSeconds;
    private final int visibilityTimeoutSeconds;

    public WorkflowConsumer(QueueResolver queueResolver, SqsClient sqsClient, WorkflowProcessor workflowProcessor,
            @Value("${worker.sqs.maxMessages:10}") int maxMessages,
            @Value("${worker.sqs.waitSeconds:10}") int waitSeconds,
            @Value("${worker.sqs.visibilityTimeoutSeconds:30}") int visibilityTimeoutSeconds) {
        this.queueResolver = queueResolver;
        this.sqsClient = sqsClient;
        this.workflowProcessor = workflowProcessor;
        this.maxMessages = maxMessages;
        this.waitTimeSeconds = waitSeconds;
        this.visibilityTimeoutSeconds = visibilityTimeoutSeconds;
    }

    @Override
    public void start() {
        System.out.println("Starting workflow consumer...");
        isRunning = true;
        executor.submit(() -> loop(queueResolver.getQueueUrl()));
    }

    @Override
    public void stop() {
        System.out.println("Stopping workflow consumer...");
        isRunning = false;
        executor.shutdownNow();
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    private void loop(String queueUrl) {
        ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(maxMessages)
                .waitTimeSeconds(waitTimeSeconds)
                .visibilityTimeout(visibilityTimeoutSeconds)
                .build();

        while (isRunning) {
            System.out.println(String.format("Polling for messages on queue %s", queueUrl));
            try {
                ReceiveMessageResponse receiveMessageResponse = sqsClient.receiveMessage(receiveMessageRequest);
                List<Message> messages = receiveMessageResponse.messages();

                for (Message message : messages) {
                    handleOne(queueUrl, message.body(), message.receiptHandle());
                }
            } catch (Exception e) {
                System.out.println(String.format("Error receiving messages: %s", e.getMessage()));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                    System.out.println(String.format("Error sleeping: %s", ignored.getMessage()));
                }
            }
        }
    }

    private void handleOne(String queueUrl, String messageBody, String receiptHandle) {
        try {
            System.out.println(String.format("Handling message: %s on queue %s with receipt handle %s", messageBody,
                    queueUrl, receiptHandle));
            WorkflowMessageBody workflowMessageBody = WorkflowMessageBody.fromJson(messageBody);

            BigInteger workflowRunId = workflowMessageBody.workflowRunId();
            boolean success = workflowProcessor.processWorkflowRunById(workflowRunId);

            if (success) {
                System.out.println(
                        String.format("Successfully processed workflow run %s, deleting message", workflowRunId));
                try {
                    deleteMessage(queueUrl, receiptHandle);
                } catch (Exception ignored) {
                    System.out.println(String.format("Error deleting message: %s", ignored.getMessage()));
                }
            } else {
                System.out.println(String.format("Error processing workflow run %s, leaving for retry", workflowRunId));
            }
        } catch (JsonProcessingException e) {
            System.out.println(String.format("Error parsing message body: %s", e.getMessage()));
            try {
                deleteMessage(queueUrl, receiptHandle);
            } catch (Exception ignored) {
                System.out.println(String.format("Error deleting message: %s", ignored.getMessage()));
            }
        }
    }

    private void deleteMessage(String queueUrl, String receiptHandle) {
        try {
            DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .receiptHandle(receiptHandle)
                    .build();
            System.out.println(String.format("Deleting message: %s on queue %s with receipt handle %s",
                    deleteMessageRequest, queueUrl, receiptHandle));
            sqsClient.deleteMessage(deleteMessageRequest);
        } catch (Exception e) {
            System.out.println(String.format("Error deleting message: %s", e.getMessage()));
        }
    }
}
