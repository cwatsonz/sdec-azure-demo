package com.protegra.sdecdemo.helpers;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceList;
import com.microsoft.windowsazure.mobileservices.table.query.Query;
import com.microsoft.windowsazure.mobileservices.table.query.QueryOrder;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncContext;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncTable;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.ColumnDataType;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.MobileServiceLocalStoreException;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.SQLiteLocalStore;
import com.microsoft.windowsazure.mobileservices.table.sync.synchandler.SimpleSyncHandler;
import com.protegra.sdecdemo.R;
import com.protegra.sdecdemo.data.Speaker;
import com.protegra.sdecdemo.data.Speakers;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

public class AzureServiceHelper {
    private final String DATABASE_NAME = "sdec_demo";
    private final int DATABASE_VERSION = 1;

    private final Context mContext;
    private MobileServiceClient mClient;
    private SpeakerHelper mSpeakerHelper;
    private CountDownLatch latch;

    public AzureServiceHelper(Context mContext) {
        this.mContext = mContext;

        try {
            mClient = new MobileServiceClient(mContext.getString(R.string.appURL), mContext.getString(R.string.appKey), mContext);
            mSpeakerHelper = new SpeakerHelper();
            createLocalStore();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createLocalStore() throws MobileServiceLocalStoreException, InterruptedException, ExecutionException {
        SQLiteLocalStore localStore = new SQLiteLocalStore(mClient.getContext(), DATABASE_NAME, null, DATABASE_VERSION);
        SimpleSyncHandler handler = new SimpleSyncHandler();
        MobileServiceSyncContext syncContext = mClient.getSyncContext();

        mSpeakerHelper.defineTable(localStore);

        syncContext.initialize(localStore, handler).get();
    }

    public void loadData() {
        latch = new CountDownLatch(1);

        Intent intent = new Intent("data-loading");
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

        mSpeakerHelper.pull();

        waitForLoadComplete();
    }

    public void refreshData() {
        Speakers.clear();
        loadData();
    }

    private void waitForLoadComplete() {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                Intent intent = new Intent("data-loaded");
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            }
        }.execute();
    }

    class SpeakerHelper {
        protected MobileServiceSyncTable<Speaker> mTable;
        protected Query mQuery;

        SpeakerHelper() {
            mTable = mClient.getSyncTable("speakers", Speaker.class);
            mQuery = mClient.getTable(Speaker.class).orderBy("name", QueryOrder.Ascending);
        }

        public void defineTable(SQLiteLocalStore localStore) throws MobileServiceLocalStoreException {
            Map<String, ColumnDataType> tableDefinition = new HashMap<String, ColumnDataType>();

            tableDefinition.put("id", ColumnDataType.String);
            tableDefinition.put("name", ColumnDataType.String);
            tableDefinition.put("photo_small", ColumnDataType.String);
            tableDefinition.put("photo_large", ColumnDataType.String);
            tableDefinition.put("organization", ColumnDataType.String);
            tableDefinition.put("role", ColumnDataType.String);
            tableDefinition.put("twitter", ColumnDataType.String);
            tableDefinition.put("website", ColumnDataType.String);
            tableDefinition.put("description", ColumnDataType.String);
            tableDefinition.put("__deleted", ColumnDataType.String);

            localStore.defineTable("speakers", tableDefinition);
        }

        public void pull() {
            if (Speakers.ITEMS.size() > 0) {
                return;
            }

            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        // Pull data from Azure Mobile Service, which also caches data into local SQLite DB
                        mClient.getSyncContext().push().get();
                        mTable.pull(mQuery).get();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        // Read data from local DB
                        final MobileServiceList<Speaker> result = mTable.read(mQuery).get();
                        Speakers.clear();
                        for (Speaker speaker : result) {
                            Speakers.addItem(speaker);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    latch.countDown();
                    return null;
                }
            }.execute();
        }
    }
}
