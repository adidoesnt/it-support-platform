package com.adityabanerjee.api.idempotencyKeys;

import org.springframework.data.repository.ListCrudRepository;

public interface IdempotencyKeyRepository extends ListCrudRepository<IdempotencyKey, String> {
}
