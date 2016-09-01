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

package org.poseidon_project.context.logging;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import org.poseidon_project.context.database.ContextDB;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import uk.ac.mdx.cs.ie.contextserver.ContextServiceGrpc;
import uk.ac.mdx.cs.ie.contextserver.LogEventRequest;
import uk.ac.mdx.cs.ie.contextserver.RegisterUserRequest;
import uk.ac.mdx.cs.ie.contextserver.ServiceResponse;
import uk.ac.mdx.cs.ie.contextserver.SetLearningRequest;

/**
 * Handles log uploading to server via GRPC/Protocol Buffers
 *
 * @author Dean Kramer <d.kramer@mdx.ac.uk>
 */
public class ProtoBufLogUploader implements LogUploader {

    private String SERVER_URL;
    private static final int SERVER_PORT = 8080;
    private ContextDB mContextDB;
    private Context mContext;
    private DataLogger mLogger;
    private String API_KEY;
    private static final String LOG_TAG = "Protobuf Log Uploader";


    public ProtoBufLogUploader(Context context, ContextDB db, DataLogger logger) {
        mContext = context;
        mContextDB = db;
        mLogger = logger;
        Bundle metadata = context.getApplicationInfo().metaData;
        API_KEY = metadata.getString("contextService_ApiKey", "");
        SERVER_URL = metadata.getString("contextService_Host", "");
    }


    @Override
    public boolean setLearningMode(final Integer userNumber, final Boolean mode) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ManagedChannel channel = ManagedChannelBuilder.forAddress(SERVER_URL, SERVER_PORT)
                            .usePlaintext(true)
                            .build();

                    ContextServiceGrpc.ContextServiceBlockingStub stub =
                            ContextServiceGrpc.newBlockingStub(channel);

                    SetLearningRequest message = SetLearningRequest.newBuilder()
                            .setApikey(API_KEY)
                            .setUserid(userNumber)
                            .setLearning(mode)
                            .build();

                    ServiceResponse response = stub.setLearning(message);
                    channel.shutdown().awaitTermination(1, TimeUnit.SECONDS);

                } catch (Exception e) {
                    Log.e(LOG_TAG, e.getMessage());
                }
            }
        }).start();

        return true;
    }

    @Override
    public boolean registerUser(final Integer userNumber, final String userIdent, final String deviceIdent) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ManagedChannel channel = ManagedChannelBuilder.forAddress(SERVER_URL, SERVER_PORT)
                            .usePlaintext(true)
                            .build();

                    ContextServiceGrpc.ContextServiceBlockingStub stub =
                            ContextServiceGrpc.newBlockingStub(channel);

                    RegisterUserRequest message = RegisterUserRequest.newBuilder()
                            .setApikey(API_KEY)
                            .setUserid(userNumber)
                            .setUsername(userIdent)
                            .setDeviceid(deviceIdent)
                            .build();

                    ServiceResponse response = stub.registerUser(message);
                    mLogger.newUserID(response.getResponse());

                    channel.shutdown().awaitTermination(1, TimeUnit.SECONDS);

                } catch (Exception e) {
                    Log.e(LOG_TAG, e.getMessage());
                }
            }
        }).start();

        return false;
    }

    @Override
    public void uploadLogToServer(final int userID) {

        List<LogEvent> events = mContextDB.getAllEvents();

        final ArrayList origins = new ArrayList();
        final ArrayList locations = new ArrayList();
        final ArrayList dates = new ArrayList();
        final ArrayList text = new ArrayList();

        for (LogEvent event : events) {
            origins.add(event.getOrigin());
            locations.add(event.getLocation());
            dates.add(event.getDate());
            text.add(event.getText());
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ManagedChannel channel = ManagedChannelBuilder.forAddress(SERVER_URL, SERVER_PORT)
                          .usePlaintext(true)
                            .build();

                    ContextServiceGrpc.ContextServiceBlockingStub stub =
                            ContextServiceGrpc.newBlockingStub(channel);

                    LogEventRequest message = LogEventRequest.newBuilder()
                            .setApikey(API_KEY)
                            .setUserid(userID)
                            .addAllOrigin(origins)
                            .addAllLocation(locations)
                            .addAllDate(dates)
                            .addAllText(text)
                            .build();

                    ServiceResponse response = stub.logEvents(message);

                    if (response.getResponse() == 1) {
                        mLogger.completedBackup();
                    } else {
                        mLogger.incompleteBackup();
                    }

                    channel.shutdown().awaitTermination(1, TimeUnit.SECONDS);

                } catch (Exception e) {
                    Log.e(LOG_TAG, e.getMessage());
                    mLogger.incompleteBackup();
                }
            }
        }).start();
    }
}
