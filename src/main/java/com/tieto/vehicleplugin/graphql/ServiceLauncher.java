// Copyright (C) 2020 TietoEVRY
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at http://mozilla.org/MPL/2.0/.

package com.tieto.vehicleplugin.graphql;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class ServiceLauncher extends Service {
    GraphQLPluginService service = new GraphQLPluginService();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            service.registerAsService("graphql");
        } catch (RemoteException e) {
            Log.e("ServiceLauncher", "Unable to register HWService.");
        }

        String NOTIFICATION_CHANNEL_ID = "Launcher notification channel";

        getSystemService(NotificationManager.class).createNotificationChannel(
                new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_ID,
                        NotificationManager.IMPORTANCE_DEFAULT));

        Notification status = new Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.sym_def_app_icon)
                .setContentTitle("GraphQl hal running")
                .build();
        startForeground(1, status);

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
