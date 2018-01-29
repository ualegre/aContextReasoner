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
 * @author Dean Kramer <deankramer99@gmail.com> and Unai Alegre-Ibarra <u.alegre@mdx.ac.uk>
 */

public class StandStillSOI extends SituationOfInterest {

    public StandStillSOI(String name) {
        super(name);
    }

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

    public String getIsStandstillForLongQuery(String maxTimeValue, String maxDistanceValue){
        String standStillLong = new String(isStandstillForLongQuery);
        standStillLong = standStillLong.replace("$$time", String.valueOf(maxTimeValue));
        return standStillLong.replace("$$tDistance", String.valueOf(maxDistanceValue));
    }

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

    public String getIsStandstillForShortQuery(String maxTimeValue, String maxDistanceValue){
        String standStillShort = new String(isStandstillForShortQuery);
        standStillShort = standStillShort.replace("$$time", String.valueOf(maxTimeValue));
        return standStillShort.replace("$$tDistance", String.valueOf(maxDistanceValue));
    }

    public boolean registerSituationOfInterest(ReasonerManager mReasonerManager, AbstractContextMapper contextMapper, SharedPreferences mRuleSettings, DataLogger mLogger, String logTag, Map parameters) {

        boolean okExit = true;

        int maxTime = mRuleSettings.getInt(Prefs.NAVASSIST_MAXWAIT, 5);
        int maxDistance = maxTime * 8;

        CsparqlQueryResultProxy c1 = mReasonerManager.registerCSPARQLQuery(getIsStandstillForLongQuery(String.valueOf(maxTime), String.valueOf(maxDistance)));
        CsparqlQueryResultProxy c2 = mReasonerManager.registerCSPARQLQuery(getIsStandstillForShortQuery(String.valueOf(maxTime), String.valueOf(maxDistance)));

        okExit = contextMapper.registerModellingRule("standstill_long",  c1, okExit);
        okExit = contextMapper.registerModellingRule("standstill_short", c2, okExit);
        okExit = contextMapper.addObserverRequirement("engine", "DistanceTravelledContext",okExit);

        mReasonerManager.registerContextPrefAssociation("standstill","pref_max_wait");

        mLogger.logVerbose(DataLogger.REASONER, logTag, "Registered standstill");
        return okExit;
    }

    public boolean unRegisterSituationOfInterest(ContextReasonerCore mReasonerCore, ReasonerManager mReasonerManager, AbstractContextMapper contextMapper, DataLogger mLogger, String logTag) {
        boolean okExit = contextMapper.removeObserverRequirement("engine", "DistanceTravelledContext");

        okExit = contextMapper.unregisterModellingRule("standstill_long", okExit );
        okExit = contextMapper.unregisterModellingRule("standstill_short", okExit );

        mReasonerCore.removeContextValue("STANDSTILL");
        mReasonerManager.unRegisterContextPrefAssociation("standstill", Prefs.NAVASSIST_MAXWAIT);
        mLogger.logVerbose(DataLogger.REASONER, logTag, "Unregistered standstill");

        return okExit;
    }

}
