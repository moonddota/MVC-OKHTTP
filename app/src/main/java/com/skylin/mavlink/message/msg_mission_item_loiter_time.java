package com.skylin.mavlink.message;

import com.MAVLink.common.msg_mission_item_int;
import com.MAVLink.enums.MAV_CMD;
import com.MAVLink.enums.MAV_FRAME;

/**
 * Created by sjj on 2017/6/6.
 */

public class msg_mission_item_loiter_time extends msg_mission_item_int {
    public msg_mission_item_loiter_time(short target_system,short target_component,float altitude,int delay) {
        this(target_system,target_component,0,0,altitude,delay);
    }

    public msg_mission_item_loiter_time(short target_system,short target_component, int lat, int lng, float altitude, int delay) {
        this.target_system = target_system;
        this.target_component = target_component;
        autocontinue = 1;
        frame = MAV_FRAME.MAV_FRAME_GLOBAL_RELATIVE_ALT;
        command = MAV_CMD.MAV_CMD_NAV_LOITER_TIME;
        param1 = delay;
        this.x = lat;
        this.y = lng;
        this.z = altitude;
    }
}
