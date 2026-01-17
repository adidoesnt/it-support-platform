package com.adityabanerjee.api.incidents;

import java.math.BigInteger;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("incidents")
public record Incident(
        @Id BigInteger id,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}