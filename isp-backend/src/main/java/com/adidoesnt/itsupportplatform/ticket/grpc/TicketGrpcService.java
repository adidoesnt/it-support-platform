package com.adidoesnt.itsupportplatform.ticket.grpc;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.adidoesnt.itsupportplatform.ticket.TicketEntity;
import com.adidoesnt.itsupportplatform.ticket.TicketEntityStatus;
import com.adidoesnt.itsupportplatform.ticket.TicketMapper;
import com.adidoesnt.itsupportplatform.ticket.TicketService;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
public class TicketGrpcService extends TicketServiceGrpc.TicketServiceImplBase {
    private final TicketService ticketService;

    @Override
    public void createTicket(CreateTicketRequest request, StreamObserver<CreateTicketResponse> responseObserver) {
        String title = request.getTitle();
        if (title == null || title.isBlank()) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT.withDescription("title is required").asRuntimeException());
            return;
        }

        Optional<String> description = request.hasDescription() ? Optional.of(request.getDescription())
                : Optional.empty();

        Optional<TicketEntityStatus> status = Optional.empty();
        if (request.hasStatus()) {
            try {
                status = Optional.of(TicketMapper.fromGrpcStatus(request.getStatus()));
            } catch (IllegalArgumentException e) {
                responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
                return;
            }
        }

        try {
            TicketEntity ticket = ticketService.createTicket(title.trim(), description, status);

            CreateTicketResponse response = CreateTicketResponse.newBuilder()
                    .setSuccess(true)
                    .setTicket(TicketMapper.toGrpcTicket(ticket))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.withCause(e).asRuntimeException());
        }
    }

    @Override
    public void getTickets(GetTicketsRequest request, StreamObserver<GetTicketsResponse> responseObserver) {
        // TODO: Implement ticket retrieval logic
        List<TicketEntity> tickets = ticketService.getTickets();
        GetTicketsResponse response = GetTicketsResponse.newBuilder()
                .addAllTickets(tickets.stream()
                        .map(TicketMapper::toGrpcTicket)
                        .collect(Collectors.toList()))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getTicketById(GetTicketByIdRequest request, StreamObserver<GetTicketByIdResponse> responseObserver) {
        Long id = Long.parseLong(request.getId());
        Optional<TicketEntity> ticket = ticketService.getTicketById(id);
        if (ticket.isEmpty()) {
            responseObserver.onError(Status.NOT_FOUND.withDescription("Ticket not found").asRuntimeException());
            return;
        }

        GetTicketByIdResponse response = GetTicketByIdResponse.newBuilder()
                .setTicket(TicketMapper.toGrpcTicket(ticket.get()))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void updateTicketById(UpdateTicketByIdRequest request,
            StreamObserver<UpdateTicketByIdResponse> responseObserver) {
        // TODO: Implement ticket update logic
        UpdateTicketByIdResponse response = UpdateTicketByIdResponse.newBuilder()
                .setSuccess(true)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void deleteTicketById(DeleteTicketByIdRequest request,
            StreamObserver<DeleteTicketByIdResponse> responseObserver) {
        // TODO: Implement ticket deletion logic
        DeleteTicketByIdResponse response = DeleteTicketByIdResponse.newBuilder()
                .setSuccess(true)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
