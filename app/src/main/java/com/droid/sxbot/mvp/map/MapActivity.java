package com.droid.sxbot.mvp.map;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.droid.sxbot.R;
import com.droid.sxbot.customview.MapView;
import com.droid.sxbot.mvp.map.tts.TTSFragment;

/**
 * Created by lisongting on 2018/4/6.
 */

public class MapActivity extends AppCompatActivity {
    private MapFragment fragment;
    private final String TAG = "MapActivity";
    private FragmentManager fragmentManager;
    private TTSFragment ttsFragment;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container_layout);
        fragmentManager = getSupportFragmentManager();
        if (savedInstanceState != null) {
            fragment = (MapFragment) fragmentManager.getFragment(savedInstanceState, "mapFragment");
            ttsFragment = (TTSFragment) fragmentManager.getFragment(savedInstanceState, "ttsFragment");
        } else {
            fragment= new MapFragment();
            fragmentManager.beginTransaction()
                    .add(R.id.container, fragment,"mapFragment")
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_map,menu);
        return true;
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
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.mode_adjust_map:
                Toast.makeText(this, "可以双指平移、双指缩放", Toast.LENGTH_SHORT).show();
                fragment.setMapMode(MapView.MAP_MODE_ADJUST_MAP);
                break;
            case R.id.mode_select_point:
                fragment.setMapMode(MapView.MAP_MODE_SELECT_POINT);
                break;
            case R.id.mode_reset_map:
                fragment.setMapMode(MapView.MAP_MODE_RESET);
                break;
            case R.id.mode_clear_points:
                fragment.setMapMode(MapView.MAP_MODE_CLEAR_POINTS);
                break;
            default:break;
        }
        return true;
    }


}
