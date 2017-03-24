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

package uk.co.deansserver.acontextreasoner.management;

import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import uk.co.deansserver.acontextreasoner.reasoner.ReasonerManager;
import uk.co.deansserver.acontextreasoner.utility.Prefs;

import java.text.SimpleDateFormat;
import java.util.Map;

import no.tellu.findit.client.api.AsyncService;
import no.tellu.findit.client.api.GetTransaction;
import no.tellu.findit.client.api.PostTransaction;
import no.tellu.findit.client.api.QueueAsyncServiceImpl;
import no.tellu.findit.client.api.Resources;

/**
 * Implementation for synchronising settings with Tellu AS. No longer used. Kept for history.
 *
 * @author Dean Kramer <d.kramer@mdx.ac.uk>
 */

public class TelluSyncClient extends PreferenceSyncClient {

    private final static String SERVICE_URL = "https://ri.smarttracker.no/web";
    private AsyncService mTelluApiService;
    private static final String LOG_TAG = "TelluClient";
    private int mHasToken = 0;
    private SimpleDateFormat mDateFormater = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    public TelluSyncClient(ReasonerManager core, SharedPreferences preferences) {
        super(core, preferences);
    }

    @Override
    public boolean authenticate() {

        String username = mPrefs.getString(Prefs.TELLU_USER, "");
        String password = mPrefs.getString(Prefs.TELLU_PASS, "");

        if (username.isEmpty()) {
            return false;
        } else {
            AsyncService.AccountCallback callback = new AsyncService.AccountCallback() {

                @Override
                public void onAccountOK(long l, String s) {
                    mHasToken = 1;
                }

                @Override
                public void onAccountError(int i, String s, Exception e) {
                    Log.e(LOG_TAG, "Failed with code: " + i + " " + e.getMessage());
                    mHasToken = -1;
                }
            };

            mTelluApiService = new QueueAsyncServiceImpl(SERVICE_URL);
            mTelluApiService.initiate(username, password, callback);

            return true;
        }
    }

    @Override
    public void synchronisePreferences(final PreferenceSyncClient.Callback callback) {
        GetTransaction trans = new GetTransaction(Resources.ASSET);
        trans.setContent("properties");

        mTelluApiService.doDataRetrieve(trans, new AsyncService.DataRetrieveCallback() {
            @Override
            public void onRetrieveOK(JSONArray jsonArray, GetTransaction getTransaction) {
                try {
                    JSONObject asset = (JSONObject) jsonArray.get(0);
                    JSONArray props = asset.getJSONArray("properties");

                    int length = props.length();

                    JSONObject newasset = new JSONObject();
                    JSONArray newProp = new JSONArray();
                    newasset.put("id", asset.get("id"));
                    newasset.put("name", asset.get("name"));
                    newasset.put("properties", newProp);

                    long lastUpdate = mPrefs.getLong(Prefs.RULE_PREF_LASTUPATE, 0);

                    Map<String, ?> prefs = mPrefs.getAll();

                    for (int i=0; i<length;i++) {
                        JSONObject currentProp = props.getJSONObject(i);

                        String name = currentProp.getString("name");

                        if (prefs.containsKey(name)) {

                            String timestamp = currentProp.getString("timestamp");
                            long updated = 0;

                            Object value = prefs.get(name);

                            if (! timestamp.isEmpty()) {
                                updated = mDateFormater.parse(timestamp).getTime();
                            }

                            if (lastUpdate > updated) {
                                String typeId = "typePropertyIdentifier";
                                JSONObject propAlteration = new JSONObject();
                                propAlteration.put("name", name);
                                propAlteration.put("value", value);
                                propAlteration.put(typeId, currentProp.get(typeId));

                                newProp.put(propAlteration);
                            } else {
                                if (value instanceof Integer) {
                                    mReasoner.alterContextPreference(name, (Integer) value, updated);
                                } else if (value instanceof Long) {
                                    mReasoner.alterContextPreference(name, (Long) value, updated);
                                } else if (value instanceof String) {
                                    mReasoner.alterContextPreference(name, (String) value, updated);
                                } else if (value instanceof Boolean) {
                                    mReasoner.alterContextPreference(name, (Boolean) value, updated);
                                } else if (value instanceof Float) {
                                    mReasoner.alterContextPreference(name, (Float) value, updated);
                                }
                            }
                        }
                    }

                    if (newProp.length()>0) {
                        PostTransaction post = new PostTransaction(Resources.ASSET);
                        post.setObject(newasset);

                        mTelluApiService.doTransaction(post, new AsyncService.TransactionCallback() {
                            @Override
                            public void onTransactionOK(JSONObject jsonObject) {
                                callback.onSuccessful();
                            }

                            @Override
                            public void onTransactionError(int i, String s, Exception e) {
                                callback.onFail(s);
                            }
                        });
                    }

                } catch (Exception e) {
                    e.printStackTrace();

                    callback.onFail(e.getLocalizedMessage());
                }
            }

            @Override
            public void onRetrieveError(int i, String s, GetTransaction getTransaction) {
                    callback.onFail(s);
            }
        });


    }


}

