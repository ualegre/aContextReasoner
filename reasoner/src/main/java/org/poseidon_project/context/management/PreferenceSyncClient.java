/*
 * Copyright 2016 Middlesex University
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

import android.content.SharedPreferences;

import org.poseidon_project.context.reasoner.ReasonerManager;

/**
 * Interface for all internet based systems for synchronising preferences
 *
 * @author Dean Kramer <d.kramer@mdx.ac.uk>
 */

public abstract class PreferenceSyncClient {

    protected SharedPreferences mPrefs;
    protected ReasonerManager mReasoner;

    public PreferenceSyncClient(ReasonerManager core, SharedPreferences preferences) {
        mReasoner = core;
        mPrefs = preferences;
    }

    abstract boolean authenticate();
    abstract void synchronisePreferences(Callback callback);

    public interface Callback {
        void onSuccessful();
        void onFail(String error);
    }
}
