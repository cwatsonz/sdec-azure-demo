package com.protegra.sdecdemo.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.protegra.sdecdemo.helpers.AzureServiceHelper;
import com.protegra.sdecdemo.R;
import com.protegra.sdecdemo.fragments.SpeakerFragment;
import com.protegra.sdecdemo.data.Speaker;
import com.protegra.sdecdemo.data.Speakers;


public class MainActivity extends Activity implements SpeakerFragment.OnSpeakerSelectedListener {

    private ProgressBar mProgressBar;

    private BroadcastReceiver mLoadingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mProgressBar.setVisibility(View.VISIBLE);
            mProgressBar.bringToFront();
        }
    };

    private BroadcastReceiver mLoadedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mProgressBar.setVisibility(View.GONE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, SpeakerFragment.newInstance())
                    .commit();
        }

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.GONE);

        AzureServiceHelper helper = new AzureServiceHelper(this);
        helper.loadData();
    }

    @Override
    public void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(this).registerReceiver(mLoadingReceiver, new IntentFilter("data-loading"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mLoadedReceiver, new IntentFilter("data-loaded"));
    }

    @Override
    public void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mLoadingReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mLoadedReceiver);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            AzureServiceHelper helper = new AzureServiceHelper(this);
            helper.refreshData();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSpeakerSelected(String id) {
        Speaker speaker = Speakers.ITEM_MAP.get(id);
        Toast.makeText(this, speaker.name, Toast.LENGTH_SHORT).show();
    }
}
