package com.droid.sxbot.mvp.scene;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.droid.sxbot.R;
import com.droid.sxbot.mvp.map.MapActivity;

/**
 * Created by lisongting on 2018/3/19.
 */

public class SceneFragment extends Fragment {

    private Button btThreeDimensionScene,btMapView;
    public SceneFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.scene_fragment, parent, false);
        btThreeDimensionScene = view.findViewById(R.id.bt_three_dimension);
        btMapView = view.findViewById(R.id.bt_map);
        initListeners();
        return view;
    }

    private void initListeners() {
        btThreeDimensionScene.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), ModelActivity.class));
            }
        });
        btMapView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(),MapActivity.class));
            }
        });
    }
}
