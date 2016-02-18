/*
 * Copyright 2016 POSEIDON Project
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

package org.poseidon_project.context.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.widget.Toast;

import org.poseidon_project.context.R;
import org.poseidon_project.context.logging.DataLogger;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;


/**
 * The user visible fragment to personalise context rules
 *
 * @author Dean Kramer <d.kramer@mdx.ac.uk>
 */
public class ContextReasonerSettingsFragment extends PreferenceFragment
        implements OnPreferenceClickListener{

    private static final int EARLIEST_BACKUP_HOUR = 20;
    private Preference mLogUserNamePreference;
    private Preference mLastSynchronised;
    private TimePreferenceDialog mTimeToBackupPreference;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = (ContextReasonerSettings) getActivity();

        mMainSettings = mActivity.getSharedPreferences("ContextPrefs", 0);
        mRuleSettings = mActivity.getSharedPreferences("RulePrefs", 0);

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

    private void setupMainSettings(){
        setupLastSychronisedPref();
        setupUserIdentifierPref();
        setupTimeToSychonisePref();
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
            long lastBackupMS = mMainSettings.getLong("logLastBackup", 0);

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
            int userId = mMainSettings.getInt("userId", -1);
            mLogUserNamePreference.setSummary(String.valueOf(userId));
        }
    }

    private void setupTimeToSychonisePref() {

        int backupHour = mMainSettings.getInt("logBackupHour", -1);
        int backupMin = mMainSettings.getInt("logBackupMin", -1);

        if (backupHour < 0 || backupMin < 0) {
            Random randomGenerator = new Random();

            int hourAfterEarliest = randomGenerator.nextInt(8);

            backupHour = EARLIEST_BACKUP_HOUR + hourAfterEarliest;
            if (backupHour >= 24) {
                backupHour =- 24;
            }

            backupMin = randomGenerator.nextInt(60);

            SharedPreferences.Editor editor = mMainSettings.edit();
            editor.putInt("logBackupHour", backupHour);
            editor.putInt("logBackupMin", backupMin);
            editor.commit();
        }

        OnPreferenceChangeListener timeChangeListerner = new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                if (newValue != null) {
                    Integer hour = ((ArrayList<Integer>) newValue).get(0);
                    Integer min = ((ArrayList<Integer>) newValue).get(1);
                    SharedPreferences.Editor editor = mMainSettings.edit();
                    editor.putInt("logBackupHour", hour);
                    editor.putInt("logBackupMin", min);
                    editor.commit();

                    //@TODO Need to update reasoner without needing it to restart

                    return true;
                }

                return false;
            }
        };

        mTimeToBackupPreference = (TimePreferenceDialog)
                findPreference(getString(R.string.pref_backuptime));

        mTimeToBackupPreference.updateTime(backupHour, backupMin);
        mTimeToBackupPreference.setOnPreferenceChangeListener(timeChangeListerner);
    }

    private void setupWeatherSettings() {

        final String pref_hot = getString(R.string.pref_hot);
        final String pref_cold = getString(R.string.pref_cold);

        mHotTemperature = mRuleSettings.getInt(pref_hot, 25);
        mColdTemperature = mRuleSettings.getInt(pref_cold, 15);

        mHotTemperaturePreference = (EditTextPreference)
                findPreference(pref_hot);

        String hot_temp = String.valueOf(mHotTemperature);
        mHotTemperaturePreference.setText(hot_temp);
        mHotTemperaturePreference.setSummary(hot_temp);

        OnPreferenceChangeListener hotChangeListerner = new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                if (newValue != null) {

                    if (temperatureSatisfible((Integer) newValue, mColdTemperature)) {
                        SharedPreferences.Editor editor = mRuleSettings.edit();
                        editor.putInt(pref_hot, (Integer) newValue);
                        editor.commit();
                        mHotTemperature = (Integer) newValue;
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

                    if (temperatureSatisfible(mHotTemperature, (Integer) newValue)) {
                        SharedPreferences.Editor editor = mRuleSettings.edit();
                        editor.putInt(pref_cold, (Integer) newValue);
                        editor.commit();
                        mColdTemperature = (Integer) newValue;
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

    private boolean temperatureSatisfible(int hot, int cold) {

        if (hot > cold) {
            return true;
        } else {
            return false;
        }
    }

    private void setupNavigationAssistenceSettings() {
        //max wait
        final String pref_max_wait = getString(R.string.pref_max_wait);

        int max_wait = mRuleSettings.getInt(pref_max_wait, 5);

        String max_wait_str = String.valueOf(max_wait);

        mMaxWaitingPreference = (EditTextPreference)
                findPreference(pref_max_wait);

        mMaxWaitingPreference.setText(max_wait_str);
        mMaxWaitingPreference.setSummary(max_wait_str);

        OnPreferenceChangeListener maxWaitingChangeListerner = new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                if (newValue != null) {
                    SharedPreferences.Editor editor = mRuleSettings.edit();
                    editor.putInt(pref_max_wait, (Integer) newValue);
                    editor.commit();
                    preference.setSummary(String.valueOf((Integer) newValue));
                    return true;
                }

                return false;
            }
        };

        mMaxWaitingPreference.setOnPreferenceChangeListener(maxWaitingChangeListerner);


        //deviation
        final String pref_max_dev = getString(R.string.pref_max_dev);
        int max_dev = mRuleSettings.getInt(pref_max_dev, 2);

        String max_dev_str = String.valueOf(max_dev);

        mMaxSmallDevPreference = (EditTextPreference)
                findPreference(pref_max_dev);

        mMaxSmallDevPreference.setText(max_dev_str);
        mMaxSmallDevPreference.setSummary(max_dev_str);

        OnPreferenceChangeListener maxDevChangeListerner = new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                if (newValue != null) {
                    SharedPreferences.Editor editor = mRuleSettings.edit();
                    editor.putInt(pref_max_dev, (Integer) newValue);
                    editor.commit();
                    preference.setSummary(String.valueOf((Integer) newValue));
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
