/*
 * Copyright 2016 Middlesex University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.mdx.cs.ie.contextserver;

import java.io.IOException;
import java.util.logging.Logger;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

/**
 * Logging Service for handling GRPC/Protobuf Calls
 *
 * @author Dean Kramer <d.kramer@mdx.ac.uk>
 */
public class ContextServer {

    private static final Logger logger = Logger.getLogger(ContextServer.class.getName());
    private Database mDatabase;

    private static final int PORT = 8080;
    private Server mServer;

    private void start() throws IOException {

        mDatabase = new MySQLDatabase();

        mServer = ServerBuilder.forPort(PORT)
               .addService(new ServiceImpl()).build().start();

        logger.info("Server started, listening on " + PORT);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                ContextServer.this.stop();
                System.err.println("*** server shut down");
            }
        });
    }

    private void stop() {
        if (mServer != null) {
            mServer.shutdown();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        final ContextServer server = new ContextServer();
        server.start();
        server.blockUntilShutdown();
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (mServer != null) {
            mServer.awaitTermination();
        }
    }

    private class ServiceImpl extends ContextServiceGrpc.ContextServiceImplBase {

        @Override
        public void logEvents(LogEventRequest req, StreamObserver<ServiceResponse> responseObserver) {

            int dbresponse = -1;

            //try {
                /*dbresponse = mDatabase.logEvents(req.getUserid(), req.getOriginCount(),
                        req.getOriginList(), req.getLocationList(),
                        req.getDateList(), req.getTextList());
                */

            logger.info("user " + req.getUserid() + " with " + req.getOriginCount() + " Events");

                ServiceResponse reply = ServiceResponse.newBuilder().setResponse(dbresponse).build();
                responseObserver.onNext(reply);
                responseObserver.onCompleted();

           // } catch (SQLException e) {
           //     System.err.println(e.getMessage());
           // }
        }

        @Override
        public void registerUser(RegisterUserRequest req, StreamObserver<ServiceResponse> responseObserver) {
            int dbresponse = -1;

            //try {
               // dbresponse = mDatabase.registerUser(req.getUserid(), req.getUsername(), req.getDeviceid());
            logger.info("user " + req.getUserid());

                ServiceResponse reply = ServiceResponse.newBuilder().setResponse(dbresponse).build();
                responseObserver.onNext(reply);
                responseObserver.onCompleted();

            //} catch (SQLException e) {
            //    System.err.println(e.getMessage());
            //}
        }

        @Override
        public void setLearning(SetLearningRequest req, StreamObserver<ServiceResponse> responseObserver) {
            int dbresponse = -1;
            logger.info("user " + req.getUserid() + " LM " + req.getLearning());

            ServiceResponse reply = ServiceResponse.newBuilder().setResponse(dbresponse).build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
    }
}
