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
import java.util.concurrent.ExecutionException;

public class AzureServiceHelper {
    private final Context mContext;
    private MobileServiceClient mClient;
    protected MobileServiceSyncTable<Speaker> mSpeakerTable;
    protected Query mSpeakerQuery;

    public AzureServiceHelper(Context mContext) {
        this.mContext = mContext;

        try {
            mClient = new MobileServiceClient("https://sdec-demo.azure-mobile.net/", "LZXsvLGVfDqhVSmZVwvietQZhGDwhY56", mContext);
            SQLiteHelper.createLocalStore(mClient);
            mSpeakerTable = mClient.getSyncTable("speakers", Speaker.class);
            mSpeakerQuery = mClient.getTable(Speaker.class).orderBy("name", QueryOrder.Ascending);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadData() {
        notifyDataLoading();
        getSpeakers();
    }

    public void refreshData() {
        Speakers.clear();
        loadData();
    }

    public void getSpeakers() {
        if (Speakers.ITEMS.size() > 0) { // When incremental sync is implemented in the SDK, this can probably be removed.
            return;
        }

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    // Pull data from Azure Mobile Service, which also caches data into local SQLite DB
                    mClient.getSyncContext().push().get();
                    mSpeakerTable.pull(mSpeakerQuery).get();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    // Read data from local DB
                    final MobileServiceList<Speaker> result = mSpeakerTable.read(mSpeakerQuery).get();
                    Speakers.clear();
                    for (Speaker speaker : result) {
                        Speakers.addItem(speaker);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                notifyDataLoaded();
                return null;
            }
        }.execute();
    }

    private void notifyDataLoading() {
        Intent intent = new Intent("data-loading");
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    private void notifyDataLoaded() {
        Intent intent = new Intent("data-loaded");
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }
}
