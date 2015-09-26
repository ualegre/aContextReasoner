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
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
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

    private static final int ARRAY_CAPACITY = 50;

    private ContextDB mContextDB;
    private LogEvent[] mEventsArray = new LogEvent[ARRAY_CAPACITY];
    private int mEventsArraySize = 0;
    private SimpleDateFormat mDateFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private Calendar mCalendar = Calendar.getInstance();
    private static final String LOG_TAG = "Context Middleware";
    private BackupLogAlarmReceiver mBackupAlarmReceiver;
    private Context mContext;

    //Whether or not verbose events should be sent to Android Log.
    private static final boolean VERBOSE = true;

    public DebugLogger (Context context, ContextDB db) {
        mContextDB = db;
        mContext = context;


    }

    protected void attemptBackup() {

    }

    private boolean inCorrectBackupConditions() {

        if (phoneIsPluggedIn()) {
            return true;
        }

        return false;
    }

    private boolean phoneIsPluggedIn() {

        boolean pluggedIn = false;

        final Intent batteryIntent = mContext.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean batteryCharge = status==BatteryManager.BATTERY_STATUS_CHARGING;

        int chargePlug = batteryIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
        boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;

        if (batteryCharge) pluggedIn=true;
        if (usbCharge) pluggedIn=true;
        if (acCharge) pluggedIn=true;

        return pluggedIn;

    }


    private void log(String event) {

        attemptBackup();

        if (mEventsArraySize == 50)  {
            persist();
        }

        synchronized (this) {

            String dateTime = mDateFormater.format(mCalendar.getTime());

            LogEvent logEvent = new LogEvent(0, "", dateTime, event);

            mEventsArray[mEventsArraySize] = logEvent;
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

    private synchronized LogEvent[] copyCache() {
        LogEvent[] temp = mEventsArray;

        mEventsArray = new LogEvent[ARRAY_CAPACITY];
        mEventsArraySize = 0;

        return temp;
    }

    private boolean persist() {

        LogEvent[] temp = copyCache();

        return mContextDB.newEvents(temp);
    }
}
