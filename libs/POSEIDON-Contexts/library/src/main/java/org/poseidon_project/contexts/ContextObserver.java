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
package org.poseidon_project.contexts;

import android.content.Context;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * The interface that all context observing components must implement.
 * @author Dean Kramer <d.kramer@mdx.ac.uk>
 *
 */
public abstract class ContextObserver {

    protected String mName;
    protected final Context mContext;
    protected ContextReceiver mReceiver;
    protected LinkedHashSet<String> mRequiringApps = new LinkedHashSet<>();
    protected boolean mIsRunning = false;

    public ContextObserver(Context context) {
        mContext = context;
    }

    public ContextObserver(Context context, ContextReceiver cr) {
        mContext = context;
        mReceiver = cr;
    }

    public ContextObserver(Context context, ContextReceiver cr, String name){
        mContext = context;
        mReceiver = cr;
        mName = name;
    }

    public boolean setContextParameters(HashMap<String, Object> parameters) {
        if (parameters == null) {
            Log.e(mName, "Parameter Map is null!");
            return false;
        } else {
            if (parameters.isEmpty()) {
                Log.e(mName, "There are no parameters!");
                return false;
            } else {
                return true;
            }
        }

    }

	/*
	 * Used to start collecting and observing context data.
	 * @return   A boolean to indicate the context component started correctly.
	 */
	public abstract boolean start();

	/*
	 * Used to pause collection of context data.
	 * @return   A boolean to indicate the context component paused correctly.
	 */
	public abstract boolean pause();

	/*
	 * Used to resume collection of context data.
	 * @return   A boolean to indicate the context component paused correctly.
	 */
	public abstract boolean resume();


	/*
	 * Used to stop collection of context data.
	 * @return   A boolean to indicate the context component stopped correctly.
	 */
	public abstract boolean stop();

	/*
	 * Gets the name of the context component
	 * @return   A String containing the context name.
	 */
    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public void setContextReceiver(ContextReceiver cr) {
        mReceiver = cr;
    }

    public void addRequiringApp(String appid) { mRequiringApps.add(appid); }

    public void removeRequiringApp(String appid) { mRequiringApps.remove(appid); }

    public int numberOfRequiringApps() { return mRequiringApps.size(); }

    public void removeAllRequiringApps() { mRequiringApps.clear(); }

    public boolean isARequiringApp(String appid) { return mRequiringApps.contains(appid); }

    public boolean isRunning() { return mIsRunning; }

}
