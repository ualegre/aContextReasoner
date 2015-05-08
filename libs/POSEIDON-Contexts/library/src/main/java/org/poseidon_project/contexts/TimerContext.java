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

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;

/**
 * Abstract class to hold everything required by timer based context components
 * @author Dean Kramer <d.kramer@mdx.ac.uk>
 *
 */
public abstract class TimerContext extends ContextObserver{

	private Timer mTimer;
	private int mInterval = 2000; //default every 2 seconds


	public TimerContext(Context c) {
		super(c);
	}

	public TimerContext(Context c, ContextReceiver cr) {
		super(c, cr);
	}

	public TimerContext(Context c, ContextReceiver cr, int interval, String name) {
        super(c, cr, name);
		mInterval = interval;
	}

	public int getInterval() {
		return mInterval;
	}

	public void setInterval(int interval) {
		mInterval = interval;

		if (mIsRunning) {
			stop();
			start();
		}
	}

	@Override
	public boolean resume() {
		return start();
	}

	@Override
	public boolean pause() {
		return stop();

	}

	@Override
	public boolean start() {
		mTimer = new Timer();

		mTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				checkContext();
			}
		}, mInterval, mInterval);

		mIsRunning = true;

		return true;

	}

	protected abstract void checkContext();

	@Override
	public boolean stop() {
		mTimer.cancel();
		mIsRunning = false;
		return true;
	}

}
