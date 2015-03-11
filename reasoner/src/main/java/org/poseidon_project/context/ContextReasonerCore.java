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
import android.util.Log;

import org.poseidon_project.context.management.ContextManager;
import org.poseidon_project.context.reasoner.OntologyManager;

/**
 * Core class for service management
 *
 * @author Dean Kramer <d.kramer@mdx.ac.uk>
 */
public class ContextReasonerCore {

    private ContextManager mContextManager;
    private OntologyManager mOntologyManager;
    private Context mContext;
    public static final String BROADCAST_INTENT = "org.poseidon_project.context.CONTEXT_UPDATE";
    private static final String LOGTAG = "ContextService";
    private static final String CONTEXT_VALUE = "context_value";


    public ContextReasonerCore(Context c) {

        mContext = c;
        mOntologyManager = new OntologyManager(c, this);
        mContextManager = new ContextManager(c);

    }


    public void importDexFile(String appkey, final String newDex,
                                 String[] contexts, String packagename, int permission) {
        mContextManager.copyDexFile(appkey, newDex, contexts, packagename, permission);
    }

    public boolean addContextRequirement(String appkey, String observerName) {
        return mContextManager.addObserverRequirement(appkey, observerName);
    }

    public boolean removeContextRequirement(String appkey, String observerName) {
        return mContextManager.removeObserverRequirement(appkey, observerName);
    }

    public void sendContextResult(String contextValue) {

        Intent intent = new Intent();
        try {
            intent.setAction(BROADCAST_INTENT);
            intent.putExtra(CONTEXT_VALUE, contextValue);
            mContext.sendBroadcast(intent);
        } catch (Exception e) {
            Log.e(LOGTAG, "Cannot Broadcast Context Change");
        }

    }


}
