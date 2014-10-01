package com.protegra.sdecdemo.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.protegra.sdecdemo.helpers.AzureServiceHelper;
import com.protegra.sdecdemo.R;
import com.protegra.sdecdemo.fragments.SpeakerFragment;
import com.protegra.sdecdemo.data.Speaker;
import com.protegra.sdecdemo.data.Speakers;


public class MainActivity extends Activity implements SpeakerFragment.OnSpeakerSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, SpeakerFragment.newInstance())
                    .commit();
        }

        AzureServiceHelper helper = new AzureServiceHelper(this);
        helper.getSpeakers();
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
            helper.getSpeakers();
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
