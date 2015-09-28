/*Copyright 2015 POSEIDON Project

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.poseidon_project.context.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.poseidon_project.context.logging.LogEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

/**
 * Implementation class to handle context database operations
 *
 * @author Dean Kramer <d.kramer@mdx.ac.uk>
 */
public class ContextDBImpl implements ContextDB{

    private OpenDbHelper dbHelper;
    public static final String CONTEXTTABLE = "usable_contexts";
    public static final String DEBUGEVENTSTABLE = "events_data";

    public ContextDBImpl(Context context) {
        dbHelper = new OpenDbHelper(context);
    }

    public void getDB(Context context) {
        dbHelper = new OpenDbHelper(context);
    }

    public void closeDB() {
        dbHelper.close();
    }

    @Override
    public HashMap<Integer, String> getContextAllOwners() {

        HashMap<Integer, String> result = new HashMap<Integer, String>();

        try {
            SQLiteDatabase sqlite = dbHelper.getReadableDatabase();
            Cursor crsr = sqlite.rawQuery(
                    "Select _id, owner from usable_contexts;",
                    null);
            crsr.moveToFirst();

            int numRows = crsr.getCount();
        } catch (Exception sqlerror) {
            Log.v("Table read error", sqlerror.getMessage());
        }

        return result;
    }

    @Override
    public List<String> getUsableContextList(String applicationId) {
        List<String> contexts = new Vector<String>();

        try {
            SQLiteDatabase sqlite = dbHelper.getReadableDatabase();
            Cursor crsr = sqlite.rawQuery(
                    "Select name, owner, permission from usable_contexts;",
                    null);
            crsr.moveToFirst();

            int numRows = crsr.getCount();
            for (int i = 0; numRows > 0; i++) {
                if (crsr.getString(1).equalsIgnoreCase(applicationId)) {
                    contexts.add(crsr.getString(i));
                } else {
                    if (crsr.getInt(3) == 0) {
                        contexts.add(crsr.getString(i));
                    }
                }
                crsr.moveToNext();
            }

        } catch (Exception sqlerror) {
            Log.v("Table read error", sqlerror.getMessage());

        }

        return contexts;
    }

    @Override
    public String getDexFile(String componentName) {
        try {
            SQLiteDatabase sqlite = dbHelper.getReadableDatabase();
            Cursor crsr = sqlite.rawQuery(
                    "Select dex_file from usable_contexts where name='"
                            + componentName + "';", null);
            crsr.moveToFirst();

            int numRows = crsr.getCount();
            if (numRows > 0) {
                return crsr.getString(0);
            }

        } catch (Exception sqlerror) {
            Log.v("Table read error", sqlerror.getMessage());

        }

        return "";
    }

    @Override
    public int getPermission(String componentName) {
        try {
            SQLiteDatabase sqlite = dbHelper.getReadableDatabase();
            Cursor crsr = sqlite.rawQuery(
                    "Select permission from usable_contexts where name='"
                            + componentName + "';", null);
            crsr.moveToFirst();

            int numRows = crsr.getCount();
            if (numRows > 0) {
                return crsr.getInt(0);
            }

        } catch (Exception sqlerror) {
            Log.v("Table read error", sqlerror.getMessage());

        }

        return 1;
    }

    @Override
    public boolean insertComponent(String packageName, String name,
                                   String owner, int permission, String dex_file) {
        try {
            SQLiteDatabase sqlite = dbHelper.getWritableDatabase();

            ContentValues initialValues = new ContentValues();
            initialValues.put("packagename", packageName);
            initialValues.put("name", name);
            initialValues.put("owner", owner);
            initialValues.put("permission", permission);
            initialValues.put("dex_file", dex_file);
            sqlite.insert(CONTEXTTABLE, null, initialValues);
            sqlite.close();
            return true;

        } catch (Exception sqlerror) {
            Log.v("Table insert error", sqlerror.getMessage());
            return false;
        }
    }

    @Override
    public boolean removeComponent(String name, String owner) {
        try {
            List<String> component = getLoadComponentInfo(owner, name);
            if (component.size() == 0 || component == null) {
                return false;
            } else {
                if (component.get(2).equalsIgnoreCase(owner)) {
                    SQLiteDatabase sqlite = dbHelper.getWritableDatabase();
                    sqlite.delete(CONTEXTTABLE, "name = ?", new String[] {name});
                    return true;
                }
                return false;
            }

        } catch(Exception sqlerror) {
            Log.e("Error", sqlerror.getMessage());
            return false;
        }

    }

    @Override
    public String getPackageName(String componentName) {
        try {
            SQLiteDatabase sqlite = dbHelper.getReadableDatabase();
            Cursor crsr = sqlite.rawQuery(
                    "Select packageName from usable_contexts where name='"
                            + componentName + "';", null);
            crsr.moveToFirst();

            int numRows = crsr.getCount();
            if (numRows > 0) {
                return crsr.getString(0);
            }

        } catch (Exception sqlerror) {
            Log.v("Table read error", sqlerror.getMessage());

        }

        return "";
    }

