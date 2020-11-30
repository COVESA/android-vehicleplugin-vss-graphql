// Copyright (C) 2020 TietoEVRY
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at http://mozilla.org/MPL/2.0/.

package com.tieto.vehicleplugin.graphql;

import android.hardware.automotive.vehicle.V2_0.VehiclePropValue;
import android.hardware.automotive.vehicle.V2_0.VehicleProperty;
import android.hardware.automotive.vehicle.V2_0.VehiclePropertyStatus;
import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vendor.tieto.hardware.automotive.vehicle.plugin.V1_0.IVehiclePluginCallback;

class SubscriptionManager {
    static {
        Looper.prepare();
    }

    private final VssServer mVssServer;
    private final Map<Integer, List<Subscriber>> mSubscribers = new HashMap<>();
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    SubscriptionManager(VssServer vssServer) {
        mVssServer = vssServer;

        Runnable requestCurrentGearTask = new Runnable() {
            @Override
            public void run() {
                List<Subscriber> propertySubscribers = mSubscribers.get(VehicleProperty.CURRENT_GEAR);
                if (propertySubscribers != null) {
                    mVssServer.requestCurrentGear((currentGear) -> {
                        VehiclePropValue requestedPropValue = new VehiclePropValue();
                        requestedPropValue.prop = VehicleProperty.CURRENT_GEAR;
                        requestedPropValue.value.int32Values.add(currentGear);
                        requestedPropValue.status = VehiclePropertyStatus.AVAILABLE;
                        synchronized (mSubscribers) {
                            for (Subscriber subscriber : propertySubscribers) {
                                subscriber.onPropertyEvent(requestedPropValue);
                            }
                        }
                    });
                }
                mHandler.postDelayed(this, 100);
            }
        };
        mHandler.post(requestCurrentGearTask);

        Runnable requestEngineRpmTask = new Runnable() {
            @Override
            public void run() {
                List<Subscriber> propertySubscribers = mSubscribers.get(VehicleProperty.ENGINE_RPM);
                if (propertySubscribers != null) {
                    mVssServer.requestEngineRpm((engineRpm) -> {
                        VehiclePropValue requestedPropValue = new VehiclePropValue();
                        requestedPropValue.prop = VehicleProperty.ENGINE_RPM;
                        requestedPropValue.value.floatValues.add(engineRpm);
                        requestedPropValue.status = VehiclePropertyStatus.AVAILABLE;
                        synchronized (mSubscribers) {
                            for (Subscriber subscriber : propertySubscribers) {
                                subscriber.onPropertyEvent(requestedPropValue);
                            }
                        }
                    });
                }
                mHandler.postDelayed(this, 100);
            }
        };
        mHandler.post(requestEngineRpmTask);

        Runnable requestFuelLevelTask = new Runnable() {
            @Override
            public void run() {
                List<Subscriber> propertySubscribers = mSubscribers.get(VehicleProperty.FUEL_LEVEL);
                if (propertySubscribers != null) {
                    mVssServer.requestFuelLevel((fuelLevel) -> {
                        VehiclePropValue requestedPropValue = new VehiclePropValue();
                        requestedPropValue.prop = VehicleProperty.FUEL_LEVEL;
                        requestedPropValue.value.floatValues.add(fuelLevel);
                        requestedPropValue.status = VehiclePropertyStatus.AVAILABLE;
                        synchronized (mSubscribers) {
                            for (Subscriber subscriber : propertySubscribers) {
                                subscriber.onPropertyEvent(requestedPropValue);
                            }
                        }
                    });
                }
                mHandler.postDelayed(this, 100);
            }
        };
        mHandler.post(requestFuelLevelTask);
    }

    void subscribe(IVehiclePluginCallback callback, int propId, float sampleRate) {
        List<Subscriber> propertySubscribers = mSubscribers.get(propId);
        if (propertySubscribers == null) {
            propertySubscribers = new ArrayList<>();
        }
        propertySubscribers.add(new Subscriber(callback, sampleRate));
        mSubscribers.put(propId, propertySubscribers);
    }
}
