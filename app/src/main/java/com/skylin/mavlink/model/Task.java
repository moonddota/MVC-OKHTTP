package com.skylin.mavlink.model;

import com.skylin.mavlink.message.msg_mission_item_servo;

import java.util.List;

/**
 * Created by sjj on 2017/4/17.
 */

public class Task {
    public static final int MODE_NORMAL = 0;
    public static final int MODE_LOITER = 1;
    private int mode = MODE_NORMAL;
    private Point home;
    private boolean takeOff;
    /**
     * 起飞高度 m 有起点才会生效
     */
    private float takeOffAltitude;
    /**
     * 起飞
     */
    private List<Point> takeOffPoints;
    /**
     * 路径点
     */
    private List<Point> wayPoints;
    /**
     * 降落
     */
    private List<Point> landingPoints;
    /**
     * m
     */
    private float flightAltitude;
    /**
     * 返航高度 m
     */
    private float returnAltitude;
    private int sprayPwm;
    private double baseAltitude;
    private int loiterTime;
    private String wayPointType;
    private double uavYaw;
    private double takeoffCorner = -1;
    private List<Point> safeFramePoints;

    private List<List<Point>> roadblocks;

    public double getTakeoffCorner() {
        return takeoffCorner;
    }

    public void setTakeoffCorner(double takeoffCorner) {
        this.takeoffCorner = takeoffCorner;
    }

    public List<List<Point>> getRoadblocks() {
        return roadblocks;
    }

    public void setRoadblocks(List<List<Point>> roadblocks) {
        this.roadblocks = roadblocks;
    }

    public List<Point> getSafeFramePoints() {
        return safeFramePoints;
    }

    public void setSafeFramePoints(List<Point> safeFramePoints) {
        this.safeFramePoints = safeFramePoints;
    }


    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public double getBaseAltitude() {
        return baseAltitude;
    }

    public void setBaseAltitude(double baseAltitude) {
        this.baseAltitude = baseAltitude;
    }

    public void setSprayPwm(int sprayPwm) {
        this.sprayPwm = sprayPwm;
    }

    public int getSprayPwm(int status) {
        switch (status) {
            case Point.OPEN:
                return sprayPwm;
            case Point.IDLING:
                return msg_mission_item_servo.spray_pwm_idling;
            case Point.TURNOFF:
            default:
                return msg_mission_item_servo.spray_pwm_close;
        }
    }

    public Point getHome() {
        return home;
    }

    public void setHome(Point home) {
        this.home = home;
    }

    public boolean isTakeOff() {
        return takeOff;
    }

    public void setTakeOff(boolean takeOff) {
        this.takeOff = takeOff;
    }

    public float getTakeOffAltitude() {
        return takeOffAltitude;
    }

    public void setAltitude(float takeOffAltitude, float flightAltitude, float returnAltitude) {
        this.takeOffAltitude = takeOffAltitude;
        this.flightAltitude = flightAltitude;
        this.returnAltitude = returnAltitude;
    }

    public List<Point> getTakeOffWayPoints() {
        return takeOffPoints;
    }

    public void setTakeOffWayPoints(List<Point> takeOffPoints) {
        this.takeOffPoints = takeOffPoints;
    }

    public List<Point> getWayPoints() {
        return wayPoints;
    }

    public void setWayPoints(List<Point> wayPoints) {
        this.wayPoints = wayPoints;
    }

    public List<Point> getLandingWayPoints() {
        return landingPoints;
    }

    public void setLandingWayPoints(List<Point> landingPoints) {
        this.landingPoints = landingPoints;
    }

    public float getFlightAltitude() {
        return flightAltitude;
    }

    public float getReturnAltitude() {
        return returnAltitude;
    }

    public int getLoiterTime() {
        return loiterTime;
    }

    public void setLoiterTime(int loiterTime) {
        this.loiterTime = loiterTime;
    }

    public void setWayPointType(String wayPointType) {
        this.wayPointType = wayPointType;
    }

    public String getWayPointType() {
        return wayPointType;
    }

    public void setUavYaw(double uavYaw) {
        this.uavYaw = uavYaw;
    }

    public double getUavYaw() {
        return uavYaw;
    }
}
