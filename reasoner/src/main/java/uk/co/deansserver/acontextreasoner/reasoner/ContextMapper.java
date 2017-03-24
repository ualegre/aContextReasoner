/*
 * Copyright 2017 aContextReasoner Project
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

package uk.co.deansserver.acontextreasoner.reasoner;

import android.content.Context;
import android.content.SharedPreferences;

import uk.co.deansserver.acontextreasoner.ContextReasonerCore;
import uk.co.deansserver.acontextreasoner.logging.DataLogger;
import uk.co.deansserver.acontextreasoner.management.ContextManager;
import uk.co.deansserver.acontextreasoner.utility.Prefs;

import java.util.HashMap;
import java.util.Map;

import eu.larkc.csparql.core.engine.CsparqlQueryResultProxy;

/**
 * Temporary class for mapping contexts to components and rules for pilot 1
 *
 * @author Dean Kramer <deankramer99@gmail.com>
 */
public class ContextMapper {

    private ContextReasonerCore mReasonerCore;
    private ContextManager mContextManager;
    private ReasonerManager mReasonerManager;
    private HashMap<String, CsparqlQueryResultProxy> rules = new HashMap<>();
    private static final String LOGTAG = "ContextMapper";
    private DataLogger mLogger;
    private static final String mStreamRoot = "http://poseidon-project.org/context-stream";
    private SharedPreferences mRuleSettings;
    private Context mContext;
    public HashMap<String, Map> mContextParameters;



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

    private static final String tempOkay =
            "REGISTER QUERY tempContextIsOkay AS "
                    + "PREFIX ex: <http://ie.cs.mdx.ac.uk/POSEIDON/envir#> "
                    + "CONSTRUCT { ex:temp <http://ie.cs.mdx.ac.uk/POSEIDON/context/is> \"TEMP_OKAY\" } "
                    + "FROM STREAM <http://poseidon-project.org/context-stream> [RANGE 10s STEP 4s] "
                    + "WHERE { ?m ex:hasTemperatureValue ?tempValueIRI . "
                    + "?tempValueIRI ex:temperatureValue ?tempValue . "
                    + "FILTER (?tempValue >= $$pref_cold) "
                    + "FILTER (?tempValue < $$pref_hot) "
                    + "}";

    private static final String tempCold =
            "REGISTER QUERY tempContextIsCold AS "
                    + "PREFIX ex: <http://ie.cs.mdx.ac.uk/POSEIDON/envir#> "
                    + "CONSTRUCT { ex:temp <http://ie.cs.mdx.ac.uk/POSEIDON/context/is> \"TEMP_COLD\" } "
                    + "FROM STREAM <http://poseidon-project.org/context-stream> [RANGE 10s STEP 4s] "
                    + "WHERE { ?m ex:hasTemperatureValue ?tempValueIRI . "
                    + "?tempValueIRI ex:temperatureValue ?tempValue . "
                    + "FILTER (?tempValue < $$pref_cold) "
                    + "}";

    private static final String tempHot =
            "REGISTER QUERY tempContextIsOkay AS "
                    + "PREFIX ex: <http://ie.cs.mdx.ac.uk/POSEIDON/envir#> "
                    + "CONSTRUCT { ex:temp <http://ie.cs.mdx.ac.uk/POSEIDON/context/is> \"TEMP_HOT\" } "
                    + "FROM STREAM <http://poseidon-project.org/context-stream> [RANGE 10s STEP 4s] "
                    + "WHERE { ?m ex:hasTemperatureValue ?tempValueIRI . "
                    + "?tempValueIRI ex:temperatureValue ?tempValue . "
                    + "FILTER (?tempValue >= $$pref_hot) "
                    + "}";

    private static final String precipRain =
            "REGISTER QUERY precipContextIsRaining AS "
                    + "PREFIX ex: <http://ie.cs.mdx.ac.uk/POSEIDON/envir#> "
                    + "CONSTRUCT { ex:temp <http://ie.cs.mdx.ac.uk/POSEIDON/context/is> \"PRECIP_RAINING\" } "
                    + "FROM STREAM <http://poseidon-project.org/context-stream> [RANGE 10s STEP 4s] "
                    + "WHERE { ?m ex:hasPrecipitationValue ?precipValueIRI . "
                    + "?precipValueIRI ex:precipitationValue ?precipValue . "
                    + "FILTER (?precipValue >= 0.1) "
                    + "}";

