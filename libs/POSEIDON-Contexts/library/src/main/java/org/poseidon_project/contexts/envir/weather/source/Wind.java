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
package org.poseidon_project.contexts.envir.weather.source;

/**
 * This class holds information about wind
 * @author Dean Kramer <d.kramer@mdx.ac.uk>
 *
 */
public class Wind {

	private SpeedUnit mWindSpeedUnit;
	private Direction mWindDirection;
	private int mWindSpeed;
	private String mWindDesc;

	public enum Direction {
		N, NE, E, SE, S, SW, W, NW;
	}

	public enum SpeedUnit {
		KPH, MPH, MPS, KN, FPS;
	}

	public Wind (SpeedUnit sunit) {
		mWindSpeedUnit = sunit;
	}

	public Wind (Direction dir, SpeedUnit sunit) {
		mWindDirection = dir;
		mWindSpeedUnit = sunit;
	}

	public Wind (Direction dir, SpeedUnit sunit, int speed) {
		mWindDirection = dir;
		mWindSpeedUnit = sunit;
		mWindSpeed = convertSpeed(speed, sunit, sunit);
	}

	public Wind (int direction, SpeedUnit sunit, int speed) {
		mWindDirection =  getDirection(direction);
		mWindSpeedUnit = sunit;
		mWindSpeed = convertSpeed(speed, sunit, sunit);
	}

	public int getSpeed() {
		return mWindSpeed;
	}

	public SpeedUnit getSpeedUnit () {
		return mWindSpeedUnit;
	}

	public void setSpeed(int speed) {
		mWindSpeed = speed;
	}

	public void setSpeed(int speed, SpeedUnit speedunit){

		if (mWindSpeedUnit==speedunit) {
			mWindSpeed = speed;
		} else {
			mWindSpeed = convertSpeed(speed, speedunit, mWindSpeedUnit);
		}
	}

	public void setDirection(Direction dir) {
		mWindDirection = dir;
	}

	public void setDirection(int degrees) {
		mWindDirection = getDirection(degrees);
	}

	public Direction getDirection() {
		return mWindDirection;
	}

	public void setDescription(String desc) {
		mWindDesc = desc;
	}

	public String getDescription() {
		return mWindDesc;
	}


	public static Direction getDirection(int degrees) {

		int degreesPositive = degrees;
        if (degrees < 0) {
            degreesPositive += (-degrees / 360 + 1) * 360;
        }
        int degreesNormalized = degreesPositive % 360;
        int degreesRotated = degreesNormalized + (360 / 16 / 2);
        int zone = degreesRotated / (360 / 8);

		switch (zone) {
			case 0: return Direction.N;
			case 1: return Direction.NE;
			case 2: return Direction.E;
			case 3: return Direction.SE;
			case 4: return Direction.S;
			case 5: return Direction.SW;
			case 6: return Direction.W;
			case 7: return Direction.NW;
		}

		return Direction.N;

	}

	public static int convertSpeed(int speed, SpeedUnit currentSpeedUnit, SpeedUnit desiredSpeedUnit) {

		if((currentSpeedUnit == null) || (desiredSpeedUnit == null)) {
			return Integer.MIN_VALUE;
		}

		switch (currentSpeedUnit) {

		case KPH:
			switch (desiredSpeedUnit) {
			case KPH:
				return speed;
			case MPH:
				return (int)Math.round(speed * 0.62137);
			case MPS:
				return (int)Math.round(speed * 0.27777);
			case KN:
				return (int)Math.round(speed * 0.53995);
			case FPS:
				return (int)Math.round(speed * 0.91134);
			}

		case MPH:
			switch (desiredSpeedUnit) {
			case KPH:
				return (int)Math.round(speed * 1.60934);
			case MPH:
				return speed;
			case MPS:
				return (int)Math.round(speed * 0.44704);
			case KN:
				return (int)Math.round(speed * 0.86897);
			case FPS:
				return (int)Math.round(speed * 1.46666);
			}

		case MPS:
			switch (desiredSpeedUnit) {
			case KPH:
				return (int)Math.round(speed * 3.60000);
			case MPH:
				return (int)Math.round(speed * 2.23693);
			case MPS:
				return speed;
			case KN:
				return (int)Math.round(speed * 1.94384);
			case FPS:
				return (int)Math.round(speed * 3.28084);
			}

		case KN:
			switch (desiredSpeedUnit) {
			case KPH:
				return (int)Math.round(speed * 1.85200);
			case MPH:
				return (int)Math.round(speed * 1.15077);
			case MPS:
				return (int)Math.round(speed * 0.51444);
			case KN:
				return speed;
			case FPS:
				return (int)Math.round(speed * 1.68781);
			}

		case FPS:
			switch (desiredSpeedUnit) {
			case KPH:
				return (int)Math.round(speed * 1.09728);
			case MPH:
				return (int)Math.round(speed * 1.09728);
			case MPS:
				return (int)Math.round(speed * 0.30480);
			case KN:
				return (int)Math.round(speed * 0.59248);
			case FPS:
				return speed;
			}
		}

		return Integer.MIN_VALUE;
	}
}
