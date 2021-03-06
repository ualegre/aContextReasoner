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

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import org.poseidon_project.context.ILogBackup;
import org.poseidon_project.context.utility.ExplicitIntentGenerator;

/**
 * BroadcastReceiver for getting alarms for log backup.
 *
 * @author Dean Kramer <d.kramer@mdx.ac.uk>
 */
public class BackupLogAlarmReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {


        Intent serviceIntent = new Intent(ILogBackup.class.getName());

        serviceIntent = ExplicitIntentGenerator
                .createExplicitFromImplicitIntent(context, serviceIntent);

        startWakefulService(context, serviceIntent);

    }
}
