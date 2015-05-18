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

package org.poseidon_project.contexts.hardware;

import android.content.Context;
import android.location.Location;

import org.poseidon_project.contexts.ContextReceiver;
import org.poseidon_project.contexts.LocationContext;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Calculates distanced travelled continously
 *
 * @author Dean Kramer <d.kramer@mdx.ac.uk>
 */
public class DistanceTravelledContext extends LocationContext {

    private Timer mTimer;
    private int mInteval = 30000;
    private Location mLastLocation = null;
    private float mTotalDistance;

    public DistanceTravelledContext(Context c, ContextReceiver cr) {
        super(c, cr, 3000, 0, "DistanceTravelledContext");
    }

    @Override
    protected void checkContext(Location newLocation) {

        float distance = mLastLocation.distanceTo(newLocation);
        mTotalDistance += distance;

    }

    @Override
    public boolean start() {

        mTimer = new Timer();

        mTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                sendUpdate();
            }
        }, mInteval, mInteval);

        return super.start();
    }

    private void sendUpdate() {
        long distance = Math.round(mTotalDistance);
        mReceiver.newContextValue("device.distancetravelled", distance);
    }

    @Override
    public boolean stop() {
        mTimer.cancel();
        return super.stop();
    }
}
