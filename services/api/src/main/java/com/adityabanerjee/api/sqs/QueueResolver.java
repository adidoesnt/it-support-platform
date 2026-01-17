package com.adityabanerjee.api.sqs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse;

@Component
public class QueueResolver {
    private final SqsClient sqsClient;
    private final String queueName;

    private volatile String cachedQueueUrl;

    public QueueResolver(SqsClient sqsClient, @Value("${aws.sqs.queue-name}") String queueName) {
        this.sqsClient = sqsClient;
        this.queueName = queueName;
    }

    public String getQueueUrl() {
        if (cachedQueueUrl == null) {
            GetQueueUrlRequest getQueueUrlRequest = GetQueueUrlRequest.builder().queueName(queueName).build();
            GetQueueUrlResponse getQueueUrlResponse = sqsClient.getQueueUrl(getQueueUrlRequest);
            cachedQueueUrl = getQueueUrlResponse.queueUrl();
        }

        return cachedQueueUrl;
    }
}
