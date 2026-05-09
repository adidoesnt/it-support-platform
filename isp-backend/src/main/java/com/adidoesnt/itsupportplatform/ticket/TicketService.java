package com.adidoesnt.itsupportplatform.ticket;

import java.util.Optional;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TicketService {
    private final TicketRepository ticketRepository;

    @Transactional
    public Ticket createTicket(String title, Optional<String> description, Optional<TicketStatus> status) {
        Ticket ticket = new Ticket();

        ticket.setTitle(title);
        description.ifPresent(ticket::setDescription);
        ticket.setStatus(status.orElse(TicketStatus.OPEN));
        
        Ticket savedTicket = ticketRepository.save(ticket);
        return savedTicket;
    }
}