    private static final String precipDry =
            "REGISTER QUERY precipContextIsDry AS "
                    + "PREFIX ex: <http://ie.cs.mdx.ac.uk/POSEIDON/envir#> "
                    + "CONSTRUCT { ex:temp <http://ie.cs.mdx.ac.uk/POSEIDON/context/is> \"PRECIP_DRY\" } "
                    + "FROM STREAM <http://poseidon-project.org/context-stream> [RANGE 10s STEP 4s] "
                    + "WHERE { ?m ex:hasPrecipitationValue ?precipValueIRI . "
                    + "?precipValueIRI ex:precipitationValue ?precipValue . "
                    + "FILTER (?precipValue < 0.1) "
                    + "}";

    private static final String newWeatherOKAY = "TEMP_OKAY and PRECIP_DRY iff WEATHER_OKAY";
    private static final String newWeatherRAINING = "TEMP_OKAY and PRECIP_RAINING iff WEATHER_RAINING";
    private static final String newWeatherCOLD = "TEMP_COLD and PRECIP_DRY iff WEATHER_COLD";
    private static final String newWeatherCOLDANDRAINING = "TEMP_COLD and PRECIP_RAINING iff WEATHER_RAININGANDCOLD";
    private static final String newWeatherHOT = "TEMP_HOT and PRECIP_DRY iff WEATHER_HOT";

    /*
        Checks if navigation assistance is required. We do this by seeing if the user has either:
        1 - Critically deviated, which requires a new route calculation; or
        2 - Deviated a 2 or more times (albeit small deviations) within 10 minutes.

        Deviation data is received in terms of integers:
        0 - Navigation is off
        1 - Navigation is on, and a critical deviation has happened.
        2 - Navigation is on, and a small deviation has happened.
        3 - Navigation is on, currently no deviation.

        We therefore firstly count the number of small deviations (subquery). We then see if
        either the number of small deviations is 2 or greater, or if a critical deviation has
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
                    + " FILTER( ?smallDevNum >= $$pref_max_dev || ?o = 1 ) "
                    + " } ";

    /*
        Checks if navigation assistance is NOT required. We do this by seeing if the user has either:
        1 - Critically deviated, which requires a new route calculation; or
        2 - Deviated less than 2 times (albeit small deviations) within 10 minutes.

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
                    + " FILTER( ?smallDevNum < $$pref_max_dev && ?largeDevNum < 1) "
                    //+ " FILTER( ?o = 3) "
                    + " } ";

    /*
        Checks to see how fast the user is walking, to tell if they are too standstill for too long.
        Checks that the user is walking less than 20m every 5 minutes.
     */
    private static final String isStandstillForLongQuery =
            "REGISTER QUERY notWalkingFastEnough AS "
                    + "PREFIX ex: <http://ie.cs.mdx.ac.uk/POSEIDON/user#> "
                    + "CONSTRUCT { ex:u1 <http://ie.cs.mdx.ac.uk/POSEIDON/context/is> \"STANDSTILL_LONG\" } "
                    + "FROM STREAM <http://poseidon-project.org/context-stream> [RANGE $$timem STEP $$timem] "
                    + " WHERE { ?user ex:hasMoved ?distance . "
                    + " { "
                    + " SELECT ( SUM(?distance) AS ?totalDistance ) WHERE { ?user ex:hasMoved ?distance . } "
                    + " } . "
                    + " FILTER ( ?totalDistance < $$tDistance) "
                    + " }";

    /*
        Checks to see how fast the user is walking, to tell if they are not stanstill for long.
        Checks that the user is walking 20m or more every 5 minutes.
     */
    private static final String isStandstillForShortQuery =
            "REGISTER QUERY isWalkingFastEnough AS "
                    + "PREFIX ex: <http://ie.cs.mdx.ac.uk/POSEIDON/user#> "
                    + "CONSTRUCT { ex:u1 <http://ie.cs.mdx.ac.uk/POSEIDON/context/is> \"STANDSTILL_SHORT\" } "
                    + "FROM STREAM <http://poseidon-project.org/context-stream> [RANGE $$timem STEP $$timem] "
                    + " WHERE { ?user ex:hasMoved ?distance . "
                    + " { "
                    + " SELECT ( SUM(?distance) AS ?totalDistance ) WHERE { ?user ex:hasMoved ?distance . } "
                    + " } . "
                    + " FILTER( ?totalDistance >= $$tDistance) "
                    + " }";

