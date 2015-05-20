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
package org.poseidon_project.contexts.envir;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import org.poseidon_project.contexts.ContextException;
import org.poseidon_project.contexts.ContextReceiver;
import org.poseidon_project.contexts.TimerContext;
import org.poseidon_project.contexts.envir.weather.openweathermap.OpenWeatherMapSource;
import org.poseidon_project.contexts.envir.weather.source.Weather;

import java.util.HashMap;

/**
 * Monitors Weather Data for specific locations
 * @author Dean Kramer <d.kramer@mdx.ac.uk>
 *
 */
public class LocationWeatherContext extends TimerContext{

	private OpenWeatherMapSource mWeatherSource;
	private Weather mCurrentWeather;
	private String mPlace;
	private Location mLocationPlace;
	private boolean mFirstTime = true;
	private boolean mIsStrings = true;

	public LocationWeatherContext(Context c, ContextReceiver cr) {
		super(c, cr, 2000, "LocationWeatherContext");
		mWeatherSource = new OpenWeatherMapSource();
	}

	public LocationWeatherContext(Context c, ContextReceiver cr, String place) {
		super(c, cr, 2000, "LocationWeatherContext");
		mPlace = place;
		mWeatherSource = new OpenWeatherMapSource();
	}

	public LocationWeatherContext(Context c, ContextReceiver cr, Location place) {
		super(c, cr, 2000, "LocationWeatherContext");
		mLocationPlace = place;
		mIsStrings = false;
		mWeatherSource = new OpenWeatherMapSource();
	}

    @Override
    public boolean setContextParameters(HashMap<String, Object> parameters) {
        if (super.setContextParameters(parameters)) {
            String stringPlace = (String) parameters.get("place");
			Location locationPlace = (Location) parameters.get("location");
			if ( stringPlace != null && locationPlace == null ) {
				setPlace(stringPlace);
				Log.e(mName, "setting place para with: " + stringPlace);
				return true;
			} else if ( stringPlace == null && locationPlace != null) {
				setPlace(locationPlace);
				Log.e(mName, "setting location para with: " + locationPlace.toString());
				return true;
			} else {
				Log.e(mName, "You shouldn't send both Location and String places!");
				return false;
			}
        } else {
            return false;
        }

    }

    @Override
	protected void checkContext() {
		try {

			if (mIsStrings) {
				mCurrentWeather = mWeatherSource.query(mPlace,0);
			} else {
				mCurrentWeather = mWeatherSource.query(mLocationPlace, 0);
			}

			mReceiver.newContextValue("weather", mCurrentWeather);

			if (mFirstTime) {
				mFirstTime = false;
				//Check every 30 mins from now.
				setInterval(1800000);
			}
		} catch (ContextException e) {
			Log.e(mName, e.toString());
		}

	}

    @Override
    public boolean start() {
        if (mPlace == null && mLocationPlace == null) {
            Log.e(mName, "No location/place to monitor!");
            return false;
        } else {
            return super.start();
        }
    }

	public String getPlace() {
		return mPlace;
	}

	public void setPlace(String place) {
		mPlace = place;
		mLocationPlace = null;
		mIsStrings = true;
	}

	public void setPlace(Location place) {
		mLocationPlace = place;
		mPlace = null;
		mIsStrings = false;
	}

}
