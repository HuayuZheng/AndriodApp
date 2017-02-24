package com.example.s162077.helloworld;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.SystemRequirementsChecker;


public class MainActivity extends AppCompatActivity {
    private StatusDisplay statusDisplay;
    private BeaconDisplay beaconDisplay;
    private static final String LOG_TAG = "MainActivity";

    private static CoordinateModel coord;
    private static BeaconModel beacon;
    private BeaconManager beaconManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);//存了之前app的状态
        setContentView(R.layout.activity_main);

        coord = new CoordinateModel(this.getApplicationContext()) ;
        beacon = new BeaconModel(this.getApplicationContext());
        beaconManager = new BeaconManager(this.getApplicationContext());
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startNearableDiscovery();
                beaconManager.startTelemetryDiscovery();
            }
        });
        beaconDisplay = new BeaconDisplay(this.getApplicationContext(),beacon,beaconManager);
        statusDisplay = new StatusDisplay(this.getApplicationContext(),coord,beaconManager);
        statusDisplay.setListener(new StatusDisplay.Listener() {

            @Override
            public void onDisplay(String informationA) {
                ((TextView) findViewById(R.id.sticker)).setText(informationA);
            }
        });

        findViewById(R.id.pushToCloud).setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                coord.startPushReplication();
            }


        });

        beaconDisplay.setBeaconListener(new BeaconDisplay.BeaconListener() {
            @Override
            public void onDisplay(String informationB) {
                ((TextView)findViewById(R.id.beacon)).setText(informationB);
            }
        });


        };





    @Override
    protected void onResume() {
        super.onResume();

        if (!SystemRequirementsChecker.checkWithDefaultDialogs(this)) {
            Log.e(LOG_TAG, "Can't scan for beacons, some pre-conditions were not met");
            Log.e(LOG_TAG, "Read more about what's required at: http://estimote.github.io/Android-SDK/JavaDocs/com/estimote/sdk/SystemRequirementsChecker.html");
            Log.e(LOG_TAG, "If this is fixable, you should see a popup on the app's screen right now, asking to enable what's necessary");
        } else {
            Log.d(LOG_TAG, "Starting ShowroomManager updates");
            statusDisplay.startUpdates();
            beaconDisplay.startUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "Stopping ShowroomManager updates");
        statusDisplay.stopUpdates();
        beaconDisplay.stopUpdates();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        statusDisplay.destroy();
        beaconDisplay.destroy();
    }



}



