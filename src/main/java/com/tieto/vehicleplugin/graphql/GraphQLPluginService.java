// Copyright (C) 2020 TietoEVRY
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at http://mozilla.org/MPL/2.0/.

package com.tieto.vehicleplugin.graphql;

import android.hardware.automotive.vehicle.V2_0.StatusCode;
import android.hardware.automotive.vehicle.V2_0.VehiclePropConfig;
import android.hardware.automotive.vehicle.V2_0.VehiclePropValue;
import android.hardware.automotive.vehicle.V2_0.VehicleProperty;
import android.hardware.automotive.vehicle.V2_0.VehiclePropertyAccess;
import android.hardware.automotive.vehicle.V2_0.VehiclePropertyChangeMode;
import android.hardware.automotive.vehicle.V2_0.VehiclePropertyStatus;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;

import vendor.tieto.hardware.automotive.vehicle.plugin.V1_0.IVehiclePluginCallback;

public class GraphQLPluginService extends vendor.tieto.hardware.automotive.vehicle.plugin.V1_0.IVehiclePlugin.Stub {
    private static final String LOG_TAG = GraphQLPluginService.class.getSimpleName();
    private static final VssServer VSS_SERVER = new GraphQlVssServer();
    private SubscriptionManager mSubscriptionManager;

    @Override
    public ArrayList<VehiclePropConfig> getPropertyConfigurations() throws RemoteException {
        Log.d(LOG_TAG, "getPropertyConfigurations");
        ArrayList propConfigs = new ArrayList<VehiclePropConfig>();

        final VehiclePropConfig currentGearConfig = new VehiclePropConfig();
        currentGearConfig.prop = VehicleProperty.CURRENT_GEAR;
        currentGearConfig.access = VehiclePropertyAccess.READ;
        currentGearConfig.changeMode = VehiclePropertyChangeMode.ON_CHANGE;
        propConfigs.add(currentGearConfig);

        final VehiclePropConfig gearSelectionConfig = new VehiclePropConfig();
        gearSelectionConfig.prop = VehicleProperty.GEAR_SELECTION;
        gearSelectionConfig.access = VehiclePropertyAccess.READ;
        gearSelectionConfig.changeMode = VehiclePropertyChangeMode.ON_CHANGE;
        propConfigs.add(gearSelectionConfig);


        final VehiclePropConfig engineRpmConfig = new VehiclePropConfig();
        engineRpmConfig.prop = VehicleProperty.ENGINE_RPM;
        engineRpmConfig.access = VehiclePropertyAccess.READ;
        engineRpmConfig.changeMode = VehiclePropertyChangeMode.CONTINUOUS;
        engineRpmConfig.minSampleRate = 1.0f;
        engineRpmConfig.minSampleRate = 10.0f;
        propConfigs.add(engineRpmConfig);

        final VehiclePropConfig fuelLevelConfig = new VehiclePropConfig();
        fuelLevelConfig.prop = VehicleProperty.FUEL_LEVEL;
        fuelLevelConfig.access = VehiclePropertyAccess.READ;
        fuelLevelConfig.changeMode = VehiclePropertyChangeMode.CONTINUOUS;
        fuelLevelConfig.minSampleRate = 1.0f;
        fuelLevelConfig.minSampleRate = 10.0f;
        propConfigs.add(fuelLevelConfig);

        final VehiclePropConfig fuelCapacityConfig = new VehiclePropConfig();
        fuelCapacityConfig.prop = VehicleProperty.INFO_FUEL_CAPACITY;
        fuelCapacityConfig.access = VehiclePropertyAccess.READ;
        fuelCapacityConfig.changeMode = VehiclePropertyChangeMode.STATIC;
        propConfigs.add(fuelCapacityConfig);

        return propConfigs;
    }

    @Override
    public void getProperty(final VehiclePropValue requestedPropValue, final getPropertyCallback _hidl_cb) throws RemoteException {
        Log.d(LOG_TAG, "getProperty");
        switch (requestedPropValue.prop) {
            case VehicleProperty.CURRENT_GEAR:
            case VehicleProperty.GEAR_SELECTION:
                VSS_SERVER.requestCurrentGear((currentGear) -> {
                    synchronized (GraphQLPluginService.this) {
                        requestedPropValue.value.int32Values.add(currentGear);
                        requestedPropValue.status = VehiclePropertyStatus.AVAILABLE;
                        GraphQLPluginService.this.notify();
                    }
                });
                break;
            case VehicleProperty.ENGINE_RPM:
                VSS_SERVER.requestEngineRpm((engineRpm) -> {
                    synchronized (GraphQLPluginService.this) {
                        requestedPropValue.value.floatValues.add(engineRpm);
                        requestedPropValue.status = VehiclePropertyStatus.AVAILABLE;
                        GraphQLPluginService.this.notify();
                    }
                });
                break;
            case VehicleProperty.FUEL_LEVEL:
                VSS_SERVER.requestFuelLevel((fuelLevel) -> {
                    synchronized (GraphQLPluginService.this) {
                        requestedPropValue.value.floatValues.add(fuelLevel);
                        requestedPropValue.status = VehiclePropertyStatus.AVAILABLE;
                        GraphQLPluginService.this.notify();
                    }
                });
                break;
            case VehicleProperty.INFO_FUEL_CAPACITY:
                VSS_SERVER.requestFuelCapacity((fuelCapacity) -> {
                    synchronized (GraphQLPluginService.this) {
                        requestedPropValue.value.floatValues.add(fuelCapacity);
                        requestedPropValue.status = VehiclePropertyStatus.AVAILABLE;
                        GraphQLPluginService.this.notify();
                    }
                });
                break;
            default:
                _hidl_cb.onValues(StatusCode.NOT_AVAILABLE, requestedPropValue);
        }
        synchronized (GraphQLPluginService.this) {
            try {
                GraphQLPluginService.this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            _hidl_cb.onValues(StatusCode.OK, requestedPropValue);
        }
    }

    @Override
    public int handlePropChange(VehiclePropValue propValue) throws RemoteException {
        Log.d(LOG_TAG, "handlePropChange");
        return 0;
    }

    @Override
    public int subscribe(IVehiclePluginCallback callback, int propId, float sampleRate) throws RemoteException {
        Log.d(LOG_TAG, "subscribe");

        if (mSubscriptionManager == null) {
            mSubscriptionManager = new SubscriptionManager(VSS_SERVER);
        }
        mSubscriptionManager.subscribe(callback, propId, sampleRate);
        return 0;
    }

    @Override
    public int unsubscribe(int propId) throws RemoteException {
        Log.d(LOG_TAG, "unsubscribe");
        return 0;
    }

    @Override
    public int isInitialized() throws RemoteException {
        Log.d(LOG_TAG, "isInitialized");
        return 0;
    }
}
