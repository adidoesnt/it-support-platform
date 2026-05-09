package com.adidoesnt.itsupportplatform.ticket;

import java.time.Instant;

import com.adidoesnt.itsupportplatform.ticket.grpc.Ticket;
import com.adidoesnt.itsupportplatform.ticket.grpc.TicketStatus;

public final class TicketMapper {

    private TicketMapper() {
    }

    public static Ticket toGrpcTicket(TicketEntity ticket) {
        return Ticket.newBuilder()
                .setId(ticket.getId() != null ? ticket.getId().toString() : "")
                .setTitle(ticket.getTitle() != null ? ticket.getTitle() : "")
                .setDescription(ticket.getDescription() != null ? ticket.getDescription() : "")
                .setCreatedAt(ticket.getCreatedAt() != null ? ticket.getCreatedAt().toString() : "")
                .setUpdatedAt(ticket.getUpdatedAt() != null ? ticket.getUpdatedAt().toString() : "")
                .setStatus(TicketStatus.valueOf(ticket.getStatus().name()))
                .build();
    }

    public static TicketEntity toTicketEntity(Ticket ticket) {
        TicketEntity entity = new TicketEntity();
        if (!ticket.getId().isBlank()) {
            entity.setId(Long.parseLong(ticket.getId()));
        }
        entity.setTitle(ticket.getTitle());
        if (!ticket.getDescription().isBlank()) {
            entity.setDescription(ticket.getDescription());
        }
        TicketStatus grpcStatus = ticket.getStatus();
        if (grpcStatus != TicketStatus.UNRECOGNIZED) {
            entity.setStatus(TicketEntityStatus.valueOf(grpcStatus.name()));
        }
        if (!ticket.getCreatedAt().isBlank()) {
            entity.setCreatedAt(Instant.parse(ticket.getCreatedAt()));
        }
        if (!ticket.getUpdatedAt().isBlank()) {
            entity.setUpdatedAt(Instant.parse(ticket.getUpdatedAt()));
        }
        return entity;
    }

    public static TicketEntityStatus fromGrpcStatus(TicketStatus grpc) {
        if (grpc == null || grpc == TicketStatus.UNRECOGNIZED) {
            throw new IllegalArgumentException("Invalid ticket status");
        }
        return TicketEntityStatus.valueOf(grpc.name());
    }

    public static TicketStatus toGrpcStatus(TicketEntityStatus status) {
        return TicketStatus.valueOf(status.name());
    }
}
