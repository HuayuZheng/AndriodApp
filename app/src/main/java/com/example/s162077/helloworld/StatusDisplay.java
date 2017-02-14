package com.example.s162077.helloworld;

import android.content.Context;

import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Nearable;

import java.util.List;

/**
 * Created by s162077 on 22-01-2017.
 */

public class StatusDisplay {
    private BeaconManager beaconManager;
    private Listener listener;
    private String scanId;

    public StatusDisplay(Context context) {

        beaconManager = new BeaconManager(context);
        beaconManager.setNearableListener(new BeaconManager.NearableListener() {

            @Override
            public void onNearablesDiscovered(List<Nearable> list) {
                //login database
                 for (Nearable nearable : list) {
                    if (!nearable.identifier.equals("2fade4429af7aa73") ) {
                        continue;
                    }
                    String information =
                            "ID:" + nearable.identifier + "\n"
                            + "temp:" + nearable.temperature +"\n"
                            + "x acc:" + nearable.xAcceleration + "\n"
                            + "y acc:" + nearable.yAcceleration + "\n"
                            + "z acc:" + nearable.zAcceleration;
                    getListener().onDisplay(information);
                     Coordinate c = new Coordinate();
                     c.setX(nearable.xAcceleration);
                     c.setY(nearable.yAcceleration);
                     c.setZ(nearable.zAcceleration);

                    ;//????????????????????
//                    try {
//                        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//                        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
//                        r.play();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }


                }
            }
        });
    }



    public Listener getListener() {
        return listener;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public interface Listener {
        void onDisplay(String information);
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
}
