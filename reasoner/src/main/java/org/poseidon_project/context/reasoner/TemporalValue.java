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

        String[] startDateTime = mStartTimeString.split(" ");

        Calendar startCal = Calendar.getInstance();

        if (startDateTime.length == 1) {
            startCal.setTime(timeFormatter.parse(mStartTimeString));
        } else if (startDateTime.length == 2) {
            startCal.setTime(dateFormatter.parse(mStartTimeString));
        }


        mStartTime = startCal.getTimeInMillis();

        Calendar cal = Calendar.getInstance();
        long currentMilli = cal.getTimeInMillis();

        //if time is later in the day, we must check against yesterday with same time.
        if (mStartTime > currentMilli) {
            mStartTime -= 86400000;
        }

        if (! mEndTimeString.isEmpty()) {
            String[] endDateTime = mEndTimeString.split(" ");

            Calendar endCal = Calendar.getInstance();

            if (endDateTime.length == 1) {
                endCal.setTime(timeFormatter.parse(mEndTimeString));
            } else if (endDateTime.length == 2) {
                endCal.setTime(dateFormatter.parse(mEndTimeString));
            }

            mEndTime = endCal.getTimeInMillis();

            //Only if the temporalValue is strong do both values need to be prior to current time
            if (mStrong) {
                if (mEndTime > currentMilli) {
                    mEndTime -= 86400000;
                }
            }

        }

        if (mEndTime > 0) {
            mAbsolute = true;
        }

        return mAbsolute;
    }
}
