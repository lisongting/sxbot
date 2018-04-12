package com.droid.sxbot.mvp.map;

import android.os.Binder;
import android.support.annotation.NonNull;

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
    private volatile int completedCount = 0;
    private RosConnectionService.ServiceBinder proxy;

    public MapPresenter(MapContract.View view) {
        this.view = view;
        view.setPresenter(this);
        start();
    }

    @Override
    public void start() {
        threadPool = Executors.newFixedThreadPool(20);
    }

    @Override
    public void publishPoints(List<Indicator> positionList) {
        JSONObject body = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        for(int i=0;i<positionList.size();i++) {
            JSONObject object = new JSONObject();
            Indicator indicator = positionList.get(i);
            //todo:进行位置点坐标转换
            try {
                object.put("x", indicator.getX());
                object.put("y", indicator.getY());
                object.put("theta", indicator.getTheta());
                if (indicator.getFile().length() != 0) {
                    object.put("file", indicator.getFile());
                } else {
                    object.put("file", "none");
                }
                jsonArray.put(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        try {
            body.put("op", "publish");
            body.put("topic", Constant.PUBLISH_TOPIC_POSE_AND_AUDIO);
            body.put("msg", jsonArray.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        proxy.sendJson(body.toString());
    }

    @Override
    public void uploadFiles(final List<String> audioList, final MapContract.uploadListener listener) {
        this.listener = listener;
        //去除重复元素
        Set<String> set = new HashSet<>();
        set.addAll(audioList);

        for (String s : set) {
            UpLoadTask upLoadTask =
                    new UpLoadTask(Config.ROS_SERVER_IP,
                            Config.FILE_TRANSFER_PORT,s,
                            new UpLoadTask.OnCompleteListener() {
                @Override
                public void onComplete() {
                    completedCount++;
                    if (completedCount == audioList.size()) {
                        listener.onComplete();
                    }
                }
                @Override
                public void onError(String s) {
                    listener.onError(s);
                }
            });
            threadPool.execute(upLoadTask);

        }

    }

    public void setServiceProxy(@NonNull Binder binder){
        this.proxy = (RosConnectionService.ServiceBinder) binder;
    }


}
