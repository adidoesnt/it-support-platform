package com.adidoesnt.itsupportplatform.ticket.grpc;

import io.grpc.stub.StreamObserver;

public class TicketGrpcService extends TicketServiceGrpc.TicketServiceImplBase {
    @Override
    public void createTicket(CreateTicketRequest request, StreamObserver<CreateTicketResponse> responseObserver) {
        // TODO: Implement ticket creation logic
        CreateTicketResponse response = CreateTicketResponse
                .newBuilder()
                .setSuccess(true)
                .setTicket(Ticket.getDefaultInstance())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getTickets(GetTicketsRequest request, StreamObserver<GetTicketsResponse> responseObserver) {
        // TODO: Implement ticket retrieval logic
        GetTicketsResponse response = GetTicketsResponse.newBuilder().build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getTicketById(GetTicketByIdRequest request, StreamObserver<GetTicketByIdResponse> responseObserver) {
        // TODO: Implement ticket retrieval logic
        GetTicketByIdResponse response = GetTicketByIdResponse.newBuilder()
                .setTicket(Ticket.getDefaultInstance())
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
