package com.droid.sxbot.mvp.map;

import android.os.Binder;
import android.support.annotation.NonNull;

import com.droid.sxbot.entity.Indicator;
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
        void onError(String s);
        void onUpdateProgress(int percent);
    }

    interface Presenter extends BasePresenter{

        void publishPoints(List<Indicator> positionList);

        void uploadFiles(List<String> audioList,uploadListener listener);

        void setServiceProxy(@NonNull Binder binder);
    }

    interface View extends BaseView<Presenter>{
        void showUploadSuccess();

        void showUploadError();

    }

}
