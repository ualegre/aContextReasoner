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

package edu.casetools.icase.custom;

import android.content.Context;

import uk.co.deansserver.acontextreasoner.ContextReasonerCore;
import uk.co.deansserver.acontextreasoner.logging.DataLogger;
import uk.co.deansserver.acontextreasoner.reasoner.AbstractContextMapper;
import uk.co.deansserver.acontextreasoner.reasoner.ReasonerManager;
import uk.co.deansserver.acontextreasoner.utility.Prefs;

import java.util.Map;

import eu.larkc.csparql.core.engine.CsparqlQueryResultProxy;

/**
 * Temporary class for mapping contexts to components and rules for pilot 1
 *
 * @author Dean Kramer <deankramer99@gmail.com> and Unai Alegre-Ibarra <u.alegre@mdx.ac.uk>
 */
public class CustomContextMapper extends AbstractContextMapper {

    private CustomModellingRules customModellingRules;
    private CustomReasoningRules customReasoningRules;

    public CustomContextMapper(ContextReasonerCore crc, ReasonerManager rm, Context con) {
        super(crc,rm,con);
        customModellingRules = new CustomModellingRules();
        customReasoningRules = new CustomReasoningRules();
    }

    @Override
    public boolean registerCustomContextRules(String context, Map parameters) {
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

    @Override
    public boolean unregisterCustomContextRules(String context, Map parameters) {
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
        return false;
    }

    private boolean registerWeather(Map parameters) {

        boolean okExit = true;

        String coldTemp = String.valueOf(mRuleSettings.getInt(Prefs.WEATHER_COLD, 15));
        String hotTemp     = String.valueOf(mRuleSettings.getInt(Prefs.WEATHER_HOT, 25));

        CsparqlQueryResultProxy c1 = mReasonerManager.registerCSPARQLQuery(customModellingRules.getColdQuery(coldTemp));
        CsparqlQueryResultProxy c2 = mReasonerManager.registerCSPARQLQuery(customModellingRules.getTempHotQuery(hotTemp));
        CsparqlQueryResultProxy c3 = mReasonerManager.registerCSPARQLQuery(customModellingRules.getTempOkQuery(hotTemp,coldTemp));
        CsparqlQueryResultProxy c4 = mReasonerManager.registerCSPARQLQuery(customModellingRules.getPrecipRain());
        CsparqlQueryResultProxy c5 = mReasonerManager.registerCSPARQLQuery(customModellingRules.getPrecipDry());

        okExit = registerModellingRule("tempCold", c1, okExit);
        okExit = registerModellingRule("tempHot",  c2, okExit);
        okExit = registerModellingRule("tempOkay", c3, okExit);
        okExit = registerModellingRule(customModellingRules.getPrecipRain(), c4, okExit);
        okExit = registerModellingRule(customModellingRules.getPrecipDry(),  c5, okExit);

        mReasonerManager.registerReasoningRule("weatherokay",           customReasoningRules.getNewWeatherOKAY());
        mReasonerManager.registerReasoningRule("weatherraining",        customReasoningRules.getNewWeatherRAINING());
        mReasonerManager.registerReasoningRule("weatherrainingandcold", customReasoningRules.getNewWeatherCOLDANDRAINING());
        mReasonerManager.registerReasoningRule("weathercold",           customReasoningRules.getNewWeatherCOLD());
        mReasonerManager.registerReasoningRule("weatherhot",            customReasoningRules.getNewWeatherHOT());

        mReasonerManager.registerContextPrefAssociation("weather", Prefs.WEATHER_COLD);
        mReasonerManager.registerContextPrefAssociation("weather", Prefs.WEATHER_HOT);

        mLogger.logVerbose(DataLogger.REASONER, LOGTAG, "Registered weather");

        return this.addObserverRequirementWithParameters("engine", "LocationWeatherContext",okExit, parameters);

    }

    private boolean registerAgg() {

        mReasonerManager.registerReasoningRule("agg", customReasoningRules.getNewTEMPCOLD());
        mReasonerManager.fireAggregateRules("TEMP_COLD");

        return true;
    }

    private boolean unRegisterWeatherContext() {

        boolean okExit = mContextManager.removeObserverRequirement("engine", "LocationWeatherContext");

        CsparqlQueryResultProxy c1 = rules.remove("tempOkay");
        CsparqlQueryResultProxy c2 = rules.remove("tempCold");
        CsparqlQueryResultProxy c3 = rules.remove("tempHot");
        CsparqlQueryResultProxy c4 = rules.remove(customModellingRules.getPrecipRain());
        CsparqlQueryResultProxy c5 = rules.remove(customModellingRules.getPrecipDry());

        okExit = this.unregisterModellingRule("tempOkay",   c1, okExit);
        okExit = this.unregisterModellingRule("tempCold",   c2, okExit);
        okExit = this.unregisterModellingRule("tempHot",    c3, okExit);
        okExit = this.unregisterModellingRule("precipDry",  c4, okExit);
        okExit = this.unregisterModellingRule("precipRain", c5, okExit);

        mReasonerManager.unregisterReasoningRule("weatherokay");
        mReasonerManager.unregisterReasoningRule("weatherraining");
        mReasonerManager.unregisterReasoningRule("weatherrainingandcold");
        mReasonerManager.unregisterReasoningRule("weathercold");
        mReasonerManager.unregisterReasoningRule("weatherhot");

        mReasonerCore.removeContextValue("WEATHER");
        mReasonerCore.removeContextValue("TEMP");
        mReasonerCore.removeContextValue("PRECIP");

        mReasonerManager.unRegisterContextPrefAssociation("weather", Prefs.WEATHER_COLD);
        mReasonerManager.unRegisterContextPrefAssociation("weather", Prefs.WEATHER_HOT);

        mLogger.logVerbose(DataLogger.REASONER, LOGTAG, "Unregistered weather");

        return okExit;
    }


    private boolean registerStandstill() {

        boolean okExit = true;

        int maxTime = mRuleSettings.getInt(Prefs.NAVASSIST_MAXWAIT, 5);
        int maxDistance = maxTime * 8;

        CsparqlQueryResultProxy c1 = mReasonerManager.registerCSPARQLQuery(customModellingRules.getIsStandstillForLongQuery(String.valueOf(maxTime), String.valueOf(maxDistance)));
        CsparqlQueryResultProxy c2 = mReasonerManager.registerCSPARQLQuery(customModellingRules.getIsStandstillForShortQuery(String.valueOf(maxTime), String.valueOf(maxDistance)));

        okExit = this.registerModellingRule("standstill_long",  c1, okExit);
        okExit = this.registerModellingRule("standstill_short", c2, okExit);

        okExit = this.addObserverRequirement("engine", "DistanceTravelledContext",okExit);

        mReasonerManager.registerContextPrefAssociation("standstill","pref_max_wait");

        mLogger.logVerbose(DataLogger.REASONER, LOGTAG, "Registered standstill");
        return okExit;
    }

    private boolean unRegisterStandstill() {
        boolean okExit = true;

        mContextManager.removeObserverRequirement("engine", "DistanceTravelledContext");

        CsparqlQueryResultProxy c1 = rules.remove("standstill_long");
        CsparqlQueryResultProxy c2 = rules.remove("standstill_short");

        okExit = this.unregisterModellingRule("isStandstillForLong",  c1, okExit );
        okExit = this.unregisterModellingRule("isStandstillForShort", c2, okExit );

        mReasonerCore.removeContextValue("STANDSTILL");
        mReasonerManager.unRegisterContextPrefAssociation("standstill", Prefs.NAVASSIST_MAXWAIT);
        mLogger.logVerbose(DataLogger.REASONER, LOGTAG, "Unregistered standstill");

        return okExit;
    }


    private boolean registerNavAssistance() {
        boolean okExit = true;

        String maxDev = String.valueOf(mRuleSettings.getInt(Prefs.NAVASSIST_MAXDEV, 2));

        CsparqlQueryResultProxy c1 = mReasonerManager.registerCSPARQLQuery(customModellingRules.getNavigationAssistNeededQuery(maxDev));
        CsparqlQueryResultProxy c2 = mReasonerManager.registerCSPARQLQuery(customModellingRules.getNavigationAssistNotNeededQuery(maxDev));

        okExit = this.registerModellingRule("navAssNeeded", c1, okExit);
        okExit = this.registerModellingRule("navAssNotNeeded", c2, okExit);

        mReasonerManager.registerContextPrefAssociation("navassistance", Prefs.NAVASSIST_MAXDEV);

        mLogger.logVerbose(DataLogger.REASONER, LOGTAG, "Registered navassistance");

        return okExit;
    }

    private boolean unRegisterNavAssistance() {
        boolean okExit = true;

        CsparqlQueryResultProxy c1 = rules.remove("navAssNeeded");
        CsparqlQueryResultProxy c2 = rules.remove("navAssNotNeeded");

        okExit = this.unregisterModellingRule("navAssNeeded",c1,okExit);
        okExit = this.unregisterModellingRule("navAssNotNeeded",c2,okExit);

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

        CsparqlQueryResultProxy c1 = mReasonerManager.registerCSPARQLQuery(customModellingRules.getWeatherRainingAndColdQuery());
        CsparqlQueryResultProxy c2 = mReasonerManager.registerCSPARQLQuery(customModellingRules.getWeatherColdQuery());
        CsparqlQueryResultProxy c3 = mReasonerManager.registerCSPARQLQuery(customModellingRules.getWeatherRainingQuery());
        CsparqlQueryResultProxy c4 = mReasonerManager.registerCSPARQLQuery(customModellingRules.getWeatherHotQuery());
        CsparqlQueryResultProxy c5 = mReasonerManager.registerCSPARQLQuery(customModellingRules.getWeatherOkayQuery());

        okExit = this.registerModellingRule(customModellingRules.getWeatherRainingAndColdQuery(), c1, okExit);
        okExit = this.registerModellingRule(customModellingRules.getWeatherColdQuery(), c2, okExit);
        okExit = this.registerModellingRule(customModellingRules.getWeatherRainingQuery(), c3, okExit);
        okExit = this.registerModellingRule(customModellingRules.getWeatherHotQuery(), c4, okExit);
        okExit = this.registerModellingRule(customModellingRules.getWeatherOkayQuery(), c5, okExit);

        mLogger.logVerbose(DataLogger.REASONER, LOGTAG, "Registered weather");

        if (okExit) {
            return mContextManager.addObserverRequirementWithParameters("engine", "LocationWeatherContext", parameters);
        } else {
            return false;
        }

    }

    private boolean unRegisterOldWeatherContext() {

        boolean okExit = mContextManager.removeObserverRequirement("engine", "LocationWeatherContext");

        CsparqlQueryResultProxy c1 = rules.remove(customModellingRules.getWeatherRainingAndColdQuery());
        CsparqlQueryResultProxy c2 = rules.remove(customModellingRules.getWeatherColdQuery());
        CsparqlQueryResultProxy c3 = rules.remove(customModellingRules.getWeatherRainingQuery());
        CsparqlQueryResultProxy c4 = rules.remove(customModellingRules.getWeatherHotQuery());
        CsparqlQueryResultProxy c5 = rules.remove(customModellingRules.getWeatherOkayQuery());

        okExit = this.unregisterModellingRule("weatherRainingAndCold", c1, okExit);
        okExit = this.unregisterModellingRule("weatherCold",           c2, okExit);
        okExit = this.unregisterModellingRule("weatherRaining",        c3, okExit);
        okExit = this.unregisterModellingRule("weatherHot",            c4, okExit);
        okExit = this.unregisterModellingRule("weatherOkay",           c5, okExit);

        mReasonerCore.removeContextValue("WEATHER");

        mLogger.logVerbose(DataLogger.REASONER, LOGTAG, "Unregistered weather");

        return okExit;
    }

    public boolean registerBatteryContext() {
        boolean okExit = true;

        CsparqlQueryResultProxy c1 = mReasonerManager.registerCSPARQLQuery(customModellingRules.getBatteryLOWQuery());
        CsparqlQueryResultProxy c2 = mReasonerManager.registerCSPARQLQuery(customModellingRules.getBatteryOkQuery());

        okExit = this.registerModellingRule(customModellingRules.getBatteryLOWQuery(), c1, okExit);
        okExit = this.registerModellingRule(customModellingRules.getBatteryOkQuery(),  c2, okExit);

        mLogger.logVerbose(DataLogger.REASONER, LOGTAG, "Registered battery");

        return this.addObserverRequirement("engine","BatteryContext",okExit);
    }

    public boolean unRegisterBatteryContext() {

        boolean okExit = mContextManager.removeObserverRequirement("engine", "BatteryContext");

        CsparqlQueryResultProxy c1 = rules.remove(customModellingRules.getBatteryLOWQuery());
        CsparqlQueryResultProxy c2 = rules.remove(customModellingRules.getBatteryOkQuery());

        okExit = this.unregisterModellingRule("batteryLOW",c1,okExit);
        okExit = this.unregisterModellingRule("batteryOk",c2,okExit);

        mReasonerCore.removeContextValue("BATTERY");
        mLogger.logVerbose(DataLogger.REASONER, LOGTAG, "Unregistered battery");

        return okExit;
    }
}
