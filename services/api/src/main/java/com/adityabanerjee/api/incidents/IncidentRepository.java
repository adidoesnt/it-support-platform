package com.adityabanerjee.api.incidents;

import org.springframework.data.repository.ListCrudRepository;

import java.math.BigInteger;

public interface IncidentRepository extends ListCrudRepository<Incident, BigInteger> {
}
