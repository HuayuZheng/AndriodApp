package com.example.s162077.helloworld;

import android.app.Application;

import com.estimote.sdk.EstimoteSDK;

//
// Running into any issues? Drop us an email to: contact@estimote.com
//

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // TODO: put your App ID and App Token here
        // You can get them by adding your app on https://cloud.estimote.com/#/apps
        EstimoteSDK.initialize(getApplicationContext(), "getbeacon-cm9", "cde46e60643eaff8b08f17b07b6e1a75");

        // uncomment to enable debug-level logging
        // it's usually only a good idea when troubleshooting issues with the Estimote SDK
//        EstimoteSDK.enableDebugLogging(true);
    }
}
