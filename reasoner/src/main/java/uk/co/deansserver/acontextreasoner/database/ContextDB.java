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

package uk.co.deansserver.acontextreasoner.database;

import uk.co.deansserver.acontextreasoner.logging.LogEvent;

import java.util.HashMap;
import java.util.List;

/**
 * Interface for context database operations
 *
 * @author Dean Kramer <deankramer99@gmail.com>
 */
public interface ContextDB {

    List<String> getUsableContextList(String applicationId);

    List<String> getAppContextList(String applicationId);

    String getDexFile(String observerName);

    int getPermission(String observerName);

    String getPackageName(String observerName);

    boolean insertObserver(String packageName, String name, String owner,
                            int permission, String dex_file);

    boolean removeObserver(String name, String owner);

    List<String> getLoadObserverInfo(String applicationId, String observerName);

    boolean newEvents(List<LogEvent> events);

    List<LogEvent> getAllEvents();

    boolean emptyEvents();

    int getNumberOfReceivers();

    List<String> getContextReceiver(long id);

    void closeDB();

    HashMap<Integer, String> getContextAllOwners();

    ContextResult newContextValue(ContextResult previousContextValue, String context, long time);

    boolean updateContextValueToTime(ContextResult contextResult, long time);

    boolean contextValuePresentAbsolute(String context, long startTime, long endTime, boolean strict);

    boolean contextValuePresentRelative(String context, long startTime, boolean strict);
}
