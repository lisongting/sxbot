package com.droid.sxbot.customview;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by lisongting on 2018/4/3.
 * 表示一个地图选点
 */

public class IndicatorView extends View{

    private float x;
    private float y;
    private float radius;

    public IndicatorView(Context context) {
        this(context,null);
    }

    public IndicatorView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public IndicatorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


}
