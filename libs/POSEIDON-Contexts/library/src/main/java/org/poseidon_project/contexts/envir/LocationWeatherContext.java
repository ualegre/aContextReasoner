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

import org.poseidon_project.contexts.ContextException;
import org.poseidon_project.contexts.ContextReceiver;
import org.poseidon_project.contexts.TimerContext;
import org.poseidon_project.contexts.envir.weather.openweathermap.OpenWeatherMapSource;
import org.poseidon_project.contexts.envir.weather.source.Weather;

import android.content.Context;
import android.util.Log;

/**
 * Monitors Weather Data for specific locations
 * @author Dean Kramer <d.kramer@mdx.ac.uk>
 *
 */
public class LocationWeatherContext extends TimerContext{

	private OpenWeatherMapSource mWeatherSource;
	private Weather mCurrentWeather;
	private String mPlace;

	public LocationWeatherContext(Context c, ContextReceiver cr) {
		super(c, cr, 5000, "LocationWeatherContext");
		mWeatherSource = new OpenWeatherMapSource();
	}

	public LocationWeatherContext(Context c, ContextReceiver cr, String place) {
		super(c, cr, 5000, "LocationWeatherContext");
		mPlace = place;
		mWeatherSource = new OpenWeatherMapSource();
	}

	@Override
	protected void checkContext() {
		try {
			mCurrentWeather = mWeatherSource.query(mPlace,0);
			mReceiver.newContextValue("weather", mCurrentWeather);
		} catch (ContextException e) {
			Log.e("LocationWeatherContext", e.toString());
		}

	}

	public String getPlace() {
		return mPlace;
	}

	public void setPlace(String place) {
		mPlace = place;
	}

}
