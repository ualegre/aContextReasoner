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

package uk.co.deansserver.acontextreasoner.logging;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import uk.co.deansserver.acontextreasoner.database.ContextDB;

import java.io.IOException;
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
 * @author Dean Kramer <deankramer99@gmail.com>
 */
public class ProtoBufLogUploader implements LogUploader {

    private String SERVER_URL;
    private static final int SERVER_PORT = 8080;
    private ContextDB mContextDB;
    private DataLogger mLogger;
    private String API_KEY;
    private static final String LOG_TAG = "Protobuf Log Uploader";
    private ManagedChannel mChannel;


    public ProtoBufLogUploader(Context context, ContextDB db, DataLogger logger) {
        mContextDB = db;
        mLogger = logger;
        Bundle metadata = null;

        try {
            metadata = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA).metaData;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(LOG_TAG, e.getMessage());
        }

        API_KEY = metadata.getString("contextService_ApiKey", "");
        SERVER_URL = metadata.getString("contextService_Host", "");

        mChannel = ManagedChannelBuilder.forAddress(SERVER_URL, SERVER_PORT)
                .usePlaintext(true)
                .build();

    }


    @Override
    public boolean setLearningMode(final int userNumber, final boolean mode) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    ContextServiceGrpc.ContextServiceBlockingStub stub =
                            ContextServiceGrpc.
                                    newBlockingStub(mChannel).withDeadlineAfter(10, TimeUnit.SECONDS);

                    SetLearningRequest message = SetLearningRequest.newBuilder()
                            .setApikey(API_KEY)
                            .setUserid(userNumber)
                            .setLearning(mode)
                            .build();

                    ServiceResponse response = stub.setLearning(message);

                } catch (Exception e) {
                    Log.e(LOG_TAG, e.getMessage());
                }
            }
        }).start();

        return true;
    }

    @Override
    public boolean registerUser(final int userNumber, final String userIdent, final String deviceIdent) {

        final boolean[] success = {false};

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    try {
                        if (!isConnected()) {
                            return;
                        }
                    } catch (Exception e) {
                        return;
                    }

                    ContextServiceGrpc.ContextServiceBlockingStub stub =
                            ContextServiceGrpc.
                                    newBlockingStub(mChannel).withDeadlineAfter(10, TimeUnit.SECONDS);

                    RegisterUserRequest message = RegisterUserRequest.newBuilder()
                            .setApikey(API_KEY)
                            .setUserid(userNumber)
                            .setUsername(userIdent)
                            .setDeviceid(deviceIdent)
                            .build();

                    ServiceResponse response = stub.registerUser(message);
                    mLogger.newUserID(response.getResponse());

                    success[0] = true;

                } catch (Exception e) {
                    Log.e(LOG_TAG, e.getMessage());
                }
            }
        });

        t.start();

        try {
            t.join();
        } catch (InterruptedException e) {
            Log.e(LOG_TAG, e.getMessage());
        }

        return success[0];
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

                    try {
                        if (!isConnected()) {
                            mLogger.incompleteBackup();
                            return;
                        }
                    } catch (Exception e) {
                        mLogger.incompleteBackup();
                        return;
                    }

                    ContextServiceGrpc.ContextServiceBlockingStub stub =
                            ContextServiceGrpc.
                                    newBlockingStub(mChannel).withDeadlineAfter(10, TimeUnit.SECONDS);

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

                } catch (Exception e) {
                    Log.e(LOG_TAG, e.getMessage());
                    mLogger.incompleteBackup();
                }
            }
        }).start();
    }

    @Override
    public void stop() {
        try {
            mChannel.shutdown().awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Log.e(LOG_TAG, e.getMessage());
        }
    }

    public boolean isConnected() throws InterruptedException, IOException {
        String command = "ping -c 1 google.com";
        return (Runtime.getRuntime().exec(command).waitFor() == 0);
    }
}
