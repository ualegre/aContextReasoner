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
 * Service Interface for the Context Reasoner
 *
 * @author Dean Kramer <deankramer99@gmail.com>
 */
interface IContextReasoner {

    //***Copy new context Components over***
    void importDexFile(in String appKey, in String newDex, in String[] contexts, in String packageName, int permission);

    //***Asks for a particular context to be used***
    boolean addContextRequirement(in String appKey, in String observerName);

    //***Asks for a particular context to be used, with parameters***
    boolean addContextRequirementWithParameters(in String appkey, in String observerName, in Map parameters);

    //***Removes the need for a particular context***
    boolean removeContextRequirement(in String appKey, in String observerName);

    //***Set internal context parameters***
    boolean setContextParameters(in String appkey, in String observerName, in Map parameters);

    //***Register user identifier if unknown
    void registerUserIdentifier(in String userID);
}
