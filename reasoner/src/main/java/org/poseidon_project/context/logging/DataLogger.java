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
import android.os.Build;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.poseidon_project.context.BuildConfig;
import org.poseidon_project.context.database.ContextDB;
import org.poseidon_project.contexts.hardware.PluggedInContext;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;


/**
 * Provides logging capabilities for off-device learning, debugging, and data collection
 *
 * @author Dean Kramer <d.kramer@mdx.ac.uk>
 */
public class DataLogger {

    public static final int SYSTEM_CORE = 0;
    public static final int CONTEXT_MANAGER = 1;
    public static final int REASONER = 2;

    private static final int EARLIEST_BACKUP_HOUR = 20;
    private static final int ARRAY_CAPACITY = 50;
    private static final long FORCE_BACKUP_TIME = 1000 * 60 * 60 * 48;
    private static final long FORCED_RETRY_TIME = 1000 * 60 * 30;
    private static final long NEXT_BACKUP_TIME = 1000 * 60 * 60 * 24;
    private long mBackupTime;
    private int mBackupHour;
    private int mBackupMin;
    private int mUserID;
    private boolean mAlarmset = false;

    private ContextDB mContextDB;
    private List<LogEvent> mEventsArray = new ArrayList<>(ARRAY_CAPACITY);
    private int mEventsArraySize = 0;
    private SimpleDateFormat mDateFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final String LOG_TAG = "Context Middleware";
    private Context mContext;
    private LogUploader mUploader;
    public static final String CONTEXT_PREFS = "ContextPrefs";
    private Intent mAlarmIntent = null;
    private LogLocationReceiver mLocationReceiver;
    private PluggedInContext mPluggedInContext;
    private boolean mForcedBackup = false;
    private String mDeviceID;
    private SharedPreferences mSettings;

    public static final String BROADCAST_INTENT = "org.poseidon_project.context.NEEDS_BAT_OP_SET_OFF";
    public static final String BROADCAST_BACKUP = "org.poseidon_project.context.BACKEDUP";


    //Whether or not verbose events should be sent to Android Log.
    private static final boolean VERBOSE = true;

    public DataLogger(Context context, ContextDB db) {
        mContextDB = db;
        mContext = context;
        mUploader = new LogUploader(mContext, mContextDB, this);
        mLocationReceiver = new LogLocationReceiver(mContext);
        mSettings = mContext.getSharedPreferences(CONTEXT_PREFS, 0);

        checkBackupSettings();
        setupBackupAlarm();


        attemptBackup(null);

    }

    private void setupBackupAlarm() {

        Calendar timeToStart = Calendar.getInstance();
        timeToStart.set(Calendar.HOUR_OF_DAY, mBackupHour);
        timeToStart.set(Calendar.MINUTE, mBackupMin);
        timeToStart.set(Calendar.SECOND, 0);
        mBackupTime = timeToStart.getTimeInMillis();

        if(mBackupTime < System.currentTimeMillis()) {
            mBackupTime += NEXT_BACKUP_TIME;
        }

        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);

        PendingIntent pi = PendingIntent.getBroadcast(mContext, 0, new Intent(mContext, BackupLogAlarmReceiver.class), 0);

