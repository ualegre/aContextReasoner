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

package uk.co.deansserver.acontextreasoner.logging;

import android.content.Context;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import java.text.DecimalFormat;
import java.util.Map;

import uk.ac.mdx.cs.ie.acontextlib.IContextReceiver;
import uk.ac.mdx.cs.ie.acontextlib.hardware.CurrentLocationObserver;
import uk.ac.mdx.cs.ie.acontextlib.hardware.PluggedInObserver;

/**
 * Deals with receiving location updates for the logger
 *
 * @author Dean Kramer <deankramer99@gmail.com>
 */
public class LogLocationReceiver {

    private PluggedInObserver mPluggedInContext;
    private CurrentLocationObserver mLocationContext;
    private long mLastUpdate = 0;
    private Location mCurrentLocation;
    private Thread mGetterThread;
    private static final String LOG_TAG = "LocationReceiver";

    //To 5 decimal places is accurate enough
    private DecimalFormat mFormatter = new DecimalFormat("###.#####");

    //Ten Minutes
    private static final int TIME_TO_REFRESH = 1000 * 60 * 10;

    public LogLocationReceiver(Context context) {
        mPluggedInContext = new PluggedInObserver(context);
        mPluggedInContext.addContextReceiver(mContextListener);
        mPluggedInContext.addRequiringApp("logger");

        mLocationContext = new CurrentLocationObserver(context, "passive");
        mLocationContext.addContextReceiver(mContextListener);
        mLocationContext.addRequiringApp("logger");

        mPluggedInContext.start();
    }

    public synchronized String getLocationString() {

        mCurrentLocation = mLocationContext.getLastKnownLocation("passive");

        if (mCurrentLocation != null) {

            mLastUpdate = mCurrentLocation.getTime();

            long timeSinceLast = System.currentTimeMillis() - mLastUpdate;


            if (timeSinceLast > TIME_TO_REFRESH) {
                manualRefresh();
            }

        } else {
            manualRefresh();
        }

        StringBuilder s = new StringBuilder();

        if (mCurrentLocation != null) {
            s.append(mFormatter.format(mCurrentLocation.getLatitude()));
            s.append(",");
            s.append(mFormatter.format(mCurrentLocation.getLongitude()));
        } else {
            s.append("unknown");
            Log.e(LOG_TAG, "Unknown Location");
        }

        return s.toString();

    }

    private void manualRefresh() {

        Log.v(LOG_TAG, "Manual Refresh");
        mGetterThread = mLocationContext.getCurrentLocation();

        if (mGetterThread != null) {
            try {
                mGetterThread.join();
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage().toString());
            }
        }

    }

    public boolean stop() {
        return  mPluggedInContext.stop();
    }

    public IContextReceiver mContextListener = new IContextReceiver() {
        @Override
        public void newContextValue(String name, long value) {

        }

        @Override
        public void newContextValue(String name, double value) {

        }

        @Override
        public void newContextValue(String name, boolean value) {
                if (value) {
                    mLocationContext.changeProvider(false);
                } else {
                    mLocationContext.changeProvider(true);
                }
        }

        @Override
        public void newContextValue(String name, String value) {

        }

        @Override
        public void newContextValue(String name, Object value) {

            mCurrentLocation = (Location) value;
            mLastUpdate = mCurrentLocation.getTime();

            if (Thread.currentThread() == mGetterThread) {
                mLocationContext.stop();
                Looper.myLooper().quit();
                return;
            }

        }

        @Override
        public void newContextValues(Map<String, String> values) {

        }


    };

}
