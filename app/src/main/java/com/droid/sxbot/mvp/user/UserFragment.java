package com.droid.sxbot.mvp.user;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.droid.sxbot.R;
import com.droid.sxbot.mvp.user.recognize.RecogActivity;
import com.droid.sxbot.mvp.user.register.RegisterFragment;
import com.droid.sxbot.mvp.user.userlist.UserListActivity;


/**
 * Created by lisongting on 2017/12/11.
 */

public class UserFragment extends Fragment {

    private Button btUserList,btRegister, btRecognize;
    private RegisterFragment registerFragment;
    public UserFragment() {}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_user, container, false);
        btUserList = v.findViewById(R.id.bt_user_list);
        btRecognize = v.findViewById(R.id.bt_recognize);
        btRegister = v.findViewById(R.id.bt_register);

        initListeners();
        return v;
    }


    private void initListeners() {
        btUserList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), UserListActivity.class));
            }
        });

        btRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerFragment = new RegisterFragment();
                getFragmentManager().beginTransaction()
                        .add(registerFragment, "registerFragment")
                        .commit();
            }
        });
        btRecognize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED) {
                    startActivity(new Intent(getContext(), RecogActivity.class));
                } else {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
                }
            }
        });

    }

    public void onResume(){
        super.onResume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivity(new Intent(getContext(), RecogActivity.class));
            } else {
                if(!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)){
                    Intent intent = new Intent();
                    intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                    intent.setData(Uri.fromParts("package", getContext().getPackageName(), null));
                    startActivity(intent);
                    Toast.makeText(getContext(), "无法获取摄像头权限,请到手机设置中进行授权", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getContext(), "无法获取摄像头权限,请进行授权" , Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
