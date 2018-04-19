package com.droid.sxbot.mvp.map.tts;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.droid.sxbot.R;

/**
 * Created by lisongting on 2018/4/19.
 */

public class TTSActivity extends AppCompatActivity {

    private FragmentManager fragmentManager;
    private TTSFragment ttsFragment;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container_layout);
        fragmentManager = getSupportFragmentManager();
        if (savedInstanceState != null) {
            ttsFragment = (TTSFragment) fragmentManager.getFragment(savedInstanceState, "ttsFragment");
        } else {
            ttsFragment = new TTSFragment();
            fragmentManager.beginTransaction()
                    .add(R.id.container, ttsFragment, "ttsFragment")
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        fragmentManager.putFragment(outState, "ttsFragment", ttsFragment);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }
}
