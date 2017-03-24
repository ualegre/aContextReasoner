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

package uk.co.deansserver.acontextreasoner;

/**
 * Service Interface for the Context Preferences Activity
 *
 * @author Dean Kramer <deankramer99@gmail.com>
 */
interface IContextPreference {

    void synchroniseService();
    void alterSynchroniseTime(int hour, int min);
    void alterLearning(boolean mode);
    void alterPreferenceInt(in String prefName, int value);
    void alterPreferenceLong(in String prefName, long value);
    void alterPreferenceFloat(in String prefName, float value);
    void alterPreferenceBool(in String prefName, boolean value);
    void alterPreferenceString(in String prefName, in String value);
    void registerUserIdentifier(in String userID);
}
