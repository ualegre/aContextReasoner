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

package org.poseidon_project.context.reasoner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Holds Temporal Literal Value information
 *
 * @author Dean Kramer <d.kramer@mdx.ac.uk>
 */
public class TemporalValue {

    public long mStartTime = 0;
    public String mStartTimeString = "";
    public long mEndTime = 0;
    public String mEndTimeString = "";
    public boolean mAbsolute = false;
    public boolean mStrong = false;
    public boolean mStartUpdate = false;
    public boolean mEndUpdate = false;

    public boolean parseTemporalValues() throws ParseException {

        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");

        Date day = new Date();

        String[] startDateTime = mStartTimeString.split(" ");

        Calendar startCal = Calendar.getInstance();

        if (startDateTime.length == 1) {
            if (mStartTimeString.contains(":")) {
                Date date = timeFormatter.parse(mStartTimeString);
                startCal.set(Calendar.HOUR_OF_DAY, date.getHours());
                startCal.set(Calendar.MINUTE, date.getMinutes());
                startCal.set(Calendar.SECOND, date.getSeconds());
                mStartTime = startCal.getTimeInMillis();
                mAbsolute = true;
                mStartUpdate = true;
            } else {
                mStartTime = Long.parseLong(mStartTimeString);
            }

        } else if (startDateTime.length == 2) {
            startCal.setTime(dateFormatter.parse(mStartTimeString));
            mStartTime = startCal.getTimeInMillis();
            mAbsolute = true;
        }

        Calendar cal = Calendar.getInstance();
        long currentMilli = cal.getTimeInMillis();

        //if time is later in the day, we must check against yesterday with same time.
        boolean startYesterday = false;
        if (mStartTime > currentMilli) {
            mStartTime -= 86400000;
            startYesterday = true;
        }

        if (! mEndTimeString.isEmpty() && mAbsolute) {
            String[] endDateTime = mEndTimeString.split(" ");

            Calendar endCal = Calendar.getInstance();

            if (endDateTime.length == 1) {
                Date date = timeFormatter.parse(mEndTimeString);
                endCal.set(Calendar.HOUR_OF_DAY, date.getHours());
                endCal.set(Calendar.MINUTE, date.getMinutes());
                endCal.set(Calendar.SECOND, date.getSeconds());
                mEndUpdate = true;
            } else if (endDateTime.length == 2) {
                endCal.setTime(dateFormatter.parse(mEndTimeString));
            }

            mEndTime = endCal.getTimeInMillis();

            //Only if the temporalValue is strong do both values need to be prior to current time
            if (mStrong) {
                if (mEndTime > currentMilli) {
                    mEndTime -= 86400000;

                    //If the start date isn't yesterday already we need to also change
                    if (!startYesterday) {
                        mStartTime -= 86400000;
                    }
                }
            }

        }

        return mAbsolute;
    }
}
