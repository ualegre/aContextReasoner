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
import android.os.BatteryManager;
import android.os.Bundle;

import org.poseidon_project.contexts.BroadcastContext;
import org.poseidon_project.contexts.ContextReceiver;

/**
 * Monitors whether the device is plugged in
 *
 * @author Dean Kramer <d.kramer@mdx.ac.uk>
 */
public class PluggedInContext extends BroadcastContext {

    private boolean mPluggedIn = false;


    public PluggedInContext(Context c, ContextReceiver cr) {
        super(c, cr, "Intent.Action_BATTERY_CHANGED", "PluggedInContext");
    }


    @Override
    protected void checkContext(Bundle data) {
        int status = data.getInt(BatteryManager.EXTRA_STATUS, -1);
        boolean batteryCharge = status==BatteryManager.BATTERY_STATUS_CHARGING;

        int chargePlug = data.getInt(BatteryManager.EXTRA_PLUGGED, -1);
        boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
        boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;

        if (batteryCharge) {
            mPluggedIn = true;
        } else if (usbCharge) {
            mPluggedIn = true;
        } else if (acCharge) {
            mPluggedIn = true;
        } else {
            mPluggedIn = false;
        }

        //Send the receiver the context update
        mReceiver.newContextValue("sensor.plugged_in", mPluggedIn);
    }

    public boolean isPluggedIn() {
        return mPluggedIn;
    }
}