    @Override
    public List<String> getLoadComponentInfo(String applicationId,
                                             String componentName) {
        List<String> returnValues = new Vector<String>();
        try {
            SQLiteDatabase sqlite = dbHelper.getReadableDatabase();
            Cursor crsr = sqlite
                    .rawQuery(
                            "Select dex_file, packageName, owner, permission from usable_contexts where name='"
                                    + componentName + "';", null);
            crsr.moveToFirst();

            int numRows = crsr.getCount();
            if (numRows > 0) {
                String owner = crsr.getString(2);
                int permission = crsr.getInt(3);
                if (owner.equalsIgnoreCase(applicationId)) {
                    returnValues.add(crsr.getString(0));
                    returnValues.add(crsr.getString(1));
                    returnValues.add(owner);
                    returnValues.add(String.valueOf(permission));
                } else {
                    if (permission == 0) {
                        returnValues.add(crsr.getString(0));
                        returnValues.add(crsr.getString(1));
                        returnValues.add(owner);
                        returnValues.add(String.valueOf(permission));
                    }
                }

            }

        } catch (Exception sqlerror) {
            Log.v("Table read error", sqlerror.getMessage());
            return null;
        }

        return returnValues;
    }

    @Override
    public boolean newEvents(LogEvent[] events) {

        SQLiteDatabase sqlite = dbHelper.getWritableDatabase();
        try {
            sqlite.beginTransaction();

            int length = events.length;
            for (int i=0; i<length;i++) {
                ContentValues initialValues = new ContentValues();
                initialValues.put("eventOrigin", events[i].getOrigin());
                initialValues.put("eventLocation", events[i].getLocation());
                initialValues.put("eventDateTime", events[i].getDate());
                initialValues.put("eventText", events[i].getText());
                sqlite.insert(DEBUGEVENTSTABLE, null, initialValues);
            }

            sqlite.setTransactionSuccessful();

        } catch (Exception sqlerror) {
            Log.v("Table insert error", sqlerror.getMessage());
            return false;
        } finally {
            sqlite.endTransaction();
        }

        return true;
    }

    @Override
    public List<LogEvent> getAllEvents() {
        List<LogEvent> events = new ArrayList<>();

        try {

            SQLiteDatabase sqlite = dbHelper.getReadableDatabase();
            Cursor crsr = sqlite
                    .rawQuery(
                            "Select _id, eventOrigin, eventLocation, eventDateTime, " +
                                    "eventText from events_data;", null);

            while (crsr.moveToNext()) {
                int id = crsr.getInt(0);
                int origin = crsr.getInt(1);
                String location = crsr.getString(2);
                String dateTime = crsr.getString(3);
                String text = crsr.getString(4);
                events.add(new LogEvent(id,origin, location, dateTime, text));
            }

        } catch (Exception sqlerror) {
            Log.v("Table read error", sqlerror.getMessage());
        }

        return events;
    }

    public boolean emptyEvents() {

        try {
            SQLiteDatabase sqlite = dbHelper.getWritableDatabase();
            sqlite.delete(OpenDbHelper.DEBUGEVENTSTABLE, null, null);
        } catch (Exception sqlerror) {
            Log.v("Table delete error", sqlerror.getMessage());
            return false;
        }

        return true;
    }

    @Override
    public int getNumberOfReceivers() {
        return 0;
    }

    @Override
    public List<String> getContextReceiver(long id) {
        List<String> returnValues = new Vector<String>();
        try {
            SQLiteDatabase sqlite = dbHelper.getReadableDatabase();
            Cursor crsr = sqlite
                    .rawQuery(
                            "Select dex_file, packageName, name from usable_contexts where id='"
                                    + String.valueOf(id) + "';", null);
            crsr.moveToFirst();

            int numRows = crsr.getCount();
            if (numRows > 0) {

                returnValues.add(crsr.getString(0));
                returnValues.add(crsr.getString(1));
                returnValues.add(crsr.getString(2));

            }

        } catch (Exception sqlerror) {
            Log.v("Table read error", sqlerror.getMessage());
            return null;
        }

        return returnValues;
    }

    @Override
    public List<String> getAppContextList(String applicationId) {
        List<String> contexts = new Vector<String>();

        try {
            SQLiteDatabase sqlite = dbHelper.getReadableDatabase();
            Cursor crsr = sqlite.rawQuery(
                    "Select name from usable_contexts where owner ='"
                            + applicationId + "';", null);
            crsr.moveToFirst();

            int numRows = crsr.getCount();
            for (int i = 0; numRows > 0; i++) {
                contexts.add(crsr.getString(i));
                crsr.moveToNext();
            }

        } catch (Exception sqlerror) {
            Log.v("Table read error", sqlerror.getMessage());

        }

        return contexts;
    }

}
