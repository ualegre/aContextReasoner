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
package org.poseidon_project.contexts.envir.weather.source;

/**
 * This class holds information about temperature
 * @author Dean Kramer <d.kramer@mdx.ac.uk>
 *
 */
public class Temperature {

	private Unit mTemperatureUnit;
	private int mCurrentTemp = Integer.MIN_VALUE;
	private int mHighTemp = Integer.MIN_VALUE;
	private int mLowTemp = Integer.MIN_VALUE;


	public enum Unit {
		C, F, K;
	}

	public Temperature(Unit tempUnit) {
		mTemperatureUnit = tempUnit;
	}

	public Temperature(Unit tempUnit, int currentTemp) {
		mTemperatureUnit = tempUnit;
		mCurrentTemp =  currentTemp;
	}

	public Temperature(Unit tempUnit, int currentTemp, int highTemp, int lowTemp) {
		mTemperatureUnit = tempUnit;
		mCurrentTemp =  currentTemp;
		mHighTemp = highTemp;
		mLowTemp = lowTemp;
	}

	public Unit getTemperatureUnit() {
		return mTemperatureUnit;
	}

	public void setHighValue(int value) {
		mHighTemp = value;
	}

	public void setLowValue(int value) {
		mLowTemp = value;
	}

    public void setCurrentValue(int value) { mCurrentTemp = value; }

	public void setTemperatureUnit(Unit newUnit) {
		if (newUnit == mTemperatureUnit) {
			return;
		}

		if (mCurrentTemp != Integer.MIN_VALUE) {
			mCurrentTemp = convertTemperature(mCurrentTemp, mTemperatureUnit, newUnit);
		}

		if (mHighTemp != Integer.MIN_VALUE) {
			mHighTemp = convertTemperature(mHighTemp, mTemperatureUnit, newUnit);
		}

		if (mHighTemp != Integer.MIN_VALUE) {
			mCurrentTemp = convertTemperature(mCurrentTemp, mTemperatureUnit, newUnit);
		}

		mTemperatureUnit = newUnit;
	}

	public int getCurrentValue () {
		return mCurrentTemp;
	}

	public void setCurrentValue (int value, Unit tempUnit) {
		mCurrentTemp = convertTemperature(value, tempUnit, mTemperatureUnit);
	}

	public int getHighValue () {
		return mHighTemp;
	}

	public void setHighValue (int value, Unit tempUnit) {
		mHighTemp = convertTemperature(value, tempUnit, mTemperatureUnit);
	}

	public int getLowValue () {
		return mLowTemp;
	}

	public void setLowValue (int value, Unit tempUnit) {
		mLowTemp = convertTemperature(value, tempUnit, mTemperatureUnit);
	}

	public static int convertTemperature(int value, Unit currentTempUnit, Unit desiredTempUnit) {

		if ((currentTempUnit == null) | (desiredTempUnit == null)) {
			return Integer.MIN_VALUE;
		}

		switch (currentTempUnit) {
		case C:
			switch (desiredTempUnit) {
			case C:
				return value;
			case F:
				return Math.round(value * 9f / 5f + 32);
			case K:
				return Math.round(value + 273.15f);
			}

		case F:
			switch (desiredTempUnit) {
			case C:
				return Math.round((value - 32) * 5f / 9f);
			case F:
				return value;
			case K:
				return Math.round((value - 32) * 5f / 9f + 273.15f);
			}

		case K:
			switch (desiredTempUnit) {
			case C:
				return Math.round(value - 273.15f);
			case F:
				return Math.round((value - 273.15f) * 9f / 5f + 32);
			case K:
				return value;
			}
		}

		return value;
	}


}
