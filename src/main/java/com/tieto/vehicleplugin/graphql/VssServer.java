// Copyright (C) 2020 TietoEVRY
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at http://mozilla.org/MPL/2.0/.

package com.tieto.vehicleplugin.graphql;

interface VssServer {
    // applicable for ENGINE_RPM
    void requestEngineRpm(RequestCallback<Float> callback);

    // applicable for FUEL_LEVEL
    void requestFuelLevel(RequestCallback<Float> callback);

    // applicable for INFO_FUEL_CAPACITY
    void requestFuelCapacity(RequestCallback<Float> callback);

    // applicable for GEAR_SELECTION
    void requestCurrentGear(RequestCallback<Integer> callback);

    interface RequestCallback<T> {
        void onResponse(T value);
    }
}