    public ContextMapper(ContextReasonerCore crc, ReasonerManager rm, Context con) {

        mReasonerCore = crc;
        mContext = con;
        mRuleSettings = con.getSharedPreferences(Prefs.RULE_PREFS, 0);
        mLogger = crc.getLogger();
        mContextManager = crc.getContextManager();
        mReasonerManager = rm;
        mContextParameters = new HashMap<>();
        //checkForDefaultContextPrefs();

    }

    private void checkForDefaultContextPrefs() {

    }

    public boolean registerContext(String context, Map parameters) {

        if (mContextManager == null) {
            mContextManager = mReasonerCore.getContextManager();
        }

        if (context.equals("battery")) {
            return registerBatteryContext();
        } else if (context.equals("oldweather")) {
            return registerOldWeatherContext(parameters);
        } else if (context.equals("indoorOutdoor")) {
            return registerIndoorOutdoorsContext();
        } else if (context.equals("navassistance")) {
            return registerNavAssistance();
        } else if (context.equals("standstill")) {
            return registerStandstill();
        } else if (context.equals("agg")) {
            return registerAgg();
        } else if (context.equals("weather")) {
            return registerWeather(parameters);
        }

        mLogger.logError(DataLogger.REASONER, LOGTAG, "Context: " + context + " not found!");

        return false;
    }

    private boolean registerWeather(Map parameters) {

        boolean okExit = true;

        int coldTemp = mRuleSettings.getInt(Prefs.WEATHER_COLD, 15);

        String cold = new String(tempCold);
        cold = cold.replace("$$pref_cold", String.valueOf(coldTemp));

        int hotTemp = mRuleSettings.getInt(Prefs.WEATHER_HOT, 25);

        String hot = new String(tempHot);
        hot = hot.replace("$$pref_hot", String.valueOf(hotTemp));

        String ok = new String(tempOkay);
        ok = ok.replace("$$pref_hot", String.valueOf(hotTemp));
        ok = ok.replace("$$pref_cold", String.valueOf(coldTemp));



        CsparqlQueryResultProxy c1 = mReasonerManager.registerCSPARQLQuery(cold);
        CsparqlQueryResultProxy c2 = mReasonerManager.registerCSPARQLQuery(hot);
        CsparqlQueryResultProxy c3 = mReasonerManager.registerCSPARQLQuery(ok);
        CsparqlQueryResultProxy c4 = mReasonerManager.registerCSPARQLQuery(precipRain);
        CsparqlQueryResultProxy c5 = mReasonerManager.registerCSPARQLQuery(precipDry);

        if (c1 != null) {
            rules.put("tempCold", c1);
        } else {
            okExit = false;
            mLogger.logError(DataLogger.REASONER, LOGTAG,
                    "tempColdQuery couldn't register");
        }

        if (c2 != null) {
            rules.put("tempHot", c2);
        } else {
            okExit = false;
            mLogger.logError(DataLogger.REASONER, LOGTAG,
                    "tempHotQuery couldn't register");
        }

        if (c3 != null) {
            rules.put("tempOkay", c3);
        } else {
            okExit = false;
            mLogger.logError(DataLogger.REASONER, LOGTAG,
                    "tempOkayQuery couldn't register");
        }

        if (c4 != null) {
            rules.put(precipRain, c4);
        } else {
            okExit = false;
            mLogger.logError(DataLogger.REASONER, LOGTAG,
                    "precipRain couldn't register");
        }

        if (c5 != null) {
            rules.put(precipDry, c5);
        } else {
            okExit = false;
            mLogger.logError(DataLogger.REASONER, LOGTAG,
                    "precipDry couldn't register");
        }

        mReasonerManager.registerAggregateRule("weatherokay", newWeatherOKAY);
        mReasonerManager.registerAggregateRule("weatherraining", newWeatherRAINING);
        mReasonerManager.registerAggregateRule("weatherrainingandcold", newWeatherCOLDANDRAINING);
        mReasonerManager.registerAggregateRule("weathercold", newWeatherCOLD);
        mReasonerManager.registerAggregateRule("weatherhot", newWeatherHOT);

        mReasonerManager.registerContextPrefAssociation("weather", Prefs.WEATHER_COLD);
        mReasonerManager.registerContextPrefAssociation("weather", Prefs.WEATHER_HOT);

        mLogger.logVerbose(DataLogger.REASONER, LOGTAG, "Registered weather");

        if (okExit) {
            return mContextManager.addObserverRequirementWithParameters("engine", "LocationWeatherContext", parameters);
        } else {
            return false;
        }


    }

