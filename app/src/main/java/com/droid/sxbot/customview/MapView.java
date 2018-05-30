package com.droid.sxbot.customview;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.droid.sxbot.R;
import com.droid.sxbot.entity.Indicator;
import com.droid.sxbot.util.Util;

import java.util.ArrayList;

/**
 * Created by lisongting on 2018/4/3.
 * 用来放置平面地图
 */

public class MapView extends View{

    private Matrix adjustMapMatrix,outBoundMatrix;
    private Bitmap map,smallIcon,bigIcon;
    private Paint shadowPaint,indicatorPaint,numTextPaint,titleTextPaint;
    private float indicatorRadius = 20;
    //序号的大小是indicatorRadius的1.5倍
    private float numTextSize = (float) (20 * 1.5);
    private Path path ;
    private Rect numTextBound,titleTextBound;
    private RectF smallIconRectf,bigIconRectf;
    private ArrayList<Indicator> indicators;
    //表示当前是否手指还在屏幕上
    private boolean isTouching;
    private float touchX,touchY;
    private OnCreateIndicatorListener listener;
    private Paint outBoundPaint;
    private String[] modeArray = {"缩放调节模式","地图选点模式"};
    private final String pointSizeText="位置点大小调节:";
    //地图交互模式：包括两种，一是调整地图，二是地图选点
    private int mapMode = MAP_MODE_SELECT_POINT;
    public static final int MAP_MODE_ADJUST_MAP = 1;
    public static final int MAP_MODE_SELECT_POINT = 2;
    public static final int MAP_MODE_RESET = 3;
    public static final int MAP_MODE_CLEAR_POINTS = 4;

    private float scaleX = 1.0F;
    private float scaleY = 1.0F;
    //控件整体的缩放比例
    private float ratio = 1;

    //两手指的中心点
    private float gestureCenterX = 0;
    private float gestureCenterY = 0;

    //两手指上次的距离
    private double oldDistance;

    //平移的长度
    private float translationX = 0;
    private float translationY = 0;

    private int bottomTabIconHeight;
    //无效果
    private final int MODE_NONE = 0;
    //缩放
    private final int MODE_SCALE = 1;
    //平移
    private final int MODE_DRAG = 2;
    //地图缩放模式下的
    private int mode = MODE_NONE;

    //地图的外边界
    private RectF mapOutBoundRectf;
    private boolean isAdjusted = false;

    public interface OnCreateIndicatorListener{
        void getIndicator(Indicator indicator);
    }

    public MapView(Context context) {
        this(context,null);
    }

    public MapView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public MapView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        adjustMapMatrix = new Matrix();
        indicators = new ArrayList<>();
        shadowPaint = new Paint();
        path = new Path();
        numTextPaint = new Paint();
        titleTextPaint = new Paint();
        numTextBound = new Rect();
        titleTextBound = new Rect();
        mapOutBoundRectf = new RectF();
        smallIconRectf = new RectF();
        bigIconRectf = new RectF();
        outBoundPaint = new Paint();
        outBoundPaint.setColor(Color.GREEN);
        outBoundPaint.setStrokeWidth(8);
        outBoundPaint.setStyle(Paint.Style.STROKE);

        shadowPaint.setColor(Color.parseColor("#aae0e0e0"));
        numTextPaint.setColor(Color.RED);
        numTextPaint.setStyle(Paint.Style.FILL);
        numTextPaint.setTextSize(numTextSize);
        numTextPaint.setAntiAlias(true);

        titleTextPaint.setColor(Color.BLACK);
        titleTextPaint.setStyle(Paint.Style.FILL);
        titleTextPaint.setTextSize(45);
        titleTextPaint.setAntiAlias(true);

        indicatorPaint = new Paint();
        indicatorPaint.setColor(Color.parseColor("#c6ff00"));
        indicatorPaint.setStyle(Paint.Style.FILL);
        indicatorPaint.setAntiAlias(true);

