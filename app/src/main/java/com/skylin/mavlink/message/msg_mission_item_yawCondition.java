package com.skylin.mavlink.message;

import com.MAVLink.common.msg_mission_item_int;
import com.MAVLink.enums.MAV_CMD;
import com.MAVLink.enums.MAV_FRAME;


/**
 * Created by sjj on 2017/4/14.
 */

public class msg_mission_item_yawCondition extends msg_mission_item_int {
    private final boolean isRelative = false;

    /**
     * @param angle        飞机转向角度
     */
    public msg_mission_item_yawCondition(short target_system,short target_component,float angle) {
        this.target_system = target_system;
        this.target_component = target_component;
        autocontinue = 1;
        frame = MAV_FRAME.MAV_FRAME_GLOBAL_RELATIVE_ALT;

        command = MAV_CMD.MAV_CMD_CONDITION_YAW;
        param1 = angle;
        param2 = 15;
        param3 = 0;
        param4 = 0;
    }
}
