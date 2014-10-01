package com.protegra.sdecdemo.helpers;

import android.content.Context;
import android.os.AsyncTask;

import com.microsoft.windowsazure.mobileservices.*;
import com.protegra.sdecdemo.R;
import com.protegra.sdecdemo.data.Speaker;
import com.protegra.sdecdemo.data.Speakers;

import java.net.MalformedURLException;
import java.util.List;

public class AzureServiceHelper {
    private final Context mContext;
    private MobileServiceClient mClient;

    public AzureServiceHelper(Context mContext) {
        this.mContext = mContext;

        try {
            mClient = new MobileServiceClient(mContext.getString(R.string.appURL), mContext.getString(R.string.appKey), mContext);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public void getSpeakers() {
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
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void getSpeakersFromWebService() {
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... strings) {
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
                                    return null;
                                }
                            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, result.toArray(new Speaker[result.size()]));
                        } else {
                            exception.printStackTrace();
                        }
                    }
                });

                return null;
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
