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

package uk.co.deansserver.acontextreasoner.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.widget.Toast;

import org.poseidon_project.acontextreasoner.R;
import uk.co.deansserver.acontextreasoner.logging.DataLogger;
import uk.co.deansserver.acontextreasoner.utility.Prefs;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;


/**
 * The user visible fragment to personalise context rules
 *
 * @author Dean Kramer <deankramer99@gmail.com>
 */
public class ContextReasonerSettingsFragment extends PreferenceFragment
        implements OnPreferenceClickListener{

    private static final int EARLIEST_BACKUP_HOUR = 20;
    private Preference mLogUserNamePreference;
    private Preference mLastSynchronised;
    private TimePreferenceDialog mTimeToBackupPreference;
    private CheckBoxPreference mLearningPreference;
    private EditTextPreference mHotTemperaturePreference;
    private EditTextPreference mColdTemperaturePreference;
    private EditTextPreference mMaxWaitingPreference;
    private EditTextPreference mMaxSmallDevPreference;
    private BroadcastReceiver mLastSynchronisedBReceiver;
    private SharedPreferences mMainSettings;
    private SharedPreferences mRuleSettings;
    private ContextReasonerSettings mActivity;
    private int mHotTemperature;
    private int mColdTemperature;
    private Integer mBackupHour;
    private Integer mBackupMin;
    private boolean mSetBackupTime = false;
    private boolean mSetUserIdentifier = true;
    private static final String LOG_TAG = "ContextReasonerSettings";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = (ContextReasonerSettings) getActivity();

        mMainSettings = mActivity.getSharedPreferences(Prefs.REASONER_PREFS, 0);
        mRuleSettings = mActivity.getSharedPreferences(Prefs.RULE_PREFS, 0);

        addPreferencesFromResource(R.xml.settings);

        setupMainSettings();

        setupWeatherSettings();

        setupNavigationAssistenceSettings();

    }

    @Override
    public void onResume() {
        super.onResume();
        setupLastSychronisedBReceiver();
    }

    public void loggedIn() {

        if (mSetBackupTime) {
            setTimePreference(mBackupHour, mBackupMin);
        }

        if (! mSetUserIdentifier) {
            setupUserIdentifierPref();
        }
    }

    private void setupMainSettings(){
        setupLastSychronisedPref();
        setupUserIdentifierPref();
        setupTimeToSychonisePref();
        setupLearningPref();
    }

    private void setupLearningPref() {
        mLearningPreference = (CheckBoxPreference) findPreference(getString(R.string.pref_learning));

        if (mLearningPreference != null) {
            boolean mode = mMainSettings.getBoolean(Prefs.REASONER_LEARNING, true);
            mLearningPreference.setChecked(mode);

            mLearningPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {

                    if (newValue instanceof Boolean) {

                        try {
                            mActivity.mContextService.alterLearning((Boolean) newValue);
                        } catch (RemoteException e) {
                            Log.e("error", e.getMessage());
                        }

                        return true;
                    }

                    return false;
                }
            });
        }
    }

    private void setupLastSychronisedBReceiver() {
        mLastSynchronisedBReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                setupLastSychronisedPref();
            }
        };

        IntentFilter filter = new IntentFilter(DataLogger.BROADCAST_BACKUP);
        mActivity.registerReceiver(mLastSynchronisedBReceiver, filter);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {

        String preferenceKey = preference != null? preference.getKey() : "";

        if(preferenceKey.equals(getString(R.string.pref_sync))) {
            handleSynchronisedPreferenceClick();
        }

        return false;
    }

    private void handleSynchronisedPreferenceClick() {
        if (mActivity.isBound()) {
            try {
                mActivity.mContextService.synchroniseService();
            } catch (RemoteException e) {
                Log.e("error", e.getMessage());
            }
        }
    }

    private void setupLastSychronisedPref() {
        mLastSynchronised = (Preference) findPreference(getString(R.string.pref_sync));

        if (mLastSynchronised != null) {
            long lastBackupMS = mMainSettings.getLong(Prefs.REASONER_LASTBACKUP, 0);

            if (lastBackupMS > 0) {
                Calendar time = Calendar.getInstance();
                time.setTimeInMillis(lastBackupMS);

                mLastSynchronised.setSummary(time.getTime().toString());
            } else {
                mLastSynchronised.setSummary(R.string.sync_never);
            }
        }

        mLastSynchronised.setOnPreferenceClickListener(this);
    }

    private void setupUserIdentifierPref() {
        mLogUserNamePreference = (Preference) findPreference(getString(R.string.pref_userid));

        if (mLogUserNamePreference != null) {
            int userId = mMainSettings.getInt(Prefs.REASONER_USERID, -1);

            if (userId == -1) {
                mSetUserIdentifier = false;
            }

            mLogUserNamePreference.setSummary(String.valueOf(userId));
        }
    }

    private void setupTimeToSychonisePref() {

        mBackupHour = mMainSettings.getInt(Prefs.REASONER_BACKUPHOUR, -1);
        mBackupMin = mMainSettings.getInt(Prefs.REASONER_BACKUPMIN, -1);

        if (mBackupHour < 0 || mBackupMin < 0) {
            Random randomGenerator = new Random();

            int hourAfterEarliest = randomGenerator.nextInt(8);

            mBackupHour = EARLIEST_BACKUP_HOUR + hourAfterEarliest;
            if (mBackupHour >= 24) {
                mBackupHour -= 24;
            }

            mBackupMin = randomGenerator.nextInt(60);

            mSetBackupTime = true;

        }

        OnPreferenceChangeListener timeChangeListerner = new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                if (newValue != null) {
                    Integer hour = ((ArrayList<Integer>) newValue).get(0);
                    Integer min = ((ArrayList<Integer>) newValue).get(1);

                    setTimePreference(hour, min);

                    return true;
                }

                return false;
            }
        };

        mTimeToBackupPreference = (TimePreferenceDialog)
                findPreference(getString(R.string.pref_backuptime));

        mTimeToBackupPreference.updateTime(mBackupHour, mBackupMin);
        mTimeToBackupPreference.setOnPreferenceChangeListener(timeChangeListerner);
    }

    private void setTimePreference(Integer backupHour, Integer backupMin) {
        try {
            mActivity.mContextService.alterSynchroniseTime(backupHour, backupMin);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void setupWeatherSettings() {

        mHotTemperature = mRuleSettings.getInt(Prefs.WEATHER_HOT, 25);
        mColdTemperature = mRuleSettings.getInt(Prefs.WEATHER_COLD, 15);

        String pref_hot = getString(R.string.pref_hot);
        String pref_cold = getString(R.string.pref_cold);

        mHotTemperaturePreference = (EditTextPreference)
                findPreference(pref_hot);

        String hot_temp = String.valueOf(mHotTemperature);
        mHotTemperaturePreference.setText(hot_temp);
        mHotTemperaturePreference.setSummary(hot_temp);

        OnPreferenceChangeListener hotChangeListerner = new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                if (newValue != null) {

                    int newValueint = Integer.parseInt((String) newValue);

                    if (temperatureSatisfible(newValueint, mColdTemperature)) {
                        try {
                            mActivity.mContextService.alterPreferenceInt(Prefs.WEATHER_COLD, newValueint);
                        } catch (RemoteException e) {
                            Log.e(LOG_TAG, e.getMessage());
                        }
                        mHotTemperature = newValueint;
                        preference.setSummary(String.valueOf(mHotTemperature));
                        return true;
                    } else {
                        ((EditTextPreference) preference).setText(String.valueOf(mHotTemperature));
                        preference.setSummary(String.valueOf(mHotTemperature));
                        Toast.makeText(mActivity.getApplicationContext(),
                                R.string.hot_unsat, Toast.LENGTH_LONG).show();
                        return false;
                    }
                }

                return false;
            }
        };

        mHotTemperaturePreference.setOnPreferenceChangeListener(hotChangeListerner);

        mColdTemperaturePreference = (EditTextPreference)
                findPreference(pref_cold);


        String cold_temp = String.valueOf(mColdTemperature);
        mColdTemperaturePreference.setText(cold_temp);
        mColdTemperaturePreference.setSummary(cold_temp);

        OnPreferenceChangeListener coldChangeListerner = new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                if (newValue != null) {

                    int newValueint = Integer.parseInt((String) newValue);

                    if (temperatureSatisfible(mHotTemperature, newValueint)) {
                        try {
                            mActivity.mContextService.alterPreferenceInt(Prefs.WEATHER_HOT, newValueint);
                        } catch (RemoteException e) {
                            Log.e(LOG_TAG, e.getMessage());
                        }
                        mColdTemperature = newValueint;
                        preference.setSummary(String.valueOf(mColdTemperature));
                        return true;
                    } else {
                        ((EditTextPreference) preference).setText(String.valueOf(mColdTemperature));
                        preference.setSummary(String.valueOf(mColdTemperature));
                        Toast.makeText(mActivity.getApplicationContext(),
                                R.string.cold_unsat, Toast.LENGTH_LONG).show();
                        return false;
                    }
                }

                return false;
            }
        };

        mColdTemperaturePreference.setOnPreferenceChangeListener(coldChangeListerner);

    }

    private boolean temperatureSatisfible(long hot, long cold) {

        if (hot > cold) {
            return true;
        } else {
            return false;
        }
    }

    private void setupNavigationAssistenceSettings() {
        //max wait
        final String pref_max_wait = getString(R.string.pref_max_wait);

        int max_wait = mRuleSettings.getInt(Prefs.NAVASSIST_MAXWAIT, 5);

        String max_wait_str = String.valueOf(max_wait);

        mMaxWaitingPreference = (EditTextPreference)
                findPreference(pref_max_wait);

        mMaxWaitingPreference.setText(max_wait_str);
        mMaxWaitingPreference.setSummary(max_wait_str);

        OnPreferenceChangeListener maxWaitingChangeListerner = new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                if (newValue != null) {

                    int newValueint = Integer.parseInt((String) newValue);

                    try {
                        mActivity.mContextService.alterPreferenceInt(Prefs.NAVASSIST_MAXWAIT, newValueint);
                    } catch (RemoteException e) {
                        Log.e(LOG_TAG, e.getMessage());
                    }
                    preference.setSummary((String) newValue);
                    return true;
                }

                return false;
            }
        };

        mMaxWaitingPreference.setOnPreferenceChangeListener(maxWaitingChangeListerner);


        //deviation
        final String pref_max_dev = getString(R.string.pref_max_dev);
        int max_dev = mRuleSettings.getInt(Prefs.NAVASSIST_MAXDEV, 2);

        String max_dev_str = String.valueOf(max_dev);

        mMaxSmallDevPreference = (EditTextPreference)
                findPreference(pref_max_dev);

        mMaxSmallDevPreference.setText(max_dev_str);
        mMaxSmallDevPreference.setSummary(max_dev_str);

        OnPreferenceChangeListener maxDevChangeListerner = new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                if (newValue != null) {

                    int newValueint = Integer.parseInt((String) newValue);

                    try {
                        mActivity.mContextService.alterPreferenceInt(Prefs.NAVASSIST_MAXDEV, newValueint);
                    } catch (RemoteException e) {
                        Log.e(LOG_TAG, e.getMessage());
                    }
                    preference.setSummary(String.valueOf(newValueint));
                    return true;
                }

                return false;
            }
        };

        mMaxSmallDevPreference.setOnPreferenceChangeListener(maxDevChangeListerner);
    }

    @Override
    public void onStop() {
        mActivity.unregisterReceiver(mLastSynchronisedBReceiver);
        super.onStop();
    }

}
