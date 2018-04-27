package com.droid.sxbot.util;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.util.DisplayMetrics;
import android.util.Size;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;

import java.io.Closeable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by lisongting on 2017/6/18.
 */

public class Util {

    //将十六进制的字符串还原为utf-8中文名
    public static String hexStringToString(String s) {
        if (s == null || s.equals("")) {
            return null;
        }
        s = s.replace(" ", "");
        byte[] baKeyword = new byte[s.length() / 2];
        for (int i = 0; i < baKeyword.length; i++) {
            try {
                baKeyword[i] = (byte) (0xff & Integer.parseInt(
                        s.substring(i * 2, i * 2 + 2), 16));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            s = new String(baKeyword, "utf-8");
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return s;
    }

    //sizes是支持的size集合，width和height是期望展示的图像宽高
    public static Size getPreferredPreviewSize(Size[] sizes, int width, int height) {
        List<Size> collectorSizes = new ArrayList<>();
        for (Size option : sizes) {
            //找到长宽都大于指定宽高的size，把这些size放在List中
            if (width > height) {
                if (option.getWidth() > width && option.getHeight() > height) {
                    collectorSizes.add(option);
                }
            } else {
                if (option.getHeight() > width && option.getWidth() > height) {
                    collectorSizes.add(option);
                }
            }
        }
        if (collectorSizes.size() > 0) {
            return Collections.min(collectorSizes, new Comparator<Size>() {
                @Override
                public int compare(Size s1, Size s2) {
                    return Long.signum(s1.getWidth() * s1.getHeight() - s2.getWidth() * s2.getHeight());
                }
            });
        }
        return sizes[0];
    }

    //将中文用户名转换为十六进制字符串形式
    public static String makeUserNameToHex(String userName) {
        byte[] src = new byte[0];
        try {
            src = userName.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    //获取状态栏的高度
    public static int getStatusBarHeight(Context context) {
        int height = 0;
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            height = resources.getDimensionPixelSize(resourceId);
        }
        return height;
    }

    public static int getActionBarHeight(Context context) {
        TypedValue tv = new TypedValue();
        int height = 0;
        if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            height = TypedValue.complexToDimensionPixelSize(tv.data, context.getResources().getDisplayMetrics());
        }
        return height;
    }

    public static void disableShiftMode(BottomNavigationView bottomNavigationView) {
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) bottomNavigationView.getChildAt(0);
        try {
            Field shiftMode = menuView.getClass().getDeclaredField("mShiftingMode");
            shiftMode.setAccessible(true);
            shiftMode.setBoolean(menuView,false);
            shiftMode.setAccessible(false);
            for(int i=0;i<menuView.getChildCount();i++) {
                BottomNavigationItemView itemView = (BottomNavigationItemView) menuView.getChildAt(i);
                itemView.setShiftingMode(false);
                itemView.setChecked(itemView.getItemData().isChecked());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static boolean isLargeDevice(Context context) {
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels > 2000;
    }

    public static boolean isPortrait(Context context) {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    public static DisplayMetrics getScreenInfo(Context context) {
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);

        return metrics;
    }

    public static void close(Closeable... closeList) {
        for(Closeable c:closeList) {
            if(c!=null) {
                try {
                    c.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
