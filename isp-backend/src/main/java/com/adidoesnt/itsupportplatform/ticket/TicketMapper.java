package com.adidoesnt.itsupportplatform.ticket;

import java.time.Instant;

import com.adidoesnt.itsupportplatform.ticket.grpc.GrpcTicket;
import com.adidoesnt.itsupportplatform.ticket.grpc.TicketGrpcStatus;

public final class TicketMapper {

    private TicketMapper() {
    }

    public static GrpcTicket toGrpcTicket(TicketEntity ticket) {
        return GrpcTicket.newBuilder()
                .setId(ticket.getId() != null ? ticket.getId().toString() : "")
                .setTitle(ticket.getTitle() != null ? ticket.getTitle() : "")
                .setDescription(ticket.getDescription() != null ? ticket.getDescription() : "")
                .setCreatedAt(ticket.getCreatedAt() != null ? ticket.getCreatedAt().toString() : "")
                .setUpdatedAt(ticket.getUpdatedAt() != null ? ticket.getUpdatedAt().toString() : "")
                .setStatus(TicketGrpcStatus.valueOf(ticket.getStatus().name()))
                .build();
    }

    public static TicketEntity toTicketEntity(GrpcTicket ticket) {
        TicketEntity entity = new TicketEntity();
        if (!ticket.getId().isBlank()) {
            entity.setId(Long.parseLong(ticket.getId()));
        }
        entity.setTitle(ticket.getTitle());
        if (!ticket.getDescription().isBlank()) {
            entity.setDescription(ticket.getDescription());
        }
        TicketGrpcStatus grpcStatus = ticket.getStatus();
        if (grpcStatus != TicketGrpcStatus.UNRECOGNIZED) {
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

    public static TicketEntityStatus fromGrpcStatus(TicketGrpcStatus grpc) {
        if (grpc == null || grpc == TicketGrpcStatus.UNRECOGNIZED) {
            throw new IllegalArgumentException("Invalid ticket status");
        }
        return TicketEntityStatus.valueOf(grpc.name());
    }

    public static TicketGrpcStatus toGrpcStatus(TicketEntityStatus status) {
        return TicketGrpcStatus.valueOf(status.name());
    }
}
