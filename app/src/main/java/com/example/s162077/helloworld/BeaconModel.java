package com.example.s162077.helloworld;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.cloudant.sync.documentstore.ConflictException;
import com.cloudant.sync.documentstore.DocumentBodyFactory;
import com.cloudant.sync.documentstore.DocumentException;
import com.cloudant.sync.documentstore.DocumentNotFoundException;
import com.cloudant.sync.documentstore.DocumentRevision;
import com.cloudant.sync.documentstore.DocumentStore;
import com.cloudant.sync.documentstore.DocumentStoreException;
import com.cloudant.sync.documentstore.DocumentStoreNotOpenedException;
import com.cloudant.sync.event.Subscribe;
import com.cloudant.sync.event.notifications.ReplicationCompleted;
import com.cloudant.sync.event.notifications.ReplicationErrored;
import com.cloudant.sync.replication.Replicator;
import com.cloudant.sync.replication.ReplicatorBuilder;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by s162077 on 21-02-2017.
 */


public class BeaconModel {

    private static final String LOG_TAG = "CoordinateModel";

    private static final String DOCUMENT_STORE_DIR = "data";
    private static final String DOCUMENT_STORE_NAME = "tasks";

    private DocumentStore mDocumentStore;

    private Replicator mPushReplicator;
    private Replicator mPullReplicator;

    private final Context mContext;
    private final Handler mHandler;
    private MainActivity mListener;

    public BeaconModel(Context context) {

        this.mContext = context;

        // Set up our tasks DocumentStore within its own folder in the applications
        // data directory.
        File path = this.mContext.getApplicationContext().getDir(
                DOCUMENT_STORE_DIR,
                Context.MODE_PRIVATE
        );

        try {
            this.mDocumentStore = DocumentStore.getInstance(new File(path, DOCUMENT_STORE_NAME));
        } catch (DocumentStoreNotOpenedException e) {
            Log.e(LOG_TAG, "Unable to open DocumentStore", e);
        }

        Log.d(LOG_TAG, "Set up database at " + path.getAbsolutePath());

        // Set up the replicator objects from the app's settings.
        try {
            this.reloadReplicationSettings();
        } catch (URISyntaxException e) {
            Log.e(LOG_TAG, "Unable to construct remote URI from configuration", e);
        }

        // Allow us to switch code called by the ReplicationListener into
        // the main thread so the UI can update safely.
        this.mHandler = new Handler(Looper.getMainLooper());

        Log.d(LOG_TAG, "CoordinateModel set up " + path.getAbsolutePath());
    }


    /**
     * Sets the listener for replication callbacks as a weak reference.
     * @param listener {@link MainActivity} to receive callbacks.
     */
    public void setReplicationListener(MainActivity listener) {
        this.mListener = listener;
    }//copy to remote database

    /**
     * Creates a task, assigning an ID.
     * @param task task to create
     * @return new revision of the document
     */
    public BeaconParameters createDocument(BeaconParameters task) {
        DocumentRevision rev = new DocumentRevision();
        rev.setBody(DocumentBodyFactory.create(task.asMap()));
        try {
            DocumentRevision created = this.mDocumentStore.database().create(rev);
            return BeaconParameters.fromRevision(created);
        } catch (DocumentException de) {
            return null;
        } catch (DocumentStoreException de) {
            return null;
        }
    }

    /**
     * Updates a Task document within the DocumentStore.
     * @param task task to update
     * @return the updated revision of the Task
     * @throws ConflictException if the task passed in has a rev which doesn't
     *      match the current rev in the DocumentStore.
     * @throws DocumentStoreException if there was an error updating the rev for this task
     */
    public BeaconParameters updateDocument(BeaconParameters task) throws ConflictException, DocumentStoreException {
        DocumentRevision rev = task.getDocumentRevision();
        rev.setBody(DocumentBodyFactory.create(task.asMap()));
        try {
            DocumentRevision updated = this.mDocumentStore.database().update(rev);
            return BeaconParameters.fromRevision(updated);
        } catch (DocumentException de) {
            return null;
        }
    }

    /**
     * Deletes a Task document within the DocumentStore.
     * @param task task to delete
     * @throws ConflictException if the task passed in has a rev which doesn't
     *      match the current rev in the DocumentStore.
     * @throws DocumentNotFoundException if the rev for this task does not exist
     * @throws DocumentStoreException if there was an error deleting the rev for this task
     */
    public void deleteDocument(Coordinate task) throws ConflictException, DocumentNotFoundException, DocumentStoreException {
        this.mDocumentStore.database().delete(task.getDocumentRevision());
    }

