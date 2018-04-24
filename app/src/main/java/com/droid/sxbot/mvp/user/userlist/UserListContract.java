package com.droid.sxbot.mvp.user.userlist;


import com.droid.sxbot.entity.UserInfo;
import com.droid.sxbot.mvp.BasePresenter;
import com.droid.sxbot.mvp.BaseView;

/**
 * Created by lisongting on 2017/7/11.
 * 采用MVP架构
 */

public interface UserListContract {

    interface View extends BaseView<Presenter> {

        void showUserInList(UserInfo info);

        void showRefreshError();

        void hideLoading();

        void showInfo(String s);

        void removeUser(String id);
    }

    interface Presenter extends BasePresenter {

        void requestUserData();

        void deleteUser(String userId);
    }

}
