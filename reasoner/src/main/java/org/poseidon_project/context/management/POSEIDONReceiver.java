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

import org.openrdf.query.algebra.Str;
import org.poseidon_project.contexts.ContextReceiver;
import org.poseidon_project.contexts.IContextManager;
import org.poseidon_project.contexts.IOntologyManager;
import org.poseidon_project.contexts.UIEvent;
import org.poseidon_project.contexts.envir.weather.source.Precipitation;
import org.poseidon_project.contexts.envir.weather.source.Temperature;
import org.poseidon_project.contexts.envir.weather.source.Weather;
import org.poseidon_project.contexts.envir.weather.source.WeatherPeriod;

import java.util.Map;

/**
 * The Context Receiver to handle built in POSEIDON contexts.
 *
 * @author Dean Kramer <d.kramer@mdx.ac.uk>
 */
public class POSEIDONReceiver extends ContextReceiver{

    private int mCounter = 1 ;

    public POSEIDONReceiver(IContextManager contextManager, IOntologyManager ontologyManager) {
        super(contextManager, ontologyManager);
    }

    @Override
    public void newContextValue(String name, long value) {

        String strValue = String.valueOf(value) + "^^http://www.w3.org/2001/XMLSchema#integer";

           if(name.equals("sensor.battery_level")) {
               Log.d("receiver", "battery Context value: " + String.valueOf(value));
               getOntologyManager().updateValues("system#device", "batteryRemaining", strValue);
           } else if (name.equals("sensor.light_lumens")) {
               Log.d("receiver", "Light Context value: " + String.valueOf(value));
               getOntologyManager().updateValues("system#device", "hasLightLevel", strValue);
           } else if (name.equals("NavState")) {
               if (mCounter > 999) {
                   mCounter = 1;
               }

               getOntologyManager().updateValues("user#pu" + mCounter, "user#hasNavigationStatus", strValue);
               mCounter++;
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
               responseString = "INDOOR/OUTDOOR";
               if (value) {
                   valueString = "ISOUTDOORS";
               } else {
                   valueString = "ISINDOORS";
               }
           }

           getContextManager().sendContextUpdate(responseString, valueString);
    }

    @Override
    public void newContextValue(String name, String value) {

    }

    @Override
    public void newContextValue(String name, Object value) {

        IOntologyManager ontologyManager = getOntologyManager();


        if (name.equals("weather")) {
            Weather current = (Weather) value;

            WeatherPeriod period = current.getWeatherPeriods().get(0);

            if (period != null ){
                long time = System.currentTimeMillis();

                String iri = "http://ie.cs.mdx.ac.uk/POSEIDON/";

                //WeatherPeriod
                ontologyManager.updateValues("envir#w1", "envir#hasTemperatureValue", iri + "envir#t1", time);
                ontologyManager.updateValues("envir#w1", "envir#hasPrecipitationValue", iri + "envir#p1", time);

                //Temperature - First lets be sure it is in the units we need!
                Temperature temp = period.getTemperature();
                temp.setTemperatureUnit(Temperature.Unit.C);
                ontologyManager.updateValues("envir#t1", "envir#temperatureValue", temp.getCurrentValue() + "^^http://www.w3.org/2001/XMLSchema#integer", time);
                //ontologyManager.updateValues("envir#t1", "envir#temperatureUnit", iri + "envir#C", time);

                //Rain - First lets be sure it is in the units we need!
                Precipitation precip = period.getPrecipitation();
                precip.setPrecipitationUnit(Precipitation.Unit.MM);
                ontologyManager.updateValues("envir#p1", "envir#precipitationValue", precip.getValue() + "^^http://www.w3.org/2001/XMLSchema#float", time);
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
    public void newUIEvent(UIEvent event) {

    }
}
