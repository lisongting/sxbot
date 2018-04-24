package com.droid.sxbot.mvp.user;


import com.droid.sxbot.mvp.BasePresenter;
import com.droid.sxbot.mvp.BaseView;

/**
 * Created by lisongting on 2017/12/11.
 */

public interface UserContract {

    interface Presenter extends BasePresenter {

    }

    interface View extends BaseView<Presenter> {

    }

}
