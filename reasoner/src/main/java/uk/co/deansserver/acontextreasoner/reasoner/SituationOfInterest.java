package uk.co.deansserver.acontextreasoner.reasoner;

import java.util.Map;

import uk.co.deansserver.acontextreasoner.ContextReasonerCore;
import uk.co.deansserver.acontextreasoner.logging.DataLogger;
import uk.co.deansserver.acontextreasoner.reasoner.AbstractContextMapper;
import uk.co.deansserver.acontextreasoner.reasoner.ReasonerManager;

import android.content.SharedPreferences;
/**
 * Created by Unai Alegre-Ibarra <u.alegre@mdx.ac.uk> 26/01/2018.
 */

public abstract class SituationOfInterest {
    private String name;

    public String getName() {
        return name;
    }

    public abstract boolean registerSituationOfInterest(ReasonerManager mReasonerManager, AbstractContextMapper contextMapper, SharedPreferences mRuleSettings, DataLogger mLogger, String logTag, Map parameters);
    public abstract boolean unRegisterSituationOfInterest(ContextReasonerCore mReasonerCore, ReasonerManager mReasonerManager, AbstractContextMapper contextMapper, DataLogger mLogger, String logTag);

    public void setName(String name) {
        this.name = name;
    }

    public SituationOfInterest(String name){
        this.name = name;

    }
}
