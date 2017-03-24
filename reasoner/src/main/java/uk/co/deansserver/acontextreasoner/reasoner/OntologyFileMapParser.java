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

package uk.co.deansserver.acontextreasoner.reasoner;

import android.os.Environment;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import uk.co.deansserver.acontextreasoner.utility.FileOperations;

import java.io.InputStream;
import java.util.HashMap;

/**
 * A Parser to read ontology URL to file mappings.
 *
 * @author Dean Kramer <deankramer99@gmail.com>
 */
public class OntologyFileMapParser {

    private static final String LOGTAG = "OntologyFileMapParser";
    private InputStream mFileToParse;

    public OntologyFileMapParser(InputStream in) {
        mFileToParse = in;
    }

    public HashMap<String, String> parse() {
        HashMap<String, String> result = new HashMap<>();

        final String jsonText = FileOperations.convertStreamToString(mFileToParse);

        try {
            final JSONObject json = new JSONObject(jsonText);

            final JSONArray ontologies = json.getJSONArray("ontologies");

            parseOntologies(ontologies, result);

        } catch (JSONException e) {
            Log.e(LOGTAG, e.getStackTrace().toString());        }

        return result;
    }

    private static void parseOntologies(JSONArray ontologies, HashMap<String, String> result)
            throws JSONException {

        final int numOfOntologies = ontologies.length();

        for (int i = 0; i < numOfOntologies; i++) {
            final JSONObject ontology = ontologies.getJSONObject(i);

            String ontologyFileLocation = ontology.getString("file_location");

            ontologyFileLocation = ontologyFileLocation.replace("$SD$", Environment.getExternalStorageDirectory().getAbsolutePath());

            final String ontologyURL = ontology.getString("url");

            result.put(ontologyURL, ontologyFileLocation);
        }
    }
}
