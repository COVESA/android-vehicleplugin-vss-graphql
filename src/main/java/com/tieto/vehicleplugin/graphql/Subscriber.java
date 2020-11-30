// Copyright (C) 2020 TietoEVRY
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at http://mozilla.org/MPL/2.0/.

package com.tieto.vehicleplugin.graphql;

import android.hardware.automotive.vehicle.V2_0.VehiclePropValue;
import android.os.RemoteException;

import java.time.Duration;
import java.time.Instant;

import vendor.tieto.hardware.automotive.vehicle.plugin.V1_0.IVehiclePluginCallback;

class Subscriber {
    private final IVehiclePluginCallback mCallback;
    private final Duration mNotificationPeriod;
    private VehiclePropValue mLastValue = new VehiclePropValue();
    private Instant mLastUpdateTime = Instant.MIN;

    Subscriber(IVehiclePluginCallback callback, float sampleRate) {
        mCallback = callback;
        mNotificationPeriod = Duration.ofMillis((long) (1.0 / sampleRate * 1000));
    }

    void onPropertyEvent(VehiclePropValue value) {
        if (!(mLastValue.value.equals(value.value))
                && (Duration.between(mLastUpdateTime, Instant.now()).compareTo(mNotificationPeriod) >= 0)) {
            try {
                mCallback.onPropertyEvent(value);
            } catch (RemoteException e) {
                // subscriber is not informed so not updating the cached value;
                e.printStackTrace();
                return;
            }
            mLastValue = value;
            mLastUpdateTime = Instant.now();
        }
    }
}
