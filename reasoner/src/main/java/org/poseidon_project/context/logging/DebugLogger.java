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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.util.Log;

import org.poseidon_project.context.database.ContextDB;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;


/**
 * Provides logging capabilities for pilot debugging/data recovery.
 *
 * @author Dean Kramer <d.kramer@mdx.ac.uk>
 */
public class DebugLogger {

    private static final int EARLIEST_BACKUP_HOUR = 20;
    private static final int ARRAY_CAPACITY = 50;
    private int mBackupHour;
    private int mBackupMin;
    private int mUserID;

    private ContextDB mContextDB;
    private List<LogEvent> mEventsArray = new ArrayList<>(ARRAY_CAPACITY);
    private int mEventsArraySize = 0;
    private SimpleDateFormat mDateFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private Calendar mCalendar = Calendar.getInstance();
    private static final String LOG_TAG = "Context Middleware";
    private Context mContext;
    private LogUploader mUploader;
    public static final String CONTEXT_PREFS = "ContextPrefs";
    private Intent mAlarmIntent = null;

    //Whether or not verbose events should be sent to Android Log.
    private static final boolean VERBOSE = true;

    public DebugLogger (Context context, ContextDB db) {
        mContextDB = db;
        mContext = context;
        mUploader = new LogUploader(mContext, mContextDB, this);
        checkBackupSettings();
        setupBackupAlarm();

    }

    private void setupBackupAlarm() {

        Calendar timeToStart = Calendar.getInstance();
        timeToStart.set(Calendar.HOUR_OF_DAY, mBackupHour);
        timeToStart.set(Calendar.MINUTE, mBackupMin);
        timeToStart.set(Calendar.SECOND, 0);

        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        //Intent i = ExplicitIntentGenerator.createExplicitFromImplicitIntent
        //        (mContext, new Intent(mContext, BackupLogAlarmReceiver.class));

        PendingIntent pi = PendingIntent.getBroadcast(mContext, 0, new Intent(mContext, BackupLogAlarmReceiver.class), 0);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, timeToStart.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pi);

        Log.v(LOG_TAG, "Alarm Set");

    }

    private void checkBackupSettings() {

        SharedPreferences settings = mContext.getSharedPreferences(CONTEXT_PREFS, 0);
        int backupHour = settings.getInt("logBackupHour", -1);
        int backupMin = settings.getInt("logBackupMin", -1);
        String lastBackupDate = settings.getString("logLastBackup", "");
        mUserID = settings.getInt("userId", -1);

        if (backupHour < 0 || backupMin < 0) {
            Random randomGenerator = new Random();

            int hourAfterEarliest = randomGenerator.nextInt(8);

            mBackupHour = EARLIEST_BACKUP_HOUR + hourAfterEarliest;
            if (mBackupHour > 24) {
                mBackupHour =- 24;
            }

            mBackupMin = randomGenerator.nextInt(60);

            SharedPreferences.Editor editor = settings.edit();
            editor.putInt("logBackupHour", mBackupHour);
            editor.putInt("logBackupMin", mBackupMin);
            editor.commit();
        } else {
            mBackupHour = backupHour;
            mBackupMin = backupMin;
        }
    }

    public void attemptBackup() {
        if (inCorrectBackupConditions()) {
            persist();
            mUploader.uploadLogToServer(mUserID);
        }
    }

    public void attemptBackup(Intent intent) {

        if (inCorrectBackupConditions()) {
            mAlarmIntent = intent;
            persist();
            mUploader.uploadLogToServer(mUserID);
        } else {
            BackupLogAlarmReceiver.completeWakefulIntent(intent);
        }
    }

    private boolean inCorrectBackupConditions() {

        if (phoneIsPluggedIn()) {
            return true;
        }

        return false;
    }

    public void completedBackup() {

        SharedPreferences settings = mContext.getSharedPreferences(CONTEXT_PREFS, 0);
        SharedPreferences.Editor editor = settings.edit();
        String date =  mDateFormater.format(mCalendar.getTime());
        editor.putString("logLastBackup", date);
        mContextDB.emptyEvents();

        if (mAlarmIntent != null) {
            BackupLogAlarmReceiver.completeWakefulIntent(mAlarmIntent);
            mAlarmIntent = null;
        }
    }

    private boolean isConnectedToWifiInternet() {
        return true;
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

        if (mEventsArraySize == 50)  {
            persist();
        }

        synchronized (this) {

            String dateTime = mDateFormater.format(mCalendar.getTime());

            //TODO
            //Need best way of getting location

            LogEvent logEvent = new LogEvent(0, "", dateTime, event);

            mEventsArray.add(logEvent);
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

    private synchronized List<LogEvent> copyCache() {
        List<LogEvent> temp = mEventsArray;

        mEventsArray = new ArrayList<>(ARRAY_CAPACITY);
        mEventsArraySize = 0;

        return temp;
    }

    private boolean persist() {

        List<LogEvent> temp = copyCache();

        return mContextDB.newEvents(temp);
    }

    public boolean registerUser(String userIdent) {

        mUploader.registerUser(userIdent);
        return true;
    }

    public void newUserID(int userID) {

        if (userID > 0) {
            SharedPreferences settings = mContext.getSharedPreferences(CONTEXT_PREFS, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt("userId", userID);
            editor.commit();
        }
    }
}
