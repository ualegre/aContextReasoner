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

package org.poseidon_project.context.reasoner;

import android.os.storage.OnObbStateChangeListener;
import android.util.Log;

import org.poseidon_project.context.ContextReasonerCore;
import org.poseidon_project.context.management.ContextManager;

import java.util.HashMap;
import java.util.Map;

import eu.larkc.csparql.core.engine.CsparqlQueryResultProxy;

/**
 * Temporary class for mapping contexts to components and rules for pilot 1
 *
 * @author Dean Kramer <d.kramer@mdx.ac.uk>
 */
public class ContextMapper {

    private ContextReasonerCore mReasonerCore;
    private ContextManager mContextManager;
    private OntologyManager mOntologyManager;
    private HashMap<String, CsparqlQueryResultProxy> rules = new HashMap<>();
    private static final String LOGTAG = "ContextMapper";

    private static final String batteryLOWQuery =
            "REGISTER STREAM batteryContextIsLOW AS "
                    + "PREFIX ex: <http://ie.cs.mdx.ac.uk/POSEIDON/system#> "
                    + "CONSTRUCT { ?s <http://poseidon-project.org/context/is> \"BATTERY_OKAY\"} "
                    + "FROM STREAM <http://poseidon-project.org/context-stream> [RANGE TRIPLES 1] "
                    + "WHERE { ?s ex:batteryRemaining ?o "
                    + "FILTER ( ?o < 25) }";

    private static final String batteryOkQuery =
            "REGISTER STREAM batteryContextIsOkay AS "
            + "PREFIX ex: <http://ie.cs.mdx.ac.uk/POSEIDON/system#> "
            + "CONSTRUCT { ?s <http://poseidon-project.org/context/is> \"BATTERY_OKAY\"} "
            + "FROM STREAM <http://poseidon-project.org/context-stream> [RANGE TRIPLES 1] "
            + "WHERE { ?s ex:batteryRemaining ?o "
            + "FILTER ( ?o > 25) }";

    private static final String lightingLowQuery =
            "REGISTER QUERY batteryContextIsLow AS "
                    + "PREFIX ex: <http://ie.cs.mdx.ac.uk/POSEIDON/system#> "
                    + "CONSTRUCT {?s <http://ie.cs.mdx.ac.uk/POSEIDON/context/is> \"LIGHT_LOW\"} "
                    + "FROM STREAM <http://poseidon-project.org/context-stream> [RANGE TRIPLES 1] "
                    + " WHERE { ?s ex:hasLightLevel ?o "
                    + " FILTER (?o < 20) }";

    private static final String weatherRainingAndColdQuery =
            "REGISTER QUERY weatherContextIsRainingAndCold AS "
                    + "PREFIX ex: <http://ie.cs.mdx.ac.uk/POSEIDON/envir#> "
                    + "CONSTRUCT { ex:weather <http://ie.cs.mdx.ac.uk/POSEIDON/context/is> \"WEATHER_RAININGANDCOLD\" } "
                    + "FROM STREAM <http://poseidon-project.org/context-stream> [RANGE 10s STEP 4s] "
                    + "WHERE { ?m ex:hasTemperatureValue ?tempValueIRI . "
                    + "?m ex:hasPrecipitationValue ?precipValueIRI . "
                    + "?precipValueIRI ex:precipitationValue ?precipValue . "
                    + "?tempValueIRI ex:temperatureValue ?tempValue . "
                    + "FILTER (?precipValue > 0) "
                    + "FILTER (?tempValue < 15) "
                    + "}";

    private static final String weatherRainingQuery =
            "REGISTER QUERY weatherContextIsRaining AS "
                    + "PREFIX ex: <http://ie.cs.mdx.ac.uk/POSEIDON/envir#> "
                    + "CONSTRUCT { ex:weather <http://ie.cs.mdx.ac.uk/POSEIDON/context/is> \"WEATHER_RAINING\" } "
                    + "FROM STREAM <http://poseidon-project.org/context-stream> [RANGE 10s STEP 4s] "
                    + "WHERE { ?m ex:hasTemperatureValue ?tempValueIRI . "
                    + "?m ex:hasPrecipitationValue ?precipValueIRI . "
                    + "?precipValueIRI ex:precipitationValue ?precipValue . "
                    + "?tempValueIRI ex:temperatureValue ?tempValue . "
                    + "FILTER (?precipValue > 0) "
                    + "FILTER (?tempValue > 15) "
                    + "}";

    private static final String weatherColdQuery =
            "REGISTER QUERY weatherContextIsCold AS "
                    + "PREFIX ex: <http://ie.cs.mdx.ac.uk/POSEIDON/envir#> "
                    + "CONSTRUCT { ex:weather <http://ie.cs.mdx.ac.uk/POSEIDON/context/is> \"WEATHER_COLD\" } "
                    + "FROM STREAM <http://poseidon-project.org/context-stream> [RANGE 10s STEP 4s] "
                    + "WHERE { ?m ex:hasTemperatureValue ?tempValueIRI . "
                    + "?m ex:hasPrecipitationValue ?precipValueIRI . "
                    + "?precipValueIRI ex:precipitationValue ?precipValue . "
                    + "?tempValueIRI ex:temperatureValue ?tempValue . "
                    + " FILTER (?precipValue < 0.1) "
                    + " FILTER (?tempValue < 15) "
                    + "}";

