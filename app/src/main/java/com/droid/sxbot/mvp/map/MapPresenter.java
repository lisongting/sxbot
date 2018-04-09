package com.droid.sxbot.mvp.map;

import com.droid.sxbot.Config;
import com.droid.sxbot.UpLoadTask;
import com.droid.sxbot.entity.RobotPosition;

import java.util.List;
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
    public void publishPoints(List<RobotPosition> positionList) {

    }

    @Override
    public void uploadFiles(final List<String> audioList, final MapContract.uploadListener listener) {
        this.listener = listener;
        for (String s : audioList) {
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


}
