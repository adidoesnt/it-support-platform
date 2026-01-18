package com.adityabanerjee.worker.tickets;

import java.math.BigInteger;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;

@Table("tickets")
public record Ticket(
        @Id BigInteger id,
        BigInteger incidentId,
        BigInteger workflowRunId,
        String title,
        String description,
        TicketStatus status,
        @CreatedDate LocalDateTime createdAt,
        @LastModifiedDate LocalDateTime updatedAt) {
}
