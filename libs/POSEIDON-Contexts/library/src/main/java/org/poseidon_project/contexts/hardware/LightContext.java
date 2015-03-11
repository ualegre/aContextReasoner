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

import org.poseidon_project.contexts.ContextReceiver;
import org.poseidon_project.contexts.SensorContext;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;

/**
 * Gets the level of light lumens
 * @author Dean Kramer <d.kramer@mdx.ac.uk>
 *
 */
public class LightContext extends SensorContext{

	private long mCurrentValue;
	private long mContextDifference = 300;

	public LightContext(Context c, ContextReceiver cr) {
		super(c, cr, Sensor.TYPE_LIGHT, SensorManager.SENSOR_DELAY_NORMAL, "LightContext");

	}

	public LightContext(Context c, ContextReceiver cr, long difference) {
		super(c, cr, Sensor.TYPE_LIGHT, SensorManager.SENSOR_DELAY_NORMAL, "LightContext");
		mContextDifference = difference;
	}

	public void setContextDifference(long difference) {
		mContextDifference = difference;
	}

	@Override
	protected void checkContext(float[] values) {
		long value = Math.round(values[0]);

		long difference = Math.abs(mCurrentValue-value);

		if (difference >=mContextDifference) {
			mCurrentValue = value;
			mReceiver.newContextValue("sensor.light_lumens", mCurrentValue);
		}

	}



}
