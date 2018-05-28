package com.droid.sxbot.mvp.user.register;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.droid.sxbot.AnimateDialog;
import com.droid.sxbot.R;
import com.droid.sxbot.mvp.user.GroupAdapter;
import com.droid.sxbot.util.ImageUtils;
import com.droid.sxbot.util.Util;
import com.leon.lfilepickerlibrary.LFilePicker;

import java.util.List;


/**
 * Created by lisongting on 2017/12/13.
 */

public class RegisterFragment extends DialogFragment implements RegisterContract.View{
    private Window window;
    public RegisterFragment(){}
    private Button btCapture,btCancel;
    private Button btSelectModeShoot,btSelectModeLocal,btSelectModeCancel,btSelectModeConfirm;
    private TextView textView;
    private TextView groupName;
    private RecyclerView recyclerView;
    private GroupAdapter groupAdapter;
    private TextInputLayout textInputLayout;
    private String group,userGroup,userName;
    private LinearLayout centerInputLayout,btGroup;
    private ViewStub registerViewStub;
    private View inflatedStub;
    private String selectedPhotoFileName;
    private AnimateDialog dialog;
    private FragmentManager fragmentManager;
    private Bitmap faceBitmap;
    private RegisterContract.Presenter presenter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
        fragmentManager = getFragmentManager();
        presenter = new RegisterPresenter(this);
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
        btGroup = v.findViewById(R.id.id_button_group);
        registerViewStub = v.findViewById(R.id.id_view_stub);
        centerInputLayout = v.findViewById(R.id.center_input_layout);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        window = getDialog().getWindow();
        groupAdapter = new GroupAdapter(getContext());
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        group = groupName.getText().toString();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }
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
            //如果对话库中的控件(最大200dp)大于dialogWidth，则将dialogwidth设置为200dp的1.2倍
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
                userName = textView.getEditableText().toString();
                userGroup = groupName.getText().toString();
                if (userGroup.equals(group)) {
                    Toast.makeText(getContext(), "请选择一个分组", Toast.LENGTH_SHORT).show();
                }else {
                    if (userName.length()>=2) {
                        centerInputLayout.setVisibility(View.INVISIBLE);
                        btGroup.setVisibility(View.INVISIBLE);
                        inflatedStub = registerViewStub.inflate();
                        btSelectModeShoot = inflatedStub.findViewById(R.id.bt_select_mode_shoot);
                        btSelectModeLocal = inflatedStub.findViewById(R.id.bt_select_mode_local);
                        btSelectModeConfirm = inflatedStub.findViewById(R.id.bt_select_mode_confirm);
                        btSelectModeCancel = inflatedStub.findViewById(R.id.bt_select_mode_cancel);
                        initRegisterModeClickEvent();
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
                    textInputLayout.setVisibility(View.GONE);
                } else {
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

    private void initRegisterModeClickEvent() {
        btSelectModeShoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                Intent intent = new Intent(getContext(), CameraActivity.class);
                intent.putExtra("userName", userGroup+"_"+userName);
                Toast.makeText(getContext(), userGroup+"_"+userName, Toast.LENGTH_SHORT).show();
                startActivity(intent);
            }
        });

        btSelectModeLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPhoto();
            }
        });

        btSelectModeConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (faceBitmap != null) {
                    Toast.makeText(getContext(), "正在注册，请稍候...", Toast.LENGTH_LONG).show();
                    String hexStr = Util.makeUserNameToHex(userGroup + "_" + userName);
                    presenter.register(hexStr,
                            ImageUtils.encodeBitmapToBase64(faceBitmap, Bitmap.CompressFormat.JPEG, 70));
                } else {
                    Toast.makeText(getContext(), "请拍摄照片或选择本地照片", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btSelectModeCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    private void selectPhoto() {
        int r = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (r == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        } else {
            new LFilePicker()
                    .withSupportFragment(RegisterFragment.this)
                    .withRequestCode(0)
                    .withStartPath("/storage/emulated/0")
                    .withMaxNum(1)
                    .withTitle("请选择一张照片")
                    .withFileFilter(new String[]{".jpg", ".png"})
                    .start();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                new LFilePicker()
                        .withSupportFragment(RegisterFragment.this)
                        .withRequestCode(0)
                        .withStartPath("/storage/emulated/0")
                        .withMaxNum(1)
                        .withTitle("请选择一张照片")
                        .withFileFilter(new String[]{".jpg", ".png"})
                        .start();
            } else {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Intent intent = new Intent();
                    intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                    intent.setData(Uri.fromParts("package", getContext().getPackageName(), null));
                    startActivity(intent);
                    Toast.makeText(getContext(), "无法获取文件读写权限，请到手机设置中进行授权", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getContext(), "无法获取文件读写权限,请进行授权" , Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            List<String> list = data.getStringArrayListExtra("paths");
            selectedPhotoFileName = list.get(0);
            String fileName = selectedPhotoFileName.substring(selectedPhotoFileName.lastIndexOf('/')+1,
                    selectedPhotoFileName.length());
            btSelectModeLocal.setText("照片："+fileName);
            btSelectModeConfirm.setVisibility(View.VISIBLE);
        } else {
            selectedPhotoFileName = "";
        }
        if (!TextUtils.isEmpty(selectedPhotoFileName)) {
            //图片压缩
            DisplayMetrics metrics = Util.getScreenInfo(getContext());
            faceBitmap = ImageUtils.getBitmap(selectedPhotoFileName,
                    (int) (metrics.widthPixels*0.2F),
                    (int) (metrics.heightPixels*0.2F));
        }
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
    public void showSuccess() {
        dismiss();
        if (dialog != null) {
            fragmentManager.beginTransaction().remove(dialog).commit();
        }
        dialog = new AnimateDialog();
        dialog.setModeAndContent(AnimateDialog.DIALOG_STYLE_SHOW_CONTENT,
                "注册成功", false,
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
    public void initView() {

    }

    @Override
    public void setPresenter(RegisterContract.Presenter presenter) {
        this.presenter = presenter;
    }
}
