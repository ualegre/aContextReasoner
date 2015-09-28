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

package org.poseidon_project.context.logging;

/**
 * A small class to hold log event data
 *
 * @author Dean Kramer <d.kramer@mdx.ac.uk>
 */
public class LogEvent {

    private int event_id;
    private int event_origin;
    private String event_location;
    private String event_date;
    private String event_text;

    public LogEvent(int origin, String location, String date, String text) {

        event_origin = origin;
        event_location = location;
        event_date = date;
        event_text = text;
    }

    public LogEvent(int id, int origin, String location, String date, String text) {

        event_id = id;
        event_origin = origin;
        event_location = location;
        event_date = date;
        event_text = text;
    }

    public int getId() { return event_id; }

    public int getOrigin() {
        return event_origin;
    }

    public String getLocation() {
        return event_location;
    }

    public String getDate() {
        return event_date;
    }

    public String getText() {
        return event_text;
    }
}
