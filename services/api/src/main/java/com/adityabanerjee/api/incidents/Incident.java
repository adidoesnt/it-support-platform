package com.adityabanerjee.api.incidents;

import java.math.BigInteger;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;

@Table("incidents")
public record Incident(
                @Id BigInteger id,
                String description,
                @CreatedDate LocalDateTime createdAt,
                @LastModifiedDate LocalDateTime updatedAt) {
}