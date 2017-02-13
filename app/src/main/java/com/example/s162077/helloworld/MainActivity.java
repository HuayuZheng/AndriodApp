package com.example.s162077.helloworld;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import com.cloudant.sync.documentstore.DocumentStoreException;
import com.estimote.sdk.SystemRequirementsChecker;

import java.util.List;


public class MainActivity extends AppCompatActivity {
    private StatusDisplay statusDisplay;
    private static final String TAG = "MainActivity";

    private static final int DIALOG_NEW_TASK = 1;
    private static final int DIALOG_PROGRESS = 2;

    static final String SETTINGS_CLOUDANT_USER = "pref_key_username";
    static final String SETTINGS_CLOUDANT_DB = "pref_key_dbname";
    static final String SETTINGS_CLOUDANT_API_KEY = "pref_key_api_key";
    static final String SETTINGS_CLOUDANT_API_SECRET = "pref_key_api_password";

    // Main data model object
    private static TasksModel sTasks;
    private TaskAdapter mTaskAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);//存了之前app的状态
        setContentView(R.layout.activity_main);

        statusDisplay = new StatusDisplay(this);
        statusDisplay.setListener(new StatusDisplay.Listener() {

            @Override
            public void onDisplay(String information) {
                ((EditText) findViewById(R.id.edit_message)).setText(information);
            }
        });
        findViewById(R.id.settingActivity).setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, MainActivity.class);
            }
        });

        // Load default settings when we're first created.
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // Register to listen to the setting changes because replicators
        // uses information managed by shared preference.
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPref.registerOnSharedPreferenceChangeListener(this);

        // Protect creation of static variable.
        if (sTasks == null) {
            // Model needs to stay in existence for lifetime of app.
            this.sTasks = new TasksModel(this.getApplicationContext());
        }

        // Register this activity as the listener to replication updates
        // while its active.
        this.sTasks.setReplicationListener(this);

        // Load the tasks from the model
        this.reloadTasksFromModel();

    }

    private void reloadTasksFromModel() {
        try {
            List<Coordinate> coordinates = this.sTasks.coordinates();
            this.mTaskAdapter = new TaskAdapter(this, coordinates);
            this.setListAdapter(this.mTaskAdapter);
        } catch (DocumentStoreException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        View emptyView = findViewById(com.android.internal.R.id.empty);
        mList = (ListView)findViewById(com.android.internal.R.id.list);
        if (mList == null) {
            throw new RuntimeException(
                    "Your content must have a ListView whose id attribute is " +
                            "'android.R.id.list'");
        }
        if (emptyView != null) {
            mList.setEmptyView(emptyView);
        }
        mList.setOnItemClickListener(mOnClickListener);
        if (mFinishedStart) {
            setListAdapter(mAdapter);
        }
        mHandler.post(mRequestFocus);
        mFinishedStart = true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!SystemRequirementsChecker.checkWithDefaultDialogs(this)) {
            Log.e(TAG, "Can't scan for beacons, some pre-conditions were not met");
            Log.e(TAG, "Read more about what's required at: http://estimote.github.io/Android-SDK/JavaDocs/com/estimote/sdk/SystemRequirementsChecker.html");
            Log.e(TAG, "If this is fixable, you should see a popup on the app's screen right now, asking to enable what's necessary");
        } else {
            Log.d(TAG, "Starting ShowroomManager updates");
            statusDisplay.startUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "Stopping ShowroomManager updates");
        statusDisplay.stopUpdates();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        statusDisplay.destroy();
        this.sTasks.setReplicationListener(null);
    }

}



