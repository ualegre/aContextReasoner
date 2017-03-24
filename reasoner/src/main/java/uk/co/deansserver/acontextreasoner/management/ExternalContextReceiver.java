/*
 * Copyright 2017 aContextReasoner Project
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

package uk.co.deansserver.acontextreasoner.management;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import uk.co.deansserver.acontextreasoner.logging.DataLogger;

import java.util.Map;

/**
 * Listens for context values being broadcasted from other applications.
 *
 * @author Dean Kramer <deankramer99@gmail.com>
 */
public class ExternalContextReceiver extends BroadcastReceiver {

    public static final String CONTEXT_INTENT = "org.poseidon_project.context.EXTERNAL_CONTEXT_UPDATE";
    public static final String CONTEXT_NAME = "context_name";
    public static final String CONTEXT_VALUE_TYPE = "context_value_type";
    public static final String CONTEXT_VALUE = "context_value";
    private ContextManager mContextManager;
    private DataLogger mLogger;

    public ExternalContextReceiver(ContextManager cm, DataLogger logger) {
        mContextManager = cm;
        mLogger = logger;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle bundle = intent.getExtras();

        String contextName = bundle.getString(CONTEXT_NAME);
        String contextType = bundle.getString(CONTEXT_VALUE_TYPE);
        Object contextValue = bundle.getSerializable(CONTEXT_VALUE);


        if (contextName == null) {
            mLogger.logError(DataLogger.CONTEXT_MANAGER, "Null Context Name in External Context");
            return;
        }

        if (contextType == null) {
            mLogger.logError(DataLogger.CONTEXT_MANAGER, "Null Context Type in External Context");
            return;
        }

        if (contextValue == null) {
            mLogger.logError(DataLogger.CONTEXT_MANAGER, "Null Context Value in External Context");
            return;
        }

        if (contextType.equals("long")) {

            long value = 0;

            if (contextValue instanceof Long) {
                value = (Long) contextValue;
            } else if (contextValue instanceof Integer) {
                value = ((Integer) contextValue).longValue();
            } else if (contextValue instanceof String) {
                try {
                    value = Long.parseLong((String) contextValue);
                } catch (Exception ex) {
                    mLogger.logError(DataLogger.CONTEXT_MANAGER, "Couldn't pass: " + contextValue +
                    " to a long");
                    return;
                }

            } else {
                mLogger.logError(DataLogger.CONTEXT_MANAGER, "context object: " + contextValue +
                        " could not be used for Long");
                return;
            }

            mContextManager.newExternalContextValue(contextName, value);

        } else if (contextType.equalsIgnoreCase("double")) {

            double value = 0;

            if (contextValue instanceof Double) {
                value = (Double) contextValue;
            } else if (contextValue instanceof Float) {
                value = ((Float) contextValue).doubleValue();
            } else if (contextValue instanceof Integer) {
                value = ((Integer) contextValue).doubleValue();
            } else if (contextValue instanceof Long) {
                value = ((Long) contextValue).doubleValue();
            } else if (contextValue instanceof String) {
                try {
                    value = Double.parseDouble((String) contextValue);
                } catch (Exception ex) {
                    mLogger.logError(DataLogger.CONTEXT_MANAGER, "Couldn't pass: " + contextValue +
                            " to a double");
                    return;
                }

            } else {
                mLogger.logError(DataLogger.CONTEXT_MANAGER, "context object: " + contextValue +
                        " could not be used for Double");
                return;
            }

            mContextManager.newExternalContextValue(contextName, value);

        } else if (contextType.equalsIgnoreCase("boolean")) {

            boolean value = false;

            if (contextValue instanceof Boolean) {
                value = (Boolean) contextValue;
            } else if (contextValue instanceof Integer) {
                value = (((Integer) contextValue) > 0) ? true : false;
            } else if (contextValue instanceof Long) {
                value = (((Long) contextValue) > 0) ? true : false;
            } else if (contextValue instanceof String) {
                try {
                    value = Boolean.parseBoolean((String) contextValue);
                } catch (Exception ex) {
                    mLogger.logError(DataLogger.CONTEXT_MANAGER, "Couldn't pass: " + contextValue +
                            " to a boolean");
                    return;
                }

            } else {
                mLogger.logError(DataLogger.CONTEXT_MANAGER, "context type: " + contextValue
                        +
                        " could not be used for boolean");
                return;
            }

            mContextManager.newExternalContextValue(contextName, value);

        } else if (contextType.equalsIgnoreCase("String")) {

            if (contextValue instanceof String) {
                mContextManager.newExternalContextValue(contextName, (String) contextValue);
            } else {
                mLogger.logError(DataLogger.CONTEXT_MANAGER, "context object: " + contextValue +
                        " is not a String");
                return;
            }

        } else if (contextType.equalsIgnoreCase("Object")) {
            mContextManager.newExternalContextValue(contextName, contextValue);

        } else if (contextType.equalsIgnoreCase("Map")) {

            if (contextValue instanceof Map) {
                mContextManager.newExternalContextValue(contextName, (Map) contextValue);
            } else {
                mLogger.logError(DataLogger.CONTEXT_MANAGER, "context object: " + contextValue +
                        " is not a Map");
                return;
            }

        } else {
            mLogger.logError(DataLogger.CONTEXT_MANAGER, "context name: " + contextName +
                    " does not have a recognisable type");
        }
    }
}