    private static final String weatherHotQuery =
            "REGISTER QUERY weatherContextIsCold AS "
                    + "PREFIX ex: <http://ie.cs.mdx.ac.uk/POSEIDON/envir#> "
                    + "CONSTRUCT { ex:weather <http://ie.cs.mdx.ac.uk/POSEIDON/context/is> \"WEATHER_COLD\" } "
                    + "FROM STREAM <http://poseidon-project.org/context-stream> [RANGE 10s STEP 4s] "
                    + "WHERE { ?m ex:hasTemperatureValue ?tempValueIRI . "
                    + "?m ex:hasPrecipitationValue ?precipValueIRI . "
                    + "?precipValueIRI ex:precipitationValue ?precipValue . "
                    + "?tempValueIRI ex:temperatureValue ?tempValue . "
                    + " FILTER (?precipValue < 0.1) "
                    + " FILTER (?tempValue > 25) "
                    + "}";

    public ContextMapper(ContextReasonerCore crc, OntologyManager on) {

        mReasonerCore = crc;
        mContextManager = crc.getContextManager();
        mOntologyManager = on;

    }


    public boolean registerContext(String context, Map parameters) {

        if (mContextManager==null) {
            mContextManager = mReasonerCore.getContextManager();
        }

        if (context.equals("battery")) {
            return registerBatteryContext();
        } else if (context.equals("light")) {
            return registerLightContext();
        } else if (context.equals("weather")) {
            return registerWeatherContext(parameters);
        } else if (context.equals("indoor/outdoor")) {
            return registerIndoorOutdoorsContext();
        }

        Log.e(LOGTAG, "Context: " + context + " not found!");

        return false;
    }

    public boolean unregisterContext(String context, Map parameters) {

        if (context.equals("battery")) {
            return unRegisterBatteryContext();
        } else if (context.equals("light")) {
            return unRegisterLightContext();
        } else if (context.equals("weather")) {
            return unRegisterWeatherContext();
        } else if (context.equals("indoor/outdoor")) {
            return unRegisterIndoorOutdoorsContext();
        }

        Log.e(LOGTAG, "Context: " + context + " not found!");

        return false;
    }

    private boolean registerIndoorOutdoorsContext() {
        mContextManager.addObserverRequirement("engine", "GPSIndoorOutdoorContext");
        return true;
    }

    private boolean unRegisterIndoorOutdoorsContext() {
        mContextManager.removeObserverRequirement("engine", "GPSIndoorOutdoorContext");
        return true;
    }

    private boolean registerWeatherContext(Map parameters) {
        CsparqlQueryResultProxy c1 = mOntologyManager.registerCSPARQLQuery(weatherRainingAndColdQuery);
        CsparqlQueryResultProxy c2 = mOntologyManager.registerCSPARQLQuery(weatherColdQuery);
        CsparqlQueryResultProxy c3 = mOntologyManager.registerCSPARQLQuery(weatherRainingQuery);
        CsparqlQueryResultProxy c4 = mOntologyManager.registerCSPARQLQuery(weatherHotQuery);
        rules.put(weatherRainingAndColdQuery, c1);
        rules.put(weatherColdQuery, c2);
        rules.put(weatherRainingQuery, c3);
        rules.put(weatherHotQuery, c4);
        mContextManager.addObserverRequirementWithParameters("engine", "LocationWeatherContext", parameters);
        return true;
    }

    private boolean unRegisterWeatherContext() {
        mContextManager.removeObserverRequirement("engine", "LocationWeatherContext");
        mOntologyManager.unregisterCSPARQLQuery(rules.remove(weatherRainingAndColdQuery).getId());
        mOntologyManager.unregisterCSPARQLQuery(rules.remove(weatherColdQuery).getId());
        mOntologyManager.unregisterCSPARQLQuery(rules.remove(weatherRainingQuery).getId());
        mOntologyManager.unregisterCSPARQLQuery(rules.remove(weatherHotQuery).getId());
        return true;
    }

    public boolean registerBatteryContext() {

        mContextManager.addObserverRequirement("engine", "BatteryContext");
        CsparqlQueryResultProxy c1 = mOntologyManager.registerCSPARQLQuery(batteryLOWQuery);
        CsparqlQueryResultProxy c2 = mOntologyManager.registerCSPARQLQuery(batteryOkQuery);
        rules.put(batteryLOWQuery, c1);
        rules.put(batteryOkQuery, c2);
        return true;
    }

    public boolean unRegisterBatteryContext() {
        mContextManager.removeObserverRequirement("engine", "BatteryContext");
        mOntologyManager.unregisterCSPARQLQuery(rules.remove(batteryLOWQuery).getId());
        mOntologyManager.unregisterCSPARQLQuery(rules.remove(batteryOkQuery).getId());
        return true;
    }

    public boolean registerLightContext() {
        mContextManager.addObserverRequirement("engine", "LightContext");
        CsparqlQueryResultProxy c1 = mOntologyManager.registerCSPARQLQuery(lightingLowQuery);
        rules.put("light", c1);
        return true;
    }

    public boolean unRegisterLightContext() {
        mContextManager.removeObserverRequirement("engine", "LightContext");
        mOntologyManager.unregisterCSPARQLQuery(rules.remove("light").getId());
        return true;
    }


}
