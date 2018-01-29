package edu.casetools.icase.custom.situations;

import android.content.SharedPreferences;

import java.util.Map;

import eu.larkc.csparql.core.engine.CsparqlQueryResultProxy;
import org.poseidon_project.context.reasoner.SituationOfInterest;
import org.poseidon_project.context.ContextReasonerCore;
import org.poseidon_project.context.logging.DataLogger;
import org.poseidon_project.context.reasoner.AbstractContextMapper;
import org.poseidon_project.context.reasoner.ReasonerManager;
import org.poseidon_project.context.utility.Prefs;
/**
 * @author Dean Kramer <deankramer99@gmail.com> and Unai Alegre-Ibarra <u.alegre@mdx.ac.uk>
 */

public class NavAssistanceSOI extends SituationOfInterest {

    public NavAssistanceSOI(String name) {
        super(name);
    }

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

    public String getNavigationAssistNeededQuery(String maxDevValue){
        String navAssNeeded = new String(navigationAssistNeededQuery);
        return navAssNeeded.replace("$$pref_max_dev", maxDevValue);
    }

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


    public String getNavigationAssistNotNeededQuery(String maxDevValue){
        String navAssNotNeeded = new String(navigationAssistNotNeededQuery);
        return navAssNotNeeded.replace("$$pref_max_dev", maxDevValue);
    }

    @Override
    public boolean registerSituationOfInterest(ReasonerManager mReasonerManager, AbstractContextMapper contextMapper, SharedPreferences mRuleSettings, DataLogger mLogger, String logTag, Map parameters) {
        boolean okExit = true;

        String maxDev = String.valueOf(mRuleSettings.getInt(Prefs.NAVASSIST_MAXDEV, 2));

        CsparqlQueryResultProxy c1 = mReasonerManager.registerCSPARQLQuery(getNavigationAssistNeededQuery(maxDev));
        CsparqlQueryResultProxy c2 = mReasonerManager.registerCSPARQLQuery(getNavigationAssistNotNeededQuery(maxDev));

        okExit = contextMapper.registerModellingRule("navAssNeeded", c1, okExit);
        okExit = contextMapper.registerModellingRule("navAssNotNeeded", c2, okExit);

        mReasonerManager.registerContextPrefAssociation("navassistance", Prefs.NAVASSIST_MAXDEV);

        mLogger.logVerbose(DataLogger.REASONER, logTag, "Registered navassistance");

        return okExit;
    }

    @Override
    public boolean unRegisterSituationOfInterest(ContextReasonerCore mReasonerCore, ReasonerManager mReasonerManager, AbstractContextMapper contextMapper, DataLogger mLogger, String logTag) {
        boolean okExit = true;

        okExit = contextMapper.unregisterModellingRule("navAssNeeded",okExit);
        okExit = contextMapper.unregisterModellingRule("navAssNotNeeded",okExit);

        mReasonerManager.unRegisterContextPrefAssociation("navassistance", Prefs.NAVASSIST_MAXDEV);
        mReasonerCore.removeContextValue("NAV");
        mLogger.logVerbose(DataLogger.REASONER, logTag, "Unregistered navassistance");

        return okExit;
    }


}
