package com.example.s162077.helloworld;

import com.cloudant.sync.documentstore.DocumentRevision;
import com.estimote.sdk.connection.settings.Sensors;
import com.estimote.sdk.telemetry.Vector;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by s162077 on 21-02-2017.
 */

public class BeaconParameters {
    double light;
    double temp;
    Date clock;
    Vector accelerometer;

    private DocumentRevision rev;
    public DocumentRevision getDocumentRevision() {
        return rev;
    }

    private String type = DOC_TYPE;
    static final String DOC_TYPE = "com.cloudant.sync.example.task";//??????????????
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }


    public Map<String, Object> asMap() {
        // this could also be done by a fancy object mapper
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("Light",getLight());
        map.put("Temperature", getTemp());
        map.put("Clock", getClock());
        map.put("Accelerometer",getAccelerometer());
        return map;
    }

    public static BeaconParameters fromRevision(DocumentRevision rev) {
        BeaconParameters beacon = new BeaconParameters();
        beacon.rev = rev;
        // this could also be done by a fancy object mapper
        Map<String, Object> map = rev.getBody().asMap();
        if (map.containsKey("type") && map.get("type").equals(BeaconParameters.DOC_TYPE)) {
            beacon.setType((String) map.get("type")); //type到底是什么值啊
            return beacon;
        }
        return null;
    }

    public double getLight() {
        return light;
    }

    public void setLight(double light) {
        this.light = light;
    }

    public double getTemp() {
        return temp;
    }

    public void setTemp(double temp) {
        this.temp = temp;
    }

    public Date getClock() {
        return clock;
    }

    public void setClock(Date clock) {
        this.clock = clock;
    }

    public Vector getAccelerometer() {
        return accelerometer;
    }

    public void setAccelerometer(Vector accelerometer) {
        this.accelerometer = accelerometer;
    }
}
