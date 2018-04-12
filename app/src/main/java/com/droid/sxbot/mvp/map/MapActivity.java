package com.droid.sxbot.mvp.map;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.droid.sxbot.R;

/**
 * Created by lisongting on 2018/4/6.
 */

public class MapActivity extends AppCompatActivity {
    private MapFragment fragment;
    private final String TAG = "MapActivity";
    private FragmentManager fragmentManager;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container_layout);
        fragmentManager = getSupportFragmentManager();
        if (savedInstanceState != null) {
            fragment = (MapFragment) fragmentManager.getFragment(savedInstanceState, "mapFragment");
        } else {
            fragment= new MapFragment();
            fragmentManager.beginTransaction()
                    .add(R.id.container, fragment,"mapFragment")
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MapPresenter presenter = new MapPresenter(fragment);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        fragmentManager.putFragment(outState, "mapFragment",fragment);
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
