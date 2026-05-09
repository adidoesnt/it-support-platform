package com.adidoesnt.itsupportplatform.ticket.grpc;

import io.grpc.stub.StreamObserver;

public class TicketGrpcService extends TicketServiceGrpc.TicketServiceImplBase {
    @Override
    public void createTicket(CreateTicketRequest request, StreamObserver<CreateTicketResponse> responseObserver) {
        CreateTicketResponse response = CreateTicketResponse.newBuilder().setMessage("Ticket created successfully")
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
                .setMessage("Ticket updated successfully")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void deleteTicketById(DeleteTicketByIdRequest request,
            StreamObserver<DeleteTicketByIdResponse> responseObserver) {
        DeleteTicketByIdResponse response = DeleteTicketByIdResponse.newBuilder()
                .setMessage("Ticket deleted successfully")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
