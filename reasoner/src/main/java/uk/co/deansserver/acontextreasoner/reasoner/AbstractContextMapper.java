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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import eu.larkc.csparql.core.engine.CsparqlQueryResultProxy;

/**
 * Temporary class for mapping contexts to components and rules for pilot 1
 *
 * @author Dean Kramer <deankramer99@gmail.com> and Unai Alegre-Ibarra <u.alegre@mdx.ac.uk>
 */
public abstract class AbstractContextMapper {

    public    HashMap<String, Map>                      mContextParameters;
    protected ContextReasonerCore                       mReasonerCore;
    protected ContextManager                            mContextManager;
    protected ReasonerManager                           mReasonerManager;
    protected HashMap<String, CsparqlQueryResultProxy>  rules = new HashMap<>();
    protected DataLogger                                mLogger;
    protected SharedPreferences                         mRuleSettings;
    protected Context                                   mContext;
    protected ArrayList<SituationOfInterest>            situationsOfInterest;
    protected String                                    contextMapperName;

    public AbstractContextMapper(String contextMapperName, ContextReasonerCore crc, ReasonerManager rm, Context con) {
        this.contextMapperName = contextMapperName;
        mContextParameters  = new HashMap<>();
        mReasonerCore       = crc;
        mContext            = con;
        mRuleSettings       = con.getSharedPreferences(Prefs.RULE_PREFS, 0);
        mLogger             = crc.getLogger();
        mContextManager     = crc.getContextManager();
        mReasonerManager    = rm;
        situationsOfInterest = new ArrayList<>();

        //checkForDefaultContextPrefs();
    }

    private void checkForDefaultContextPrefs() {

    }

    public boolean registerContext(String context, Map parameters) {

        if (mContextManager == null)
            mContextManager = mReasonerCore.getContextManager();

        for(SituationOfInterest soi : situationsOfInterest){
            if(context.equals(soi.getName()))
                return soi.registerSituationOfInterest(mReasonerManager,this, mRuleSettings, mLogger, contextMapperName, parameters);
        }

        mLogger.logError(DataLogger.REASONER, contextMapperName, "Context: " + context + " not found!");
        return false;
    }

    public boolean unregisterContext(String context, Map parameters) {

        for(SituationOfInterest soi : situationsOfInterest){
            if(context.equals(soi.getName()))
                return soi.unRegisterSituationOfInterest(mReasonerCore,mReasonerManager,this, mLogger, contextMapperName);
        }

        mLogger.logError(DataLogger.REASONER, contextMapperName, "Context: " + context + " not found!");

        return false;
    }

    public boolean registerModellingRule(String queryName, CsparqlQueryResultProxy queryResult, boolean okExit) {
        if (queryResult != null) {
            rules.put(queryName, queryResult);
        } else {
            okExit = false;
            mLogger.logError(DataLogger.REASONER, contextMapperName, queryName+"Query could't register");
        }
        return okExit;
    }

    public boolean unregisterModellingRule(String queryName, boolean okExit) {

        CsparqlQueryResultProxy queryResult = rules.remove(queryName);
        if (queryResult != null) {
            mReasonerManager.unregisterCSPARQLQuery(queryResult.getId());
        } else {
            okExit = false;
            mLogger.logError(DataLogger.REASONER, contextMapperName,
                    queryName+"Query was not registered");
        }
        return okExit;
    }

    public boolean addObserverRequirement(String appkey, String observerName, boolean okExit) {
        if (okExit)
            return mContextManager.addObserverRequirement(appkey, observerName);
        else
            return false;
    }

    public boolean addObserverRequirementWithParameters(String appkey, String observerName, boolean okExit, Map parameters) {
        if (okExit)
            return mContextManager.addObserverRequirementWithParameters(appkey, observerName, parameters);
        else
            return false;
    }

}
