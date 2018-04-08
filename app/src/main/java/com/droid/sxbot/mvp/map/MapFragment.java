package com.droid.sxbot.mvp.map;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.droid.sxbot.ListAdapter;
import com.droid.sxbot.R;
import com.droid.sxbot.customview.MapView;
import com.droid.sxbot.entity.Indicator;
import com.leon.lfilepickerlibrary.LFilePicker;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lisongting on 2018/4/6.
 */

public class MapFragment extends Fragment {

    private MapView mapView;
    private RelativeLayout bottom;
    private ValueAnimator translateAnimIn,translateAnimOut;
    private int popHeight;
    private RelativeLayout parentView;
    private ImageView expandImg;
    private LinearLayout prevBottomLine,bottomLine;
    private List<Indicator> indicatorList;
    private ListAdapter adapter;
    private RecyclerView recyclerView;
    private String selectedFileName = "";
    private boolean isShowingList = false;
    private int currentSelectPos = -1;

    public MapFragment(){
        indicatorList = new ArrayList<>();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        adapter = new ListAdapter(getContext());
        View view = inflater.inflate(R.layout.map_fragment, parent,false);
        mapView = view.findViewById(R.id.map_view);
        parentView = view.findViewById(R.id.parent_view);
        bottom = (RelativeLayout) inflater.inflate(R.layout.bottom_popup_layout,null);
        recyclerView = bottom.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        bottom.setLayoutParams(params);
        parentView.addView(bottom);
        bottom.setVisibility(View.INVISIBLE);

        prevBottomLine = view.findViewById(R.id.ll_prev_bottom_line);
        
        bottomLine = view.findViewById(R.id.ll_bottom_line);
        expandImg = bottom.findViewById(R.id.expand_icon);

        adapter.setOnClickListener(new ListAdapter.OnClickListener() {
            @Override
            public void onClick(View view, int pos) {
                selectFile(pos);
//                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                intent.addCategory(Intent.CATEGORY_OPENABLE);
//                intent.setType("audio/*");
//                startActivityForResult(Intent.createChooser(intent, "请选择一个音频文件"),pos);
            }
        });
        recyclerView.setAdapter(adapter);
        prevBottomLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isShowingList) {
                    showList();
                }
            }
        });

        bottomLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isShowingList) {
                    hideList();
                } else {
                    showList();
                }
            }
        });
        mapView.setIndicatorListener(new MapView.OnCreateIndicatorListener() {
            @Override
            public void getIndicator(Indicator indicator) {
                log("get Indicator:" + indicator);
                indicatorList.add(indicator);
                adapter.setData(indicatorList);
            }
        });

        return view;
    }

    private void log(String s) {
        Log.i("MapFragment", s);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void initAnimation() {
        if (translateAnimIn != null && translateAnimOut != null) {
            return;
        }
        popHeight = bottom.getHeight()-expandImg.getHeight();
        translateAnimIn = ValueAnimator.ofInt(popHeight, 0);
        translateAnimIn.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                bottom.setTranslationY(value);
            }
        });
        translateAnimIn.setDuration(500);
        translateAnimIn.setInterpolator(new DecelerateInterpolator());


        translateAnimOut = ValueAnimator.ofInt(0, popHeight);
        translateAnimOut.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                bottom.setTranslationY(value);
            }
        });
        translateAnimOut.setDuration(500);
        translateAnimOut.setInterpolator(new DecelerateInterpolator());
    }

    private void showList(){
        if (!isShowingList) {
            initAnimation();
            bottom.setVisibility(View.VISIBLE);
            translateAnimIn.start();
            expandImg.setImageResource(R.drawable.ic_arrow_down);
            Drawable d = getResources().getDrawable(R.drawable.ic_arrow_down,null);
            d.setBounds(0, 0, d.getMinimumWidth(), d.getMinimumHeight());
            prevBottomLine.setVisibility(View.INVISIBLE);
            isShowingList = true;
        }

    }

    private void hideList(){
        if (isShowingList) {
            initAnimation();
            translateAnimOut.start();
            expandImg.setImageResource(R.drawable.ic_arrow_up);
            Drawable d = getResources().getDrawable(R.drawable.ic_arrow_up,null);
            d.setBounds(0, 0, d.getMinimumWidth(), d.getMinimumHeight());
            isShowingList = false;
        }

    }

    private void selectFile(int pos) {
        currentSelectPos = pos;
        int i = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (i == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);


        } else {
            new LFilePicker()
                    .withSupportFragment(MapFragment.this)
                    .withRequestCode(pos)
                    .withStartPath("/storage/emulated/0")
                    .withMaxNum(1)
                    .withTitle("请选择一个音频文件")
                    .withFileFilter(new String[]{".mp3", ".wav"})
                    .start();
        } 

    }

    private String getRealPathFromUri(Uri uri) {
        String res ;
        String[] proj = {MediaStore.Audio.AudioColumns.DATA};
        Cursor cursor = getContext().getContentResolver().query(uri, proj, null, null, null);
        if (cursor == null) {
            res = "";
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA);
            res = cursor.getString(index);
            cursor.close();
        }

        return res;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (currentSelectPos != -1) {
                    new LFilePicker()
                            .withSupportFragment(MapFragment.this)
                            .withRequestCode(currentSelectPos)
                            .withStartPath("/storage/emulated/0")
                            .withMaxNum(1)
                            .withTitle("请选择一个音频文件")
                            .withFileFilter(new String[]{".mp3", ".wav"})
                            .start();
                }
                currentSelectPos = -1;
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
                currentSelectPos = -1;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
//            Uri uri = data.getData();
//            if ("file".equals(uri.getScheme())) {
//                selectedFileName = uri.getPath();
//            } else {
//                selectedFileName = getRealPathFromUri(uri);
//            }
//            indicatorList.get(requestCode).setFile(selectedFileName);
//            adapter.setData(indicatorList);
//            //recyclerView.setAdapter(adapter);
//            log("文件URI：" + uri.getPath());
//            log("scheme:" + uri.getScheme());
//            log("lastPathSegment:" + uri.getLastPathSegment());
////            log("")
//            log("文件绝对路径：" + getRealPathFromUri(uri));
            List<String> list = data.getStringArrayListExtra("paths");
            log("selected:"+list.get(0));
            selectedFileName = list.get(0);
            indicatorList.get(requestCode).setFile(selectedFileName);
            adapter.setData(indicatorList);
        } else {
            selectedFileName = "";
        }
    }
}
