package com.klipspringercui.sgbusgo;

/**
 * Copyright 2014 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.IntentService;
import android.app.Notification;
import android.content.Intent;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

import static com.klipspringercui.sgbusgo.R.id.alightingBusStop;

/**
 * Listener for geofence transition changes.
 *
 * Receives geofence transition events from Location Services in the form of an Intent containing
 * the transition type and geofence id(s) that triggered the transition. Creates a notification
 * as the output.
 */
public class ProximityIntentService extends IntentService implements GetJSONETAData.ETADataAvailableCallable {

    private static final String TAG = "ProximityIntentService";

    /**
     * This constructor is required, and calls the super IntentService(String)
     * constructor with the name for a worker thread.
     */
    public ProximityIntentService() {
        // Use the TAG to name the worker thread.
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    /**
     * Handles incoming intents.
     * @param intent sent by Location Services. This Intent is provided to Location
     *               Services (inside a PendingIntent) when addGeofences() is called.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent: starts");
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            Log.e(TAG, "onHandleIntent: Error in geofencing event");
            return;
        }
        // Get the transition type.
        String description = null;
        String busStopCode = null;
        String busServiceNo = null;

        int geofenceTransition = geofencingEvent.getGeofenceTransition();
        // Test that the reported transition was of interest.
        Bundle bundle = intent.getExtras();
        //BusStop alightingBusStop = null;
        if (bundle != null) {
            description = bundle.getString(BaseActivity.ALIGHTING_BUSSTOP);
            if (description == null)
                description = bundle.getString(BaseActivity.STARTING_BUSSTOP_DESCRIPTION);
            busServiceNo = bundle.getString(BaseActivity.FREQUENT_SERVICE_NO);
            busStopCode = bundle.getString(BaseActivity.STARTING_BUSSTOP_CODE);
        }
        if (description == null) {
            Log.e(TAG, "onHandleIntent: bus stop null");
            return;
        }
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            if (busServiceNo != null && busStopCode != null) {
                Log.d(TAG, "onHandleIntent: ETA Mode initiated");
                GetJSONETAData getETA = new GetJSONETAData(this, BaseActivity.ETA_URL);
                List<ETAItem> etas = getETA.runInSameThread(busStopCode, busServiceNo);
                if (etas != null && etas.size() > 0) {
                    ETAItem etaItem = etas.get(0);
                    Log.d(TAG, "onReceive: building notification");
                    NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(this);
                    nBuilder.setSmallIcon(R.drawable.notification_bus_white)
                            .setDefaults(Notification.DEFAULT_ALL)
                            .setContentTitle("Estimated Arrival Time of " + busServiceNo + " @" + description)
                            .setContentText(" Next Bus: " + etaItem.getArrival1() + "  Subsequent Bus: " + etaItem.getArrival2())
                            .setPriority(Notification.PRIORITY_HIGH);
                    Intent resultIntent = new Intent(getApplicationContext(), ETAActivity.class);
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                    stackBuilder.addParentStack(ETAActivity.class);
                    stackBuilder.addNextIntent(resultIntent);
                    PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_CANCEL_CURRENT);
                    nBuilder.setContentIntent(resultPendingIntent);
                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(1, nBuilder.build());
                }
                return;
            }

            // Get the geofences that were triggered. A single event can trigger multiple geofences.

            Log.d(TAG, "onReceive: building notification");
            NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(this);
            nBuilder.setSmallIcon(R.drawable.notification_bus_white)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setContentTitle("You are about to arrive!")
                    .setContentText("approaching " + description)
                    .setPriority(Notification.PRIORITY_HIGH);
            Intent resultIntent = new Intent(getApplicationContext(), CurrentTripActivity.class);
            Bundle bundle2 = new Bundle();
            bundle.putBoolean(BaseActivity.AA_FROM_NOTIFICATION, true);
            resultIntent.putExtras(bundle2);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addParentStack(CurrentTripActivity.class);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_CANCEL_CURRENT);
            nBuilder.setContentIntent(resultPendingIntent);

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(1, nBuilder.build());
            //operations here
        } else {
            // Log the error.
            Log.e(TAG, "onHandleIntent: Geofence transition - invalid type");
        }
    }

    @Override
    public void onETADataAvailable(List<ETAItem> data, String serviceNo, String busStopCode) {

    }
}
