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
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import org.poseidon_project.context.R;

/**
 * Reusable Login dialog
 *
 * @author Dean Kramer <d.kramer@mdx.ac.uk>
 */
public class LoginDialogFragment extends DialogFragment {

    private EditText mUsername;
    private EditText mPassword;
    private AlertDialog mDialog;
    private Activity mActivity;

    public static LoginDialogFragment newInstance(int title) {
        LoginDialogFragment fragment = new LoginDialogFragment();
        Bundle args = new Bundle();
        args.putInt("title", title);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        int title = getArguments().getInt("title");
        mActivity = getActivity();

        mDialog = new AlertDialog.Builder(mActivity)
                .setTitle(title)
                .setPositiveButton(R.string.login,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                doPositiveClick();
                            }
                        })
                .setNegativeButton(R.string.cancelTime,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                doNegativeClick();
                            }
                        })
                .create();

        View dialogView = mActivity.getLayoutInflater().inflate(R.layout.logindialog, null);
        mDialog.setView(dialogView);

        mUsername = (EditText) dialogView.findViewById(R.id.txt_username);
        mPassword = (EditText) dialogView.findViewById(R.id.txt_password);

        return mDialog;
    }

    private void doPositiveClick() {

        String username = mUsername.getText().toString();
        String password = mPassword.getText().toString();

        ((DialogReturnInterface) getActivity()).doPositiveButtonClick(username, password);
    }

    private void doNegativeClick() {
        ((DialogReturnInterface) getActivity()).doNegativeButtonClick();
    }

    public void setUsername(String username) {
        mUsername.setText(username);
    }

    public void setPassword(String password) {
        mPassword.setText(password);
    }

}
