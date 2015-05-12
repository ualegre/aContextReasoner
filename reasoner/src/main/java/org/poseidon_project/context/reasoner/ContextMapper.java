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
    private static final String mColdTemp = "15";
    private static final String mPrepValue = "0";
    //private static final String mNumofDev = "3";

    private static final String batteryLOWQuery =
            "REGISTER STREAM batteryContextIsLOW AS "
                    + "PREFIX ex: <http://ie.cs.mdx.ac.uk/POSEIDON/system#> "
                    + "CONSTRUCT { ?s <http://poseidon-project.org/context/is> \"BATTERY_LOW\"} "
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
            "REGISTER QUERY weatherContextIsHot AS "
                    + "PREFIX ex: <http://ie.cs.mdx.ac.uk/POSEIDON/envir#> "
                    + "CONSTRUCT { ex:weather <http://ie.cs.mdx.ac.uk/POSEIDON/context/is> \"WEATHER_HOT\" } "
                    + "FROM STREAM <http://poseidon-project.org/context-stream> [RANGE 10s STEP 4s] "
                    + "WHERE { ?m ex:hasTemperatureValue ?tempValueIRI . "
                    + "?m ex:hasPrecipitationValue ?precipValueIRI . "
                    + "?precipValueIRI ex:precipitationValue ?precipValue . "
                    + "?tempValueIRI ex:temperatureValue ?tempValue . "
                    + " FILTER (?precipValue < 0.1) "
                    + " FILTER (?tempValue > 25) "
                    + "}";
    private static final String weatherOkayQuery =
            "REGISTER QUERY weatherContextIsOkay AS "
                    + "PREFIX ex: <http://ie.cs.mdx.ac.uk/POSEIDON/envir#> "
                    + "CONSTRUCT { ex:weather <http://ie.cs.mdx.ac.uk/POSEIDON/context/is> \"WEATHER_OKAY\" } "
                    + "FROM STREAM <http://poseidon-project.org/context-stream> [RANGE 10s STEP 4s] "
                    + "WHERE { ?m ex:hasTemperatureValue ?tempValueIRI . "
                    + "?m ex:hasPrecipitationValue ?precipValueIRI . "
                    + "?precipValueIRI ex:precipitationValue ?precipValue . "
                    + "?tempValueIRI ex:temperatureValue ?tempValue . "
                    + " FILTER (?precipValue < 0.1) "
                    + " FILTER (?tempValue > 14) "
                    + " FILTER (?tempValue < 25) "
                    + "}";

    private static final String navigationAssistNeededQuery =
            "REGISTER QUERY needNavigationAssistance AS "
                    + "PREFIX ex: <http://ie.cs.mdx.ac.uk/POSEIDON/user#> "
                    + "CONSTRUCT { ex:u1 <http://ie.cs.mdx.ac.uk/POSEIDON/context/is> \"NAV_ASSISTNEEDED\" } "
                    + "FROM STREAM <http://poseidon-project.org/context-stream> [RANGE 10m Step 1m] "
                    + " WHERE { ?user ex:hasNavigationStatus ?o . "
                    + " {"
                    + " SELECT (COUNT(?user) AS ?num) WHERE { ?user ex:hasNavigationStatus 1 . }"
                    + " }"
                    + " FILTER( ?num > 3 || ?o = 2 ) "
                    + " } ";

    private static final String navigationAssistNotNeededQuery =
            "REGISTER QUERY needNavigationAssistance AS "
                    + "PREFIX ex: <http://ie.cs.mdx.ac.uk/POSEIDON/user#> "
                    + "CONSTRUCT { ex:u1 <http://ie.cs.mdx.ac.uk/POSEIDON/context/is> \"NAV_ASSISTNOTNEEDED\" } "
                    + "FROM STREAM <http://poseidon-project.org/context-stream> [RANGE 10m Step 1m] "
                    + " WHERE { ?user ex:hasNavigationStatus ?o . "
                    + " {"
                    + " SELECT (COUNT(?user) AS ?numS) WHERE { ?user ex:hasNavigationStatus 1 . }"
                    + " } . "
                    + " {"
                    + " SELECT (COUNT(?user) AS ?numB) WHERE { ?user ex:hasNavigationStatus 2 . }"
                    + " } . "
                    + " FILTER( ?numS < 3 && ?numB < 1) "
                    + " FILTER( ?o = 3) "
                    + " } ";

    private static final String isStandstillForLongQuery =
            "REGISTER QUERY notWalkingFastEnough AS "
                    + "PREFIX ex: <http://ie.cs.mdx.ac.uk/POSEIDON/user#> "
                    + "CONSTRUCT { ex:u1 <http://ie.cs.mdx.ac.uk/POSEIDON/context/is> \"isStandStillForLong\" } "
                    + "FROM STREAM <http://poseidon-project.org/context-stream> [RANGE 5m STEP 20s] "
                    + "WHERE { ?user ex:hasStepped ?steps . "
                    + " {"
                    + " SELECT ( SUM(?steps) AS ?totalSteps ) WHERE { ?user ex:hasStepped ?step . }"
                    + " }"
                    + " FILTER( ?totalSteps < 10) "
                    + " }";

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
        } else if (context.equals("navassistance")) {
            return registerNavAssistance();
        } else if (context.equals("standstill")) {
            return registerStandstill();
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
        } else if (context.equals("navassistance")) {
            return unRegisterNavAssistance();
        } else if (context.equals("standstill")) {
            return unRegisterStandstill();
        }

        Log.e(LOGTAG, "Context: " + context + " not found!");

        return false;
    }


    private boolean registerStandstill() {
        CsparqlQueryResultProxy c1 = mOntologyManager.registerCSPARQLQuery(isStandstillForLongQuery);
        rules.put(isStandstillForLongQuery, c1);
        mContextManager.addObserverRequirement("engine", "StepCounter");
        return true;
    }

    private boolean unRegisterStandstill() {
        mContextManager.removeObserverRequirement("engine", "StepCounter");
        mOntologyManager.unregisterCSPARQLQuery(rules.remove(isStandstillForLongQuery).getId());
        return true;
    }


    private boolean registerNavAssistance() {
        CsparqlQueryResultProxy c1 = mOntologyManager.registerCSPARQLQuery(navigationAssistNeededQuery);
        CsparqlQueryResultProxy c2 = mOntologyManager.registerCSPARQLQuery(navigationAssistNotNeededQuery);
        rules.put(navigationAssistNeededQuery, c1);
        rules.put(navigationAssistNotNeededQuery, c2);
        return true;
    }

    private boolean unRegisterNavAssistance() {
        mOntologyManager.unregisterCSPARQLQuery(rules.remove(navigationAssistNeededQuery).getId());
        mOntologyManager.unregisterCSPARQLQuery(rules.remove(navigationAssistNotNeededQuery).getId());
        return true;
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
        CsparqlQueryResultProxy c5 = mOntologyManager.registerCSPARQLQuery(weatherOkayQuery);
        rules.put(weatherRainingAndColdQuery, c1);
        rules.put(weatherColdQuery, c2);
        rules.put(weatherRainingQuery, c3);
        rules.put(weatherHotQuery, c4);
        rules.put(weatherOkayQuery, c5);
        mContextManager.addObserverRequirementWithParameters("engine", "LocationWeatherContext", parameters);
        return true;
    }

    private boolean unRegisterWeatherContext() {
        mContextManager.removeObserverRequirement("engine", "LocationWeatherContext");
        mOntologyManager.unregisterCSPARQLQuery(rules.remove(weatherRainingAndColdQuery).getId());
        mOntologyManager.unregisterCSPARQLQuery(rules.remove(weatherColdQuery).getId());
        mOntologyManager.unregisterCSPARQLQuery(rules.remove(weatherRainingQuery).getId());
        mOntologyManager.unregisterCSPARQLQuery(rules.remove(weatherHotQuery).getId());
        mOntologyManager.unregisterCSPARQLQuery(rules.remove(weatherOkayQuery).getId());
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
