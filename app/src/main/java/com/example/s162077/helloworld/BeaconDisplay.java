package com.example.s162077.helloworld;

import android.content.Context;
import android.util.Log;

import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.cloud.model.Telemetry;
import com.estimote.sdk.telemetry.EstimoteTelemetry;

import java.util.List;

/**
 * Created by s162077 on 21-02-2017.
 */

public class BeaconDisplay {

    private BeaconManager beaconManager;
    private BeaconListener beaconListener;
    private String scanId;


    public BeaconDisplay(final Context context, final BeaconModel model, BeaconManager beaconManager) {
       this.beaconManager = beaconManager;
        beaconManager.setTelemetryListener(new BeaconManager.TelemetryListener() {
            @Override
            public void onTelemetriesFound(List<EstimoteTelemetry> telemetries) {
                for (EstimoteTelemetry tlm : telemetries) {
                    if (tlm.deviceId == null ||
                            !tlm.deviceId.toString().equals("[fbc7ed741c620f8c4c6e2d4bc234023a]") ||
                            tlm.ambientLight == null) {
                        continue;
                    }
                    String informationB =
                            "ID:" + tlm.deviceId + "\n"
                                    + "Accelerometer:" + tlm.accelerometer + "\n"
                                    + "temperature:" + tlm.temperature + "\n"
                                    + "timestamp:" + tlm.timestamp;
                    getListener().onDisplay(informationB);

                    BeaconParameters b = new BeaconParameters();
                    b.setClock(tlm.timestamp);
                    b.setAccelerometer(tlm.accelerometer);
                    b.setTemp(tlm.temperature);

                  //  model.createDocument(b);
//                }

                }
            }
        });
    }

//        beaconManager.setTelemetryListener(new BeaconManager.TelemetryListener() {
//            @Override
//            public void onTelemetriesFound(List<EstimoteTelemetry> list) {


    public BeaconListener getListener() {
        return beaconListener;
    }

    public void setBeaconListener(BeaconListener beaconListener) {
        this.beaconListener = beaconListener;
    }

    public void startUpdates() {
      /*  beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                scanId = beaconManager.startTelemetryDiscovery();
            }
        });*/
    }

    public void stopUpdates() {
        beaconManager.stopTelemetryDiscovery(scanId);
    }

    public void destroy() {
        beaconManager.disconnect();
    }


    public interface BeaconListener {
        void onDisplay(String information);
    }
}
