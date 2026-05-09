package com.adidoesnt.itsupportplatform.ticket.grpc;

import java.util.UUID;

import io.grpc.stub.StreamObserver;

public class TicketGrpcService extends TicketServiceGrpc.TicketServiceImplBase {
    @Override
    public void createTicket(CreateTicketRequest request, StreamObserver<CreateTicketResponse> responseObserver) {
        // TODO: Implement ticket creation logic
        CreateTicketResponse response = CreateTicketResponse
                .newBuilder()
                .setId(UUID.randomUUID().toString())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getTickets(GetTicketsRequest request, StreamObserver<GetTicketsResponse> responseObserver) {
        GetTicketsResponse response = GetTicketsResponse.newBuilder().setMessage("Tickets retrieved successfully")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getTicketById(GetTicketByIdRequest request, StreamObserver<GetTicketByIdResponse> responseObserver) {
        GetTicketByIdResponse response = GetTicketByIdResponse.newBuilder().setMessage("Ticket retrieved successfully")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void updateTicketById(UpdateTicketByIdRequest request,
            StreamObserver<UpdateTicketByIdResponse> responseObserver) {
        UpdateTicketByIdResponse response = UpdateTicketByIdResponse.newBuilder()
                .setSuccess(true)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void deleteTicketById(DeleteTicketByIdRequest request,
            StreamObserver<DeleteTicketByIdResponse> responseObserver) {
        DeleteTicketByIdResponse response = DeleteTicketByIdResponse.newBuilder()
                .setSuccess(true)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
