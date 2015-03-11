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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.poseidon_project.contexts.ContextException;

import android.util.Log;


public class HttpWeatherSource {

    public static final String ENCODING = "UTF-8";

    static final String CHARSET = "charset=";

    private static final int HTTP_STATUS_OK = 200;

    static final String USER_AGENT = "POSEIDON-Contexts (Weather)";

    private HttpClient client;

    private static final String mTag = "POSEIDON-Contexts";


    protected InputStreamReader getReaderForURL(String url) throws ContextException {
        Log.d(mTag, "requesting " + url);
        HttpGet request;
        try {
            request = new HttpGet(url);
            request.setHeader("User-Agent", USER_AGENT);
            prepareRequest(request);
        } catch (Exception e) {
        	throw new ContextException("Having difficulty preparing HTTP Request", e);
        }

        String charset = ENCODING;
        try {
            HttpResponse response = getClient().execute(request);

            StatusLine status = response.getStatusLine();
            if (status.getStatusCode() != HTTP_STATUS_OK) {
                throw new ContextException("Invalid response from server: " +
                        status.toString());
            }

            HttpEntity entity = response.getEntity();
            charset = HttpUtils.getCharset(entity);
            InputStreamReader inputStream = new InputStreamReader(entity.getContent(), charset);

            return inputStream;
        } catch (UnsupportedEncodingException uee) {
            throw new ContextException("unsupported charset: " + charset, uee);
        } catch (IOException e) {
            throw new ContextException("Problem communicating with API", e);
        }
    }


    protected void prepareRequest(HttpGet request) {
    }


    HttpClient getClient(){
        if (this.client == null) {
            try {
                this.client = new DefaultHttpClient();
            } catch (Exception e) {
                Log.e(mTag, e.toString());
            }
        }
        return this.client;
    }

}
