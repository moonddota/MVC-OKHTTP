package com.skylin.mavlink.model;

import android.location.Location;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.blankj.utilcode.util.EmptyUtils;

import java.io.Serializable;

/**
 * Created by SJJ on 2017/2/23.
 * 经纬度坐标系上的点
 */
public class Point implements Cloneable, Serializable, Comparable<Point> {
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
    public static final int IDLING = 2;

    /**
     * 经度
     */
    private final double longitude;
    /**
     * 纬度
     */
    private final double latitude;
    private double altitude;
    /**
     * 0° ~ 360°
     */
    public double corner = -1;
    /**
     * {@link #IDLING}
     * {@link #TURNOFF}
     * {@link #OPEN}
     */
    private int status = IDLING;
    public static final int EXTRA_NORMAL = 0;
    public static final int EXTRA_CUT_POINT = 1;
    public static final int EXTRA_TOGGLE_LINE = 2;
//    public static final int EXTRA_PATH_FRAME = 3;
    public static final int EXTRA_IMITATION_FLIGHT_START = 4;
    public static final int EXTRA_IMITATION_FLIGHT_END = 5;
    public static final int EXTRA_FRAME_POINT = 6;
    private int extra = EXTRA_NORMAL;


    //S 南纬 N 北纬 E 东经 W 西经
    public Point(String s) {
        longitude = parse(s, "E", "W");
        latitude = parse(s, "N", "S");
    }

    private double parse(String s, String flag1, String flag2) {
        String[] split = s.split("\\′");
        int n = 1;
        String lngStr = null;
        for (String s1 : split) {
            s1 = s1.trim();
            if (!TextUtils.isEmpty(s1)) {
                if (s1.startsWith(flag1)) {
                    n = 1;
                    lngStr = s1.replace(flag1, "");
                    break;
                } else if (s1.startsWith(flag2)) {
                    n = -1;
                    lngStr = s1.replace(flag2, "");
                    break;
                }
            }
        }
        if (EmptyUtils.isEmpty(lngStr))
            throw new IllegalArgumentException("格式不正确:" + s + " " + flag1 + " " + flag2);
        String[] split1 = lngStr.split("°");
        return n * (Double.parseDouble(split1[0]) + Double.parseDouble(split1[1].split("\\'")[0]) / 60);
    }

    public Point(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public Point(double longitude, double latitude, double altitude) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.altitude = altitude;
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
     * 纬度
     *
     * @return
     */
    public double getLatitude() {
        return latitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public int getStatus() {
        return status;
    }

    public Point setStatus(int status) {
        this.status = status;
        return this;
    }

    public int getExtra() {
        return extra;
    }

    public void setExtra(int extra) {
        this.extra = extra;
    }

    public double distance(Point end) {
        float[] results = new float[2];
        Location.distanceBetween(latitude, longitude, end.getLatitude(), end.getLongitude(), results);
//        if (altitude == end.altitude) {
//            return results[0];
//        }
//        double diffAlt = end.altitude - altitude;
//        return Math.sqrt(results[0] * results[0] + diffAlt * diffAlt);
        return results[0];
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
            return distance(point) < 0.01;
        }
        return false;
    }

    @Override
    public Point clone() {
        try {
            return (Point) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return "Point{" +
                "longitude=" + longitude +
                ", latitude=" + latitude +
                ", altitude=" + altitude +
                ", status=" + status +
                '}';
    }

    @Override
    public int compareTo(@NonNull Point o) {
        if (longitude == o.longitude) {
            if (latitude == o.latitude) {
                return 0;
            } else {
                return latitude > o.latitude ? 1 : -1;
            }
        } else {
            return longitude > o.longitude ? 1 : -1;
        }
    }
}
