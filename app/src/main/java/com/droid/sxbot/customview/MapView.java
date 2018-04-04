package com.droid.sxbot.customview;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by lisongting on 2018/4/3.
 * 用来放置平面地图
 */

public class MapView extends View{


    public MapView(Context context) {
        this(context,null);
    }

    public MapView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public MapView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
//        super.onDraw();

    }


}
