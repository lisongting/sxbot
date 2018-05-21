package com.droid.sxbot.mvp.user.userlist;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.droid.sxbot.AnimateDialog;
import com.droid.sxbot.R;
import com.droid.sxbot.entity.UserInfo;
import com.droid.sxbot.util.Util;


/**
 * Created by lisongting on 2017/7/11.
 * 采用MVP架构
 */

public class UserListActivity extends AppCompatActivity implements UserListContract.View{

    public static final String TAG = "UserListActivity";
    private UserListContract.Presenter presenter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private UserListAdapter userListAdapter;
    private TextView title;
    private ImageButton btBack,btDelete;
    private AnimateDialog dialog;
    private FragmentManager fragmentManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_user_list);

//        getSupportActionBar().setTitle("用户列表");
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        presenter = new UserListPresenter(this,this);
        fragmentManager = getSupportFragmentManager();
        initView();
        initListeners();
    }


    @Override
    public void initView() {
        title = findViewById(R.id.page_title);
        btBack = findViewById(R.id.ib_back);
        btDelete = findViewById(R.id.ib_delete);

        //获取状态栏高度，显示一个占位的View(该view和actionbar颜色相同)，达到沉浸式状态栏效果
        View status_bar = findViewById(R.id.status_bar_view);
        ViewGroup.LayoutParams params = status_bar.getLayoutParams();
        params.height = Util.getStatusBarHeight(this);
        status_bar.setLayoutParams(params);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerview_user_list);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        userListAdapter = new UserListAdapter(this);
        recyclerView.setAdapter(userListAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,R.color.colorPrimaryDark,R.color.colorLightPink);

    }

    private void initListeners() {
        btBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        btDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userListAdapter.setDeleteMode(!userListAdapter.getDeleteMode());
            }
        });

        userListAdapter.setOnDeleteListener(new UserListAdapter.OnDeleteListener() {
            @Override
            public void onDelete(int position,final String group, final String name) {
                if (dialog != null) {
                    fragmentManager.beginTransaction().remove(dialog).commit();
                }
                StringBuilder sb = new StringBuilder("是否删除用户：");
                sb.append(name).append("\n").append("(清除该用户的面部数据)");
                dialog = new AnimateDialog();
                dialog.setModeAndContent(AnimateDialog.DIALOG_STYLE_SHOW_CONTENT,
                        sb.toString(), true,
                        new AnimateDialog.OnButtonClickListener() {
                            @Override
                            public void onCancel() {
                                userListAdapter.setDeleteMode(false);
                            }
                            @Override
                            public void onConfirm() {
                                userListAdapter.setDeleteMode(false);
                                StringBuilder sb = new StringBuilder();
                                if (group.length() > 0) {
                                    sb.append(group);
                                    sb.append('_');
                                }
                                sb.append(name);
                                presenter.deleteUser(Util.makeUserNameToHex(sb.toString()));
                            }
                        });
                dialog.show(fragmentManager, "dialog");
            }
        });

        //从列表顶部向下拉动的时候触发
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                presenter.requestUserData();
            }
        });
    }



    @Override
    public void onResume() {
        super.onResume();
        presenter.start();
        swipeRefreshLayout.setRefreshing(true);
        presenter.requestUserData();

    }

    @Override
    public void onBackPressed() {
        if (userListAdapter.getDeleteMode()) {
            userListAdapter.setDeleteMode(false);
        } else {
            finish();
        }
    }

    @Override
    public void setPresenter(UserListContract.Presenter presenter) {
        this.presenter = presenter;
    }


    @Override
    public void showUserInList(UserInfo info) {
        if (info != null) {
            userListAdapter.addUser(info);
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    public void removeUser(String name){
        userListAdapter.removeUser(name);
    }

    @Override
    public void showRefreshError() {
        swipeRefreshLayout.setRefreshing(false);
        Toast.makeText(this, "人脸识别服务器连接超时，下拉重试", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void hideLoading() {
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void showInfo(String s) {
        if (dialog != null) {
            fragmentManager.beginTransaction().remove(dialog).commit();
        }
        dialog = new AnimateDialog();
        dialog.setModeAndContent(AnimateDialog.DIALOG_STYLE_SHOW_CONTENT,
                s, false,
                new AnimateDialog.OnButtonClickListener() {
                    @Override
                    public void onCancel() {}
                    @Override
                    public void onConfirm() {
                        dialog.dismiss();
                    }
                });
        dialog.show(fragmentManager, "dialog");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void log(String s) {
        Log.i(TAG,  s);
    }
}
