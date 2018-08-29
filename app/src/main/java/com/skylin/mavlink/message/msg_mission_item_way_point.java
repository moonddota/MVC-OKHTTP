package com.skylin.mavlink.message;

import com.MAVLink.common.msg_mission_item_int;
import com.MAVLink.enums.MAV_CMD;
import com.MAVLink.enums.MAV_FRAME;
import com.skylin.mavlink.model.Point;

/**
 * Created by sjj on 2017/4/14.
 */

public class msg_mission_item_way_point extends msg_mission_item_int {
    public static final float DEFAULT_DELAY = 1f;//todo 航点延迟 1s

    public msg_mission_item_way_point(short target_system, short target_component, Point wayPoint, float altitude) {
        this(target_system, target_component, wayPoint, altitude, DEFAULT_DELAY);
    }

    public msg_mission_item_way_point(short target_system, short target_component, Point wayPoint, float altitude, float delay) {
        this.target_system = target_system;
        this.target_component = target_component;
        autocontinue = 1;
        frame = MAV_FRAME.MAV_FRAME_GLOBAL_RELATIVE_ALT;
        command = MAV_CMD.MAV_CMD_NAV_WAYPOINT;
        this.x = (int) (wayPoint.getLatitude() * 1e7 + 0.5);
        this.y = (int) (wayPoint.getLongitude() * 1e7 + 0.5);
        this.z = altitude;
        param1 = delay;// 1s
//        if (App.getApp().getConfig().imitation())
            param2 = wayPoint.getExtra() == Point.EXTRA_IMITATION_FLIGHT_START ? 1 : wayPoint.getExtra() == Point.EXTRA_IMITATION_FLIGHT_END ? 2 : 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof msg_mission_item_way_point)) return false;
        msg_mission_item_way_point point = (msg_mission_item_way_point) obj;
        return equals(point.x, x) &&
                equals(point.y, y) &&
                equals(point.z, z) &&
                equals(point.param1, param1)&&
                equals(point.param2, param2)&&
                equals(point.param3, param3)&&
                equals(point.param4, param4);
    }

    private boolean equals(double v1, double v2) {
        return Math.abs(v1 - v2) <= 10e-8;
    }
}
