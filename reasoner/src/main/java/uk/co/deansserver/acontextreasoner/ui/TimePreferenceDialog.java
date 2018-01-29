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

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

import uk.co.deansserver.acontextreasoner.R;

import java.util.ArrayList;

/**
 * A Time Picker Prefs that can be reused for multiple preferences
 *
 * @author Dean Kramer <deankramer99@gmail.com>
 */
public class TimePreferenceDialog extends DialogPreference{

    private int mHour = 0;
    private int mMinute = 0;
    private TimePicker picker = null;

    public TimePreferenceDialog(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TimePreferenceDialog(Context context, AttributeSet attrs) {
        super(context, attrs);
        setPositiveButtonText(R.string.setTime);
        setNegativeButtonText(R.string.cancelTime);
    }

    public TimePreferenceDialog(Context context) {
        super(context, null);
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);
        picker.setCurrentHour(mHour);
        picker.setCurrentMinute(mMinute);
    }

    @Override
    protected View onCreateDialogView() {
        picker = new TimePicker(getContext());

        return picker;
    }

    public void updateTime(int hour, int min) {
        mHour = hour;
        mMinute = min;
        updateSummary();
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {

        String time = null;

        time = defaultValue.toString();

        String[] split = time.split(":");

        mHour = Integer.parseInt(split[0]);
        mMinute = Integer.parseInt(split[1]);

        updateSummary();
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            int currHour = picker.getCurrentHour();
            int currMinute = picker.getCurrentMinute();


            //Not great to autobox, but don't feel I have an easier choice.
            ArrayList<Integer> results = new ArrayList<>(2);
            results.add(currHour);
            results.add(currMinute);

            if (!callChangeListener(results)) {
                return;
            }

            mHour = currHour;
            mMinute = currMinute;
            updateSummary();
        }
    }

    public void updateSummary() {

        String hour = String.valueOf(mHour);

        if (hour.length() == 1) {
            hour = "0" + hour;
        }

        String min = String.valueOf(mMinute);

        if (min.length() == 1) {
            min = "0" + min;
        }

        String time = hour + ":" + min;
        setSummary(time);
    }
}
