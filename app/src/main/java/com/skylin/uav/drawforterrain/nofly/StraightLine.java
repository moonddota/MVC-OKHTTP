package com.skylin.uav.drawforterrain.nofly;

import java.io.Serializable;

import task.util.TQMath;

/**
 * Created by SJJ on 2017/2/25.
 * 直线
 * y = a*x + b;
 */

public class StraightLine implements Serializable{
    /**
     * 斜率
     */
    private double slope;
    /**
     * 纵截距
     */
    private double interceptY;

    public StraightLine(double slope, double interceptY) {
        init(slope, interceptY);
    }

    public StraightLine(Point start, Point end) {
        double slope = slope(start, end);
        double interceptY = interceptY(slope, start);
        init(slope, interceptY);
    }

    public StraightLine(double slope, Point point) {
        init(slope, interceptY(slope, point));
    }

    protected void init(double slope, double interceptY) {
        this.slope = slope;
        this.interceptY = interceptY;
    }

    protected double slope(Point start, Point end) {
        double dLong = end.getLongitude() - start.getLongitude();
        if (dLong == 0) return Math.tan(Math.toRadians(90));
        return (end.getLatitude() - start.getLatitude()) / dLong;
    }

    protected double interceptY(double slope, Point point) {
        return point.getLatitude() - slope * point.getLongitude();
    }

    /**
     * 使直线以给定点为中心旋转一定角度
     *
     * @param centrePoint 旋转中心点，如果点不在直线上，则直线在旋转角度之后将会被平移到给定点
     * @param degrees     旋转角度
     */
    public void rotate(Point centrePoint, double degrees) {
        double degreesNew = Math.toDegrees(Math.atan(slope)) + degrees;
        double slopeNew = Math.tan(Math.toRadians(degreesNew));
        init(slopeNew, interceptY(slopeNew, centrePoint));
    }

    /**
     * 指定点到直线的距离|ax-y+b|/√（a*a+1）
     *
     * @param point
     * @return
     */
    public double distance(Point point) {
        return Math.abs(slope * point.getLongitude() - point.getLatitude() + interceptY) / Math.sqrt(slope * slope + 1);
    }

    /**
     * 将直线平移到指定点上
     *
     * @param point
     */
    public void translation(Point point) {
        init(slope, interceptY(slope, point));
    }

    public double getSlope() {
        return slope;
    }

    public double getInterceptY() {
        return interceptY;
    }

    public void setInterceptY(double interceptY) {
        this.interceptY = interceptY;
    }

    public double getY(double x) {
        return slope * x + interceptY;
    }

    public double getX(double y) {
        if (slope == 0) return Double.NaN;
        return (y - interceptY) / slope;
    }

    public double getY(StraightLine straightLine) {
        if (TQMath.equals(slope , straightLine.slope)) {
            return Double.NaN;
        } else if (slope == 0) {
            return interceptY;
        } else if (straightLine.slope == 0) {
            return straightLine.interceptY;
        } else {
            return (straightLine.slope * interceptY - slope * straightLine.interceptY)/ (straightLine.slope - slope);
        }
    }

    public double getX(StraightLine straightLine) {
        if (TQMath.equals(slope , straightLine.slope)) {
            return Double.NaN;
        } else if (slope == 0) {
            return straightLine.getX(interceptY);
        } else if (straightLine.slope == 0) {
            return getX(straightLine.interceptY);
        } else {
            return (straightLine.interceptY - interceptY) / (slope - straightLine.slope);
        }
    }

    public boolean contain(Point point) {
        double y = getY(point.getLongitude());
        return y == point.getLatitude();
    }

    public Point intersection(StraightLine straightLine) {
        double x = getX(straightLine);
        if (Double.isNaN(x)) return null;
        double y = getY(straightLine);
        if (Double.isNaN(y)) return null;
        return new Point(x, y);
    }

    @Override
    public String toString() {
        return "[slope:" + slope + " y:" + interceptY + "]";
    }
}
