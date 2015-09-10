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

import android.util.Log;

import org.poseidon_project.context.database.ContextDB;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Provides logging capabilities for pilot debugging/data recovery.
 *
 * @author Dean Kramer <d.kramer@mdx.ac.uk>
 */
public class DebugLogger {

    private ContextDB mContextDB;
    private String[][] mEventsArray = new String[50][2];
    private int mEventsArraySize = 0;
    private SimpleDateFormat mDateFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private Calendar mCalendar = Calendar.getInstance();
    private static final String LOG_TAG = "Context Middleware";

    //Whether or not verbose events should be sent to Android Log.
    private static final boolean VERBOSE = true;

    public DebugLogger (ContextDB db) {
        mContextDB = db;
    }

    private void log(String event) {

        if (mEventsArraySize == 50)  {
            persist();
        }

        synchronized (this) {
            mEventsArray[mEventsArraySize][0] = event;
            mEventsArray[mEventsArraySize][1] = mDateFormater.format(mCalendar.getTime());
            mEventsArraySize++;
        }

    }

    public void logError(String event) {
        event = "Error: " + event;
        log(event);
        Log.e(LOG_TAG, event);
    }

    public void logError(String logtag, String event) {
        event = "Error: " + event;
        log(event);
        Log.e(logtag, event);
    }

    public void logVerbose(String event) {
        log(event);
        if(VERBOSE) {
            Log.v(LOG_TAG, event);
        }
    }

    public void logVerbose(String logtag, String event) {
        log(event);
        if (VERBOSE) {
            Log.v(logtag, event);
        }
    }

    public boolean stop() {
        if (mEventsArraySize != 0) {
            return persist();
        } else {
            return true;
        }
    }

    private synchronized String[][] copyCache() {
        String[][] temp = new String[mEventsArraySize][2];

        for (int i = 0; i < mEventsArraySize; i++) {
            temp[i] = mEventsArray[i].clone();
        }

        mEventsArraySize = 0;

        return temp;
    }

    private boolean persist() {

        String[][] temp = copyCache();

        return mContextDB.newEvents(temp);
    }
}
