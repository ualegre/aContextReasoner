/*
 * Copyright 2015 POSEIDON Project
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
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import org.poseidon_project.context.database.ContextDB;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCClient;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;

/**
 * Handles log uploading to server via XMLRPC
 *
 * @author Dean Kramer <d.kramer@mdx.ac.uk>
 */
public class XmlRpcLogUploader implements LogUploader, XMLRPCCallback{

    //The RPC Server address
    private String SERVER_URL;
    private static final String RPC_UPLOAD_FUNCTION = "poseidon.logEvents";
    private static final String RPC_REGISTER_FUNCTION = "poseidon.registerUser";
    private static final String RPC_SET_LEARNING_FUNCTION = "poseidon.setLearning";
    private String API_KEY;

    private static final String LOG_TAG = "XMLRPC Log Uploader";

    private XMLRPCClient mRPCClient;
    private ContextDB mContextDB;
    private DataLogger mLogger;


    public XmlRpcLogUploader(Context context, ContextDB db, DataLogger logger) {

        mContextDB = db;
        mLogger = logger;
        Bundle metadata = null;

        try {
            metadata = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA).metaData;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(LOG_TAG, e.getMessage());
        }

        API_KEY = metadata.getString("contextService_ApiKey", "");
        SERVER_URL = "https://" + metadata.getString("contextService_Host", "") + "/rpc/rpc.php";

        try {
            mRPCClient = new XMLRPCClient(new URL(SERVER_URL));
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, e.getMessage());
        }

    }

    @Override
    public boolean setLearningMode(final int userNumber, final boolean mode) {

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ArrayList params = new ArrayList();
                    params.add(API_KEY);
                    params.add(userNumber);
                    params.add(mode);

                    Object i = mRPCClient.call(RPC_SET_LEARNING_FUNCTION, params);


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

        return true;
    }

    @Override
    public boolean registerUser(final int userNumber, final String userIdent, final String deviceIdent) {

        final boolean[] success = {false};

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ArrayList params = new ArrayList();
                    params.add(API_KEY);
                    params.add(userNumber);
                    params.add(userIdent);
                    params.add(deviceIdent);

                    Object i = mRPCClient.call(RPC_REGISTER_FUNCTION, params);

                    if (i instanceof Integer) {
                        mLogger.newUserID((Integer) i);
                    } else if (i instanceof String) {
                        mLogger.newUserID(Integer.valueOf((String) i));
                    }

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
    public void uploadLogToServer(int userID) {

        List<LogEvent> events = mContextDB.getAllEvents();

        ArrayList origins = new ArrayList();
        ArrayList locations = new ArrayList();
        ArrayList dates = new ArrayList();
        ArrayList text = new ArrayList();

        for (LogEvent event : events) {
            origins.add(event.getOrigin());
            locations.add(event.getLocation());
            dates.add(mLogger.mDateFormater.format(event.getDate() * 1000));
            text.add(event.getText());
        }

        final ArrayList params = new ArrayList();
        params.add(API_KEY);
        params.add(events.size());
        params.add(userID);
        params.add(origins);
        params.add(locations);
        params.add(dates);
        params.add(text);

        new Thread(new Runnable() {
            @Override
            public void run() {
                mRPCClient.callAsync(XmlRpcLogUploader.this, RPC_UPLOAD_FUNCTION, params);
            }
        }).start();
    }

    @Override
    public void stop() {

    }

    @Override
    public void onResponse(long id, Object result) {
        if ((Integer) result == 1) {
            mLogger.completedBackup();
        }
    }

    @Override
    public void onError(long id, XMLRPCException error) {
        Log.e(LOG_TAG, error.getMessage().toString());
        mLogger.incompleteBackup();
    }

    @Override
    public void onServerError(long id, XMLRPCServerException error) {
        Log.e(LOG_TAG, error.getMessage().toString());
        mLogger.incompleteBackup();
    }
}
