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

package org.poseidon_project.context.test;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.poseidon_project.context.IContextReasoner;
import org.poseidon_project.context.R;

public class AIDLTest extends ActionBarActivity {

    private static final String LOGTAG = "AIDLTest";
    private static final String APPID = "TestApp";
    private IContextReasoner mTestService;
    private boolean mBound = false;
    private ServiceConnection mConnection  = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mTestService = IContextReasoner.Stub.asInterface(service);
            mBound = ! mBound;
            Log.d(LOGTAG, "AIDL Service Connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = ! mBound;
            mTestService = null;
            Log.d(LOGTAG, "AIDL Service disconnected");
        }
    };
    private Intent mServiceIntent = new Intent(IContextReasoner.class.getName());
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        mContext = getApplicationContext();


    }

    protected void setupService(View v){

    }

    @Override
    protected void onStart() {
        super.onStart();
        //Bind to service
        mServiceIntent = ExplicitIntentGenerator.createExplicitFromImplicitIntent(mContext, mServiceIntent);
        bindService(mServiceIntent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //unbind from the service
        unbindService(mConnection);
    }

    public void finishTest(View v){
        try {
            mTestService.removeContextRequirement("test", "BatteryContext");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        finish();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_aidltest, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void testStartContexts(View v) {
        try {
            //mTestService.addContextRequirement("test", "BatteryContext");
            mTestService.addContextRequirement("test", "LightContext");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
