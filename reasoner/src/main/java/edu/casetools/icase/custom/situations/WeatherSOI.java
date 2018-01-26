package edu.casetools.icase.custom.situations;

import android.content.SharedPreferences;

import java.util.Map;

import uk.co.deansserver.acontextreasoner.reasoner.SituationOfInterest;
import eu.larkc.csparql.core.engine.CsparqlQueryResultProxy;
import uk.co.deansserver.acontextreasoner.ContextReasonerCore;
import uk.co.deansserver.acontextreasoner.logging.DataLogger;
import uk.co.deansserver.acontextreasoner.reasoner.AbstractContextMapper;
import uk.co.deansserver.acontextreasoner.reasoner.ReasonerManager;
import uk.co.deansserver.acontextreasoner.utility.Prefs;


/**
 * Created by Unai on 26/01/2018.
 */

public class WeatherSOI extends SituationOfInterest {

    public WeatherSOI(String name){
        super(name);
    }

    // Modelling Rules
    private static final String tempCold =
            "REGISTER QUERY tempContextIsCold AS "
                    + "PREFIX ex: <http://ie.cs.mdx.ac.uk/POSEIDON/envir#> "
                    + "CONSTRUCT { ex:temp <http://ie.cs.mdx.ac.uk/POSEIDON/context/is> \"TEMP_COLD\" } "
                    + "FROM STREAM <http://poseidon-project.org/context-stream> [RANGE 10s STEP 4s] "
                    + "WHERE { ?m ex:hasTemperatureValue ?tempValueIRI . "
                    + "?tempValueIRI ex:temperatureValue ?tempValue . "
                    + "FILTER (?tempValue < $$pref_cold) "
                    + "}";
    public String getColdQuery(String coldValue) {
        String cold = new String(tempCold);
        return cold.replace("$$pref_cold", String.valueOf(coldValue));
    }

    private static final String tempHot =
            "REGISTER QUERY tempContextIsOkay AS "
                    + "PREFIX ex: <http://ie.cs.mdx.ac.uk/POSEIDON/envir#> "
                    + "CONSTRUCT { ex:temp <http://ie.cs.mdx.ac.uk/POSEIDON/context/is> \"TEMP_HOT\" } "
                    + "FROM STREAM <http://poseidon-project.org/context-stream> [RANGE 10s STEP 4s] "
                    + "WHERE { ?m ex:hasTemperatureValue ?tempValueIRI . "
                    + "?tempValueIRI ex:temperatureValue ?tempValue . "
                    + "FILTER (?tempValue >= $$pref_hot) "
                    + "}";
    public String getTempHotQuery(String tempHotValue){
        String hot = new String(tempHot);
        return hot.replace("$$pref_hot", String.valueOf(tempHotValue));
    }

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
    public String getTempOkQuery(String hotTempValue, String coldTempValue){
        String ok = new String(tempOkay);
        ok = ok.replace("$$pref_hot", String.valueOf(hotTempValue));
        return ok.replace("$$pref_cold", String.valueOf(coldTempValue));
    }

    private static final String precipRain =
            "REGISTER QUERY precipContextIsRaining AS "
                    + "PREFIX ex: <http://ie.cs.mdx.ac.uk/POSEIDON/envir#> "
                    + "CONSTRUCT { ex:temp <http://ie.cs.mdx.ac.uk/POSEIDON/context/is> \"PRECIP_RAINING\" } "
                    + "FROM STREAM <http://poseidon-project.org/context-stream> [RANGE 10s STEP 4s] "
                    + "WHERE { ?m ex:hasPrecipitationValue ?precipValueIRI . "
                    + "?precipValueIRI ex:precipitationValue ?precipValue . "
                    + "FILTER (?precipValue >= 0.1) "
                    + "}";
    public String getPrecipRain(){
        return precipRain;
    }

    private static final String precipDry =
            "REGISTER QUERY precipContextIsDry AS "
                    + "PREFIX ex: <http://ie.cs.mdx.ac.uk/POSEIDON/envir#> "
                    + "CONSTRUCT { ex:temp <http://ie.cs.mdx.ac.uk/POSEIDON/context/is> \"PRECIP_DRY\" } "
                    + "FROM STREAM <http://poseidon-project.org/context-stream> [RANGE 10s STEP 4s] "
                    + "WHERE { ?m ex:hasPrecipitationValue ?precipValueIRI . "
                    + "?precipValueIRI ex:precipitationValue ?precipValue . "
                    + "FILTER (?precipValue < 0.1) "
                    + "}";
    public String getPrecipDry(){
        return  precipDry;
    }

    //Reasoning rules
    private static final String newWeatherOKAY           = "TEMP_OKAY and PRECIP_DRY iff WEATHER_OKAY";
    public String getNewWeatherOKAY(){
        return newWeatherOKAY;
    }

