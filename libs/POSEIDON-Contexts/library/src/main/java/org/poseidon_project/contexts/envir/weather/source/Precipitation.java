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
 * This class holds information about precipitation
 * @author Dean Kramer <d.kramer@mdx.ac.uk>
 *
 */
public class Precipitation {

	private int mPeriodHours = 0;
	private Unit mPrecipitationUnit;
	private float mPrecipitationValue = 0;

	public enum Unit {
		MM, IN;
	}


	public Precipitation(Unit unit) {
		mPrecipitationUnit = unit;
	}

	public int getHours() {
		return mPeriodHours;
	}

	public void setHours(int hours) {
		mPeriodHours = hours;
	}

	public void setValue(float value) {
		mPrecipitationValue = value;
	}

	public void setValue(float value, Unit valueUnit) {
		mPrecipitationValue = convertPrecipitation(value, valueUnit, mPrecipitationUnit);
	}

	public float getValue() {
		return mPrecipitationValue;
	}

	public static float convertPrecipitation(float value, Unit currentPrecipUnit, Unit desiredPrecipUnit) {

		if (value == 0) {
			return value;
		}

		switch(currentPrecipUnit) {
		case MM:
			switch (desiredPrecipUnit) {
			case MM :
				return value;
			case IN :
				return (float) (value * 0.03937);
			}

		case IN:
			switch (desiredPrecipUnit) {
			case MM :
				return (float) (value * 25.4);
			case IN :
				return value;
			}
		}

		return value;
	}

}
