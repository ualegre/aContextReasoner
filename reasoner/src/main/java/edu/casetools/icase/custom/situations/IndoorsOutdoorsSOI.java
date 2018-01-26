package edu.casetools.icase.custom.situations;

import android.content.SharedPreferences;

import java.util.Map;

import uk.co.deansserver.acontextreasoner.reasoner.SituationOfInterest;
import uk.co.deansserver.acontextreasoner.ContextReasonerCore;
import uk.co.deansserver.acontextreasoner.logging.DataLogger;
import uk.co.deansserver.acontextreasoner.reasoner.AbstractContextMapper;
import uk.co.deansserver.acontextreasoner.reasoner.ReasonerManager;

/**
 * Created by Unai on 26/01/2018.
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

        boolean okExit = contextMapper.removeObserverRequirement("engine", "GPSIndoorOutdoorContext",true);
        mReasonerCore.removeContextValue("INDOOROUTDOOR");
        mLogger.logVerbose(DataLogger.REASONER, logTag, "Unregistered GPSIndoorOutdoor");

        return okExit;
    }
}
