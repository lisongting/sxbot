package com.droid.sxbot.mvp.map;

import com.droid.sxbot.entity.RobotPosition;
import com.droid.sxbot.mvp.BasePresenter;
import com.droid.sxbot.mvp.BaseView;

import java.util.List;

/**
 * Created by lisongting on 2018/4/6.
 */

public interface MapContract {

    //文件上传的监听器
    interface uploadListener{
        void onComplete();
        void onError();
    }

    interface Presenter extends BasePresenter{

        void publishPoints(List<RobotPosition> positionList);

        void uploadFiles(List<String> audioList,uploadListener listener);

    }

    interface View extends BaseView<Presenter>{
        void showUploadSuccess();

        void showUploadError();

    }

}
