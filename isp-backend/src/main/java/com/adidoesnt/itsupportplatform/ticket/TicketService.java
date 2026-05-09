package com.adidoesnt.itsupportplatform.ticket;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TicketService {
    private final TicketRepository ticketRepository;

    @Transactional
    public TicketEntity createTicket(String title, Optional<String> description, Optional<TicketEntityStatus> status) {
        TicketEntity ticket = new TicketEntity();

        ticket.setTitle(title);
        description.ifPresent(ticket::setDescription);
        ticket.setStatus(status.orElse(TicketEntityStatus.OPEN));

        TicketEntity savedTicket = ticketRepository.save(ticket);
        return savedTicket;
    }

    @Transactional(readOnly = true)
    public List<TicketEntity> getTickets() {
        // TODO: Implement pagination
        List<TicketEntity> tickets = ticketRepository.findAll();
        return tickets;
    }

    @Transactional(readOnly = true)
    public Optional<TicketEntity> getTicketById(Long id) {
        Optional<TicketEntity> ticket = ticketRepository.findById(id);
        return ticket;
    }

    @Transactional
    public TicketEntity updateTicketById(
            Long id,
            Optional<String> title,
            Optional<String> description,
            Optional<TicketEntityStatus> status) throws IllegalArgumentException, EntityNotFoundException {
        if (title.isEmpty() && description.isEmpty() && status.isEmpty()) {
            throw new IllegalArgumentException("At least one of title, description, or status must be provided");
        }

        Optional<TicketEntity> ticket = ticketRepository.findById(id);
        if (ticket.isEmpty()) {
            throw new EntityNotFoundException("GrpcTicket not found");
        }

        TicketEntity ticketToSave = ticket.get();

        title.ifPresent(ticketToSave::setTitle);
        description.ifPresent(ticketToSave::setDescription);
        status.ifPresent(ticketToSave::setStatus);

        TicketEntity updatedTicket = ticketRepository.save(ticketToSave);
        return updatedTicket;
    }

    @Transactional
    public boolean deleteTicketById(Long id) {
        if (!ticketRepository.existsById(id)) {
            return false;
        }
        ticketRepository.deleteById(id);
        return true;
    }
}