        Resources res = getResources();
        map = BitmapFactory.decodeResource(res, R.drawable.map_pic);
        smallIcon = BitmapFactory.decodeResource(res, R.drawable.ic_point_smaller);
        bigIcon = BitmapFactory.decodeResource(res, R.drawable.ic_point_larger);
        bottomTabIconHeight = BitmapFactory.decodeResource(res, R.drawable.robot).getHeight();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = map.getWidth();
        int height = map.getHeight();
        if (!isAdjusted) {
            mapOutBoundRectf.left = 0;
            mapOutBoundRectf.top = 0;
            mapOutBoundRectf.right = width;
            mapOutBoundRectf.bottom = height;
        }

//        log("map size:" + width + "x" + height);
        DisplayMetrics metrics= Util.getScreenInfo(getContext());
        int statusBarHeight = Util.getStatusBarHeight(getContext());
        int actionbarSize = Util.getActionBarHeight(getContext());
        if (Util.isPortrait(getContext())) {
            setMeasuredDimension(metrics.widthPixels,
                    metrics.heightPixels-statusBarHeight-actionbarSize-bottomTabIconHeight);
        } else {
            setMeasuredDimension(metrics.widthPixels,
                    metrics.heightPixels-statusBarHeight-actionbarSize);
        }
//        log("measuredDimension:" + getMeasuredWidth() + "," + getMeasuredHeight());

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        outBoundMatrix = canvas.getMatrix();
        //对地图做大小或平移变换，同时把地图的边界矩阵outBoundMatrix也进行相同的变换
        if (mode == MODE_SCALE) {
            adjustMapMatrix.postScale(scaleX, scaleY, gestureCenterX, gestureCenterY);
            outBoundMatrix.postScale(scaleX, scaleY, gestureCenterX, gestureCenterY);
            ratio *= scaleX;
            //将这个矩形做坐标映射，跟随Matrix进行变换
            outBoundMatrix.mapRect(mapOutBoundRectf);
        } else if (mode == MODE_DRAG) {
            adjustMapMatrix.postTranslate(translationX, translationY);
            outBoundMatrix.postTranslate(translationX, translationY);
            outBoundMatrix.mapRect(mapOutBoundRectf);
        }
        canvas.drawBitmap(map, adjustMapMatrix, null);

        int indicatorCount = indicators.size();
        for(int i=0;i<indicatorCount;i++) {
            Indicator indicator = indicators.get(i);
            float centerX = indicator.getX() * (mapOutBoundRectf.width()) + mapOutBoundRectf.left;
            float centerY = mapOutBoundRectf.bottom - indicator.getY() * (mapOutBoundRectf.height());

            float theta = indicator.getTheta();
            if (indicatorCount == i + 1 && isTouching) {
                //绘制当前选中指示器的灰色外圆形部分
                canvas.drawCircle(centerX, centerY,
                        3 * indicatorRadius * ratio, shadowPaint);
                float distance = (float) Math.sqrt(Math.pow(centerX - touchX, 2) +
                        Math.pow(centerY - touchY, 2));
                if (distance > 1) {
                    theta = touchX > centerX ? (float) Math.asin((centerY - touchY) / distance) :
                            (float) (Math.PI - Math.acos((centerX - touchX) / distance));
                    if (touchX < centerX && touchY > centerY) {
                        theta = -theta;
                    }
                    indicator.setTheta(theta);
                }
            }

            path.reset();
            //绘制指示器(位置点)的圆形部分
            canvas.drawCircle(centerX, centerY, indicatorRadius * ratio, indicatorPaint);
            if (Math.abs(theta) < Math.PI / 2) {
                //将path移动到三个点,围成一个等腰三角形
                path.moveTo(centerX - ratio * indicatorRadius * (float) Math.sin(theta),
                        centerY - ratio * indicatorRadius * (float)Math.cos(theta));
                path.lineTo(centerX + ratio * indicatorRadius * (float) Math.sin(theta),
                        centerY + ratio * indicatorRadius * (float) Math.cos(theta));
                path.lineTo(centerX + 3 * ratio * indicatorRadius * (float) Math.cos(theta),
                        centerY - 3 * ratio * indicatorRadius * (float) Math.sin(theta));
            }else {
                theta = (float) (Math.PI - theta);
                path.moveTo(centerX - ratio * indicatorRadius * (float) Math.sin(theta),
                        centerY + ratio * indicatorRadius * (float)Math.cos(theta));
                path.lineTo(centerX + ratio * indicatorRadius * (float) Math.sin(theta),
                        centerY - ratio * indicatorRadius * (float) Math.cos(theta));
                path.lineTo(centerX - 3 * ratio * indicatorRadius * (float) Math.cos(theta),
                        centerY - 3 * ratio * indicatorRadius * (float) Math.sin(theta));
            }
            path.close();
            //画三角形
            canvas.drawPath(path, indicatorPaint);
            //绘制文字
            String textNum = String.valueOf(indicator.getNumber());
            numTextPaint.setTextSize(numTextSize*ratio);
            numTextPaint.getTextBounds(textNum, 0, textNum.length(), numTextBound);
            canvas.drawText(textNum, 0, textNum.length(),
                    centerX - numTextBound.width() / 2,
                    centerY + numTextBound.height() / 2, numTextPaint);
        }

