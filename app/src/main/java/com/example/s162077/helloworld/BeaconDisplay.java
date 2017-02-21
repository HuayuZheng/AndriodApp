package com.example.s162077.helloworld;

import android.content.Context;

import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.cloud.model.Telemetry;
import com.estimote.sdk.telemetry.EstimoteTelemetry;

import java.util.List;

/**
 * Created by s162077 on 21-02-2017.
 */

public class BeaconDisplay {

    private BeaconManager beaconManager;
    private BeaconDisplay.Listener listener;
    private String scanId;


    public BeaconDisplay(final Context context, final BeaconModel model) {
        beaconManager = new BeaconManager(context);
        beaconManager.setTelemetryListener(new BeaconManager.TelemetryListener() {
            @Override
            public void onTelemetriesFound(List<EstimoteTelemetry> list) {
                for (EstimoteTelemetry telemetry : list) {
                    if (!telemetry.deviceId.equals("fbc7ed741c620f8c4c6e2d4bc234023a")) {
                        continue;
                    }
                    String informationB =
                            "ID:" + telemetry.deviceId + "\n"
                                    + "Accelerometer:" + telemetry.accelerometer + "\n"
                                    + "temperature:" + telemetry.temperature + "\n"
                                    + "timestamp:" + telemetry.timestamp;
                    getListener().onDisplay(informationB);

                    BeaconParameters b = new BeaconParameters();
                    b.setClock(telemetry.timestamp);
                    b.setAccelerometer(telemetry.accelerometer);
                    b.setTemp(telemetry.temperature);

                    model.createDocument(b);
                }
            }
        });
    }

    public BeaconDisplay.Listener getListener() {
        return listener;
    }

    public void setListener(BeaconDisplay.Listener listener) {
        this.listener = listener;
    }

    public void startUpdates() {
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                scanId = beaconManager.startNearableDiscovery();
            }
        });
    }

    public void stopUpdates() {
        beaconManager.stopNearableDiscovery(scanId);
    }

    public void destroy() {
        beaconManager.disconnect();
    }



    public interface Listener {
        void onDisplay(String information);
    }
}
