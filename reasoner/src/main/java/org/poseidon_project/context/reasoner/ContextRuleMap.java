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

import java.util.ArrayList;


/**
 * A class which maps context names to context components and rules
 *
 * @author Dean Kramer <d.kramer@mdx.ac.uk>
 */
public class ContextRuleMap {

    private String mName;
    private ArrayList<String> mContextObservers;
    private ArrayList<String> mContextRules;

    public ContextRuleMap() {
        mName = "";
        mContextObservers = new ArrayList<>();
        mContextRules = new ArrayList<>();
    }

    public ContextRuleMap(String name, ArrayList observers, ArrayList rules) {
        mName = name;
        mContextObservers = observers;
        mContextRules = rules;
    }

    public String getName() { return mName; }

    public void setName(String name) { mName = name; }

    public ArrayList<String> getContextObservers() { return mContextObservers; }

    public void setContextObservers(ArrayList observers) { mContextObservers = observers; }

    public void addContextObserver(String observer) { mContextObservers.add(observer); }

    public ArrayList<String> getContextRules() { return mContextRules; }

    public void setContextRules(ArrayList rules) { mContextRules = rules; }

    public void addContextRule(String rule) { mContextRules.add(rule); }
}
