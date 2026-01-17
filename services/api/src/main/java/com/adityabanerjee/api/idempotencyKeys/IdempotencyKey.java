package com.adityabanerjee.api.idempotencyKeys;

import java.math.BigInteger;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

/*
We need to implement Persistable<String> to allow for the idempotency key to be the ID of the entity.
Otherwise, when save is called with a non-null ID, it tries to update rather than insert.
*/

@Table("idempotency_keys")
public record IdempotencyKey(
        @Id String key,
        BigInteger workflowRunId,
        @CreatedDate LocalDateTime createdAt,
        @LastModifiedDate LocalDateTime updatedAt) implements Persistable<String> {

    @Override
    public String getId() {
        return key;
    }

    @Override
    public boolean isNew() {
        return createdAt == null;
    }
}
