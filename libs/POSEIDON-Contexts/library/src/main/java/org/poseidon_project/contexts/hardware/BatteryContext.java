/*Copyright 2014 POSEIDON Project

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package org.poseidon_project.contexts.hardware;

import org.poseidon_project.contexts.BroadcastContext;
import org.poseidon_project.contexts.ContextReceiver;

import android.content.Context;
import android.os.BatteryManager;
import android.os.Bundle;


/**
 * Gets Battery Level
 * @author Dean Kramer <d.kramer@mdx.ac.uk>
 *
 */
public class BatteryContext extends BroadcastContext {

	private int mBatteryLevel;


	public BatteryContext(Context c, ContextReceiver cr) {
		super(c, cr, "Intent.Action_BATTERY_CHANGED", "BatteryContext");
	}


	@Override
	protected void checkContext(Bundle data) {
		int rawlevel = data.getInt(BatteryManager.EXTRA_LEVEL, -1);
		int scale = data.getInt(BatteryManager.EXTRA_SCALE, -1);

		if (rawlevel >= 0 && scale > 0) {
			mBatteryLevel = (rawlevel * 100) / scale;

			//Send the receiver the context update
			mReceiver.newContextValue("sensor.battery_level", mBatteryLevel);
		}
	}

	public int getBatteryLevel() {
		return mBatteryLevel;
	}

}
