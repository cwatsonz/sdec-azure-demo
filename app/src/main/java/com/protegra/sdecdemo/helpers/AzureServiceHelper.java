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
    private CountDownLatch latch;

    public AzureServiceHelper(Context mContext) {
        this.mContext = mContext;

        try {
            mClient = new MobileServiceClient(mContext.getString(R.string.appURL), mContext.getString(R.string.appKey), mContext);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public void loadData() {
        latch = new CountDownLatch(1);

        Intent intent = new Intent("data-loading");
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

        getSpeakers();

        waitForLoadComplete();
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

    private void getSpeakers() {
        new AsyncTask<String, Void, Integer>() {
            @Override
            protected Integer doInBackground(String... params) {
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
                    getSpeakersFromWebService();
                } else {
                    latch.countDown();
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void getSpeakersFromWebService() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                MobileServiceTable<Speaker> speakerTable = mClient.getTable("speakers", Speaker.class);
                speakerTable.top(1000).orderBy("name", QueryOrder.Ascending).execute(new TableQueryCallback<Speaker>() {
                    public void onCompleted(List<Speaker> result, int count,
                                            Exception exception, ServiceFilterResponse response) {
                        if (exception == null) {
                            new AsyncTask<Speaker, Void, String>() {
                                @Override
                                protected String doInBackground(Speaker... speakers) {
                                    SQLiteHelper db = SQLiteHelper.getInstance(mContext);
                                    for (Speaker item : speakers) {
                                        Speakers.addItem(item);
                                    }

                                    db.bulkAddSpeakers(speakers);
                                    latch.countDown();
                                    return null;
                                }
                            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, result.toArray(new Speaker[result.size()]));
                        } else {
                            exception.printStackTrace();
                            latch.countDown();
                        }
                    }
                });

                return null;
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
