/*Copyright 2014 POSEIDON Project

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
package org.poseidon_project.contexts.envir.weather.source;

import android.util.Log;

import org.poseidon_project.contexts.ContextException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;


public class HttpWeatherSource {

    public static final String ENCODING = "UTF-8";

    static final String CHARSET = "charset=";

    private static final int HTTP_STATUS_OK = 200;

    static final String USER_AGENT = "POSEIDON-Contexts (Weather)";

    private HttpURLConnection client;

    private static final String mTag = "POSEIDON-Contexts";


    protected InputStreamReader getReaderForURL(String urlString) throws ContextException {
        Log.d(mTag, "requesting " + urlString);
        try {
            URL url = new URL(urlString);
            client = (HttpURLConnection) url.openConnection();
            client.addRequestProperty("User-Agent", USER_AGENT);
            prepareRequest(client);
        } catch (Exception e) {
        	throw new ContextException("Having difficulty preparing HTTP Request", e);
        }

        String charset = ENCODING;
        try {
            client.connect();

            int status = client.getResponseCode();
            if (status != HTTP_STATUS_OK) {
                throw  new ContextException("Invalid response from server: " +
                        String.valueOf(status));
            }

            charset = HttpUtils.getCharset(client);

            InputStreamReader inputStream = new InputStreamReader(client.getInputStream(), charset);

            return inputStream;
        } catch (UnsupportedEncodingException uee) {
            throw new ContextException("Unsupported charset: " + charset, uee);
        } catch (IOException e) {
            throw new ContextException("Problem communicating with API", e);
        }
    }


    protected void prepareRequest(HttpURLConnection request) {}

}
