/*
 * Copyright 2016 POSEIDON Project
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

package org.poseidon_project.context.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import org.poseidon_project.context.IContextPreference;
import org.poseidon_project.context.R;
import org.poseidon_project.context.utility.ExplicitIntentGenerator;

import no.tellu.findit.client.api.AsyncService;
import no.tellu.findit.client.api.AsyncServiceImpl;


/**
 * The user visible activity to personalise context rules
 *
 * @author Dean Kramer <d.kramer@mdx.ac.uk>
 */
public class ContextReasonerSettings extends Activity implements DialogReturnInterface {

    public IContextPreference mContextService;
    private boolean mBound = false;
    private Context mContext;

    //Tellu related
    private AsyncService mTelluApiService;
    private FragmentManager mFragManager;
    private final static String mTelluApiServiceURL = "https://ri.smarttracker.no/web";
    private ContextReasonerSettingsFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getApplicationContext();

        mFragManager = getFragmentManager();

        mFragment = new ContextReasonerSettingsFragment();

        mFragManager.beginTransaction()
                .replace(android.R.id.content, mFragment)
                .commit();

        bindToService();
    }

    private void bindToService() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Intent serviceIntent = new Intent(IContextPreference.class.getName());

                serviceIntent = ExplicitIntentGenerator
                        .createExplicitFromImplicitIntent(mContext, serviceIntent);
                if (serviceIntent != null) {
                    bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
                    Log.d("POSEIDON-Context", "Context Reasoner Middleware is connected");
                } else {
                    Log.e("POSEIDON-Context", "Context Reasoner Middleware not installed!");
                }
            }
        }).start();
    }

    public boolean isBound(){
        return mBound;
    }

    @Override
    protected void onPause() {
        super.onPause();
        Fragment logindialog = mFragManager.findFragmentByTag("logindialog");

        if (logindialog != null) {
            LoginDialogFragment df = (LoginDialogFragment) logindialog;
            df.dismiss();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LoginDialogFragment loginFrag = LoginDialogFragment.newInstance(R.string.logintitle);
        loginFrag.show(mFragManager, "logindialog");
    }

    public void authenticateUser(String username, String password) {
        mTelluApiService = new AsyncServiceImpl(mTelluApiServiceURL);

        AsyncService.AccountCallback callback = new AsyncService.AccountCallback() {
            @Override
            public void onAccountOK(long customerId, String customer) {
                Toast.makeText(getApplicationContext(), R.string.authyes, Toast.LENGTH_SHORT).show();
                mFragment.loggedIn();
            }

            @Override
            public void onAccountError(int code, String message, Exception e) {
                Toast.makeText(getApplicationContext(), R.string.authno, Toast.LENGTH_SHORT).show();
                LoginDialogFragment loginFrag = LoginDialogFragment.newInstance(R.string.logintitle);
                loginFrag.show(mFragManager, "logindialog");

            }
        };

        mTelluApiService.initiate(username, password, callback);

    }

    @Override
    public void doPositiveButtonClick(Object... para) {
        authenticateUser((String) para[0], (String) para[1]);
    }

    @Override
    public void doNegativeButtonClick(Object... para) {
        finish();
    }

    @Override
    public void doNeutralButtonClick(Object... para) {

    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mContextService = IContextPreference.Stub.asInterface(service);
            mBound = ! mBound;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mContextService = null;
            mBound = ! mBound;
        }
    };

    public void unBindFromService() {
        if (mBound) {
            unbindService(mConnection);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unBindFromService();
    }
}
