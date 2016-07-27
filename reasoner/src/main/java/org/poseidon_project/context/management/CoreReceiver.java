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

package org.poseidon_project.context.management;

import android.util.Log;

import org.poseidon_project.context.logging.DataLogger;

import java.util.Map;

import uk.ac.mdx.cs.ie.acontextlib.ContextReceiver;
import uk.ac.mdx.cs.ie.acontextlib.IContextManager;
import uk.ac.mdx.cs.ie.acontextlib.IReasonerManager;
import uk.ac.mdx.cs.ie.acontextlib.envir.weather.source.Precipitation;
import uk.ac.mdx.cs.ie.acontextlib.envir.weather.source.Temperature;
import uk.ac.mdx.cs.ie.acontextlib.envir.weather.source.Weather;
import uk.ac.mdx.cs.ie.acontextlib.envir.weather.source.WeatherPeriod;

/**
 * The Context Receiver to handle built in context observers.
 *
 * @author Dean Kramer <d.kramer@mdx.ac.uk>
 */
public class CoreReceiver extends ContextReceiver {

    private int mCounter = 1 ;
    private DataLogger mLogger;

    public CoreReceiver(IContextManager contextManager, IReasonerManager reasonerManager) {
        super(contextManager, reasonerManager);
    }

    public CoreReceiver(IContextManager contextManager, IReasonerManager reasonerManager,
                        DataLogger logger) {
        super(contextManager, reasonerManager);
        mLogger = logger;
    }

    @Override
    public void newContextValue(String name, long value) {

        String strValue = String.valueOf(value) + "^^http://www.w3.org/2001/XMLSchema#integer";

           if(name.equals("sensor.battery_level")) {
               Log.d("receiver", "battery Context value: " + String.valueOf(value));
               getReasonerManager().updateValues("system#device", "batteryRemaining", strValue);
           } else if (name.equals("sensor.light_lumens")) {
               Log.d("receiver", "Light Context value: " + String.valueOf(value));
               getReasonerManager().updateValues("system#device", "hasLightLevel", strValue);
           } else if (name.equals("NavState")) {
               if (mCounter > 999) {
                   mCounter = 1;
               }

               getReasonerManager().updateValues("user#pu" + mCounter, "user#hasNavigationStatus", strValue);
               mCounter++;
           } else if (name.equals("device.stepcounter")) {
               getReasonerManager().updateValues("user#pu", "user#hasStepped", strValue);
               Log.d("Steps", strValue);
           } else if (name.equals("device.distancetravelled")) {
               getReasonerManager().updateValues("user#pu", "user#hasMoved", strValue);
               Log.d("Moved", strValue);
           } else if (name.equals("CalEvent")) {
               mLogger.logVerbose(DataLogger.CONTEXT_MANAGER, "CalEvent : " + strValue);
           } else if (name.equals("CalReminder")) {
               mLogger.logVerbose(DataLogger.CONTEXT_MANAGER, "CalReminder : " + strValue);
           }
    }

    @Override
    public void newContextValue(String name, double value) {

    }

    @Override
    public void newContextValue(String name, boolean value) {

        String responseString = name;
        String valueString = String.valueOf(value);

        if (name.equals("sensor.gps_indoor_outdoor")) {
               responseString = "INDOOROUTDOOR";
               if (value) {
                   valueString = "ISOUTDOORS";
               } else {
                   valueString = "ISINDOORS";
               }

               getContextManager().updateContextValue(responseString, valueString);

        } else if (name.equals("NavEnd")) {
            mLogger.logVerbose(DataLogger.CONTEXT_MANAGER, "NavEnd : " + valueString);
        }
    }

    @Override
    public void newContextValue(String name, String value) {
        if (name.equals("NavStart")) {
            mLogger.logVerbose(DataLogger.CONTEXT_MANAGER, "NavStart : " + value);
        }
    }

    @Override
    public void newContextValue(String name, Object value) {

        if (name.equals("weather")) {
            Weather current = (Weather) value;

            WeatherPeriod period = current.getWeatherPeriods().get(0);

            if (period != null ){
                long time = System.currentTimeMillis();

                String iri = "http://ie.cs.mdx.ac.uk/POSEIDON/";

                //WeatherPeriod
                getReasonerManager().updateValues("envir#w1", "envir#hasTemperatureValue", iri + "envir#t1", time);
                getReasonerManager().updateValues("envir#w1", "envir#hasPrecipitationValue", iri + "envir#p1", time);

                //Temperature - First lets be sure it is in the units we need!
                Temperature temp = period.getTemperature();
                temp.setTemperatureUnit(Temperature.UNIT_C);
                getReasonerManager().updateValues("envir#t1", "envir#temperatureValue", temp.getCurrentValue() + "^^http://www.w3.org/2001/XMLSchema#integer", time);
                //ontologyManager.updateValues("envir#t1", "envir#temperatureUnit", iri + "envir#C", time);

                //Rain - First lets be sure it is in the units we need!
                Precipitation precip = period.getPrecipitation();
                precip.setPrecipitationUnit(Precipitation.UNIT_MM);
                getReasonerManager().updateValues("envir#p1", "envir#precipitationValue", precip.getValue() + "^^http://www.w3.org/2001/XMLSchema#float", time);
                //ontologyManager.updateValues("envir#p1", "envir#precipitationUnit", iri + "envir#MilliMeter", time);
                //ontologyManager.updateValues("envir#p1", "envir#precipitationHoursValue", precip.getHours() + "^^http://www.w3.org/2001/XMLSchema#integer", time);
                //Log.e("test", "temp= " + temp.getCurrentValue() + " precip= " + precip.getValue() + " hours= " + precip.getHours());
            }

        }
    }

    @Override
    public void newContextValues(Map<String, String> values) {

    }

    @Override
    public void newUIEvent(int event) {

    }
}
