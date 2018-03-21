/*
 * Copyright 2017 lisongting
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.droid.sxbot.mvp.robot_state;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import com.droid.sxbot.App;
import com.droid.sxbot.Config;
import com.droid.sxbot.R;
import com.droid.sxbot.customview.CustomSeekBar;
import com.droid.sxbot.customview.PercentCircleView;
import com.droid.sxbot.entity.RobotState;


/**
 * Created by lisongting on 2017/11/14.
 */

public class RobotStateFragment extends Fragment implements RobotStateContract.View {
    private static final String TAG = "RobotStateFragment";

    private PercentCircleView batteryView;
    private CustomSeekBar cloudDegreeSeekBar;
    private CustomSeekBar cameraDegreeSeekBar;
    private RobotStateContract.Presenter presenter;
    private Switch switcher;
    private Button btReset;
    private Button btThreeDimension;
    public static final int ORIENTATION_PORTRAIT = 1;
    public static final int ORIENTATION_LANDSCAPE = 2;

    private static final String KEY_BATERRY = "battery";
    private static final String KEY_SWITCH = "machine_power";
    private static final String KEY_CAMERA_DEGREE = "camera_degree";
    private static final String KEY_CLOUD_DEGREE = "cloud_degree";

    public RobotStateFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        log("onCreateView -- savedInstanceState null? :" + (savedInstanceState == null));
        View view = null;
        Bundle b = getArguments() ;
        int orientation = b.getInt("orientation",ORIENTATION_PORTRAIT);
        if (orientation == ORIENTATION_PORTRAIT) {
            view = inflater.inflate(R.layout.fragment_robot_state, container, false);
        } else if (orientation == ORIENTATION_LANDSCAPE) {
            view = inflater.inflate(R.layout.fragment_robot_state_landscape, container, false);
        }
        batteryView = view.findViewById(R.id.battery_view);
        cloudDegreeSeekBar =  view.findViewById(R.id.seekbar_cloud_degree);
        cameraDegreeSeekBar =  view.findViewById(R.id.seekbar_camera_degree);
        switcher =  view.findViewById(R.id.switcher);
        btReset = view.findViewById(R.id.bt_reset);
        btThreeDimension = view.findViewById(R.id.bt_three_dimension);

        btThreeDimension.setVisibility(View.GONE);
        initListeners();
        if (savedInstanceState != null) {
            batteryView.setPercent(savedInstanceState.getInt(KEY_BATERRY, 60));
            switcher.setChecked(savedInstanceState.getBoolean(KEY_SWITCH, true));
            cameraDegreeSeekBar.setValue(savedInstanceState.getFloat(KEY_CAMERA_DEGREE, 0f));
            cloudDegreeSeekBar.setValue(savedInstanceState.getFloat(KEY_CLOUD_DEGREE, 0f));
            App app = (App) getActivity().getApplication();
            presenter = new RobotStatePresenter(getContext(), this);
            presenter.setServiceProxy(app.getRosServiceProxy());
            presenter.subscribeRobotState();
        }

        return view;
    }

    @Override
    public void initView() {
        if (Config.isRosServerConnected) {
            batteryView.startAnimation();
        } else {
            batteryView.stopAnimation();
        }
    }

    private void initListeners() {
        switcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (presenter!=null && Config.isRosServerConnected) {
                    if (switcher.isChecked()) {
                        presenter.publishElectricMachineryMsg(true);
                    } else {
                        presenter.publishElectricMachineryMsg(false);
                    }
                } else {
                    Toast.makeText(getActivity(), "Ros服务器未连接", Toast.LENGTH_SHORT).show();
                }
            }
        });

        cloudDegreeSeekBar.setOnSeekChangeListener(new CustomSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(float value) {
            }

            @Override
            public void onProgressChangeCompleted(float value) {
                log("cloudDegreeSeekBar value change complete :" + value);
                if (presenter != null && Config.isRosServerConnected ) {
                    presenter.publishCloudCameraMsg(Math.round(value), Math.round(cameraDegreeSeekBar.getRealValue()));
                }else {
                    Toast.makeText(getActivity(), "Ros服务器未连接", Toast.LENGTH_SHORT).show();
                }
            }
        });

        cameraDegreeSeekBar.setOnSeekChangeListener(new CustomSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(float value) {
            }

            @Override
            public void onProgressChangeCompleted(float value) {
                log("cameraDegreeSeekBar value change complete:" + value);
                if (presenter != null && Config.isRosServerConnected ) {
                    presenter.publishCloudCameraMsg(Math.round(cloudDegreeSeekBar.getRealValue()), Math.round(value));
                }else {
                    Toast.makeText(getContext(), "Ros服务器未连接", Toast.LENGTH_SHORT).show();
                }

            }
        });

        btReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (presenter != null) {
                    presenter.reset();
                }
                switcher.setChecked(true);
                cameraDegreeSeekBar.setValue(0);
                cloudDegreeSeekBar.setValue(0);
            }
        });
//        btThreeDimension.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(getContext(), RobotActivity.class));
//            }
//        });

    }



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        log("onCreate()");

        super.onCreate(savedInstanceState);

    }

    @Override
    public void onResume() {
        log("onResume()");
        super.onResume();
        initView();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        log("isHidden:" + hidden);
        super.onHiddenChanged(hidden);
        //如果没被隐藏
        if (!hidden) {
            initView();
        }
    }

    //当Ros服务器连接状态变化时，从外部通知该Fragment
    public void notifyRosConnectionStateChange(boolean isConnected) {
        if (isConnected) {
            batteryView.startAnimation();
        } else {
            batteryView.stopAnimation();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        log("onSavedInstanceState");
        outState.putInt(KEY_BATERRY, batteryView.getPercent());
        outState.putBoolean(KEY_SWITCH, switcher.isChecked());
        outState.putFloat(KEY_CAMERA_DEGREE, cameraDegreeSeekBar.getRealValue());
        outState.putFloat(KEY_CLOUD_DEGREE, cloudDegreeSeekBar.getRealValue());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void setPresenter(RobotStateContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void updateRobotState( RobotState state) {
        final int percent = state.getPowerPercent();
        final int cloudDegree = state.getCloudDegree();
        final int cameraDegree = state.getCameraDegree();
        batteryView.post(new Runnable() {
            @Override
            public void run() {
                batteryView.setPercent(percent);
                cloudDegreeSeekBar.setValue(cloudDegree);
                cameraDegreeSeekBar.setValue(cameraDegree);
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        batteryView.stopAnimation();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (presenter != null) {
            presenter.unSubscribeRobotState();
            presenter.destroy();
        }

    }
    public RobotState getRobotState() {
        return new RobotState(batteryView.getPercent(),
                0,
                Math.round(cloudDegreeSeekBar.getRealValue()),
                Math.round(cameraDegreeSeekBar.getRealValue()));
    }

    public boolean isSwitchOn(){
        return switcher.isChecked();
    }

    public void setSwitchOn(boolean on) {
        switcher.setChecked(on);
    }

    private void log(String s){
        Log.i(TAG,TAG+" -- "+ s);
    }
}