    private boolean registerAgg() {

        String rule = "TEMP_COLD[06:00:00-21:00:00] and PRECIP_RAIN[#06:06:00-11:00:00] iff WEATHER_MISERABLE";

        mReasonerManager.registerAggregateRule("agg", rule);
        mReasonerManager.fireAggregateRules("TEMP_COLD");

        return true;
    }

    public boolean unregisterContext(String context, Map parameters) {

        if (context.equals("battery")) {
            return unRegisterBatteryContext();
        } else if (context.equals("oldweather")) {
            return unRegisterOldWeatherContext();
        } else if (context.equals("indoorOutdoor")) {
            return unRegisterIndoorOutdoorsContext();
        } else if (context.equals("navassistance")) {
            return unRegisterNavAssistance();
        } else if (context.equals("standstill")) {
            return unRegisterStandstill();
        } else if (context.equals("weather")) {
            return unRegisterWeatherContext();
        }

        mLogger.logError(DataLogger.REASONER, LOGTAG, "Context: " + context + " not found!");

        return false;
    }

    private boolean unRegisterWeatherContext() {

        boolean okExit = mContextManager.removeObserverRequirement("engine", "LocationWeatherContext");

        CsparqlQueryResultProxy c1 = rules.remove("tempOkay");
        CsparqlQueryResultProxy c2 = rules.remove("tempCold");
        CsparqlQueryResultProxy c3 = rules.remove("tempHot");
        CsparqlQueryResultProxy c4 = rules.remove(precipDry);
        CsparqlQueryResultProxy c5 = rules.remove(precipRain);

        if (c1 != null) {
            mReasonerManager.unregisterCSPARQLQuery(c1.getId());
        } else {
            okExit = false;
            mLogger.logError(DataLogger.REASONER, LOGTAG,
                    "tempOkayQuery was not registered");
        }

        if (c2 != null) {
            mReasonerManager.unregisterCSPARQLQuery(c2.getId());
        } else {
            okExit = false;
            mLogger.logError(DataLogger.REASONER, LOGTAG, "tempColdQuery was not registered");
        }

        if (c3 != null) {
            mReasonerManager.unregisterCSPARQLQuery(c3.getId());
        } else {
            okExit = false;
            mLogger.logError(DataLogger.REASONER, LOGTAG, "tempHotQuery was not registered");
        }

        if (c4 != null) {
            mReasonerManager.unregisterCSPARQLQuery(c4.getId());
        } else {
            okExit = false;
            mLogger.logError(DataLogger.REASONER, LOGTAG, "precipDryQuery was not registered");
        }

        if (c5 != null) {
            mReasonerManager.unregisterCSPARQLQuery(c5.getId());
        } else {
            okExit = false;
            mLogger.logError(DataLogger.REASONER, LOGTAG, "precipRainQuery was not registered");
        }

        mReasonerManager.unregisterAggregateRule("weatherokay");
        mReasonerManager.unregisterAggregateRule("weatherraining");
        mReasonerManager.unregisterAggregateRule("weatherrainingandcold");
        mReasonerManager.unregisterAggregateRule("weathercold");
        mReasonerManager.unregisterAggregateRule("weatherhot");

        mReasonerCore.removeContextValue("WEATHER");
        mReasonerCore.removeContextValue("TEMP");
        mReasonerCore.removeContextValue("PRECIP");

        mReasonerManager.unRegisterContextPrefAssociation("weather", Prefs.WEATHER_COLD);
        mReasonerManager.unRegisterContextPrefAssociation("weather", Prefs.WEATHER_HOT);

        mLogger.logVerbose(DataLogger.REASONER, LOGTAG, "Unregistered weather");

        return okExit;
    }


