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

package org.poseidon_project.context.reasoner;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.poseidon_project.context.ContextReasonerCore;
import org.poseidon_project.context.database.ContextDB;
import org.poseidon_project.context.database.ContextResult;
import org.poseidon_project.context.logging.DataLogger;
import org.poseidon_project.context.management.PreferenceSyncClient;
import org.poseidon_project.context.management.TelluSyncClient;
import org.poseidon_project.context.utility.Prefs;
import org.prop4j.Literal;
import org.prop4j.Node;
import org.prop4j.SatSolver;
import org.sat4j.specs.TimeoutException;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import eu.larkc.csparql.core.engine.CsparqlEngine;
import eu.larkc.csparql.core.engine.CsparqlEngineImpl;
import eu.larkc.csparql.core.engine.CsparqlQueryResultProxy;
import uk.ac.mdx.cs.ie.acontextlib.IReasonerManager;

/**
 * The main reasoning management logic
 *
 * @author Dean Kramer <d.kramer@mdx.ac.uk>
 */
public class ReasonerManager implements IReasonerManager{

    private long TIMEOUT = 5000;
    private Context mContext;
    private static final String LOGTAG  = "ReasonerManager";
    private ContextReasonerCore mReasonerCore;
    private CsparqlEngine mCsparqlEngine;
    private ContextStream mContextStream;
    private HashMap<String, ContextStream> mContextStreams;
    private ContextRuleObserver mContextRuleObserver;
    private DataLogger mLogger;
    //Only required for the pilot until the main infrastructure is done.
    public ContextMapper pilotMapper;
    private HashMap<String, AggregateRule> mAggregateRules;
    private ContextDB mContextDatabase;
    private HashMap<String,HashSet<String>> mCurrentPrefContexts;
    private SharedPreferences mContextSettings;
    private Timer mSyncTimer;
    private static final int PREF_SYNC_INTERVAL = 600000;
    private TelluSyncClient mSyncClient;
    private List<String> mUpdatedContextNames = new ArrayList<>();
    private List<String> mUpdatedContextValues = new ArrayList<>();

    public ReasonerManager(Context context, ContextReasonerCore core, ContextDB db){
        mContext = context;
        mReasonerCore = core;
        mLogger = core.getLogger();
        mAggregateRules = new HashMap<>();
        mContextDatabase = db;

        startCSPARQL();

        pilotMapper = new ContextMapper(mReasonerCore, this, mContext);
        mCurrentPrefContexts = new HashMap<>();
        mContextSettings = mContext.getSharedPreferences(Prefs.RULE_PREFS, 0);
        Prefs.setupFirstTime(mContext);
        setupPrefSyncClients();

        //Not a completely bad idea to do a GC after loading everything
        System.gc();
    }