        //绘制地图的外边界
        canvas.drawRect(mapOutBoundRectf, outBoundPaint);

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        if (mapMode == MAP_MODE_ADJUST_MAP) {
            //绘制左上角文字
            String text = modeArray[0];
            titleTextPaint.getTextBounds(text, 0, text.length(), titleTextBound);
            canvas.drawText(text,0,text.length(), 20,
                    (float)(20+titleTextBound.height()),titleTextPaint);
        } else if (mapMode == MAP_MODE_SELECT_POINT) {
            //绘制左上角文字
            String text = modeArray[1];
            titleTextPaint.getTextBounds(text, 0, text.length(), titleTextBound);
            canvas.drawText(text,0,text.length(), 20,
                    (float)(20+titleTextBound.height()),titleTextPaint);
        }
        //绘制左下角的文字
        titleTextPaint.getTextBounds(pointSizeText,0,pointSizeText.length(),titleTextBound);
        float leftStart = 20;
        float baseLine = (float) (height*0.95);
        canvas.drawText(pointSizeText, 0, pointSizeText.length(),
                leftStart, baseLine, titleTextPaint);
        float smallLeft = (float) (leftStart+titleTextBound.width()*1.1);
        float smallTop = (float) (baseLine - smallIcon.getHeight()*0.75);
        float bigLeft = (float) (leftStart+titleTextBound.width()*1.1+smallIcon.getWidth()*1.2);
        float bigTop = (float) (baseLine - smallIcon.getHeight()*0.75);
        canvas.drawBitmap(smallIcon, smallLeft, smallTop, null);
        canvas.drawBitmap(bigIcon, bigLeft, bigTop, null);
        smallIconRectf.set(smallLeft, smallTop,
                smallLeft + smallIcon.getWidth(), smallTop + smallIcon.getHeight());
        bigIconRectf.set(bigLeft, bigTop,
                bigLeft + bigIcon.getWidth(), bigTop + bigIcon.getHeight());
        //log("size:" + getMeasuredWidth() + "," + getMeasuredHeight());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            if (smallIconRectf.contains(event.getX(), event.getY())) {
                setIndicatorSize(0.9f);
                return true;
            } else if (bigIconRectf.contains(event.getX(), event.getY())) {
                setIndicatorSize(1.1f);
                return true;
            }
        }
        //如果是调整地图模式
        if (mapMode == MAP_MODE_ADJUST_MAP) {
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    getParent().requestDisallowInterceptTouchEvent(true);
                    gestureCenterX = 0;
                    gestureCenterY = 0;
                    mode = MODE_NONE;
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    mode = MODE_NONE;
                    oldDistance = getMoveDistance(event.getX(0), event.getY(0), event.getX(1), event.getY(1));
                    gestureCenterX = (event.getX(0) + event.getX(1)) * 0.5F;
                    gestureCenterY = (event.getY(0) + event.getY(1)) * 0.5F;
                    break;
                case MotionEvent.ACTION_MOVE:
                    int pointerCount = event.getPointerCount();
                    if (pointerCount == 2) {
                        double newDistance = getMoveDistance(event.getX(0), event.getY(0),
                                event.getX(1), event.getY(1));
                        float newGestureCenterX = (event.getX(0) + event.getX(1)) * 0.5F;
                        float newGestureCenterY = (event.getY(0) + event.getY(1)) * 0.5F;
                        if (Math.abs(newDistance - oldDistance) > 200 && oldDistance > 0) {
                            double delta = newDistance - oldDistance;
                            if (delta > 0) {
                                scaleX = 1.03F;
                                scaleY = 1.03F;
                            } else {
                                scaleX = 0.97F;
                                scaleY = 0.97F;
                            }
                            mode = MODE_SCALE;
                            isAdjusted = true;
                            invalidate();
                        } else if(getMoveDistance(newGestureCenterX, newGestureCenterY
                                ,gestureCenterX,gestureCenterY)>100){
                            mode = MODE_DRAG;
                            isAdjusted = true;
                            translationX = (newGestureCenterX - gestureCenterX)/10;
                            translationY = (newGestureCenterY - gestureCenterY)/10;
                            invalidate();
                        }
                    }
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    mode = MODE_NONE;
                    break;
                case MotionEvent.ACTION_UP:
                    mode = MODE_NONE;
                    oldDistance = 0;
                    scaleX=1.0F;
                    scaleY = 1.0F;
                    translationX = 0;
                    translationY = 0;
                    getParent().requestDisallowInterceptTouchEvent(false);
                    break;
                default:
                    break;
            }
        } else if (mapMode == MAP_MODE_SELECT_POINT) {
            switch (action){
                case MotionEvent.ACTION_DOWN:
                    if (mapOutBoundRectf.contains(event.getX(), event.getY())) {
                        isTouching = true;
                        //在地图中的百分比坐标
                        float percentInMapX = (event.getX() - mapOutBoundRectf.left) / (mapOutBoundRectf.width());
                        float percentInMapY = (mapOutBoundRectf.bottom - event.getY()) / (mapOutBoundRectf.height());
                        Indicator indicator = new Indicator(indicators.size()+1,
                                percentInMapX, percentInMapY, (float) (Math.PI/2));
//                        log("percent:(" + percentInMapX + "," + percentInMapY + ")");
                        indicators.add(indicator);
                        invalidate();
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    touchX = event.getX();
                    touchY = event.getY();
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    isTouching = false;
                    touchX = 0;
                    touchY = 0;
                    if (listener != null) {
                        int size = indicators.size();
                        if (size >= 1) {
                            listener.getIndicator(indicators.get(size - 1));
                        }
                    }
                    invalidate();
                    break;
                default:
                    break;
            }
        }
        return true;
    }
    //将地图重置为原始的大小和位置
    public void reset(){
        adjustMapMatrix.reset();
        mode = MODE_NONE;
        indicatorRadius = 20;
        ratio = 1;
        numTextSize = (float) (20 * 1.5);
        isAdjusted = false;
        mapOutBoundRectf.left = 0;
        mapOutBoundRectf.top = 0;
        mapOutBoundRectf.right = map.getWidth();
        mapOutBoundRectf.bottom = map.getHeight();
    }

    //调整地图模式，进行地图调节还是选点交互
    public void setMode(int mode) {
        switch (mode) {
            case MapView.MAP_MODE_ADJUST_MAP:
                this.mapMode = mode;
                outBoundPaint.setColor(Color.GRAY);
                break;
            case MapView.MAP_MODE_SELECT_POINT:
                this.mapMode = mode;
                outBoundPaint.setColor(Color.GREEN);
                break;
            case MapView.MAP_MODE_CLEAR_POINTS:
                //在这种情况下，mapMode保持不变(调整地图模式或选点模式)
                clearAll();
                break;
            case MapView.MAP_MODE_RESET:
                reset();
                break;
            default:break;
        }

        invalidate();
    }

    //清空所有的点
    public void clearAll() {
        indicators.removeAll(indicators);
        invalidate();
    }

    public void setIndicatorListener(OnCreateIndicatorListener listener) {
        this.listener = listener;
    }

    public void setIndicatorList(ArrayList<Indicator> list) {
        this.indicators = list;
        invalidate();
    }

    public void setIndicatorSize(float ratio) {
        indicatorRadius *= ratio;
        numTextSize *= ratio;
        invalidate();
    }

    //计算两个手指的移动距离
    public double getMoveDistance(float x1,float y1,float x2,float y2) {
        float x = x1 - x2;
        float y = y1 - y2;
        return Math.sqrt(x * x + y * y);
    }

    public ArrayList<Indicator> getIndicatorList(){
        return indicators;
    }

    private void log(String s) {
        Log.i("MapView", s);
    }
}
