/*
 * Copyright 2017 lisongting
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.droid.sxbot;

/**
 * Created by lisongting on 2017/9/27.
 */

public class Config {

    //标记ROS服务端是否连接成功
    public static boolean isRosServerConnected = false;

    //Ros服务器IP
    public static String ROS_SERVER_IP = "192.168.0.135";

    //ROS服务端的端口
    public static final String ROS_SERVER_PORT = "9090";

    //文件传输服务的端口
    public static final int FILE_TRANSFER_PORT = 9999;

    //控制Xbot进行移动的速度
    public static double speed = 0.3;

    //音频文件选择模式。如果为1，表示始终在选择点之后立即选择文件，如果为0，表示始终稍后选择文件
    public static int AUDIO_FILE_SELECT_MODE = -1;

}
