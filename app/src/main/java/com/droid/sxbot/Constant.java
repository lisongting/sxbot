package com.droid.sxbot;

/**
 * Created by lisongting on 2017/10/9.
 */

public class Constant {

    //广播Intent中用于存放ros连接状态的key
    public static final String KEY_BROADCAST_ROS_CONN = "ros_conn_status";

    //订阅:base64地图数据的topic
    public static final String SUBSCRIBE_TOPIC_MAP = "/base64_img/map_img";

    //订阅:Xbot状态的topic
    public static final String SUBSCRIBE_TOPIC_ROBOT_STATE = "/mobile_base/xbot/state";

    //订阅：包含Xbot的位置和朝向信息的topic
    public static final String SUBSCRIBE_TOPIC_ROBOT_POSE = "/app/xbot_pose";

    //发布：用于控制Xbot移动的topic
    public static final String PUBLISH_TOPIC_CMD_MOVE = "/cmd_vel_mux/input/teleop";

    //发布：用于控制摄像头角度和云台角度的topic
    public static final String PUBLISH_TOPIC_CMD_CLOUD_CAMERA = "/mobile_base/commands/cloud_camera";

    //发布：用于控制电机电源的topic
    public static final String PUBLISH_TOPIC_CMD_MACHINERY_POWER = "/mobile_base/commands/power";

    //发布：设置Xbot的一系列位置点，并附带音频
    public static final String PUBLISH_TOPIC_POSE_AUDIO_ARRAY = "/mobile_base/pose_audio_array";

    //讯飞开放平台中获得的APPID
    public static final String APPID = "59198461";

    //用来表示Ros服务器的连接状态:连接成功
    public static final int CONN_ROS_SERVER_SUCCESS = 0x11;

    //连接失败
    public static final int CONN_ROS_SERVER_ERROR = 0x12;

    //Intentfilter的action值，用于区别广播
    public static final String ROS_RECEIVER_INTENTFILTER = "sxbot.rosconnection.receiver";

    //rgb图像的URL后缀
    public static final String CAMERA_RGB_RTMP_SUFFIX = "/rgb";
    //深度图像的URL后缀
    public static final String CAMERA_DEPTH_RTMP_SUFFIX = "/depth";

    public static final String XBOT_MODEL = "base_link.g3dj";

    public static final String MUSEUM_MODEL = "ISCAS_museum.g3dj";


}
