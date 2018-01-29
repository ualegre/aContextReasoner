package edu.casetools.icase.custom.situations;

import android.content.SharedPreferences;

import java.util.Map;

import org.poseidon_project.context.reasoner.SituationOfInterest;
import org.poseidon_project.context.ContextReasonerCore;
import org.poseidon_project.context.logging.DataLogger;
import org.poseidon_project.context.reasoner.AbstractContextMapper;
import org.poseidon_project.context.reasoner.ReasonerManager;

/**
 * @author Dean Kramer <deankramer99@gmail.com> and Unai Alegre-Ibarra <u.alegre@mdx.ac.uk>
 */

public class IndoorsOutdoorsSOI extends SituationOfInterest {

    public IndoorsOutdoorsSOI(String name) {
        super(name);
    }

    @Override
    public boolean registerSituationOfInterest(ReasonerManager mReasonerManager, AbstractContextMapper contextMapper, SharedPreferences mRuleSettings, DataLogger mLogger, String logTag, Map parameters) {
        mLogger.logVerbose(DataLogger.REASONER, logTag, "Registered GPSIndoorOutdoor");
        return contextMapper.addObserverRequirement("engine", "GPSIndoorOutdoorContext", true);
    }

    @Override
    public boolean unRegisterSituationOfInterest(ContextReasonerCore mReasonerCore, ReasonerManager mReasonerManager, AbstractContextMapper contextMapper, DataLogger mLogger, String logTag) {

        boolean okExit = contextMapper.removeObserverRequirement("engine", "GPSIndoorOutdoorContext");
        mReasonerCore.removeContextValue("INDOOROUTDOOR");
        mLogger.logVerbose(DataLogger.REASONER, logTag, "Unregistered GPSIndoorOutdoor");

        return okExit;
    }
}
