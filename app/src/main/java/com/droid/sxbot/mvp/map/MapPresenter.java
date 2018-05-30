package com.droid.sxbot.mvp.map;

import android.os.Binder;
import android.support.annotation.NonNull;
import android.util.Log;

import com.droid.sxbot.Config;
import com.droid.sxbot.Constant;
import com.droid.sxbot.RosConnectionService;
import com.droid.sxbot.UpLoadTask;
import com.droid.sxbot.entity.Indicator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by lisongting on 2018/4/6.
 */

public class MapPresenter implements MapContract.Presenter {
    private MapContract.uploadListener listener;
    private ExecutorService threadPool;
    private MapContract.View view;
    private int completedCount = 0;
    private int totalCount = -1;
    private RosConnectionService.ServiceBinder proxy;
    //地图的宽高像素，指的是实际的图片像素而不是显示在手机中的屏幕像素
    private static final int MAP_WIDTH_PIXEL = 568;
    private static final int MAP_HEIGHT_PIXEL = 1118;
    //每个像素0.05米
    private static final float METER_PER_PIXEL = 0.05f;

    public MapPresenter(MapContract.View view) {
        this.view = view;
        view.setPresenter(this);
        start();
    }

    @Override
    public void start() {
        threadPool = Executors.newFixedThreadPool(10);
    }

    @Override
    public void publishPoints(List<Indicator> positionList) {

        JSONObject advertiseMsg = new JSONObject();
        try {
            advertiseMsg.put("op", "advertise");
            advertiseMsg.put("topic", Constant.PUBLISH_TOPIC_POSE_AUDIO_ARRAY);
            advertiseMsg.put("type", "xbot_navigoals/PoseAudioArray");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        proxy.sendJson(advertiseMsg.toString());


        JSONObject body = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        JSONObject msg = new JSONObject();
        for(int i=0;i<positionList.size();i++) {
            JSONObject object = new JSONObject();
            Indicator indicator = positionList.get(i);
            try {
                object.put("x", indicator.getX() * MAP_WIDTH_PIXEL * METER_PER_PIXEL);
                object.put("y", indicator.getY() * MAP_HEIGHT_PIXEL * METER_PER_PIXEL);
                object.put("theta", indicator.getTheta());
                if (indicator.getFile().length() != 0) {
                    String file = indicator.getFile();
                    String briefFile = file.substring(file.lastIndexOf("/") + 1, file.length());
                    object.put("file", briefFile);
                } else {
                    object.put("file", "");
                }
                jsonArray.put(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        try {
            msg.put("array", jsonArray);
            body.put("op", "publish");
            body.put("topic", Constant.PUBLISH_TOPIC_POSE_AUDIO_ARRAY);
            body.put("msg", msg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        proxy.sendJson(body.toString());
        log("publish:" + body.toString());
    }

    @Override
    public void uploadFiles(final List<String> audioList, final MapContract.uploadListener listener) {
        this.listener = listener;
        //去除重复元素
        Set<String> set = new HashSet<>();
        set.addAll(audioList);
        totalCount = set.size();
        for (String s : set) {
            UpLoadTask upLoadTask =
                    new UpLoadTask(Config.ROS_SERVER_IP,
                            Config.FILE_TRANSFER_PORT,s,
                            new UpLoadTask.OnCompleteListener() {
                @Override
                public void onComplete() {
                    increaseCount();
                }
                @Override
                public void onError(String s) {
                    listener.onError(s);
                }
            });
            threadPool.execute(upLoadTask);
        }
    }

    private synchronized void increaseCount() {
        completedCount++;
        listener.onUpdateProgress((int) (completedCount*100.0/totalCount));
        if (completedCount == totalCount) {
            completedCount = 0;
            listener.onComplete();
        }

    }

    public void setServiceProxy(@NonNull Binder binder){
        this.proxy = (RosConnectionService.ServiceBinder) binder;
    }

    private void log(String s) {
        Log.i("MapPresenter", s);
    }


}