    private boolean registerStandstill() {

        int maxTime = mRuleSettings.getInt(Prefs.NAVASSIST_MAXWAIT, 5);
        int maxDistance = maxTime * 8;

        String standStillLong = new String(isStandstillForLongQuery);
        standStillLong = standStillLong.replace("$$time", String.valueOf(maxTime));
        standStillLong = standStillLong.replace("$$tDistance", String.valueOf(maxDistance));

        String standStillShort = new String(isStandstillForShortQuery);
        standStillShort = standStillShort.replace("$$time", String.valueOf(maxTime));
        standStillShort = standStillShort.replace("$$tDistance", String.valueOf(maxDistance));


        CsparqlQueryResultProxy c1 = mReasonerManager.registerCSPARQLQuery(standStillLong);
        CsparqlQueryResultProxy c2 = mReasonerManager.registerCSPARQLQuery(standStillShort);
        rules.put("standstill_long", c1);
        rules.put("standstill_short", c2);

        mContextManager.addObserverRequirement("engine", "DistanceTravelledContext");

        mReasonerManager.registerContextPrefAssociation("standstill","pref_max_wait");
        mLogger.logVerbose(DataLogger.REASONER, LOGTAG, "Registered standstill");
        return true;
    }

    private boolean unRegisterStandstill() {
        boolean okExit = true;

        mContextManager.removeObserverRequirement("engine", "DistanceTravelledContext");

        CsparqlQueryResultProxy c1 = rules.remove("standstill_long");
        CsparqlQueryResultProxy c2 = rules.remove("standstill_short");

        if (c1 != null) {
            mReasonerManager.unregisterCSPARQLQuery(c1.getId());
        } else {
            okExit = false;
            mLogger.logError(DataLogger.REASONER, LOGTAG,
                    "isStandstillForLongQuery was not registered");
        }


        if (c2 != null) {
            mReasonerManager.unregisterCSPARQLQuery(c2.getId());
        } else {
            okExit = false;
            mLogger.logError(DataLogger.REASONER, LOGTAG,
                    "isStandstillForShortQuery was not registered");
        }

        mReasonerCore.removeContextValue("STANDSTILL");
        mReasonerManager.unRegisterContextPrefAssociation("standstill", Prefs.NAVASSIST_MAXWAIT);
        mLogger.logVerbose(DataLogger.REASONER, LOGTAG, "Unregistered standstill");

        return okExit;
    }


    private boolean registerNavAssistance() {

        int max_dev = mRuleSettings.getInt(Prefs.NAVASSIST_MAXDEV, 2);

        String navAssNeeded = new String(navigationAssistNeededQuery);
        navAssNeeded = navAssNeeded.replace("$$pref_max_dev", String.valueOf(max_dev));

        String navAssNotNeeded = new String(navigationAssistNotNeededQuery);
        navAssNotNeeded = navAssNotNeeded.replace("$$pref_max_dev", String.valueOf(max_dev));

        boolean okExit = true;

        CsparqlQueryResultProxy c1 = mReasonerManager.registerCSPARQLQuery(navAssNeeded);
        CsparqlQueryResultProxy c2 = mReasonerManager.registerCSPARQLQuery(navAssNotNeeded);

        if (c1 != null) {
            rules.put("navAssNeeded", c1);
        } else {
            okExit = false;
            mLogger.logError(DataLogger.REASONER, LOGTAG,
                    "navigationAssistNeededQuery couldn't register");
        }

        if (c2 != null) {
            rules.put("navAssNotNeeded", c2);
        } else {
            okExit = false;
            mLogger.logError(DataLogger.REASONER, LOGTAG,
                    "navigationAssistNotNeededQuery couldn't register");
        }

        mReasonerManager.registerContextPrefAssociation("navassistance", Prefs.NAVASSIST_MAXDEV);
        mLogger.logVerbose(DataLogger.REASONER, LOGTAG, "Registered navassistance");


        return okExit;
    }

