// Copyright (C) 2020 TietoEVRY
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at http://mozilla.org/MPL/2.0/.

package com.tieto.vehicleplugin.graphql;

import android.hardware.automotive.vehicle.V2_0.VehicleGear;
import android.util.Log;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import org.genivi.vss.GetCurrentGearQuery;
import org.genivi.vss.GetEngineRpmQuery;
import org.genivi.vss.GetFuelDataQuery;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

class GraphQlVssServer implements VssServer {
    private static final String LOG_TAG = GraphQlVssServer.class.getSimpleName();
    private static final String DEFAULT_SERVER_URL = "http://192.168.56.101:4000/";

    private static final Map<Integer, Integer> GEAR_MAPPING = new HashMap<Integer, Integer>();

    static {
        GEAR_MAPPING.put(-1, VehicleGear.GEAR_REVERSE);
        GEAR_MAPPING.put(0, VehicleGear.GEAR_NEUTRAL);
        GEAR_MAPPING.put(1, VehicleGear.GEAR_1);
        GEAR_MAPPING.put(2, VehicleGear.GEAR_2);
        GEAR_MAPPING.put(3, VehicleGear.GEAR_3);
        GEAR_MAPPING.put(4, VehicleGear.GEAR_4);
        GEAR_MAPPING.put(5, VehicleGear.GEAR_5);
    }

    private final ApolloClient mApolloClient;

    GraphQlVssServer() {
        this(DEFAULT_SERVER_URL);
    }

    GraphQlVssServer(String serverUrl) {
        mApolloClient = ApolloClient.builder()
                .serverUrl(serverUrl) // TODO make it configurable
                .build();
    }

    @Override
    public void requestEngineRpm(RequestCallback<Float> callback) {
        mApolloClient.query(new GetEngineRpmQuery()).enqueue(new ApolloCall.Callback<GetEngineRpmQuery.Data>() {
            @Override
            public void onResponse(@NotNull Response<GetEngineRpmQuery.Data> response) {
                // TODO Do sanity check of the response and its content
                Log.d(LOG_TAG, "Response" + response.getData() + "\n");

                int engineRpm = response.getData().vehicle().drivetrain().internalCombustionEngine().engine().speed();
                callback.onResponse((float) engineRpm);
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
                Log.e(LOG_TAG, "Error in executing GetVehicleSpeed query", e);
            }
        });
    }

    @Override
    public void requestFuelLevel(RequestCallback<Float> callback) {
        mApolloClient.query(new GetFuelDataQuery()).enqueue(new ApolloCall.Callback<GetFuelDataQuery.Data>() {
            @Override
            public void onResponse(@NotNull Response<GetFuelDataQuery.Data> response) {
                // TODO Do sanity check of the response and its content
                Log.d(LOG_TAG, "Response" + response.getData() + "\n");

                // Level in fuel tank as percent of capacity. 0 = empty. 100 = full.
                int level = response.getData().vehicle().drivetrain().fuelSystem().level();

                // Capacity of the fuel tank in liters
                int tankCapacity = response.getData().vehicle().drivetrain().fuelSystem().tankCapacity();

                // Android for FUEL_LEVEL expects: "Fuel remaining in the the vehicle, in milliliters"
                callback.onResponse(level / 100f * tankCapacity * 1000);
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
                Log.e(LOG_TAG, "Error in executing GetFuelData query", e);
            }
        });
    }

    @Override
    public void requestFuelCapacity(RequestCallback<Float> callback) {
        mApolloClient.query(new GetFuelDataQuery()).enqueue(new ApolloCall.Callback<GetFuelDataQuery.Data>() {
            @Override
            public void onResponse(@NotNull Response<GetFuelDataQuery.Data> response) {
                // TODO Do sanity check of the response and its content
                Log.d(LOG_TAG, "Response" + response.getData() + "\n");

                // Capacity of the fuel tank in liters
                int tankCapacity = response.getData().vehicle().drivetrain().fuelSystem().tankCapacity();

                // Android for FUEL_LEVEL expects: "Fuel remaining in the the vehicle, in milliliters"
                callback.onResponse(tankCapacity * 1000f);
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
                Log.e(LOG_TAG, "Error in executing GetFuelData query", e);
            }
        });
    }

    @Override
    public void requestCurrentGear(final RequestCallback<Integer> callback) {
        mApolloClient.query(new GetCurrentGearQuery()).enqueue(new ApolloCall.Callback<GetCurrentGearQuery.Data>() {
            @Override
            public void onResponse(@NotNull Response<GetCurrentGearQuery.Data> response) {
                // TODO Do sanity check of the response and its content
                Log.d(LOG_TAG, "Response" + response.getData() + "\n");

                // Current gear. 0=Neutral. -1=Reverse
                int currentGear = response.getData().vehicle().drivetrain().transmission().gear();

                callback.onResponse(GEAR_MAPPING.get(currentGear));
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
                Log.e(LOG_TAG, "Error in executing GetCurrentGear query", e);
            }
        });
    }
}
