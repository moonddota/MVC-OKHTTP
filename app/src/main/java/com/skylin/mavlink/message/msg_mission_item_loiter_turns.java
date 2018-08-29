package com.skylin.mavlink.message;

import com.MAVLink.common.msg_mission_item_int;
import com.MAVLink.enums.MAV_CMD;
import com.MAVLink.enums.MAV_FRAME;

/**
 * Created by sjj on 2017/6/6.
 */

public class msg_mission_item_loiter_turns extends msg_mission_item_int {
    public msg_mission_item_loiter_turns(float altitude, int turns,float radius ) {
        this(0,0,altitude,turns,radius);
    }

    public msg_mission_item_loiter_turns(int lat, int lng, float altitude, int turns,float radius) {
        autocontinue = 1;
        frame = MAV_FRAME.MAV_FRAME_GLOBAL_RELATIVE_ALT;
        command = MAV_CMD.MAV_CMD_NAV_LOITER_TURNS;
        param1 = turns;
        param3 = radius;
        this.x = lat;
        this.y = lng;
        this.z = altitude;
    }
}