    private boolean unRegisterNavAssistance() {

        boolean okExit = true;

        CsparqlQueryResultProxy c1 = rules.remove("navAssNeeded");
        CsparqlQueryResultProxy c2 = rules.remove("navAssNotNeeded");

        if (c1 != null) {
            mReasonerManager.unregisterCSPARQLQuery(c1.getId());
        } else {
            okExit = false;
            mLogger.logError(DataLogger.REASONER, LOGTAG,
                    "navigationAssistNeededQuery was not registered");
        }

        if (c2 != null) {
            mReasonerManager.unregisterCSPARQLQuery(c2.getId());
        } else {
            okExit = false;
            mLogger.logError(DataLogger.REASONER, LOGTAG,
                    "navigationAssistNeededQuery was not registered");
        }

        mReasonerManager.unRegisterContextPrefAssociation("navassistance", Prefs.NAVASSIST_MAXDEV);
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
        mReasonerCore.removeContextValue("INDOOROUTDOOR");
        mLogger.logVerbose(DataLogger.REASONER, LOGTAG, "Unregistered GPSIndoorOutdoor");

        return okExit;
    }

    private boolean registerOldWeatherContext(Map parameters) {

        boolean okExit = true;

        CsparqlQueryResultProxy c1 = mReasonerManager.registerCSPARQLQuery(weatherRainingAndColdQuery);
        CsparqlQueryResultProxy c2 = mReasonerManager.registerCSPARQLQuery(weatherColdQuery);
        CsparqlQueryResultProxy c3 = mReasonerManager.registerCSPARQLQuery(weatherRainingQuery);
        CsparqlQueryResultProxy c4 = mReasonerManager.registerCSPARQLQuery(weatherHotQuery);
        CsparqlQueryResultProxy c5 = mReasonerManager.registerCSPARQLQuery(weatherOkayQuery);

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

    private boolean unRegisterOldWeatherContext() {

        boolean okExit = mContextManager.removeObserverRequirement("engine", "LocationWeatherContext");

        CsparqlQueryResultProxy c1 = rules.remove(weatherRainingAndColdQuery);
        CsparqlQueryResultProxy c2 = rules.remove(weatherColdQuery);
        CsparqlQueryResultProxy c3 = rules.remove(weatherRainingQuery);
        CsparqlQueryResultProxy c4 = rules.remove(weatherHotQuery);
        CsparqlQueryResultProxy c5 = rules.remove(weatherOkayQuery);

        if (c1 != null) {
            mReasonerManager.unregisterCSPARQLQuery(c1.getId());
        } else {
            okExit = false;
            mLogger.logError(DataLogger.REASONER, LOGTAG,
                    "weatherRainingAndColdQuery was not registered");
        }

        if (c2 != null) {
            mReasonerManager.unregisterCSPARQLQuery(c2.getId());
        } else {
            okExit = false;
            mLogger.logError(DataLogger.REASONER, LOGTAG, "weatherColdQuery was not registered");
        }

        if (c3 != null) {
            mReasonerManager.unregisterCSPARQLQuery(c3.getId());
        } else {
            okExit = false;
            mLogger.logError(DataLogger.REASONER, LOGTAG, "weatherRainingQuery was not registered");
        }

        if (c4 != null) {
            mReasonerManager.unregisterCSPARQLQuery(c4.getId());
        } else {
            okExit = false;
            mLogger.logError(DataLogger.REASONER, LOGTAG, "weatherHotQuery was not registered");
        }

        if (c5 != null) {
            mReasonerManager.unregisterCSPARQLQuery(c5.getId());
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

        CsparqlQueryResultProxy c1 = mReasonerManager.registerCSPARQLQuery(batteryLOWQuery);
        CsparqlQueryResultProxy c2 = mReasonerManager.registerCSPARQLQuery(batteryOkQuery);

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
            mReasonerManager.unregisterCSPARQLQuery(c1.getId());
        } else {
            okExit = false;
            mLogger.logError(DataLogger.REASONER, LOGTAG, "batteryLOWQuery was not registered");
        }

        if (c2 != null) {
            mReasonerManager.unregisterCSPARQLQuery(c2.getId());
        } else {
            okExit = false;
            mLogger.logError(DataLogger.REASONER, LOGTAG, "batteryOkQuery was not registered");
        }

        mReasonerCore.removeContextValue("BATTERY");
        mLogger.logVerbose(DataLogger.REASONER, LOGTAG, "Unregistered battery");

        return okExit;
    }
}
