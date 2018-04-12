package com.droid.sxbot.entity;

/**
 * Created by lisongting on 2018/4/6.
 */

public class Indicator {
    //序列编号
    private int number;
    //在MapView中的x坐标
    private float x;
    //在MapView中的y坐标
    private float y;
    //朝向
    private float theta;
    //对应的音频文件
    private String file;

    public Indicator(){}

    public Indicator(float x, float y) {
        this.x = x;
        this.y = y;
        this.theta = 0F;
    }
    public Indicator(int number,float x, float y, float radius) {
        this.number = number;
        this.x = x;
        this.y = y;
        this.theta = radius;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getTheta() {
        return theta;
    }

    public void setTheta(float theta) {
        this.theta = theta;
    }

    public String getFile() {

        return file == null ? "" : file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    @Override
    public String toString() {
        return "Indicator{" +
                "number=" + number +
                ", x=" + x +
                ", y=" + y +
                ", theta=" + theta +
                ", file='" + file + '\'' +
                '}';
    }
}
