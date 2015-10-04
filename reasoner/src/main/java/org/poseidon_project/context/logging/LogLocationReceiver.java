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
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import org.poseidon_project.contexts.ContextReceiver;
import org.poseidon_project.contexts.UIEvent;
import org.poseidon_project.contexts.hardware.CurrentLocationContext;
import org.poseidon_project.contexts.hardware.PluggedInContext;

import java.util.Map;

/**
 * Deals with receiving location updates for the logger
 *
 * @author Dean Kramer <d.kramer@mdx.ac.uk>
 */
public class LogLocationReceiver {

    private PluggedInContext mPluggedInContext;
    private CurrentLocationContext mLocationContext;
    private long mLastUpdate = 0;
    private Location mCurrentLocation;
    private Thread mGetterThread;

    //Ten Minutes
    private static final int TIME_TO_REFRESH = 1000 * 60 * 10;

    public LogLocationReceiver(Context context) {
        mPluggedInContext = new PluggedInContext(context, mContextListener);
        mPluggedInContext.addRequiringApp("logger");
        mPluggedInContext.start();

        mLocationContext = new CurrentLocationContext(context, mContextListener, "passive");
        mLocationContext.addRequiringApp("logger");
    }

    public synchronized String getLocationString() {


        mCurrentLocation = mLocationContext.getLastKnownLocation("passive");
        mLastUpdate = mCurrentLocation.getTime();

        long timeSinceLast = System.currentTimeMillis() - mLastUpdate;


        if (timeSinceLast > TIME_TO_REFRESH) {
            Log.v("LocationReceiver", "Manual Refresh");
            mGetterThread = mLocationContext.getCurrentLocation();

            if (mGetterThread != null) {
                try {
                    mGetterThread.join();
                } catch (Exception e) {
                    Log.e("Log", e.getMessage().toString());
                }
            }
        }

            StringBuilder s = new StringBuilder();

            if (mCurrentLocation != null) {

                s.append(mCurrentLocation.getLatitude());
                s.append(",");
                s.append(mCurrentLocation.getLongitude());
            }

            return s.toString();
        }


    public boolean stop() {
        //mLocationContext.stop();
        return  mPluggedInContext.stop();
    }

    public void startListening() {

    }

    public void stopListening() {
        //mLocationContext.stop();
    }


    public ContextReceiver mContextListener = new ContextReceiver() {
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
                Log.v("LocationReceiver", "joined");
                mLocationContext.stop();
                Looper.myLooper().quit();
                return;
            }

        }

        @Override
        public void newContextValues(Map<String, String> values) {

        }

        @Override
        public void newUIEvent(UIEvent event) {

        }
    };

}
