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
import android.app.FragmentManager;
import android.os.Bundle;
import android.widget.Toast;

import org.poseidon_project.context.R;

import no.tellu.findit.client.api.AsyncService;
import no.tellu.findit.client.api.AsyncServiceImpl;


/**
 * The user visible activity to personalise context rules
 *
 * @author Dean Kramer <d.kramer@mdx.ac.uk>
 */
public class ContextReasonerSettings extends Activity implements DialogReturnInterface {

    //Tellu related
    private AsyncService mTelluApiService;
    private FragmentManager mFragManager;
    private final static String mTelluApiServiceURL = "https://ri.smarttracker.no/web";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFragManager = getFragmentManager();

        mFragManager.beginTransaction()
                .replace(android.R.id.content, new ContextReasonerSettingsFragment())
                .commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LoginDialogFragment loginFrag = LoginDialogFragment.newInstance(R.string.logintitle);
        loginFrag.show(mFragManager, "dialog");
    }

    public void authenticateUser(String username, String password) {
        mTelluApiService = new AsyncServiceImpl(mTelluApiServiceURL);

        AsyncService.AccountCallback callback = new AsyncService.AccountCallback() {
            @Override
            public void onAccountOK(long customerId, String customer) {
                Toast.makeText(getApplicationContext(), R.string.authyes, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAccountError(int code, String message, Exception e) {
                Toast.makeText(getApplicationContext(), R.string.authno, Toast.LENGTH_SHORT).show();
                LoginDialogFragment loginFrag = LoginDialogFragment.newInstance(R.string.logintitle);
                loginFrag.show(mFragManager, "dialog");

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
}
