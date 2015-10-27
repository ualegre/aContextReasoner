/*
 * Copyright 2014 POSEIDON Project
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
 * /
 */

package org.poseidon_project.contexts.envir.weather;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import org.poseidon_project.contexts.ContextException;
import org.poseidon_project.contexts.ContextReceiver;
import org.poseidon_project.contexts.TimerContext;
import org.poseidon_project.contexts.envir.weather.openweathermap.OpenWeatherMapSource;
import org.poseidon_project.contexts.envir.weather.source.Temperature;
import org.poseidon_project.contexts.envir.weather.source.Weather;
import org.poseidon_project.contexts.envir.weather.source.WeatherPeriod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Class Description
 *
 * @author Dean Kramer <d.kramer@mdx.ac.uk>
 */
public class BadWeatherContext extends TimerContext{

    private OpenWeatherMapSource mWeatherSource;
    private List<Weather> mCurrentWeather = new ArrayList<Weather>();
    private List<String> mStringPlaces;
    private List<Location> mLocationPlaces;
    private boolean mIsStrings;

    public BadWeatherContext(Context c, ContextReceiver cr) {
        super(c, cr, 5000, "BadWeatherContext");
        mWeatherSource = new OpenWeatherMapSource();
    }

    @Override
    public boolean setContextParameters(HashMap<String, Object> parameters) {
        if (super.setContextParameters(parameters)) {
            List<String> stringPlaces = (List<String>) parameters.get("stringPlaces");
            List<Location> locationPlaces = (List<Location>) parameters.get("locationPlaces");

            if ( stringPlaces != null && locationPlaces == null) {
                setPlace(stringPlaces, true);
                return true;
            } else if (stringPlaces == null && locationPlaces != null ) {
                setPlace(locationPlaces, false);
                return true;
            } else {
                Log.e(mName, "You shouldn't send both!");
                return false;
            }

        } else {
            return false;
        }
    }

    public BadWeatherContext(Context c, ContextReceiver cr, List places, boolean strings) {
        super(c, cr, 5000, "BadWeatherContext");
        if (strings) {
            mStringPlaces = (List<String>) places;
        } else {
            mLocationPlaces = (List<Location>) places;
        }

        mIsStrings = strings;
        mWeatherSource = new OpenWeatherMapSource();
    }

    @Override
    public void checkContext() {
        try {
            boolean rainExpected = false;
            boolean coldExpected = false;
            if (mIsStrings) {
                for (String place : mStringPlaces) {
                    mCurrentWeather.add(mWeatherSource.query(place, 0));
                }
            } else {
                for (Location place : mLocationPlaces) {
                    mCurrentWeather.add(mWeatherSource.query(place, 0));
                }
            }

            for (Weather curWeather : mCurrentWeather) {

                for (WeatherPeriod condition : curWeather.getWeatherPeriods()) {
                    if (condition.getPrecipitation().getValue() > 0 ) {
                        rainExpected = true;
                    }

                    Temperature temp = condition.getTemperature();
                    int unit = temp.getTemperatureUnit();
                    int tempUnit = temp.getHighValue();
                    int tempC = Temperature.convertTemperature(tempUnit, unit, Temperature.UNIT_C);

                    if (tempC <= 20) {
                        coldExpected = true;
                    }

                }

            }


            if ((coldExpected == true) && (rainExpected == false)) {
                mReceiver.newContextValue("envir.weather", "COLD");
            } else if ((coldExpected == false) && (rainExpected == true)) {
                mReceiver.newContextValue("envir.weather", "RAINING");
            } else {
                mReceiver.newContextValue("envir.weather", "RAININGANDCOLD");
            }


        } catch (ContextException e) {
            Log.e("LocationWeatherContext", e.toString());
        }

    }

    public List getPlaces() {

        if (mIsStrings) {
            return mStringPlaces;
        } else {
            return mLocationPlaces;
        }
    }

    public void setPlace(List places, boolean isStrings) {

        if (isStrings) {
            mStringPlaces = places;
            if (mLocationPlaces!=null) {
                mLocationPlaces = null;
            }
        } else {
            mLocationPlaces = places;
            if (mStringPlaces!=null) {
                mStringPlaces = null;
            }
        }

        mIsStrings = isStrings;
    }

}
