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

package org.poseidon_project.context.logging;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import org.poseidon_project.context.ILogBackup;
import org.poseidon_project.context.utility.ExplicitIntentGenerator;

/**
 * BroadcastReceiver for getting alarms for log backup.
 *
 * @author Dean Kramer <d.kramer@mdx.ac.uk>
 */
public class BackupLogAlarmReceiver extends BroadcastReceiver {

    private ILogBackup mLogBackupService;
    private boolean mBound = false;

    public ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mLogBackupService = ILogBackup.Stub.asInterface(service);
            mBound =! mBound;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mLogBackupService = null;
            mBound =! mBound;
        }
    };


    @Override
    public void onReceive(Context context, Intent intent) {

        Intent serviceIntent = new Intent(ILogBackup.class.getName());
        serviceIntent = ExplicitIntentGenerator
                .createExplicitFromImplicitIntent(context, serviceIntent);

        if (serviceIntent != null) {
            context.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        }

        try {
            mLogBackupService.runLogBackup();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }
}
