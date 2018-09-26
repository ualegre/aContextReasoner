package edu.casetools.icase.custom.situations;

import android.content.SharedPreferences;

import java.util.Map;

import eu.larkc.csparql.core.engine.CsparqlQueryResultProxy;
import org.poseidon_project.context.reasoner.SituationOfInterest;
import org.poseidon_project.context.ContextReasonerCore;
import org.poseidon_project.context.logging.DataLogger;
import org.poseidon_project.context.reasoner.AbstractContextMapper;
import org.poseidon_project.context.reasoner.ReasonerManager;


/**
 * @author Dean Kramer <deankramer99@gmail.com> and Unai Alegre-Ibarra <u.alegre@mdx.ac.uk>
 */

public class OldWeatherSOI extends SituationOfInterest {

    public OldWeatherSOI() {
        super("oldweather");
    }

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

    public String getWeatherRainingAndColdQuery(){
        return  weatherRainingAndColdQuery;
    }

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

    public String getWeatherColdQuery(){
        return weatherColdQuery;
    }

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

    public String getWeatherHotQuery(){
        return  weatherHotQuery;
    }

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

    public String getWeatherRainingQuery(){
        return weatherRainingQuery;
    }

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

    public String getWeatherOkayQuery(){
        return weatherOkayQuery;
    }


    @Override
    public boolean registerSituationOfInterest(ReasonerManager mReasonerManager, AbstractContextMapper contextMapper, SharedPreferences mRuleSettings, DataLogger mLogger, String logTag, Map parameters) {
        boolean okExit = true;

        CsparqlQueryResultProxy c1 = mReasonerManager.registerCSPARQLQuery(getWeatherRainingAndColdQuery());
        CsparqlQueryResultProxy c2 = mReasonerManager.registerCSPARQLQuery(getWeatherColdQuery());
        CsparqlQueryResultProxy c3 = mReasonerManager.registerCSPARQLQuery(getWeatherRainingQuery());
        CsparqlQueryResultProxy c4 = mReasonerManager.registerCSPARQLQuery(getWeatherHotQuery());
        CsparqlQueryResultProxy c5 = mReasonerManager.registerCSPARQLQuery(getWeatherOkayQuery());

        okExit = contextMapper.registerModellingRule(getWeatherRainingAndColdQuery(), c1, okExit);
        okExit = contextMapper.registerModellingRule(getWeatherColdQuery(),           c2, okExit);
        okExit = contextMapper.registerModellingRule(getWeatherRainingQuery(),        c3, okExit);
        okExit = contextMapper.registerModellingRule(getWeatherHotQuery(),            c4, okExit);
        okExit = contextMapper.registerModellingRule(getWeatherOkayQuery(),           c5, okExit);

        mLogger.logVerbose(DataLogger.REASONER, logTag, "Registered weather");

        if (okExit) {
            return contextMapper.addObserverRequirementWithParameters("engine", "LocationWeatherContext",okExit, parameters);
        } else {
            return false;
        }

    }

    @Override
    public boolean unRegisterSituationOfInterest(ContextReasonerCore mReasonerCore, ReasonerManager mReasonerManager, AbstractContextMapper contextMapper, DataLogger mLogger, String logTag) {
        boolean okExit = contextMapper.removeObserverRequirement("engine", "LocationWeatherContext");

        okExit = contextMapper.unregisterModellingRule(getWeatherRainingAndColdQuery(), okExit);
        okExit = contextMapper.unregisterModellingRule(getWeatherColdQuery(),           okExit);
        okExit = contextMapper.unregisterModellingRule(getWeatherRainingQuery(),        okExit);
        okExit = contextMapper.unregisterModellingRule(getWeatherHotQuery(),            okExit);
        okExit = contextMapper.unregisterModellingRule(getWeatherOkayQuery(),           okExit);

        mReasonerCore.removeContextValue("WEATHER");

        mLogger.logVerbose(DataLogger.REASONER, logTag, "Unregistered weather");

        return okExit;
    }
}
