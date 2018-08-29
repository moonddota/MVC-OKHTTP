package com.skylin.uav.drawforterrain.views;

import android.support.annotation.NonNull;


import java.io.Serializable;

/**
 * Created by SJJ on 2017/2/23.
 * 经纬度坐标系上的点
 */
public class Point implements Cloneable, Serializable, Comparable<Point> {
    /**
     * 空闲/无状态/没有指定
     */
    public static final int FREE = 0;
    /**
     * 关闭
     */
    public static final int TURNOFF = -1;
    /**
     * 开启
     */
    public static final int OPEN = 1;
    /**
     * 就绪
     */
    public static final int READY = 2;
    private double height = 3;
    /**
     * 经度
     */
    private double longitude;
    /**
     * 纬度
     */
    private double latitude;
    /**
     * {@link #FREE}
     * {@link #TURNOFF}
     * {@link #OPEN}
     */
    private int status = FREE;

    public Point() {
    }

    public Point(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }


    /**
     * 经度
     *
     * @return
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * 经度
     *
     * @param longitude
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * 纬度
     *
     * @return
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * 纬度
     *
     * @param latitude
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public int getStatus() {
        return status;
    }

    public Point setStatus(int status) {
        this.status = status;
        return this;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double distance(Point end) {
        double dLat = latitude - end.latitude;
        double dLong = longitude - end.longitude;
        return Math.sqrt(dLat * dLat + dLong * dLong);
    }

    @Deprecated
    @Override
    public int hashCode() {
        throw new RuntimeException("equals 方法被修改不能再使用hashCode方法");
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Point) {
            Point point = (Point) obj;
            return TQPathMath.equals(this, point);
        }
        return false;
    }

    @Override
    public Point clone() {
        try {
            return (Point) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString() {
        return "[" + longitude + "," + latitude + "]";
    }

    @Override
    public int compareTo(@NonNull Point o) {
        if (TQPathMath.equals(longitude, o.longitude)) {
            if (TQPathMath.equals(latitude, o.latitude)) {
                return 0;
            } else {
                return latitude > o.latitude ? 1 : -1;
            }
        } else {
            return longitude > o.longitude ? 1 : -1;
        }
    }
}
