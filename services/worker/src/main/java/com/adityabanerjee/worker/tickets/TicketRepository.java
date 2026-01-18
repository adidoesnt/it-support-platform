package com.adityabanerjee.worker.tickets;

import org.springframework.data.repository.ListCrudRepository;

import java.math.BigInteger;
import java.util.Optional;

public interface TicketRepository extends ListCrudRepository<Ticket, BigInteger> {
    Optional<Ticket> findByWorkflowRunId(BigInteger workflowRunId);
    Optional<Ticket> findByIncidentId(BigInteger incidentId);
}
