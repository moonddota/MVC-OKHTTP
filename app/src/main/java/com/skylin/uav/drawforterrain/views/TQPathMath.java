package com.skylin.uav.drawforterrain.views;


import android.location.Location;



import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by SJJ on 2017/2/25.
 */

public class TQPathMath {
    private static final double EARTH_RADIUS = 6378137;
    private static final double error = 0.00000002;

    /**
     * 经纬度允许误差范围内认为相等
     *
     * @param v1
     * @param v2
     * @return
     */
    public static boolean equals(double v1, double v2) {
        return Math.abs(v1 - v2) <= error;
    }

    public static boolean equals(Point p1, Point p2) {
        return p1 == null && p2 == null || p1 != null && p2 != null && equals(p1.getLongitude(), p2.getLongitude()) && equals(p1.getLatitude(), p2.getLatitude());
    }

    public static double getDistance(Point start, Point end) {
        float[] results = new float[2];
        Location.distanceBetween(start.getLatitude(), start.getLongitude(), end.getLatitude(), end.getLongitude(), results);
        return results[0];
    }

    /**
     * 根据维度与弧度值计算经度差
     *
     * @param latitude 纬度
     * @param distance 距离，纬度方向弧线长度
     * @return 返回经度值
     */
    public static double getDLongitude(double latitude, double distance) {
        return Math.toDegrees(distance / (Math.cos(Math.toRadians(latitude)) * EARTH_RADIUS));
    }

    /**
     * 根据距离返回纬度差
     *
     * @param distance 经度方向弧线长度
     * @return 返回纬度差值
     */
    public static double getDLatitude(double distance) {
        return getDLongitude(0, distance);
    }

    /**
     * 根据已有斜率逆时针旋转一定角度之后得到新的角度 的弧度值
     *
     * @param slope   斜率
     * @param degrees 旋转角度
     * @return 返回弧度值
     */
    public static double rotateDegrees(double slope, int degrees) {
        return Math.toRadians(Math.toDegrees(Math.atan(slope)) + degrees);
    }

    /**
     * @param start
     * @param end
     * @param weight 新的点距离起点距离占总距离的比重
     * @return
     */
    public static Point newPoint(Point start, Point end, double weight) {
        return new Point(weight(start.getLongitude(), end.getLongitude(), weight), weight(start.getLatitude(), end.getLatitude(), weight));
    }

    private static double weight(double start, double end, double weight) {
        return (end - start) * weight + start;
    }

    public static void sort(List<Point> points) {
        if (points.size() != 2) throw new IllegalArgumentException("Point 只能为 2");
        Point point1 = points.get(0);
        Point point2 = points.get(1);
        if (point1.getLongitude() > point2.getLongitude() || point1.getLongitude() == point2.getLongitude() && point1.getLatitude() > point2.getLatitude()) {
            Collections.swap(points, 0, 1);
        }
    }

    public static double toMapLength(double radians, double length, double latitude) {
        return Math.hypot(TQPathMath.getDLatitude(length * Math.sin(radians)), TQPathMath.getDLongitude(latitude, length * Math.cos(radians)));
    }

    public static void removeDuplicatePoint(List<Point> points) {
        List<Point> points1 = new ArrayList<>(points.size());
        for (Point point : points) {
            if (!points1.contains(point)) {
                points1.add(point);
            }
        }
        points.clear();
        points.addAll(points1);
    }

    public static void removeDuplicate(List<Pair<Point, Segment>> pairs) {
        List<Pair<Point, Segment>> pairsTemp = new ArrayList<>(pairs.size());
        List<Point> pointTemp = new ArrayList<>(pairs.size());
        for (Pair<Point, Segment> pair : pairs) {
            if (!pointTemp.contains(pair.first)) {
                pointTemp.add(pair.first);
                pairsTemp.add(pair);
            }
        }
        pairs.clear();
        pairs.addAll(pairsTemp);
    }

    public static boolean containExact(List<Point> frame, Point target) {
        boolean b = false;
        for (int i = 0, j = frame.size() - 1; i < frame.size(); j = i, i++) {
            Point lp = frame.get(j);
            Point cp = frame.get(i);
            if (lp.getLongitude() > target.getLongitude() != cp.getLongitude() > target.getLongitude()) {
                StraightLine straightLine = new StraightLine(lp, cp);
                if (target.getLatitude() < straightLine.getY(target.getLongitude())) {
                    b = !b;
                }
            }
        }
        return b;
    }

    public static boolean contain(List<Point> frame, Point target) {
        boolean b = false;
        for (int i = 0, j = frame.size() - 1; i < frame.size(); j = i, i++) {
            Point lp = frame.get(j);
            Point cp = frame.get(i);
            if ((lp.getLongitude() > target.getLongitude() || equals(lp.getLongitude(), target.getLongitude())) !=
                    (cp.getLongitude() > target.getLongitude() || equals(lp.getLongitude(), target.getLongitude()))) {
                StraightLine straightLine = new StraightLine(lp, cp);
                double y = straightLine.getY(target.getLongitude());
                if (target.getLatitude() < y || equals(y, target.getLatitude())) {
                    b = !b;
                }
            }
        }
        return b;
    }
}
