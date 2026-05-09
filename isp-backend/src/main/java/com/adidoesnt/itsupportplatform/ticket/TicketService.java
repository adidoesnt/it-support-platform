package com.adidoesnt.itsupportplatform.ticket;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional(readOnly = true)
    public List<Ticket> getTickets() {
        // TODO: Implement pagination
        List<Ticket> tickets = ticketRepository.findAll();
        return tickets;
    }

    @Transactional(readOnly = true)
    public Optional<Ticket> getTicketById(Long id) {
        Optional<Ticket> ticket = ticketRepository.findById(id);
        return ticket;
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
