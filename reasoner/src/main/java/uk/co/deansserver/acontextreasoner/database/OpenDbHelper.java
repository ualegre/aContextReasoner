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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * The SQLiteOpenHelper needed for the db
 *
 * @author Dean Kramer <deankramer99@gmail.com>
 */
public class OpenDbHelper extends SQLiteOpenHelper {

    public static final String CONTEXTTABLE = "usable_contexts";
    public static final String RECEIVERTABLE = "usable_receivers";
    public static final String DEBUGEVENTSTABLE = "events_data";
    private static final int DATABASE_VERSION = 3;
    private static final String DB_NAME = "contextDB";
    private static final String CONTEXTTABLE_CREATE = "create table usable_contexts (_id integer primary key autoincrement, "
            + "packagename text,"
            + "name text,"
            + "owner text,"
            + "permission int not null,"
            + "dex_file text);";
    private static final String RECEIVERTABLE_CREATE = "create table usable_receivers (_id integer primary key autoincrement, "
            + "packagename text,"
            + "name text,"
            + "owner text,"
            + "dex_file text);";
    private static final String DEBUGEVENTSTABLE_CREATE = "create table events_data (_id integer primary key autoincrement, "
            + "eventOrigin integer,"
            + "eventLocation text,"
            + "eventDateTime integer,"
            + "eventText text);";

    private static final String CONTEXTRESULT_CREATE = "create table context_result (_id integer primary key autoincrement, "
            + "contextState text,"
            + "value integer,"
            + "fromtime integer,"
            + "totime integer);";

    public OpenDbHelper(Context context) {
        super(context, DB_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CONTEXTTABLE_CREATE);
        db.execSQL(RECEIVERTABLE_CREATE);
        db.execSQL(DEBUGEVENTSTABLE_CREATE);
        db.execSQL(CONTEXTRESULT_CREATE);
        insertStandardContexts(db);

        //insertTestAggregationData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int curv, int newv) {

        db.execSQL("DROP TABLE IF EXISTS " + CONTEXTTABLE);
        db.execSQL(CONTEXTTABLE_CREATE);
        insertStandardContexts(db);

        db.execSQL("DROP TABLE IF EXISTS " + RECEIVERTABLE);
        db.execSQL(RECEIVERTABLE_CREATE);

        db.execSQL("DROP TABLE IF EXISTS " + DEBUGEVENTSTABLE);
        db.execSQL(DEBUGEVENTSTABLE_CREATE);

    }

    /*private void insertTestAggregationData(SQLiteDatabase db) {

        db.execSQL("insert into context_result values (1, 'TEMP_COLD', 1, 1454393100000, 1454421900000)");
        db.execSQL("insert into context_result values (2, 'PRECIP_RAIN', 1, 1454393100000, 1454421900000)");

    }*/

    private void insertStandardContexts(SQLiteDatabase db) {

        //Usable Context DB
        //Example
        //db.execSQL("insert into usable_contexts values (1, 'uk.ac.tvu.mdse.contextengine.contexts', 'BatteryContext', 'contextengine', 0, 'classes.dex')");
        db.execSQL("insert into usable_contexts values (1, 'uk.ac.mdx.cs.ie.acontextlib.envir', 'LocationWeatherContext', 'contextengine', 0, 'classes.dex')");
        db.execSQL("insert into usable_contexts values (2, 'uk.ac.mdx.cs.ie.acontextlib.envir.weather', 'BadWeatherContext', 'contextengine', 0, 'classes.dex')");
        db.execSQL("insert into usable_contexts values (3, 'uk.ac.mdx.cs.ie.acontextlib.hardware', 'BatteryContext', 'contextengine', 0, 'classes.dex')");
        db.execSQL("insert into usable_contexts values (4, 'uk.ac.mdx.cs.ie.acontextlib.hardware', 'CompassContext', 'contextengine', 0, 'classes.dex')");
        db.execSQL("insert into usable_contexts values (5, 'uk.ac.mdx.cs.ie.acontextlib.hardware', 'ExternalStorageSpaceContext', 'contextengine', 0, 'classes.dex')");
        db.execSQL("insert into usable_contexts values (6, 'uk.ac.mdx.cs.ie.acontextlib.hardware', 'GPSIndoorOutdoorContext', 'contextengine', 0, 'classes.dex')");
        db.execSQL("insert into usable_contexts values (7, 'uk.ac.mdx.cs.ie.acontextlib.hardware', 'LightContext', 'contextengine', 0, 'classes.dex')");
        db.execSQL("insert into usable_contexts values (8, 'uk.ac.mdx.cs.ie.acontextlib.hardware', 'TelephonyContext', 'contextengine', 0, 'classes.dex')");
        db.execSQL("insert into usable_contexts values (9, 'uk.ac.mdx.cs.ie.acontextlib.hardware', 'WifiContext', 'contextengine', 0, 'classes.dex')");
        db.execSQL("insert into usable_contexts values (10, 'uk.ac.mdx.cs.ie.acontextlib.hardware', 'StepCounter', 'contextengine', 0, 'classes.dex')");
        db.execSQL("insert into usable_contexts values (11, 'uk.ac.mdx.cs.ie.acontextlib.hardware', 'DistanceTravelledContext', 'contextengine', 0, 'classes.dex')");
        db.execSQL("insert into usable_contexts values (12, 'uk.ac.mdx.cs.ie.acontextlib.hardware', 'PluggedInContext', 'contextengine', 0, 'classes.dex')");
        db.execSQL("insert into usable_contexts values (13, 'uk.ac.mdx.cs.ie.acontextlib.hardware', 'CurrentLocationContext', 'contextengine', 0, 'classes.dex')");
        db.execSQL("insert into usable_contexts values (14, 'uk.ac.mdx.cs.ie.acontextlib.personal', 'HeartRateMonitor', 'contextengine', 0, 'classes.dex')");
        //Context Receiver DB
        //Example
        //db.execSQL("insert into usable_receivers values (1, 'uk.co.deansserver.context.management', 'CoreReceiver', 'contextengine', 'classes.dex')");

    }
}
