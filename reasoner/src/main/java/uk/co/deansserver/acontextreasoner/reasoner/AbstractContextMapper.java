/*
 * Copyright 2015 POSEIDON Project
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
 * @author Dean Kramer <deankramer99@gmail.com> and Unai Alegre-Ibarra <u.alegre@mdx.ac.uk>
 */
public abstract class AbstractContextMapper {

    protected ContextReasonerCore mReasonerCore;
    protected ContextManager mContextManager;
    protected ReasonerManager mReasonerManager;
    protected HashMap<String, CsparqlQueryResultProxy> rules = new HashMap<>();
    protected static final String LOGTAG = "CustomContextMapper";
    protected DataLogger mLogger;
    protected static final String mStreamRoot = "http://poseidon-project.org/context-stream";
    protected SharedPreferences mRuleSettings;
    protected Context mContext;
    public HashMap<String, Map> mContextParameters;

    public AbstractContextMapper(ContextReasonerCore crc, ReasonerManager rm, Context con) {

        mReasonerCore       = crc;
        mContext            = con;
        mRuleSettings       = con.getSharedPreferences(Prefs.RULE_PREFS, 0);
        mLogger             = crc.getLogger();
        mContextManager     = crc.getContextManager();
        mReasonerManager    = rm;
        mContextParameters  = new HashMap<>();

        //checkForDefaultContextPrefs();

    }

    public abstract boolean registerCustomContextRules(String context, Map parameters);
    public abstract boolean unregisterCustomContextRules(String context, Map parameters);

    private void checkForDefaultContextPrefs() {

    }

    public boolean registerContext(String context, Map parameters) {

        if (mContextManager == null)
            mContextManager = mReasonerCore.getContextManager();
        return registerCustomContextRules(context, parameters);
    }

    public boolean unregisterContext(String context, Map parameters) {

        boolean result = unregisterCustomContextRules(context, parameters);
        mLogger.logError(DataLogger.REASONER, LOGTAG, "Context: " + context + " not found!");

        return result;
    }

    protected boolean registerModellingRule(String queryName, CsparqlQueryResultProxy queryResult, boolean okExit) {
        if (queryResult != null) {
            rules.put(queryName, queryResult);
        } else {
            okExit = false;
            mLogger.logError(DataLogger.REASONER, LOGTAG, queryName+"Query could't register");
        }
        return okExit;
    }

    protected boolean unregisterModellingRule(String queryName, CsparqlQueryResultProxy queryResult, boolean okExit) {
        if (queryResult != null) {
            mReasonerManager.unregisterCSPARQLQuery(queryResult.getId());
        } else {
            okExit = false;
            mLogger.logError(DataLogger.REASONER, LOGTAG,
                    queryName+"Query was not registered");
        }
        return okExit;
    }

    protected boolean addObserverRequirement(String appkey, String observerName, boolean okExit) {
        if (okExit)
            return mContextManager.addObserverRequirement(appkey, observerName);
        else
            return false;
    }

    protected boolean addObserverRequirementWithParameters(String appkey, String observerName, boolean okExit, Map parameters) {
        if (okExit)
            return mContextManager.addObserverRequirementWithParameters(appkey, observerName, parameters);
        else
            return false;
    }

}
