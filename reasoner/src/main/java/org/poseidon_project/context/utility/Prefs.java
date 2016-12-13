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

package org.poseidon_project.context.utility;


import android.content.Context;
import android.content.SharedPreferences;

/**
 * Preference Identifiers.
 *
 * @author Dean Kramer <d.kramer@mdx.ac.uk>
 */

public class Prefs {

    //Reasoner
    public static final String REASONER_PREFS = "ContextPrefs";
    public static final String RULE_PREFS = "RulePrefs";
    public static final String REASONER_LASTBACKUP = "logLastBackup";
    public static final String REASONER_USERID = "userId";
    public static final String REASONER_USERVERSION = "userIdVersion";
    public static final String REASONER_DEVICEID = "deviceId";
    public static final String REASONER_BACKUPHOUR = "logBackupHour";
    public static final String REASONER_BACKUPMIN = "logBackupMin";
    public static final String RULE_PREF_LASTUPATE = "prefLastUpdate";
    public static final String REASONER_SETUPDONE = "reasoner_setup";

    //Client
    public static final String TELLU_USER = "tellu_username";
    public static final String TELLU_PASS = "tellu_password";

    //Contexts
    public static final String WEATHER_COLD = "weather_cold";
    public static final String WEATHER_HOT = "weather_hot";
    public static final String REASONER_LEARNING = "reasoner_learning";
    public static final String NAVASSIST_MAXWAIT = "navassist_maxwait";
    public static final String NAVASSIST_MAXDEV = "navassist_maxdev";

    public static void setupFirstTime(Context context) {

        SharedPreferences reasoner = context.getSharedPreferences(REASONER_PREFS, 0);
        boolean setup = reasoner.getBoolean(REASONER_SETUPDONE, false);

        if (! setup) {
            SharedPreferences.Editor editor = reasoner.edit();
            editor.putBoolean(REASONER_SETUPDONE, true);
            editor.commit();

            SharedPreferences rules = context.getSharedPreferences(RULE_PREFS, 0);
            editor = rules.edit();
            editor.putInt(WEATHER_COLD, 15);
            editor.putInt(WEATHER_HOT, 25);
            editor.putInt(NAVASSIST_MAXWAIT, 10);
            editor.putInt(NAVASSIST_MAXDEV, 3);
            editor.putBoolean(REASONER_LEARNING, true);
            editor.commit();
        }
    }
}
