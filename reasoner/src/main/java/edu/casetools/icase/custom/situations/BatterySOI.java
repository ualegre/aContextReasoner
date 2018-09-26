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

public class BatterySOI extends SituationOfInterest {

    public BatterySOI(){
        super("battery");
    }

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

    public String getBatteryLOWQuery(){
        return  batteryLOWQuery;
    }

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

    public String getBatteryOkQuery(){
        return batteryOkQuery;
    }

    @Override
    public boolean registerSituationOfInterest(ReasonerManager mReasonerManager, AbstractContextMapper contextMapper, SharedPreferences mRuleSettings, DataLogger mLogger, String logTag, Map parameters) {
        boolean okExit = true;

        CsparqlQueryResultProxy c1 = mReasonerManager.registerCSPARQLQuery(getBatteryLOWQuery());
        CsparqlQueryResultProxy c2 = mReasonerManager.registerCSPARQLQuery(getBatteryOkQuery());

        okExit = contextMapper.registerModellingRule(getBatteryLOWQuery(), c1, okExit);
        okExit = contextMapper.registerModellingRule(getBatteryOkQuery(),  c2, okExit);

        mLogger.logVerbose(DataLogger.REASONER, logTag, "Registered battery");

        return contextMapper.addObserverRequirement("engine","BatteryContext",okExit);
    }

    @Override
    public boolean unRegisterSituationOfInterest(ContextReasonerCore mReasonerCore, ReasonerManager mReasonerManager, AbstractContextMapper contextMapper, DataLogger mLogger, String logTag)  {
        boolean okExit = contextMapper.removeObserverRequirement("engine", "BatteryContext");

        okExit = contextMapper.unregisterModellingRule(getBatteryLOWQuery(),okExit);
        okExit = contextMapper.unregisterModellingRule(getBatteryOkQuery(),okExit);

        mReasonerCore.removeContextValue("BATTERY");
        mLogger.logVerbose(DataLogger.REASONER, logTag, "Unregistered battery");

        return okExit;
    }

}
