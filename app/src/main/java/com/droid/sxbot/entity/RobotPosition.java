package com.droid.sxbot.entity;

/**
 * Created by lisongting on 2018/3/22.
 * 用来描述Xbot在地图中的位置信息
 */

public class RobotPosition {
    //在地图中的x坐标
    private float x;
    //在地图中的y坐标
    private float y;
    //朝向，(弧度表示)
    private float theta;

    public RobotPosition(){}

    public RobotPosition(float x, float y, float theta) {
        this.x = x;
        this.y = y;
        this.theta = theta;
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

    @Override
    public String toString() {
        return "RobotPosition{" +
                "x=" + x +
                ", y=" + y +
                ", theta=" + theta +
                '}';
    }
}