        //Cancel previous defined alarms
        alarmManager.cancel(pi);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
            String packageName = mContext.getPackageName();
            if(powerManager.isIgnoringBatteryOptimizations(packageName)) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, mBackupTime, pi);
                mAlarmset = true;
            } else {
                //Need the running app to take the user to whitelist this app
                Intent intent = new Intent();
                intent.setAction(BROADCAST_INTENT);
                intent.putExtra("package", packageName);
                mContext.sendBroadcast(intent);
            }

        } else {
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, mBackupTime,
                    AlarmManager.INTERVAL_DAY, pi);
            mAlarmset = true;
        }

    }

    private void checkBackupSettings() {

        int backupHour = mSettings.getInt("logBackupHour", -1);
        int backupMin = mSettings.getInt("logBackupMin", -1);

        if (needsNewID(mSettings)) {
            mUserID = -1;
        } else {
            mUserID = mSettings.getInt("userId", -1);
        }

        if (backupHour < 0 || backupMin < 0) {
            Random randomGenerator = new Random();

            int hourAfterEarliest = randomGenerator.nextInt(8);

            mBackupHour = EARLIEST_BACKUP_HOUR + hourAfterEarliest;
            if (mBackupHour >= 24) {
                mBackupHour -= 24;
            }

            mBackupMin = randomGenerator.nextInt(60);

            SharedPreferences.Editor editor = mSettings.edit();
            editor.putInt("logBackupHour", mBackupHour);
            editor.putInt("logBackupMin", mBackupMin);
            editor.commit();
        } else {
            mBackupHour = backupHour;
            mBackupMin = backupMin;
        }
    }

    public void setBackupTime(int hour, int mins) {

        mBackupHour = hour;
        mBackupMin = mins;

        SharedPreferences.Editor editor = mSettings.edit();
        editor.putInt("logBackupHour", mBackupHour);
        editor.putInt("logBackupMin", mBackupMin);
        editor.commit();

        setupBackupAlarm();
    }

    private boolean needsNewID(SharedPreferences prefs) {

        int lastIDVersion = prefs.getInt("userIdVersion", -1);

        if (lastIDVersion < 16) {
            return true;
        } else {
            return false;
        }

    }

    public void uploadLog() {

        if (mUserID < 0) {
            String uuid = UUID.randomUUID().toString();
            mDeviceID = getDeviceID();

            if (! registerUser(uuid, mDeviceID)) {
                incompleteBackup();
                return;
            }
        }

        persist();
        mUploader.uploadLogToServer(mUserID);
    }

    private String getDeviceID() {

        final String deviceId = ((TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        if (deviceId != null) {
            return "p:" + deviceId;
        } else {
            return "s:" + android.os.Build.SERIAL;
        }
    }

    public void attemptBackup(Intent intent) {

        if (needsForcedBackup()) {
            mAlarmIntent = intent;
            uploadLog();
        } else {
            if (inCorrectBackupConditions()) {
                mAlarmIntent = intent;
                uploadLog();
            } else {
                if (intent != null) {
                    BackupLogAlarmReceiver.completeWakefulIntent(intent);
                }
            }
        }

    }

    private boolean inCorrectBackupConditions() {

        return phoneIsPluggedIn();

    }

    public void completedBackup() {

        if (mForcedBackup) {
            mForcedBackup = false;
        }

        mBackupTime += NEXT_BACKUP_TIME;

        SharedPreferences.Editor editor = mSettings.edit();
        editor.putLong("logLastBackup", System.currentTimeMillis());
        editor.commit();
        mContextDB.emptyEvents();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);

            PendingIntent pi = PendingIntent.getBroadcast(mContext, 0, new Intent(mContext, BackupLogAlarmReceiver.class), 0);
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, mBackupTime, pi);

        }

        if (mAlarmIntent != null) {
            BackupLogAlarmReceiver.completeWakefulIntent(mAlarmIntent);
            mAlarmIntent = null;
        }

        Intent intent = new Intent();
        intent.setAction(BROADCAST_BACKUP);
        mContext.sendBroadcast(intent);
    }

    public void incompleteBackup() {

        if (mForcedBackup) {
            AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
            long timeToRetry = System.currentTimeMillis() + FORCED_RETRY_TIME;

            PendingIntent pi = PendingIntent.getBroadcast(mContext, 0, new Intent(mContext, BackupLogAlarmReceiver.class), 0);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeToRetry, pi);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, timeToRetry, pi);
            }

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


    private void log(int origin, String event) {

        if (mEventsArraySize == 50)  {
            persist();
        }

        synchronized (this) {

            if (! mAlarmset) {
                setupBackupAlarm();
            }

            String dateTime = mDateFormater.format(new Date());

            LogEvent logEvent = new LogEvent(origin, mLocationReceiver.getLocationString(),
                    dateTime, event);

            mEventsArray.add(logEvent);
            mEventsArraySize++;
        }

    }

    public void logError(int origin, String event) {
        event = "Error: " + event;
        log(origin, event);
        Log.e(LOG_TAG, event);
    }

    public void logError(int origin, String logtag, String event) {
        event = "Error: " + event;
        log(origin, event);
        Log.e(logtag, event);
    }

    public void logVerbose(int origin, String event) {
        log(origin, event);
        if(VERBOSE) {
            Log.v(LOG_TAG, event);
        }
    }

    public void logVerbose(int origin, String logtag, String event) {
        log(origin, event);
        if (VERBOSE) {
            Log.v(logtag, event);
        }
    }

    public boolean stop() {

        mLocationReceiver.stop();

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

    public boolean registerUser(String userIdent, String deviceIdent) {
        return mUploader.registerUser(new Integer(mUserID), userIdent, deviceIdent);
    }

    public boolean registerUser(String userIdent) {
        mDeviceID = getDeviceID();
        return registerUser(userIdent, mDeviceID);
    }

    public void newUserID(int userID) {

        if (userID > 0) {
            mUserID = userID;
            SharedPreferences.Editor editor = mSettings.edit();
            editor.putInt("userId", userID);
            editor.putString("deviceId", mDeviceID);
            editor.putInt("userIdVersion", BuildConfig.VERSION_CODE);
            editor.commit();
        }
    }

    private boolean needsForcedBackup () {

        long lastBackupMS = mSettings.getLong("logLastBackup", -1);
        long diffMS = System.currentTimeMillis() - lastBackupMS;

        if (diffMS > FORCE_BACKUP_TIME) {
            mForcedBackup = true;
            return true;
        } else {
            mForcedBackup = false;
            return false;
        }
    }

    public void alterLearningMode(boolean mode) {

        mUploader.setLearningMode(mUserID, mode);
    }
}
