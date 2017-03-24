/*
 * Copyright 2017 aContextReasoner Project
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

package uk.co.deansserver.contextserver;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Logger;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

/**
 * Logging Service for handling GRPC/Protobuf Calls
 *
 * @author Dean Kramer <deankramer99@gmail.com>
 */
public class ContextServer {

    private static final Logger logger = Logger.getLogger(ContextServer.class.getName());
    private Database mDatabase;
    private static final String API_KEY = "";

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
            try {
                mDatabase.close();
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }

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

            if (req.getApikey().equals(API_KEY)) {

                int dbresponse = -1;

                try {

                    dbresponse = mDatabase.logEvents(req.getUserid(), req.getOriginCount(),
                        req.getOriginList(), req.getLocationList(),
                        req.getDateList(), req.getTextList());
                } catch (SQLException e) {
                    System.err.println(e.getMessage());
                }

                ServiceResponse reply = ServiceResponse.newBuilder().setResponse(dbresponse).build();
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
            }
        }


        @Override
        public void registerUser(RegisterUserRequest req, StreamObserver<ServiceResponse> responseObserver) {

            if (req.getApikey().equals(API_KEY)) {

                int dbresponse = -1;

                try {
                    dbresponse = mDatabase.registerUser(req.getUserid(), req.getUsername(), req.getDeviceid());
                } catch (SQLException e) {
                    System.err.println(e.getMessage());
                }

                ServiceResponse reply = ServiceResponse.newBuilder().setResponse(dbresponse).build();
                responseObserver.onNext(reply);
                responseObserver.onCompleted();

            }
        }

        @Override
        public void setLearning(SetLearningRequest req, StreamObserver<ServiceResponse> responseObserver) {

            if (req.getApikey().equals(API_KEY)) {

                int dbresponse = -1;

                try {
                    dbresponse = mDatabase.setLearning(req.getUserid(), req.getLearning());
                } catch (SQLException e) {
                    System.err.println(e.getMessage());
                }

                ServiceResponse reply = ServiceResponse.newBuilder().setResponse(dbresponse).build();
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
            }
        }
    }
}
