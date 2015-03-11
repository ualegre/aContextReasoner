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
package org.poseidon_project.contexts;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/**
 * Abstract class to hold everything required by sensor based context components
 * Interval time is by default SensorManager.SENSOR_DELAY_NORMAL
 * @author Dean Kramer <d.kramer@mdx.ac.uk>
 *
 */
public abstract class SensorContext extends ContextObserver implements SensorEventListener{

	private SensorManager mSensorManager;
	private int mSensorType = -2;
	private Sensor mSensor;
	private int mInterval = SensorManager.SENSOR_DELAY_NORMAL;

	public SensorContext(Context c) {
		super(c);
		mSensorManager = (SensorManager) c
				.getSystemService(Context.SENSOR_SERVICE);
	}

	public SensorContext(Context c, ContextReceiver cr) {
		super(c, cr);
		mSensorManager = (SensorManager) c
				.getSystemService(Context.SENSOR_SERVICE);
	}

	public SensorContext(Context c, ContextReceiver cr, int sensorType, int interval, String name) {
		super(c, cr, name);
		mSensorType = sensorType;
		mInterval = interval;
		mSensorManager = (SensorManager) c
				.getSystemService(Context.SENSOR_SERVICE);
		mSensor = mSensorManager.getDefaultSensor(sensorType);
	}

	public void setInterval(int interval) {
		mInterval = interval;
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == mSensorType) {
			checkContext(event.values);
		}
	}

	protected abstract void checkContext(float[] values);

	@Override
	public boolean resume() {
		return start();
	}

	@Override
	public boolean pause() {
		return stop();
	}

	@Override
	public boolean start() {

        if (mSensorType == -2) {
            mSensorManager.registerListener(this, mSensor, mInterval);
            return true;
        } else {
            Log.e(mName, "Sensor Type not set!");
            return false;
        }

	}

    public void setSensorType(int sensorType) {
        mSensorType = sensorType;
    }

	@Override
	public boolean stop() {
		mSensorManager.unregisterListener(this, mSensor);
		return true;
	}

}
