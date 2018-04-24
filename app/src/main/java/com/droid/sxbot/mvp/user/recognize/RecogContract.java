package com.droid.sxbot.mvp.user.recognize;


import com.droid.sxbot.mvp.BasePresenter;
import com.droid.sxbot.mvp.BaseView;

/**
 * Created by lisongting on 2017/12/14.
 */

public interface RecogContract {

    interface Presenter extends BasePresenter {

        void recognize(String strBitmap);

        void destroy();
    }

    interface View extends BaseView<Presenter> {

        void showRecognitionSuccess(String userName);

        void showError(String s);
    }


}
