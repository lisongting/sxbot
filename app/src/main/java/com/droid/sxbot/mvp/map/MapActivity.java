package com.droid.sxbot.mvp.map;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.droid.sxbot.R;

/**
 * Created by lisongting on 2018/4/6.
 */

public class MapActivity extends AppCompatActivity {
    MapFragment fragment;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container_layout);
        fragment= new MapFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, fragment)
                .commit();

    }

    @Override
    protected void onResume() {
        super.onResume();
        MapPresenter presenter = new MapPresenter(fragment);
    }
}
