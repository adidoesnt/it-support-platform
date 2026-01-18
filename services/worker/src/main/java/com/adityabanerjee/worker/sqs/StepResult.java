package com.adityabanerjee.worker.sqs;

public record StepResult(boolean deleteMessage, boolean enqueueNext) {
}
