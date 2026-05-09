package com.adidoesnt.itsupportplatform.health.grpc;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class HealthGrpcService extends HealthServiceGrpc.HealthServiceImplBase {
    @Override
    public void ping(PingRequest request, StreamObserver<PingResponse> responseObserver) {
        PingResponse response = PingResponse.newBuilder().setMessage("Pong!").build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
