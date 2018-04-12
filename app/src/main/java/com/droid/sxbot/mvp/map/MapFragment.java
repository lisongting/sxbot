package com.droid.sxbot.mvp.map;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.droid.sxbot.AnimateDialog;
import com.droid.sxbot.App;
import com.droid.sxbot.Config;
import com.droid.sxbot.ItemTouchHelperCallback;
import com.droid.sxbot.ListAdapter;
import com.droid.sxbot.R;
import com.droid.sxbot.customview.MapView;
import com.droid.sxbot.entity.Indicator;
import com.droid.sxbot.util.Util;
import com.leon.lfilepickerlibrary.LFilePicker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by lisongting on 2018/4/6.
 */

public class MapFragment extends Fragment implements MapContract.View{

    private MapView mapView;
    private RelativeLayout bottom,parentView;
    private ValueAnimator translateAnimIn,translateAnimOut;
    private ImageView expandImg;
    private LinearLayout prevBottomLine,bottomLine;
    private ArrayList<Indicator> indicatorList;
    private ListAdapter adapter;
    private RecyclerView recyclerView;
    private Button btClear,btSend;
    private FragmentManager fragmentManager;
    private MapContract.Presenter presenter;
    private ItemTouchHelper touchHelper;
    private AnimateDialog dialog;
    private String selectedFileName = "";
    private boolean isShowingList = false;
    private int popSize;
    private int currentSelectPos = -1;
    private boolean isRotated = false;

    public MapFragment(){

    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
//        log("onCreateView()");
        adapter = new ListAdapter(getContext());
        indicatorList = new ArrayList<>();
        View view;
        RelativeLayout.LayoutParams params;
        if (Util.isPortrait(getContext())) {
            view = inflater.inflate(R.layout.map_fragment_portrait, parent, false);
            bottom = (RelativeLayout) inflater.inflate(R.layout.popup_layout_portrait,null);
            params = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        } else {
            view = inflater.inflate(R.layout.map_fragment_landscape, parent, false);
            bottom = (RelativeLayout) inflater.inflate(R.layout.popup_layout_landscape,null);
            DisplayMetrics metrics = Util.getScreenInfo(getContext());
            //设置为宽300dp
            params = new RelativeLayout.LayoutParams(
                    (int) (300*metrics.scaledDensity), RelativeLayout.LayoutParams.MATCH_PARENT);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        }
        mapView = view.findViewById(R.id.map_view);
        parentView = view.findViewById(R.id.parent_view);
        btSend = bottom.findViewById(R.id.bt_send);
        btClear = bottom.findViewById(R.id.bt_clear);
        recyclerView = bottom.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        bottom.setLayoutParams(params);
        parentView.addView(bottom);
        prevBottomLine = view.findViewById(R.id.ll_prev_bottom_line);
        bottomLine = view.findViewById(R.id.ll_bottom_line);
        expandImg = bottom.findViewById(R.id.expand_icon);
        recyclerView.setAdapter(adapter);
        bottom.setVisibility(View.INVISIBLE);
        if (savedInstanceState != null) {
            indicatorList = savedInstanceState.getParcelableArrayList("list");
            isShowingList = false;
            isRotated = true;
            adapter.setData(indicatorList);
        }
        fragmentManager = getFragmentManager();
        initView();
        initClickEvents();
        return view;
    }


    @Override
    public void initView() {
        ItemTouchHelperCallback callback = new ItemTouchHelperCallback();
        callback.setItemTouchHelperCallbackListener(
                new ItemTouchHelperCallback.ItemTouchHelperCallbackListener() {
                    @Override
                    public void onMove(RecyclerView.ViewHolder srcViewHolder, RecyclerView.ViewHolder destViewHolder) {
                        adapter.notifyItemMoved(srcViewHolder.getAdapterPosition(), destViewHolder.getAdapterPosition());
                        Collections.swap(indicatorList, srcViewHolder.getAdapterPosition(), destViewHolder.getAdapterPosition());
                        mapView.setIndicatorList(indicatorList);
                    }
                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder) {
                        int removePos = viewHolder.getAdapterPosition();
                        Indicator badIndicator = indicatorList.get(removePos);
                        int size = indicatorList.size();
                        for(int i=0;i<size;i++) {
                            Indicator current = indicatorList.get(i);
                            if (current.getNumber() > badIndicator.getNumber()) {
                                current.setNumber(current.getNumber() - 1);
                            }
                        }
                        indicatorList.remove(removePos);
                        mapView.setIndicatorList(indicatorList);
                        adapter.notifyDataSetChanged();
                    }
                });

        touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerView);
    }

    private void initClickEvents() {
        adapter.setOnClickListener(new ListAdapter.OnClickListener() {
            @Override
            public void onClick(View view, int pos) {
                selectFile(pos);
            }
        });
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
                indicatorList = mapView.getIndicatorList();
                adapter.setData(indicatorList);
                if (Config.AUDIO_FILE_SELECT_MODE == -1) {
                    if (dialog != null) {
                        fragmentManager.beginTransaction().remove(dialog).commit();
                    }
                    dialog = new AnimateDialog();
                    dialog.setModeAndContent(AnimateDialog.DIALOG_STYLE_SELECT_FILE,
                            "",true,new AnimateDialog.OnButtonClickListener() {
                                @Override
                                public void onCancel() {}
                                @Override
                                public void onConfirm() {
                                    if (Config.AUDIO_FILE_SELECT_MODE != AnimateDialog.FILE_SELECT_MODE_ALWAYS_DELAY) {
                                        //如果为始终选择文件，则打开文件管理器选择音频
                                        selectFile(indicatorList.size()-1);
                                    }
                                }
                            });
                    dialog.show(fragmentManager, "dialog");
                } if (Config.AUDIO_FILE_SELECT_MODE == AnimateDialog.FILE_SELECT_MODE_ALWAYS) {
                    //如果为始终选择文件，则打开文件管理器选择音频
                    selectFile(indicatorList.size()-1);
                }
            }
        });

        btSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final List<String> fileList = new ArrayList<>();
                boolean hasBlankFile = false;
                for (Indicator indicator : indicatorList) {
                    if (indicator.getFile().length() == 0) {
                        hasBlankFile = true;
                    }else {
                        fileList.add(indicator.getFile());
                    }
                }
                if (fileList.size() == 0) {
                    Toast.makeText(getContext(),"您尚未添加任何音频", Toast.LENGTH_SHORT).show();
                }else if(fileList.size()>0){
                    if (hasBlankFile) {
                        if (dialog != null) {
                            fragmentManager.beginTransaction().remove(dialog).commit();
                        }
                        dialog = new AnimateDialog();
                        dialog.setModeAndContent(AnimateDialog.DIALOG_STYLE_SHOW_CONTENT,
                                "还有一些位置点没有设置音频,是否确认发送？", true,
                                new AnimateDialog.OnButtonClickListener() {
                                    @Override
                                    public void onCancel() {}
                                    @Override
                                    public void onConfirm() {
                                        sendFiles(fileList);
                                    }
                                });
                        dialog.show(fragmentManager, "dialog");
                    } else {
                        sendFiles(fileList);
                    }
                }

            }
        });

        btClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                indicatorList.removeAll(indicatorList);
                adapter.notifyDataSetChanged();
                mapView.clearAll();
            }
        });
    }

    private void sendFiles(List<String> fileList) {
        if (presenter != null&&fileList.size()>0) {
            if (dialog != null) {
                fragmentManager.beginTransaction().remove(dialog).commit();
            }
            dialog = new AnimateDialog();
            dialog.setModeAndContent(AnimateDialog.DIALOG_STYLE_LOADING,"",false,null);
            dialog.show(fragmentManager, "dialog");

            presenter.uploadFiles(fileList, new MapContract.uploadListener() {
                @Override
                public void onComplete() {
                    btSend.post(new Runnable() {
                        @Override
                        public void run() {
                            if (dialog != null) {
                                fragmentManager.beginTransaction().remove(dialog).commit();
                            }
                            dialog = new AnimateDialog();
                            dialog.setModeAndContent(
                                    AnimateDialog.DIALOG_STYLE_SHOW_CONTENT,"上传成功", false, null);
                            dialog.show(fragmentManager, "dialog");
                            if (Config.isRosServerConnected) {
                                //todo:
                                App app = (App) getActivity().getApplication();
                                if (app.getRosServiceProxy() != null) {
                                    presenter.setServiceProxy(app.getRosServiceProxy());
                                }
                                presenter.publishPoints(indicatorList);
                            } else {
                                Toast.makeText(getContext(), "Ros服务端未连接", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

                @Override
                public void onError(final String s) {
                    btSend.post(new Runnable() {
                        @Override
                        public void run() {
                            if (dialog != null) {
                                fragmentManager.beginTransaction().remove(dialog).commit();
                            }
                            dialog = new AnimateDialog();
                            dialog.setModeAndContent(
                                    AnimateDialog.DIALOG_STYLE_SHOW_CONTENT,"发送失败，请检查网络和服务端配置", false, null);
                            dialog.show(fragmentManager, "dialog");
                        }
                    });
                }
            });
        } else {
            log("presenter is null");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isRotated) {
            mapView.setIndicatorList(indicatorList);
        }
    }

    private void initAnimation() {
        if (translateAnimIn != null && translateAnimOut != null) {
            return;
        }
        //横屏模式和竖屏模式下的动画不一样
        if (Util.isPortrait(getContext())) {
            popSize = bottom.getHeight() - expandImg.getHeight();
            translateAnimIn = ValueAnimator.ofInt(popSize, 0);
            translateAnimIn.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (int) animation.getAnimatedValue();
                    bottom.setTranslationY(value);
                }
            });
            translateAnimIn.setDuration(500);
            translateAnimIn.setInterpolator(new DecelerateInterpolator());
            translateAnimOut = ValueAnimator.ofInt(0, popSize);
            translateAnimOut.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (int) animation.getAnimatedValue();
                    bottom.setTranslationY(value);
                }
            });
            translateAnimOut.setDuration(500);
            translateAnimOut.setInterpolator(new DecelerateInterpolator());
        } else {
            popSize = bottom.getWidth() - expandImg.getWidth();
            translateAnimIn = ValueAnimator.ofInt(popSize, 0);
            translateAnimIn.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (int) animation.getAnimatedValue();
                    bottom.setTranslationX(value);
                }
            });
            translateAnimIn.setDuration(500);
            translateAnimIn.setInterpolator(new DecelerateInterpolator());
            translateAnimOut = ValueAnimator.ofInt(0, popSize);
            translateAnimOut.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (int) animation.getAnimatedValue();
                    bottom.setTranslationX(value);
                }
            });
            translateAnimOut.setDuration(500);
            translateAnimOut.setInterpolator(new DecelerateInterpolator());
        }
       
    }

    private void showList(){
        if (!isShowingList||isRotated) {
//            log("showList()");
            initAnimation();
            bottom.setVisibility(View.VISIBLE);
            translateAnimIn.start();
            Drawable d;
            if (Util.isPortrait(getContext())) {
                expandImg.setImageResource(R.drawable.ic_arrow_down);
                d = getResources().getDrawable(R.drawable.ic_arrow_down,null);
            }else {
                expandImg.setImageResource(R.drawable.ic_arrow_right);
                d = getResources().getDrawable(R.drawable.ic_arrow_right,null);
            }
            d.setBounds(0, 0, d.getMinimumWidth(), d.getMinimumHeight());
            prevBottomLine.setVisibility(View.INVISIBLE);
            isShowingList = true;
            isRotated = false;
        }

    }

    private void hideList(){
        if (isShowingList||isRotated) {
//            log("hideList()");
            initAnimation();
            translateAnimOut.start();
            Drawable d;
            if (Util.isPortrait(getContext())) {
                expandImg.setImageResource(R.drawable.ic_arrow_up);
                d = getResources().getDrawable(R.drawable.ic_arrow_up, null);
            } else {
                expandImg.setImageResource(R.drawable.ic_arrow_left);
                d = getResources().getDrawable(R.drawable.ic_arrow_left, null);
            }
            d.setBounds(0, 0, d.getMinimumWidth(), d.getMinimumHeight());
            isShowingList = false;
            isRotated = false;
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
            List<String> list = data.getStringArrayListExtra("paths");
            log("selected:"+list.get(0));
            selectedFileName = list.get(0);
            indicatorList.get(requestCode).setFile(selectedFileName);
            adapter.setData(indicatorList);
        } else {
            selectedFileName = "";
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("list", indicatorList);
        outState.putBoolean("listOpen", isShowingList);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void showUploadSuccess() {

    }

    @Override
    public void showUploadError() {

    }

    @Override
    public void setPresenter(MapContract.Presenter presenter) {
        this.presenter = presenter;
    }

    private void log(String s) {
        Log.i("MapFragment", s);
    }
}
