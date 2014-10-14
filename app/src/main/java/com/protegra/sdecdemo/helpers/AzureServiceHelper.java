package com.protegra.sdecdemo.helpers;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;

import com.microsoft.windowsazure.mobileservices.*;
import com.protegra.sdecdemo.R;
import com.protegra.sdecdemo.data.Speaker;
import com.protegra.sdecdemo.data.Speakers;

import java.net.MalformedURLException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class AzureServiceHelper {
    private final Context mContext;
    private MobileServiceClient mClient;
    MobileServiceTable<Speaker> mSpeakerTable;

    public AzureServiceHelper(Context mContext) {
        this.mContext = mContext;

        try {
            mClient = new MobileServiceClient(mContext.getString(R.string.appURL), mContext.getString(R.string.appKey), mContext);
            mSpeakerTable = mClient.getTable("speakers", Speaker.class);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public void loadData() {
        notifyDataLoading();

        getSpeakers();
    }

    public void refreshData() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    SQLiteHelper db = SQLiteHelper.getInstance(mContext);
                    db.removeAllSpeakers();
                    Speakers.clear();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                loadData();
            }
        }.execute();
    }

    private void getSpeakers() {
        new AsyncTask<String, Void, Integer>() {
            @Override
            protected Integer doInBackground(String... params) {
                // Load Speakers from local SQLite database
                SQLiteHelper db = SQLiteHelper.getInstance(mContext);
                Speakers.clear();
                if (db.getSpeakersCount() > 0) {
                    for (Speaker item : db.getAllSpeakers()) {
                        Speakers.addItem(item);
                    }
                }

                return Speakers.ITEMS.size();
            }

            @Override
            protected void onPostExecute(Integer count) {
                if (count == 0) {
                    // If the local database contained no speakers, try loading from Azure
                    getSpeakersFromWebService();
                } else {
                    notifyDataLoaded();
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void getSpeakersFromWebService() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                mSpeakerTable.top(1000).orderBy("name", QueryOrder.Ascending).execute(new TableQueryCallback<Speaker>() {
                    public void onCompleted(List<Speaker> result, int count, Exception exception, ServiceFilterResponse response) {
                        if (exception == null) {
                            AddSpeakersToDatabase(result);
                        } else {
                            exception.printStackTrace();
                            notifyDataLoaded();
                        }
                    }
                });

                return null;
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void AddSpeakersToDatabase(List<Speaker> result) {
        new AsyncTask<Speaker, Void, String>() {
            @Override
            protected String doInBackground(Speaker... speakers) {
                SQLiteHelper db = SQLiteHelper.getInstance(mContext);
                db.bulkAddSpeakers(speakers);

                for (Speaker item : speakers) {
                    Speakers.addItem(item);
                }

                notifyDataLoaded();
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, result.toArray(new Speaker[result.size()]));
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
