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

import org.poseidon_project.context.ContextReasonerCore;
import org.poseidon_project.context.logging.DataLogger;
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
    private DataLogger mLogger;


    /*
        Check that the battery has less than 25 percent remaining.
     */
    private static final String batteryLOWQuery =
            "REGISTER STREAM batteryContextIsLOW AS "
                    + "PREFIX ex: <http://ie.cs.mdx.ac.uk/POSEIDON/system#> "
                    + "CONSTRUCT { ?s <http://poseidon-project.org/context/is> \"BATTERY_LOW\"} "
                    + "FROM STREAM <http://poseidon-project.org/context-stream> [RANGE TRIPLES 1] "
                    + "WHERE { ?s ex:batteryRemaining ?o "
                    + "FILTER ( ?o < 25) }";

    /*
        Check that the battery has 25 percent or more remaining.
     */
    private static final String batteryOkQuery =
            "REGISTER STREAM batteryContextIsOkay AS "
                    + "PREFIX ex: <http://ie.cs.mdx.ac.uk/POSEIDON/system#> "
                    + "CONSTRUCT { ?s <http://poseidon-project.org/context/is> \"BATTERY_OKAY\"} "
                    + "FROM STREAM <http://poseidon-project.org/context-stream> [RANGE TRIPLES 1] "
                    + "WHERE { ?s ex:batteryRemaining ?o "
                    + "FILTER ( ?o >= 25) }";

    /*
        Check that the precipitation value is greater than zero, and temperature is less than 15c
     */
    private static final String weatherRainingAndColdQuery =
            "REGISTER QUERY weatherContextIsRainingAndCold AS "
                    + "PREFIX ex: <http://ie.cs.mdx.ac.uk/POSEIDON/envir#> "
                    + "CONSTRUCT { ex:weather <http://ie.cs.mdx.ac.uk/POSEIDON/context/is> \"WEATHER_RAININGANDCOLD\" } "
                    + "FROM STREAM <http://poseidon-project.org/context-stream> [RANGE 10s STEP 4s] "
                    + "WHERE { ?m ex:hasTemperatureValue ?tempValueIRI . "
                    + "?m ex:hasPrecipitationValue ?precipValueIRI . "
                    + "?precipValueIRI ex:precipitationValue ?precipValue . "
                    + "?tempValueIRI ex:temperatureValue ?tempValue . "
                    + "FILTER (?precipValue >= 0.1) "
                    + "FILTER (?tempValue < 15) "
                    + "}";

    /*
        Check that the temperature is 15c or greater, and that precipitation is greater than zero.
     */
    private static final String weatherRainingQuery =
            "REGISTER QUERY weatherContextIsRaining AS "
                    + "PREFIX ex: <http://ie.cs.mdx.ac.uk/POSEIDON/envir#> "
                    + "CONSTRUCT { ex:weather <http://ie.cs.mdx.ac.uk/POSEIDON/context/is> \"WEATHER_RAINING\" } "
                    + "FROM STREAM <http://poseidon-project.org/context-stream> [RANGE 10s STEP 4s] "
                    + "WHERE { ?m ex:hasTemperatureValue ?tempValueIRI . "
                    + "?m ex:hasPrecipitationValue ?precipValueIRI . "
                    + "?precipValueIRI ex:precipitationValue ?precipValue . "
                    + "?tempValueIRI ex:temperatureValue ?tempValue . "
                    + "FILTER (?precipValue >= 0.1) "
                    + "FILTER (?tempValue >= 15) "
                    + "}";

    /*
        Check that precipitation is less than 0.1mm, and temperature is less than 15c.
     */
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

    /*
        Check that precipitation is less than 0.1mm, and temperature is greater than 25c.
     */
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
                    + " FILTER (?tempValue >= 25) "
                    + "}";

    /*
        Check that precipitation is less than 0.1mm, temperature is between 15c-24.9c.
     */
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
                    + " FILTER (?tempValue >= 15) "
                    + " FILTER (?tempValue < 25) "
                    + "}";

    /*
        Checks if navigation assistance is required. We do this by seeing if the user has either:
        1 - Critically deviated, which requires a new route calculation; or
        2 - Deviated a 3 or more times (albeit small deviations) within 10 minutes.

        Deviation data is received in terms of integers:
        0 - Navigation is off
        1 - Navigation is on, and a critical deviation has happened.
        2 - Navigation is on, and a small deviation has happened.
        3 - Navigation is on, currently no deviation.

        We therefore firstly count the number of small deviations (subquery). We then see if
        either the number of small deviations is 3 or greater, or if a critical deviation has
        occured.
     */
    private static final String navigationAssistNeededQuery =
            "REGISTER QUERY needNavigationAssistance AS "
                    + "PREFIX ex: <http://ie.cs.mdx.ac.uk/POSEIDON/user#> "
                    + "CONSTRUCT { ex:u1 <http://ie.cs.mdx.ac.uk/POSEIDON/context/is> \"NAV_ASSISTNEEDED\" } "
                    + "FROM STREAM <http://poseidon-project.org/context-stream> [RANGE 10m Step 1m] "
                    + " WHERE { ?user ex:hasNavigationStatus ?o . "
                    + " {"
                    + " SELECT (COUNT(?user) AS ?smallDevNum) WHERE { ?user ex:hasNavigationStatus 2 . }"
                    + " }"
                    + " FILTER( ?smallDevNum >= 3 || ?o = 1 ) "
                    + " } ";

    /*
        Checks if navigation assistance is NOT required. We do this by seeing if the user has either:
        1 - Critically deviated, which requires a new route calculation; or
        2 - Deviated less than 3 times (albeit small deviations) within 10 minutes.

        Deviation data is received in terms of integers:
        0 - Navigation is off
        1 - Navigation is on, and a critical deviation has happened.
        2 - Navigation is on, and a small deviation has happened.
        3 - Navigation is on, currently no deviation.

        We therefore firstly count the number of small deviations (subquery 1), and critical
        deviations (subquery 2). We then see if either the number of small deviations is less than 3
        times, if a critical deviation has occured, or if no deviation has occured.
     */
    private static final String navigationAssistNotNeededQuery =
            "REGISTER QUERY dontNeedNavigationAssistance AS "
                    + "PREFIX ex: <http://ie.cs.mdx.ac.uk/POSEIDON/user#> "
                    + "CONSTRUCT { ex:u1 <http://ie.cs.mdx.ac.uk/POSEIDON/context/is> \"NAV_ASSISTNOTNEEDED\" } "
                    + "FROM STREAM <http://poseidon-project.org/context-stream> [RANGE 10m Step 1m] "
                    + " WHERE { ?user ex:hasNavigationStatus ?o . "
                    + " {"
                    + " SELECT (COUNT(?user) AS ?smallDevNum) WHERE { ?user ex:hasNavigationStatus 2 . }"
                    + " } . "
                    + " {"
                    + " SELECT (COUNT(?user) AS ?largeDevNum) WHERE { ?user ex:hasNavigationStatus 1 . }"
                    + " } . "
                    + " FILTER( ?smallDevNum < 3 && ?largeDevNum < 1) "
                    + " FILTER( ?o = 3) "
                    + " } ";

    /*
        Checks to see how fast the user is walking, to tell if they are too standstill for too long.
        Checks that the user is walking less than 15m every 5 minutes.
     */
    private static final String isStandstillForLongQuery =
            "REGISTER QUERY notWalkingFastEnough AS "
                    + "PREFIX ex: <http://ie.cs.mdx.ac.uk/POSEIDON/user#> "
                    + "CONSTRUCT { ex:u1 <http://ie.cs.mdx.ac.uk/POSEIDON/context/is> \"STANDSTILL_LONG\" } "
                    + "FROM STREAM <http://poseidon-project.org/context-stream> [RANGE 5m STEP 1m] "
                    + " WHERE { ?user ex:hasMoved ?distance . "
                    + " { "
                    + " SELECT ( SUM(?distance) AS ?totalDistance ) WHERE { ?user ex:hasMoved ?distance . } "
                    + " } . "
                    + " FILTER ( ?totalDistance < 15) "
                    + " }";

    /*
        Checks to see how fast the user is walking, to tell if they are not stanstill for long.
        Checks that the user is walking 15m or more every 5 minutes.
     */
    private static final String isStandstillForShortQuery =
            "REGISTER QUERY isWalkingFastEnough AS "
                    + "PREFIX ex: <http://ie.cs.mdx.ac.uk/POSEIDON/user#> "
                    + "CONSTRUCT { ex:u1 <http://ie.cs.mdx.ac.uk/POSEIDON/context/is> \"STANDSTILL_SHORT\" } "
                    + "FROM STREAM <http://poseidon-project.org/context-stream> [RANGE 5m STEP 1m] "
                    + " WHERE { ?user ex:hasMoved ?distance . "
                    + " { "
                    + " SELECT ( SUM(?distance) AS ?totalDistance ) WHERE { ?user ex:hasMoved ?distance . } "
                    + " } . "
                    + " FILTER( ?totalDistance >= 15) "
                    + " }";

    public ContextMapper(ContextReasonerCore crc, OntologyManager on) {

        mReasonerCore = crc;
        mLogger = crc.getLogger();
        mContextManager = crc.getContextManager();
        mOntologyManager = on;

    }

    public boolean registerContext(String context, Map parameters) {

        if (mContextManager == null) {
            mContextManager = mReasonerCore.getContextManager();
        }

        if (context.equals("battery")) {
            return registerBatteryContext();
        } else if (context.equals("weather")) {
            return registerWeatherContext(parameters);
        } else if (context.equals("indoor/outdoor")) {
            return registerIndoorOutdoorsContext();
        } else if (context.equals("navassistance")) {
            return registerNavAssistance();
        } else if (context.equals("standstill")) {
            return registerStandstill();
        }

        mLogger.logError(DataLogger.REASONER, LOGTAG, "Context: " + context + " not found!");

        return false;
    }

    public boolean unregisterContext(String context, Map parameters) {

        if (context.equals("battery")) {
            return unRegisterBatteryContext();
        } else if (context.equals("weather")) {
            return unRegisterWeatherContext();
        } else if (context.equals("indoor/outdoor")) {
            return unRegisterIndoorOutdoorsContext();
        } else if (context.equals("navassistance")) {
            return unRegisterNavAssistance();
        } else if (context.equals("standstill")) {
            return unRegisterStandstill();
        }

        mLogger.logError(DataLogger.REASONER, LOGTAG, "Context: " + context + " not found!");

        return false;
    }


    private boolean registerStandstill() {
        CsparqlQueryResultProxy c1 = mOntologyManager.registerCSPARQLQuery(isStandstillForLongQuery);
        CsparqlQueryResultProxy c2 = mOntologyManager.registerCSPARQLQuery(isStandstillForShortQuery);
        rules.put(isStandstillForLongQuery, c1);
        rules.put(isStandstillForShortQuery, c2);
        mContextManager.addObserverRequirement("engine", "DistanceTravelledContext");

        mLogger.logVerbose(DataLogger.REASONER, LOGTAG, "Registered standstill");
        return true;
    }

    private boolean unRegisterStandstill() {
        boolean okExit = true;

        mContextManager.removeObserverRequirement("engine", "DistanceTravelledContext");

        CsparqlQueryResultProxy c1 = rules.remove(isStandstillForLongQuery);
        CsparqlQueryResultProxy c2 = rules.remove(isStandstillForShortQuery);

        if (c1 != null) {
            mOntologyManager.unregisterCSPARQLQuery(c1.getId());
        } else {
            okExit = false;
            mLogger.logError(DataLogger.REASONER, LOGTAG,
                    "isStandstillForLongQuery was not registered");
        }


        if (c2 != null) {
            mOntologyManager.unregisterCSPARQLQuery(c2.getId());
        } else {
            okExit = false;
            mLogger.logError(DataLogger.REASONER, LOGTAG,
                    "isStandstillForShortQuery was not registered");
        }

        mReasonerCore.removeContextValue("STANDSTILL");
        mLogger.logVerbose(DataLogger.REASONER, LOGTAG, "Unregistered standstill");

        return okExit;
    }


    private boolean registerNavAssistance() {
        boolean okExit = true;

        CsparqlQueryResultProxy c1 = mOntologyManager.registerCSPARQLQuery(navigationAssistNeededQuery);
        CsparqlQueryResultProxy c2 = mOntologyManager.registerCSPARQLQuery(navigationAssistNotNeededQuery);

        if (c1 != null) {
            rules.put(navigationAssistNeededQuery, c1);
        } else {
            okExit = false;
            mLogger.logError(DataLogger.REASONER, LOGTAG,
                    "navigationAssistNeededQuery couldn't register");
        }

        if (c2 != null) {
            rules.put(navigationAssistNotNeededQuery, c2);
        } else {
            okExit = false;
            mLogger.logError(DataLogger.REASONER, LOGTAG,
                    "navigationAssistNotNeededQuery couldn't register");
        }

        mLogger.logVerbose(DataLogger.REASONER, LOGTAG, "Registered navassistance");

        return okExit;
    }

    private boolean unRegisterNavAssistance() {

        boolean okExit = true;

        CsparqlQueryResultProxy c1 = rules.remove(navigationAssistNeededQuery);
        CsparqlQueryResultProxy c2 = rules.remove(navigationAssistNotNeededQuery);

        if (c1 != null) {
            mOntologyManager.unregisterCSPARQLQuery(c1.getId());
        } else {
            okExit = false;
            mLogger.logError(DataLogger.REASONER, LOGTAG,
                    "navigationAssistNeededQuery was not registered");
        }

        if (c2 != null) {
            mOntologyManager.unregisterCSPARQLQuery(c2.getId());
        } else {
            okExit = false;
            mLogger.logError(DataLogger.REASONER, LOGTAG,
                    "navigationAssistNeededQuery was not registered");
        }

        mReasonerCore.removeContextValue("NAV");
        mLogger.logVerbose(DataLogger.REASONER, LOGTAG, "Unregistered navassistance");

        return okExit;
    }

    private boolean registerIndoorOutdoorsContext() {
        mLogger.logVerbose(DataLogger.REASONER, LOGTAG, "Registered GPSIndoorOutdoor");
        return mContextManager.addObserverRequirement("engine", "GPSIndoorOutdoorContext");
    }

    private boolean unRegisterIndoorOutdoorsContext() {

        boolean okExit = mContextManager.removeObserverRequirement("engine", "GPSIndoorOutdoorContext");
        mReasonerCore.removeContextValue("INDOOR/OUTDOOR");
        mLogger.logVerbose(DataLogger.REASONER, LOGTAG, "Unregistered GPSIndoorOutdoor");

        return okExit;
    }

    private boolean registerWeatherContext(Map parameters) {

        boolean okExit = true;

        CsparqlQueryResultProxy c1 = mOntologyManager.registerCSPARQLQuery(weatherRainingAndColdQuery);
        CsparqlQueryResultProxy c2 = mOntologyManager.registerCSPARQLQuery(weatherColdQuery);
        CsparqlQueryResultProxy c3 = mOntologyManager.registerCSPARQLQuery(weatherRainingQuery);
        CsparqlQueryResultProxy c4 = mOntologyManager.registerCSPARQLQuery(weatherHotQuery);
        CsparqlQueryResultProxy c5 = mOntologyManager.registerCSPARQLQuery(weatherOkayQuery);

        if (c1 != null) {
            rules.put(weatherRainingAndColdQuery, c1);
        } else {
            okExit = false;
            mLogger.logError(DataLogger.REASONER, LOGTAG,
                    "weatherRainingAndColdQuery couldn't register");
        }

        if (c2 != null) {
            rules.put(weatherColdQuery, c2);
        } else {
            okExit = false;
            mLogger.logError(DataLogger.REASONER, LOGTAG,
                    "weatherColdQuery couldn't register");
        }

        if (c3 != null) {
            rules.put(weatherRainingQuery, c3);
        } else {
            okExit = false;
            mLogger.logError(DataLogger.REASONER, LOGTAG,
                    "weatherRainingQuery couldn't register");
        }

        if (c4 != null) {
            rules.put(weatherHotQuery, c4);
        } else {
            okExit = false;
            mLogger.logError(DataLogger.REASONER, LOGTAG,
                    "weatherHotQuery couldn't register");
        }

        if (c5 != null) {
            rules.put(weatherOkayQuery, c5);
        } else {
            okExit = false;
            mLogger.logError(DataLogger.REASONER, LOGTAG,
                    "weatherOkayQuery couldn't register");
        }

        mLogger.logVerbose(DataLogger.REASONER, LOGTAG, "Registered weather");

        if (okExit) {
            return mContextManager.addObserverRequirementWithParameters("engine", "LocationWeatherContext", parameters);
        } else {
            return false;
        }

    }

    private boolean unRegisterWeatherContext() {

        boolean okExit = mContextManager.removeObserverRequirement("engine", "LocationWeatherContext");

        CsparqlQueryResultProxy c1 = rules.remove(weatherRainingAndColdQuery);
        CsparqlQueryResultProxy c2 = rules.remove(weatherColdQuery);
        CsparqlQueryResultProxy c3 = rules.remove(weatherRainingQuery);
        CsparqlQueryResultProxy c4 = rules.remove(weatherHotQuery);
        CsparqlQueryResultProxy c5 = rules.remove(weatherOkayQuery);

        if (c1 != null) {
            mOntologyManager.unregisterCSPARQLQuery(c1.getId());
        } else {
            okExit = false;
            mLogger.logError(DataLogger.REASONER, LOGTAG,
                    "weatherRainingAndColdQuery was not registered");
        }

        if (c2 != null) {
            mOntologyManager.unregisterCSPARQLQuery(c2.getId());
        } else {
            okExit = false;
            mLogger.logError(DataLogger.REASONER, LOGTAG, "weatherColdQuery was not registered");
        }

        if (c3 != null) {
            mOntologyManager.unregisterCSPARQLQuery(c3.getId());
        } else {
            okExit = false;
            mLogger.logError(DataLogger.REASONER, LOGTAG, "weatherRainingQuery was not registered");
        }

        if (c4 != null) {
            mOntologyManager.unregisterCSPARQLQuery(c4.getId());
        } else {
            okExit = false;
            mLogger.logError(DataLogger.REASONER, LOGTAG, "weatherHotQuery was not registered");
        }

        if (c5 != null) {
            mOntologyManager.unregisterCSPARQLQuery(c5.getId());
        } else {
            okExit = false;
            mLogger.logError(DataLogger.REASONER, LOGTAG, "weatherOkayQuery was not registered");
        }

        mReasonerCore.removeContextValue("WEATHER");
        mLogger.logVerbose(DataLogger.REASONER, LOGTAG, "Unregistered weather");

        return okExit;
    }

    public boolean registerBatteryContext() {

        boolean okExit = true;

        CsparqlQueryResultProxy c1 = mOntologyManager.registerCSPARQLQuery(batteryLOWQuery);
        CsparqlQueryResultProxy c2 = mOntologyManager.registerCSPARQLQuery(batteryOkQuery);

        if (c1 != null) {
            rules.put(batteryLOWQuery, c1);
        } else {
            okExit = false;
            mLogger.logError(DataLogger.REASONER, LOGTAG, "batteryLOWQuery could't register");
        }

        if (c2 != null) {
            rules.put(batteryOkQuery, c2);
        } else {
            okExit = false;
            mLogger.logError(DataLogger.REASONER, LOGTAG, "batteryOkQuery couldn't register");
        }

        mLogger.logVerbose(DataLogger.REASONER, LOGTAG, "Registered battery");

        if (okExit) {
            return mContextManager.addObserverRequirement("engine", "BatteryContext");
        } else {
            return false;
        }
    }

    public boolean unRegisterBatteryContext() {

        boolean okExit = mContextManager.removeObserverRequirement("engine", "BatteryContext");

        CsparqlQueryResultProxy c1 = rules.remove(batteryLOWQuery);
        CsparqlQueryResultProxy c2 = rules.remove(batteryOkQuery);

        if (c1 != null) {
            mOntologyManager.unregisterCSPARQLQuery(c1.getId());
        } else {
            okExit = false;
            mLogger.logError(DataLogger.REASONER, LOGTAG, "batteryLOWQuery was not registered");
        }

        if (c2 != null) {
            mOntologyManager.unregisterCSPARQLQuery(c2.getId());
        } else {
            okExit = false;
            mLogger.logError(DataLogger.REASONER, LOGTAG, "batteryOkQuery was not registered");
        }

        mReasonerCore.removeContextValue("BATTERY");
        mLogger.logVerbose(DataLogger.REASONER, LOGTAG, "Unregistered battery");

        return okExit;
    }
}
