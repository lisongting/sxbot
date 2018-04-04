package com.droid.sxbot.mvp.scene;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.droid.sxbot.R;

/**
 * Created by lisongting on 2018/3/19.
 */

public class MapFragment extends Fragment {

    private Button btThreeDimensionScene;
    public MapFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, parent, false);
        btThreeDimensionScene = view.findViewById(R.id.bt_three_dimension);

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
    }
}
