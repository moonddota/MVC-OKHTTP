package com.skylin.mavlink.message;

import com.MAVLink.common.msg_mission_item_int;
import com.MAVLink.enums.MAV_CMD;
import com.MAVLink.enums.MAV_FRAME;

/**
 * Created by sjj on 2017/4/14.
 */

public class msg_mission_item_servo extends msg_mission_item_int {
    public static final int spray_channel = 9;
    public static final int spray_pwm_close = 0;
    public static final int spray_pwm_idling = 1100;
    public static final int spray_pwm_maximum = 1900;
    public static final int spray_pwm_minimum = 1200;
    public static final int PARAM3_NORMAL = 0;
    public static final int PARAM3_TOGGLELINE = 1;
    public static final int PARAM3_FULL_OPEN = 2;

    public msg_mission_item_servo(short target_system,short target_component,int pwm) {
        this(target_system,target_component,pwm,PARAM3_NORMAL);
    }
    public msg_mission_item_servo(short target_system,short target_component,int pwm, int param3) {
        this.target_system = target_system;
        this.target_component = target_component;
        autocontinue = 1;
        frame = MAV_FRAME.MAV_FRAME_GLOBAL_RELATIVE_ALT;
        command = MAV_CMD.MAV_CMD_DO_SET_SERVO;
        param1 = spray_channel;
        param2 = pwm;
        this.param3 = param3;//切航线1 开启4个喷头2
    }
}
