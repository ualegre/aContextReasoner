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

package org.poseidon_project.context.management;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * Listens for context values being broadcasted from other applications.
 *
 * @author Dean Kramer <d.kramer@mdx.ac.uk>
 */
public class ExternalContextReceiver extends BroadcastReceiver {

    public static final String CONTEXT_INTENT = "org.poseidon_project.context.EXTERNAL_CONTEXT_UPDATE";
    public static final String CONTEXT_NAME = "context_name";
    public static final String CONTEXT_VALUE_TYPE = "context_value_type";
    public static final String CONTEXT_VALUE = "context_value";
    private ContextManager mContextManager;

    public ExternalContextReceiver(ContextManager cm) {
        mContextManager = cm;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle bundle = intent.getExtras();

        String contextName = bundle.getString(CONTEXT_NAME);
        String contextType = bundle.getString(CONTEXT_VALUE_TYPE);

        if(contextType.equals("long")) {
            mContextManager.newExternalContextValue(contextName, bundle.getLong(CONTEXT_VALUE));

        } else if (contextType.equalsIgnoreCase("double")) {
            mContextManager.newExternalContextValue(contextName, bundle.getDouble(CONTEXT_VALUE));

        } else if (contextType.equalsIgnoreCase("boolean")) {
            mContextManager.newExternalContextValue(contextName, bundle.getBoolean(CONTEXT_VALUE));

        } else if (contextType.equalsIgnoreCase("String")) {
            mContextManager.newExternalContextValue(contextName, bundle.getString(CONTEXT_VALUE));

        } else if (contextType.equalsIgnoreCase("Object")) {
            mContextManager.newExternalContextValue(contextName, bundle.getSerializable(CONTEXT_VALUE));

        } else if (contextType.equalsIgnoreCase("Map")) {
            mContextManager.newExternalContextValue(contextName, bundle.getSerializable(CONTEXT_VALUE));

        }

    }
}
