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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.poseidon_project.context.utility.FileOperations;

import java.io.InputStream;
import java.util.ArrayList;

/**
 * Parses files made to map context rules, and context components.
 *
 * @author Dean Kramer <d.kramer@mdx.ac.uk>
 */
public class ContextRuleMapParser {

    private static final String LOGTAG = "ContextMapperParser";
    private InputStream mFileToParse;

    public ContextRuleMapParser(InputStream in) { mFileToParse = in; }

    public ArrayList<ContextRuleMap> parse() {

        ArrayList<ContextRuleMap> result = new ArrayList<>();

        final String jsonText = FileOperations.convertStreamToString(mFileToParse);

        try {
            final JSONObject json = new JSONObject(jsonText);

            final JSONArray contexts = json.getJSONArray("contexts");

            final int numOfContexts = contexts.length();

            for (int i = 0; i < numOfContexts; i++) {
                parseContext(contexts.getJSONObject(i), result);
            }

        } catch (JSONException e) {
            Log.e(LOGTAG, e.getStackTrace().toString());
        }

        return result;
    }

    public static void parseContext(JSONObject context, ArrayList<ContextRuleMap> result) {

        ContextRuleMap contextMapInstance = new ContextRuleMap();
        try {
            contextMapInstance.setName(context.getString("name"));

            JSONArray observers = context.getJSONArray("observers");

            int numOfObservers = observers.length();

            for (int i = 0; i < numOfObservers; i++) {
                JSONObject observer = observers.getJSONObject(i);
                contextMapInstance.addContextObserver(observer.getString("observer"));
            }

            JSONArray rules = context.getJSONArray("rules");

            int numOfRules = rules.length();

            for (int i = 0; i < numOfRules; i++) {
                JSONObject rule = rules.getJSONObject(i);
                contextMapInstance.addContextRule(rule.getString("rule"));
            }


        } catch (JSONException e) {
            Log.e(LOGTAG, e.getStackTrace().toString());
        }

    }
}
