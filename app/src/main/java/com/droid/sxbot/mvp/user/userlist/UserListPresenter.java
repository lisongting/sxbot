package com.droid.sxbot.mvp.user.userlist;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;

import com.droid.sxbot.Config;
import com.droid.sxbot.RecognitionRetrofit;
import com.droid.sxbot.entity.UserFaceResult;
import com.droid.sxbot.entity.UserIdList;
import com.droid.sxbot.entity.UserInfo;
import com.droid.sxbot.util.ImageUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by lisongting on 2017/7/11.
 */

public class UserListPresenter implements UserListContract.Presenter {

    public static final String TAG = "UserListPresenter";
    private Context context;
    private UserListContract.View view;
    private Handler handler;
    private Retrofit retrofit;
    private RecognitionRetrofit service;
    private OkHttpClient httpClient;

    public UserListPresenter(Context context, UserListContract.View view) {
        this.context = context;
        this.view = view;
        handler = new Handler();
        //在这里就给View绑定了presenter
        view.setPresenter(this);

    }

    @Override
    public void start() {
        StringBuilder baseUrl = new StringBuilder("http://");
        baseUrl.append(Config.RECOGNITION_SERVER_IP).append(":").append(Config.RECOGNITION_SERVER_PORT).append("/");

        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl.toString())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        service = retrofit.create(RecognitionRetrofit.class);
        httpClient = new OkHttpClient();

    }

    @Override
    public void requestUserData() {
        //先用flatMap将每个用户的id取出来
        service.getUserIdList()
                .subscribeOn(Schedulers.newThread())
                .flatMap(new Function<UserIdList, Observable<String>>() {
                    @Override
                    public Observable<String> apply(final UserIdList userList) throws Exception {
                        return Observable.fromIterable(userList.getUserList());
                    }
                })
                .map(new Function<String, UserInfo>() {
                    @Override
                    public UserInfo apply(final String id) throws Exception {
                        final UserInfo userInfo = new UserInfo();
//                        userInfo.setName(Util.hexStringToString(id));
                        userInfo.parseNameAndGroup(id);
                        service.getUserFace(id)
                                .subscribe(new Consumer<UserFaceResult>() {
                                    @Override
                                    public void accept(UserFaceResult userFaceResult) throws Exception {
                                        WeakReference<Bitmap> bitmapWeakReference =
                                                new WeakReference<>(ImageUtils.decodeBase64ToBitmap(userFaceResult.getStrFaceImage()));
                                        userInfo.setFace(bitmapWeakReference.get());
                                    }
                                });
                        Log.i("tag", userInfo.toString());
                        return userInfo;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<UserInfo>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }
                    @Override
                    public void onNext(UserInfo userInfo) {
                        view.showUserInList(userInfo);
                    }

                    @Override
                    public void onError(Throwable e) {
                        view.showRefreshError();
                    }

                    @Override
                    public void onComplete() {
                        view.hideLoading();
                    }
                });
    }

    @Override
    public void deleteUser(final String userId) {
        final StringBuilder url = new StringBuilder("http://");
        url.append(Config.RECOGNITION_SERVER_IP).append(":").append(Config.RECOGNITION_SERVER_PORT).append("/")
            .append("management/logout?userid=").append(userId);

        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(final ObservableEmitter<Integer> e) throws Exception {
                final Request request = new Request.Builder()
                        .url(url.toString())
                        .get()
                        .build();
                httpClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e1) {
                        e.onError(new Throwable("请求失败"));
                    }
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String str = response.body().string();
                        try {
                            int ret = new JSONObject(str).getInt("Ret");
                            e.onNext(ret);
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(Integer integer) {
                        if (integer == 0) {
                            view.showInfo("删除成功");
                            UserInfo info = new UserInfo();
                            info.parseNameAndGroup(userId);
                            view.removeUser(info.getName());
                        }
                    }
                    @Override
                    public void onError(Throwable e) {
                        view.showInfo("服务器连接异常");
                    }
                    @Override
                    public void onComplete() {

                    }
                });
    }



    private void log(String s) {
        Log.i(TAG, s);
    }
}
