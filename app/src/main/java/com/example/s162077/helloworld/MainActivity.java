package com.example.s162077.helloworld;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.cloudant.sync.documentstore.DocumentStoreException;
import com.estimote.sdk.SystemRequirementsChecker;

import java.net.URISyntaxException;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private StatusDisplay statusDisplay;
    private static final String LOG_TAG = "MainActivity";

    private CoordinateModel coord;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);//存了之前app的状态
        setContentView(R.layout.activity_main);
        coord = new CoordinateModel(this.getApplicationContext()) ;
        statusDisplay = new StatusDisplay(this.getApplicationContext(),coord);
        statusDisplay.setListener(new StatusDisplay.Listener() {

            @Override
            public void onDisplay(String information) {
                ((EditText) findViewById(R.id.edit_message)).setText(information);
            }
        });
        findViewById(R.id.pushToCloud).setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                coord.startPushReplication();
            }
        });


    }

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
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "Stopping ShowroomManager updates");
        statusDisplay.stopUpdates();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        statusDisplay.destroy();
    }



}



