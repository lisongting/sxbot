package com.droid.sxbot.mvp.user.register;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.droid.sxbot.R;
import com.droid.sxbot.mvp.user.GroupAdapter;
import com.droid.sxbot.util.Util;


/**
 * Created by lisongting on 2017/12/13.
 * 用来提示用户输入姓名
 */

public class RegisterFragment extends DialogFragment {
    private Window window;
    public RegisterFragment(){}
    private Button btCapture,btCancel;
    private TextView textView;
    private TextView groupName;
    private RecyclerView recyclerView;
    private GroupAdapter groupAdapter;
    private TextInputLayout textInputLayout;
    private String group;
    private Animation animationIn,animationOut;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_register, container, false);
        btCapture = v.findViewById(R.id.id_bt_camera);
        btCancel = v.findViewById(R.id.id_bt_cancel);
        textView = v.findViewById(R.id.id_textView);
        groupName = v.findViewById(R.id.id_group_name);
        textInputLayout = v.findViewById(R.id.id_text_input_layout);
        recyclerView = v.findViewById(R.id.id_group_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        window = getDialog().getWindow();
        groupAdapter = new GroupAdapter(getContext());
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        group = groupName.getText().toString();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }
        animationIn = AnimationUtils.loadAnimation(getContext(), R.anim.anim_list_in);
        animationOut = AnimationUtils.loadAnimation(getContext(), R.anim.anim_list_out);
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        DisplayMetrics metrics = Util.getScreenInfo(getContext());
        if (metrics.heightPixels < 2000) {
            window.setLayout((int) (metrics.widthPixels * 0.7), (int) (metrics.heightPixels * 0.5));
        } else {
            int dialogWidth = (int) (metrics.widthPixels * 0.5);
            int dialogHeight = (int) (metrics.heightPixels * 0.4);
            //如果对话库中的控件(最大200dp)大于dialogWidth，则将dialogwith设置为200dp的1.2倍
            float contentWidth = metrics.scaledDensity * 200;
            if (contentWidth > dialogWidth) {
                window.setLayout((int) (contentWidth * 1.2), dialogHeight);
            } else {
                window.setLayout(dialogWidth , dialogHeight);
            }
        }

        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        btCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = textView.getEditableText().toString();
                String userGroup = groupName.getText().toString();
                if (userGroup.equals(group)) {
                    Toast.makeText(getContext(), "请选择一个分组", Toast.LENGTH_SHORT).show();
                }else {
                    if (userName.length()>2) {
                        dismiss();
                        Intent intent = new Intent(getContext(), CameraActivity.class);
                        intent.putExtra("userName", userGroup+"_"+userName);
                        Toast.makeText(getContext(), userGroup+"_"+userName, Toast.LENGTH_SHORT).show();
                        startActivity(intent);
                    } else {
                        Toast.makeText(getContext(), "请输入正确的姓名", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        groupAdapter.setOnItemClickListener(new GroupAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String selectedGroup) {
                recyclerView.setVisibility(View.GONE);
                groupName.setText(selectedGroup);
                textInputLayout.setVisibility(View.VISIBLE);
            }
        });

        groupAdapter.setOnAddGroupListener(new GroupAdapter.OnAddGroupListener() {
            @Override
            public void onCheck(boolean isChecked) {
                hideKeyboard();
            }
        });

        groupName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (recyclerView.getVisibility() == View.GONE) {
                    recyclerView.setAdapter(groupAdapter);
                    recyclerView.setVisibility(View.VISIBLE);
//                    recyclerView.startAnimation(animationIn);
                    textInputLayout.setVisibility(View.GONE);
                } else {
//                    recyclerView.startAnimation(animationOut);
                    textInputLayout.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void hideKeyboard(){
        View view = getDialog().getWindow().peekDecorView();
        if (view != null) {
            // 隐藏虚拟键盘
            InputMethodManager inputManager = (InputMethodManager) getContext()
                    .getSystemService(Activity.INPUT_METHOD_SERVICE);
            if (inputManager.isActive() && getDialog().getWindow().getCurrentFocus() != null) {
                inputManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }




}
