package com.droid.sxbot.entity;

import android.graphics.Bitmap;

import com.droid.sxbot.util.Util;

/**
 * Created by lisongting on 2017/12/12.
 */

public class UserInfo {

    /**
     * 用户的中文名
     */
    private String name;

    /**
     * 用户的分组名
     */
    private String groupName ;

    /**
     * 人脸bitmap
     */
    private Bitmap face;

    public UserInfo() {
        groupName = "";
    }

    public UserInfo(String name, Bitmap face) {
        this.name = name;
        this.face = face;
        this.groupName = "";
    }

    public UserInfo(String name, String group,Bitmap face) {
        this.name = name;
        this.face = face;
        this.groupName = group;
    }

    //从十六进制的字符串中解析出姓名和组名
    public void parseNameAndGroup(String hexString) {
        String str = Util.hexStringToString(hexString);
        String[] nameAndGroup = str.split("_");
        //表示这里只有姓名而无分组
        if (nameAndGroup.length == 1) {
            this.groupName = "";
            this.name = nameAndGroup[0];
        } else if (nameAndGroup.length == 2) {
            this.groupName = nameAndGroup[0];
            this.name = nameAndGroup[1];
        }

    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Bitmap getFace() {
        return face;
    }

    public void setFace(Bitmap face) {
        this.face = face;
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "name='" + name + '\'' +
                ", groupName='" + groupName + '\'' +
                ", face=" + face +
                '}';
    }
}