    private static final String newWeatherRAINING        = "TEMP_OKAY and PRECIP_RAINING iff WEATHER_RAINING";
    public String getNewWeatherRAINING(){
        return newWeatherRAINING;
    }

    private static final String newWeatherCOLD           = "TEMP_COLD and PRECIP_DRY iff WEATHER_COLD";
    public String getNewWeatherCOLD(){
        return newWeatherCOLD;
    }

    private static final String newWeatherCOLDANDRAINING = "TEMP_COLD and PRECIP_RAINING iff WEATHER_RAININGANDCOLD";
    public String getNewWeatherCOLDANDRAINING(){ return newWeatherCOLDANDRAINING; }

    private static final String newWeatherHOT            = "TEMP_HOT and PRECIP_DRY iff WEATHER_HOT";
    public String getNewWeatherHOT(){
        return newWeatherHOT;
    }

    private static final String newTEMPCOLD              = "TEMP_COLD[06:00:00-21:00:00] and PRECIP_RAIN[#06:06:00-11:00:00] iff WEATHER_MISERABLE";
    public String getNewTEMPCOLD(){
        return newTEMPCOLD;
    }


    @Override
    public boolean registerSituationOfInterest(ReasonerManager mReasonerManager, AbstractContextMapper contextMapper, SharedPreferences mRuleSettings, DataLogger mLogger, String logTag, Map parameters){    boolean okExit = true;

        String coldTemp = String.valueOf(mRuleSettings.getInt(Prefs.WEATHER_COLD, 15));
        String hotTemp     = String.valueOf(mRuleSettings.getInt(Prefs.WEATHER_HOT, 25));

        CsparqlQueryResultProxy c1 = mReasonerManager.registerCSPARQLQuery(getColdQuery(coldTemp));
        CsparqlQueryResultProxy c2 = mReasonerManager.registerCSPARQLQuery(getTempHotQuery(hotTemp));
        CsparqlQueryResultProxy c3 = mReasonerManager.registerCSPARQLQuery(getTempOkQuery(hotTemp,coldTemp));
        CsparqlQueryResultProxy c4 = mReasonerManager.registerCSPARQLQuery(getPrecipRain());
        CsparqlQueryResultProxy c5 = mReasonerManager.registerCSPARQLQuery(getPrecipDry());

        okExit = contextMapper.registerModellingRule("tempCold", c1, okExit);
        okExit = contextMapper.registerModellingRule("tempHot",  c2, okExit);
        okExit = contextMapper.registerModellingRule("tempOkay", c3, okExit);
        okExit = contextMapper.registerModellingRule(getPrecipRain(), c4, okExit);
        okExit = contextMapper.registerModellingRule(getPrecipDry(),  c5, okExit);

        mReasonerManager.registerReasoningRule("weatherokay",           getNewWeatherOKAY());
        mReasonerManager.registerReasoningRule("weatherraining",        getNewWeatherRAINING());
        mReasonerManager.registerReasoningRule("weatherrainingandcold", getNewWeatherCOLDANDRAINING());
        mReasonerManager.registerReasoningRule("weathercold",           getNewWeatherCOLD());
        mReasonerManager.registerReasoningRule("weatherhot",            getNewWeatherHOT());
        mReasonerManager.registerReasoningRule("agg",                   getNewTEMPCOLD());

        mReasonerManager.fireReasoningRules("TEMP_COLD");

        mReasonerManager.registerContextPrefAssociation("weather", Prefs.WEATHER_COLD);
        mReasonerManager.registerContextPrefAssociation("weather", Prefs.WEATHER_HOT);

        mLogger.logVerbose(DataLogger.REASONER, logTag, "Registered weather");

        return contextMapper.addObserverRequirementWithParameters("engine", "LocationWeatherContext",okExit, parameters);
    }

    @Override
    public boolean unRegisterSituationOfInterest(ContextReasonerCore mReasonerCore, ReasonerManager mReasonerManager, AbstractContextMapper contextMapper, DataLogger mLogger, String logTag) {
        boolean okExit = true;
        okExit = contextMapper.removeObserverRequirement("engine", "LocationWeatherContext", okExit);

        okExit = contextMapper.unregisterModellingRule("tempOkay",   okExit);
        okExit = contextMapper.unregisterModellingRule("tempCold",   okExit);
        okExit = contextMapper.unregisterModellingRule("tempHot",    okExit);
        okExit = contextMapper.unregisterModellingRule(getPrecipRain(),  okExit);
        okExit = contextMapper.unregisterModellingRule(getPrecipDry(), okExit);

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

        mLogger.logVerbose(DataLogger.REASONER, logTag, "Unregistered weather");

        return okExit;
    }

}
