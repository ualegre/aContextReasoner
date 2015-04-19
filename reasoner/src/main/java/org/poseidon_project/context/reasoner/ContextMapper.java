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

import android.os.storage.OnObbStateChangeListener;

import org.poseidon_project.context.ContextReasonerCore;
import org.poseidon_project.context.management.ContextManager;

import java.util.HashMap;
import java.util.Map;

import eu.larkc.csparql.core.engine.CsparqlQueryResultProxy;

/**
 * Temporary class for mapping contexts to components and rules for pilot 1
 *
 * @author Dean Kramer <d.kramer@mdx.ac.uk>
 */
public class ContextMapper {

    private ContextReasonerCore mReasonerCore;
    private ContextManager mContextManager;
    private OntologyManager mOntologyManager;
    private HashMap<String, CsparqlQueryResultProxy> rules = new HashMap<>();

    private static final String batteryHighQuery =
              "REGISTER STREAM batteryContextIsHigh AS "
            + "CONSTRUCT {?s ?p \"BATTERY_HIGH\"} "
            + "FROM STREAM <http://poseidon-project.org/context-stream> [RANGE 1s STEP 5s] "
            + "WHERE { ?s ?p ?o "
            + "FILTER (?o > 10) "
            + "}";

    private static final String lightingLowQuery =
            "REGISTER STREAM lightContextIsLow AS "
            + "CONSTRUCT {?s ?p \"LIGHT_LOW\"} "
            + "FROM STREAM <http://poseidon-project.org/context-stream> [RANGE TRIPLES 1] "
            + "WHERE { ?s ?p ?o "
            + "FILTER (?o < 50) "
            + "}";

    public ContextMapper(ContextReasonerCore crc, OntologyManager on) {

        mReasonerCore = crc;
        mContextManager = crc.getContextManager();
        mOntologyManager = on;

    }


    public boolean registerContext(String context, Map parameters) {

        if (mContextManager==null) {
            mContextManager = mReasonerCore.getContextManager();
        }

        if (context.equals("battery")) {
            registerBatteryContext();
        } else if (context.equals("light")) {
            registerLightContext();
        }

        return true;
    }

    public boolean unregisterContext(String context, Map parameters) {

        if (context.equals("battery")) {
            unRegisterBatteryContext();
        } else if (context.equals("light")) {
            unRegisterLightContext();
        }

        return true;
    }

    public void registerBatteryContext() {

        mContextManager.addObserverRequirement("engine", "BatteryContext");
        CsparqlQueryResultProxy c1 = mOntologyManager.registerCSPARQLQuery(batteryHighQuery);
        rules.put("battery", c1);

    }

    public void unRegisterBatteryContext() {
        mContextManager.removeObserverRequirement("engine", "BatteryContext");
        mOntologyManager.unregisterCSPARQLQuery(rules.get("battery").getId());
    }

    public void registerLightContext() {
        mContextManager.addObserverRequirement("engine", "LightContext");
        CsparqlQueryResultProxy c1 = mOntologyManager.registerCSPARQLQuery(lightingLowQuery);
        rules.put("light", c1);
    }

    public void unRegisterLightContext() {
        mContextManager.removeObserverRequirement("engine", "LightContext");
        mOntologyManager.unregisterCSPARQLQuery(rules.get("light").getId());
    }


}