    /**
     * <p>Returns all {@code Task} documents in the DocumentStore.</p>
     */
    public List<BeaconParameters> coordinates() throws DocumentStoreException {
        int nDocs = this.mDocumentStore.database().getDocumentCount();
        List<DocumentRevision> all = this.mDocumentStore.database().read(0, nDocs, true);
        List<BeaconParameters> beaconParameterses = new ArrayList<BeaconParameters>();

        // Filter all documents down to those of type Task.
        for(DocumentRevision rev : all) {
            BeaconParameters b = BeaconParameters.fromRevision(rev);
            if (b != null) {
                beaconParameterses.add(b);
            }
        }

        return beaconParameterses;
    }

    //
    // MANAGE REPLICATIONS
    //

    /**
     * <p>Stops running replications.</p>
     *
     * <p>The stop() methods stops the replications asynchronously, see the
     * replicator docs for more information.</p>
     */
    public void stopAllReplications() {
        if (this.mPullReplicator != null) {
            this.mPullReplicator.stop();
        }
        if (this.mPushReplicator != null) {
            this.mPushReplicator.stop();
        }
    }

    /**
     * <p>Starts the configured push replication.</p>
     */
    public void startPushReplication() {
        if (this.mPushReplicator != null) {
            this.mPushReplicator.start();
        } else {
            throw new RuntimeException("Push replication not set up correctly");
        }
    }

    /**
     * <p>Starts the configured pull replication.</p>
     */
    public void startPullReplication() {
        if (this.mPullReplicator != null) {
            this.mPullReplicator.start();
        } else {
            throw new RuntimeException("Push replication not set up correctly");
        }
    }

    /**
     * <p>Stops running replications and reloads the replication settings from
     * the app's preferences.</p>
     */
    public void reloadReplicationSettings()
            throws URISyntaxException {

        // Stop running replications before reloading the replication
        // settings.
        // The stop() method instructs the replicator to stop ongoing
        // processes, and to stop making changes to the DocumentStore. Therefore,
        // we don't clear the listeners because their complete() methods
        // still need to be called once the replications have stopped
        // for the UI to be updated correctly with any changes made before
        // the replication was stopped.
        this.stopAllReplications();

        // Set up the new replicator objects
        URI uri = this.createServerURI();

        mPullReplicator = ReplicatorBuilder.pull().to(mDocumentStore).from(uri).build();
        mPushReplicator = ReplicatorBuilder.push().from(mDocumentStore).to(uri).build();

        mPushReplicator.getEventBus().register(this);
        mPullReplicator.getEventBus().register(this);

        Log.d(LOG_TAG, "Set up replicators for URI:" + uri.toString());
    }

    /**
     * <p>Returns the URI for the remote database, based on the app's
     * configuration.</p>
     * @return the remote database's URI
     * @throws URISyntaxException if the settings give an invalid URI
     */
    private URI createServerURI()
            throws URISyntaxException {
        // We store this in plain text for the purposes of simple demonstration,
        // you might want to use something more secure.
       /* SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this.mContext);
        String username = sharedPref.getString(TodoActivity.SETTINGS_CLOUDANT_USER, "");
        String dbName = sharedPref.getString(TodoActivity.SETTINGS_CLOUDANT_DB, "");
        String apiKey = sharedPref.getString(TodoActivity.SETTINGS_CLOUDANT_API_KEY, "");
        String apiSecret = sharedPref.getString(TodoActivity.SETTINGS_CLOUDANT_API_SECRET, "");
        String host = username + ".cloudant.com";
*/
        String username = "c606a925-d29e-4a50-8f86-b746a5498a34-bluemix";
        String dbName = "sample_nosql_db";
        String apiKey = "c606a925-d29e-4a50-8f86-b746a5498a34-bluemix";
        String apiSecret = "ea42c4e684fb2a651f5461352c7b8d69fbbf699e4f0b85559d83e1859423f92d";
        String host = username + ".cloudant.com";
        // We recommend always using HTTPS to talk to Cloudant.
        return new URI("https", apiKey + ":" + apiSecret, host, 443, "/" + dbName, null, null);
    }

    //
    // REPLICATIONLISTENER IMPLEMENTATION
    //

    /**
     * Calls the TodoActivity's replicationComplete method on the main thread,
     * as the complete() callback will probably come from a replicator worker
     * thread.
     */
    @Subscribe
    public void complete(ReplicationCompleted rc) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
//                if (mListener != null) {
//                    mListener.replicationComplete();
//                }
            }
        });
    }

    /**
     * Calls the TodoActivity's replicationComplete method on the main thread,
     * as the error() callback will probably come from a replicator worker
     * thread.
     */
    @Subscribe
    public void error(ReplicationErrored re) {
        Log.e(LOG_TAG, "Replication error:", re.errorInfo);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
//                if (mListener != null) {
//                    mListener.replicationError();
//                }
            }
        });
    }
}
