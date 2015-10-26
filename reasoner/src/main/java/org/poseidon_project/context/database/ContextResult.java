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

package org.poseidon_project.context.database;

/**
 * Class Description
 *
 * @author Dean Kramer <d.kramer@mdx.ac.uk>
 */
public class ContextResult {

    private long mID;
    private String mContextName;
    private String mValue;
    private long mTime;


    public ContextResult(long id, String context, String value, long time) {
        mID = id;
        mContextName = context;
        mValue = value;
        mTime = time;
    }

    public ContextResult(long id, String contextAndValue, long time) {
        mID = id;
        mContextName = contextAndValue.substring(0, contextAndValue.indexOf("_"));
        mValue = contextAndValue.replace(mContextName + "_", "");
        mTime = time;
    }


    public long getId() { return mID; }

    public String getContextName() { return mContextName; }

    public String getContextValue() { return mValue; }

    public long getContextTime() { return mTime; }

}