    private void setupPrefSyncClients() {

        mSyncClient = new TelluSyncClient(this, mContextSettings);
        mSyncClient.authenticate();

        mSyncTimer = new Timer();

        mSyncTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mSyncClient.synchronisePreferences(new PreferenceSyncClient.Callback() {
                    @Override
                    public void onSuccessful() {
                        Log.v(LOGTAG, "Successful Sync with Tellu");
                    }

                    @Override
                    public void onFail(String error) {
                        Log.e(LOGTAG, error);
                    }
                });
            }
        }, System.currentTimeMillis(), PREF_SYNC_INTERVAL);
    }


    private void startCSPARQL() {

        mCsparqlEngine = new CsparqlEngineImpl();

        mCsparqlEngine.initialize(true);
        mContextStream = new ContextStream("http://poseidon-project.org/context-stream");

        mCsparqlEngine.registerStream(mContextStream);
        mContextRuleObserver = new ContextRuleObserver(mReasonerCore);

        mContextStreams = new HashMap<>();
    }

    public CsparqlQueryResultProxy registerCSPARQLQuery(String query) {

        CsparqlQueryResultProxy queryResultProxy = null;
        try {
            queryResultProxy = mCsparqlEngine.registerQuery(query, true);
            queryResultProxy.addObserver(mContextRuleObserver);

        } catch (final ParseException e) {
            mLogger.logError(DataLogger.SYSTEM_CORE, LOGTAG, "Cannot parse: " + query);
            Log.e(LOGTAG, "Error Parsing: " + e.getMessage());
        }

        return queryResultProxy;
    }

    public void registerContextPrefAssociation(String context, String prefname) {

        HashSet<String> contexts = mCurrentPrefContexts.get(prefname);

        if (contexts == null) {
            contexts = new HashSet<>();
        }

        contexts.add(context);

        mCurrentPrefContexts.put(prefname, contexts);
    }

    public void unRegisterContextPrefAssociation(String context, String prefname) {

        HashSet<String> contexts = mCurrentPrefContexts.get(prefname);

        if (contexts == null){
            mLogger.logVerbose(DataLogger.REASONER, "Context Prefs name not being used, ignoring");
            return;
        }

        if (contexts.remove(context)) {
            if (contexts.isEmpty()) {
                mCurrentPrefContexts.remove(prefname);
            }
        } else {
            mLogger.logVerbose(DataLogger.REASONER, "Context Association does not exist, ignoring");
        }
    }

    public CsparqlQueryResultProxy registerCSPARQLQuery(String stream, String query) {

        ContextStream cs = new ContextStream(stream);
        mCsparqlEngine.registerStream(cs);

        mContextStreams.put(stream, cs);

        CsparqlQueryResultProxy queryResultProxy = null;
        try {
            queryResultProxy = mCsparqlEngine.registerQuery(query, true);
            queryResultProxy.addObserver(mContextRuleObserver);

        } catch (final ParseException e) {
            mLogger.logError(DataLogger.SYSTEM_CORE, LOGTAG, "Cannot parse: " + query);
            Log.e(LOGTAG, "Error Parsing: " + e.getMessage());
        }

        return queryResultProxy;
    }

    public void unregisterCSPARQLQuery(String id) {
        mCsparqlEngine.unregisterQuery(id);
    }

    public void unregisterCSPARQLStream(String stream) {
        ContextStream cs = mContextStreams.get(stream);

        if (cs == null) {
            mLogger.logVerbose(DataLogger.REASONER, "Cannot unregister stream: " + stream +
                    ". Needs to be registered first");
        } else {
            mCsparqlEngine.unregisterStream(stream);
        }
    }

    public void updatePropertyValue(String property, String value) {

    }

    @Override
    public void updateValues(String subject, String predicate, String value) {
        mContextStream.sendStream(subject, predicate, value, System.currentTimeMillis());
    }

    public void updateValues(String subject, String predicate, String value, long time) {
        mContextStream.sendStream(subject, predicate, value, time);
    }

    public void updateValues(String stream, String subject, String predicate, String value) {
        updateValues(stream, subject, predicate, value, System.currentTimeMillis());
    }

    public void updateValues(String stream, String subject, String predicate, String value, long time) {

        ContextStream cs = mContextStreams.get(stream);

        if (cs == null) {
            updateValues(subject, predicate, value, time);
        } else {
            cs.sendStream(subject, predicate, value, time);
        }
    }

    public void stop() {
        mAggregateRules.clear();
        mCsparqlEngine.destroy();

        if (mSyncTimer != null) {
            mSyncTimer.cancel();
        }
    }

    public void registerAggregateRule(String name, String rule) {

        long currentTime = System.currentTimeMillis();

        AggregateRule aggregateRule = new AggregateRule(rule);
        List<String> cachLiterals = aggregateRule.getCachibleLiterals();

        for (String needToCache : cachLiterals) {

            TemporalValue tvalue = aggregateRule.getTemporalValue(needToCache);

            if (tvalue != null) {
                aggregateRule.addCachedLiteral(
                        evaluateTemporalLiteral(needToCache, tvalue, currentTime));
            } else {
                mLogger.logError(DataLogger.REASONER, "Cachible Literal has no temporal value");
            }

        }

        mAggregateRules.put(name, aggregateRule);
    }

    public void incrementDateRelativeTemporals() {
        for (AggregateRule rule : mAggregateRules.values()) {
            rule.incrementTemporalValueDates();
        }
    }

    public boolean unregisterAggregateRule(String name) {

        AggregateRule agg = mAggregateRules.remove(name);

        if (agg == null) {
            return false;
        } else {
            return true;
        }
    }

    //Should make sure we fire all rules before considering rerunning due to context change
    public synchronized void fireAggregateRules(String newContextValue) {

        long mCurrentTime = System.currentTimeMillis();
        mUpdatedContextNames.clear();
        mUpdatedContextValues.clear();

        //Might be better to hold/check a local Map (context type-list of agg contexts) instead of
        //checking each rule separately.
        for (AggregateRule rule : mAggregateRules.values()) {
            //Consider threadpool
            if (rule.isAffectedBy(newContextValue)) {
                fireRule(rule, mCurrentTime);
            }
        }

        if (! mUpdatedContextNames.isEmpty()) {
            mReasonerCore.updateAggregateContexts(mUpdatedContextNames, mUpdatedContextValues);
        }
    }

    private void fireRule(AggregateRule rule, long mCurrentTime) {

        LinkedList<Node> literalValues = new LinkedList<Node>();

        //Lets get any cached temporal literals;
        literalValues.addAll(rule.getCachedLiterals());

        //Lets get all instance literals
        for (String instanceLiteral : rule.getInstanceLiterals()) {
            String contextName = instanceLiteral.substring(0, instanceLiteral.indexOf("_"));

            ContextResult cr = mReasonerCore.mContextValues.get(contextName);
            Literal literal = new Literal(instanceLiteral, false);

            if (cr == null) {

            } else {
                if (cr.getFullName().equals(instanceLiteral)) {
                    literal.flip();
                } else {

                }
            }

            literalValues.add(literal);

        }

        //Lets get all non-cached Temporal literals
        for (Map.Entry<String, TemporalValue> temporalLiteral : rule.getTemporalLiterals().entrySet()) {
                literalValues.add(evaluateTemporalLiteral(
                        temporalLiteral.getKey(), temporalLiteral.getValue(), mCurrentTime));
        }

        try {
            SatSolver solver = new SatSolver(rule.getPropNodes(), TIMEOUT);

            if (solver.isSatisfiable(literalValues)) {

                mUpdatedContextNames.add(rule.getContextName());
                mUpdatedContextValues.add((String) rule.getStateLiteral().var);

            }

        } catch (TimeoutException exception) {
            Log.e(LOGTAG, exception.getMessage());
        }
    }

    private Literal evaluateTemporalLiteral(String literalName, TemporalValue literalValue,
                                            long currentTime) {

        Literal literal = new Literal(literalName, false);

        if (literalValue.mAbsolute) {
            //check db
            literal.positive = mContextDatabase.
                    contextValuePresentAbsolute(literalName, literalValue.mStartTime,
                            literalValue.mEndTime, literalValue.mStrong);
        } else {
            String contextName = literalName.substring(0, literalName.indexOf("_"));

            ContextResult cr = mReasonerCore.mContextValues.get(contextName);

            if (cr != null) {
                if (cr.getFullName().equals(literalName)) {

                    long diff = currentTime - cr.getContextTime();

                    if (diff > literalValue.mStartTime) {
                        literal.positive = true;
                    }
                }
            } else {
                //check db
                literal.positive = mContextDatabase.
                        contextValuePresentRelative(literalName, currentTime - literalValue.mStartTime,
                                literalValue.mStrong);
            }

        }

        return literal;
    }

    public void alterContextPreference(String prefName, int value) {
        alterContextPreference(prefName,value,System.currentTimeMillis());
    }

    public void alterContextPreference(String prefName, long value) {
        alterContextPreference(prefName,value,System.currentTimeMillis());
    }

    public void alterContextPreference(String prefName, boolean value) {
        alterContextPreference(prefName,value,System.currentTimeMillis());
    }

    public void alterContextPreference(String prefName, float value) {
        alterContextPreference(prefName,value,System.currentTimeMillis());
    }

    public void alterContextPreference(String prefName, String value) {
        alterContextPreference(prefName,value,System.currentTimeMillis());
    }

    public void alterContextPreference(String prefName, int value, long timestamp) {

        int currentValue = mContextSettings.getInt(prefName, Integer.MIN_VALUE);

        if (currentValue == Long.MIN_VALUE){
            mLogger.logVerbose(DataLogger.REASONER, "Prefs: " + prefName
                    + " value set first time to: " + String.valueOf(value));
        } else {
            mLogger.logVerbose(DataLogger.REASONER, "Prefs: " + prefName
                    + " value changed from: " + String.valueOf(currentValue)
                    +  " to: " + String.valueOf(value));
        }

        if (value != currentValue) {
            SharedPreferences.Editor editor = mContextSettings.edit();
            editor.putInt(prefName, value);
            editor.putLong(Prefs.RULE_PREF_LASTUPATE, timestamp);
            editor.commit();
            restartRunningContextFromPreference(prefName);
        }
    }

    public void alterContextPreference(String prefName, long value, long timestamp) {

        long currentValue = mContextSettings.getLong(prefName, Long.MIN_VALUE);

        if (currentValue == Long.MIN_VALUE){
            mLogger.logVerbose(DataLogger.REASONER, "Prefs: " + prefName
                    + " value set first time to: " + String.valueOf(value));
        } else {
            mLogger.logVerbose(DataLogger.REASONER, "Prefs: " + prefName
                    + " value changed from: " + String.valueOf(currentValue)
                    +  " to: " + String.valueOf(value));
        }

        if(value != currentValue) {
            SharedPreferences.Editor editor = mContextSettings.edit();
            editor.putLong(prefName, value);
            editor.putLong(Prefs.RULE_PREF_LASTUPATE, timestamp);
            editor.commit();
            restartRunningContextFromPreference(prefName);
        }
    }

    public void alterContextPreference(String prefName, boolean value, long timestamp) {

        boolean currentValue = mContextSettings.getBoolean(prefName, false);

        mLogger.logVerbose(DataLogger.REASONER, "Prefs: " + prefName
                + " value changed from: " + String.valueOf(currentValue)
                +  " to: " + String.valueOf(value));

        if (value != currentValue) {
            SharedPreferences.Editor editor = mContextSettings.edit();
            editor.putBoolean(prefName, value);
            editor.putLong(Prefs.RULE_PREF_LASTUPATE, System.currentTimeMillis());
            editor.commit();
            restartRunningContextFromPreference(prefName);
        }
    }

    public void alterContextPreference(String prefName, float value, long timestamp) {

        float currentValue = mContextSettings.getFloat(prefName, Float.MIN_VALUE);

        if (currentValue == Float.MIN_VALUE) {
            mLogger.logVerbose(DataLogger.REASONER, "Prefs: " + prefName
                    + " value set first time to: " + String.valueOf(value));
        } else {
            mLogger.logVerbose(DataLogger.REASONER, "Prefs: " + prefName
                    + " value changed from: " + String.valueOf(currentValue)
                    +  " to: " + String.valueOf(value));
        }

        if (value != currentValue) {
            SharedPreferences.Editor editor = mContextSettings.edit();
            editor.putFloat(prefName, value);
            editor.putLong(Prefs.RULE_PREF_LASTUPATE, timestamp);
            editor.commit();
            restartRunningContextFromPreference(prefName);
        }
    }

    public void alterContextPreference(String prefName, String value, long timestamp) {

        String currentValue = mContextSettings.getString(prefName, "UnknowN");

        if (currentValue.equals("UnknowN")) {
            mLogger.logVerbose(DataLogger.REASONER, "Prefs: " + prefName
                    + " value set first time to: " + value);
        } else {
            mLogger.logVerbose(DataLogger.REASONER, "Prefs: " + prefName
                    + " value changed from: " + currentValue
                    +  " to: " + value);
        }

        if (! value.equals(currentValue)) {
            SharedPreferences.Editor editor = mContextSettings.edit();
            editor.putString(prefName, value);
            editor.putLong(Prefs.RULE_PREF_LASTUPATE, timestamp);
            editor.commit();
            restartRunningContextFromPreference(prefName);
        }
    }

    public void restartRunningContextFromPreference(String prefName) {
        HashSet<String> contexts = mCurrentPrefContexts.get(prefName);

        if (contexts != null) {
            for (String context : contexts) {
                Map preferences = pilotMapper.mContextParameters.get(context);

                pilotMapper.unregisterContext(context, preferences);
                pilotMapper.registerContext(context, preferences);
            }
        }

    }
}
