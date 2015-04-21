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

import android.util.Log;

import org.poseidon_project.context.ContextReasonerCore;

import java.util.Observable;

import eu.larkc.csparql.common.RDFTable;
import eu.larkc.csparql.common.RDFTuple;
import eu.larkc.csparql.core.ResultFormatter;

/**
 * Class to get result and pass it back to the core for broadcast
 *
 * @author Dean Kramer <d.kramer@mdx.ac.uk>
 */
public class ContextRuleObserver extends ResultFormatter {

    private ContextReasonerCore mEngineCore;


    public ContextRuleObserver(ContextReasonerCore crc) {
        mEngineCore = crc;
    }

    @Override
    public void update(Observable observable, Object data) {

        RDFTable q = (RDFTable) data;

        for (final RDFTuple t: q) {

            String context = t.get(2);
            context = context.substring(context.indexOf("\""), context.lastIndexOf("\""));
            String contextName = context.substring(0, context.indexOf("_"));
            String contextValue = context.replace(contextName + "_", "");
            mEngineCore.updateContextValue(contextName, contextValue);

        }

    }
}
