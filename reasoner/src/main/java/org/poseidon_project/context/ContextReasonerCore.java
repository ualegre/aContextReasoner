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

package org.poseidon_project.context;

import android.content.Context;
import android.content.Intent;

import org.poseidon_project.context.database.ContextDB;
import org.poseidon_project.context.database.ContextDBImpl;
import org.poseidon_project.context.database.ContextResult;
import org.poseidon_project.context.logging.DataLogger;
import org.poseidon_project.context.management.ContextManager;
import org.poseidon_project.context.reasoner.ReasonerManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Core class for service management
 *
 * @author Dean Kramer <d.kramer@mdx.ac.uk>
 */
public class ContextReasonerCore {

    public static final String BROADCAST_INTENT = "org.poseidon_project.context.CONTEXT_UPDATE";
    private static final String LOGTAG = "ContextService";
    private static final String CONTEXT_NAME = "context_name";
    private static final String CONTEXT_VALUE = "context_value";
    private ContextDB mContextDatabase;
    private ContextManager mContextManager;
    private ReasonerManager mReasonerManager;
    private Context mContext;
    public HashMap<String, ContextResult> mContextValues = new HashMap<>();
    private DataLogger mLogger;


    public ContextReasonerCore(Context c) {
        mContext = c;

        new Thread(new Runnable() {
            @Override
            public void run() {
                mContextDatabase = new ContextDBImpl(mContext);
                mLogger = new DataLogger(mContext, mContextDatabase);
                mReasonerManager = new ReasonerManager(mContext,
                        ContextReasonerCore.this, mContextDatabase);
                mContextManager = new ContextManager(mContext,
                        ContextReasonerCore.this, mContextDatabase);
            }
        }).start();
    }

    public DataLogger getLogger() { return mLogger;}

    public ReasonerManager getReasonerManager() {return mReasonerManager;}

    public ContextManager getContextManager() {return mContextManager;}


    public void importDexFile(String appkey, final String newDex,
                                 String[] contexts, String packagename, int permission) {
        mContextManager.copyDexFile(appkey, newDex, contexts, packagename, permission);
    }

    public boolean addContextRequirement(String appkey, String observerName) {
        //return mContextManager.addObserverRequirement(appkey, observerName);
        return mReasonerManager.pilotMapper.registerContext(observerName, null);
    }

    public boolean removeContextRequirement(String appkey, String observerName) {
        //return mContextManager.removeObserverRequirement(appkey, observerName);
        return mReasonerManager.pilotMapper.unregisterContext(observerName, null);
    }

    public boolean setContextParameters(String appkey, String observerName, Map parameters) {
        return mContextManager.setContextParameters(appkey, observerName, parameters);
    }

    public void sendContextResult(String contextName, String contextValue) {

        Intent intent = new Intent();
        try {
            intent.setAction(BROADCAST_INTENT);
            intent.putExtra(CONTEXT_NAME, contextName);
            intent.putExtra(CONTEXT_VALUE, contextValue);
            mContext.sendBroadcast(intent);
        } catch (Exception e) {
            mLogger.logError(DataLogger.SYSTEM_CORE, LOGTAG,
                    "Cannot Broadcast Context: " + contextName + " Change");
        }

    }

    public void onDestroy() {

        if (mContextManager != null) {
            mContextManager.stop();
        }

        if (mReasonerManager != null) {
            mReasonerManager.stop();
        }

        if (mContextDatabase != null) {

            long time = System.currentTimeMillis();

            for (ContextResult cr : mContextValues.values()) {
                mContextDatabase.updateContextValueToTime(cr,time);
            }
        }

        if ( mLogger != null ) {
            mLogger.stop();
        }
    }

    public boolean addContextRequirementWithParameters
            (String appkey, String observerName, Map parameters) {
        return mReasonerManager.pilotMapper.registerContext(observerName, parameters);
        //return mContextManager.addObserverRequirementWithParameters(appkey, observerName, parameters);
    }

    public void updateContextValue(String contextName, String value) {

        ContextResult previous = mContextValues.get(contextName);

        if (previous==null) {
            //sendBroadcast
            ContextResult newContext = mContextDatabase.newContextValue
                    (null, contextName + "_" + value, System.currentTimeMillis());

            if (newContext != null) {
                mContextValues.put(contextName, newContext);
                mReasonerManager.fireAggregateRules(contextName);
            }

            sendContextResult(contextName, value);
            mLogger.logVerbose(DataLogger.REASONER,
                    LOGTAG, "Context: " + contextName + " set to " + value);
        } else {
            if(! value.equals(previous.getContextValue())) {
                //sendBroadcast
                ContextResult newContext = mContextDatabase.newContextValue
                        (previous, contextName + "_" + value, System.currentTimeMillis());

                if (newContext != null) {
                    mContextValues.put(contextName, newContext);
                    mReasonerManager.fireAggregateRules(contextName);
                }

                sendContextResult(contextName, value);
                mLogger.logVerbose(DataLogger.REASONER,
                        LOGTAG, "Context: " + contextName + " set to " + value);
            }
        }
    }

    public void removeContextValue(String contextName) {
        ContextResult cr = mContextValues.remove(contextName);

        if (cr != null) {
            mContextDatabase.updateContextValueToTime(cr, System.currentTimeMillis());
        }
    }

    public void alterSychroiniseTime(int hour, int min) {
        mLogger.setBackupTime(hour, min);
    }


    public void alterPreferenceLong(String prefName, long value) {
        mReasonerManager.alterContextPreference(prefName, value);
    }

    public void alterPreferenceFloat(String prefName, float value) {
        mReasonerManager.alterContextPreference(prefName, value);
    }

    public void alterPreferenceBool(String prefName, boolean value) {
        mReasonerManager.alterContextPreference(prefName, value);
    }

    public void alterPreferenceString(String prefName, String value) {
        mReasonerManager.alterContextPreference(prefName, value);
    }
}
