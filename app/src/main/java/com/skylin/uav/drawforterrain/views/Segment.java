package com.skylin.uav.drawforterrain.views;


/**
 * Created by SJJ on 2017/2/23.
 * 线段
 */

public class Segment extends StraightLine implements Cloneable {
    private Point start;
    private Point end;

    public Segment(Point start, Point end) {
        super(start, end);
        init(start, end);
    }

    private void init(Point start, Point end) {
        this.start = start;
        this.end = end;
    }

    public Segment setStart(Point start) {
        double slope = slope(start, end);
        init(slope, interceptY(slope, start));
        this.start = start;
        return this;
    }

    public Point getStart() {
        return start;
    }

    public Segment setEnd(Point end) {
        double slope = slope(start, end);
        init(slope, interceptY(slope, start));
        this.end = end;
        return this;
    }

    public Point getEnd() {
        return end;
    }


    public double getLength() {
        return start.distance(end);
    }

    @Override
    public Point intersection(StraightLine straightLine) {
        Point point = super.intersection(straightLine);
        if (point == null) return null;
        //异号 在中间
        if (contain(point)) {
            if (straightLine instanceof Segment) {
                if (straightLine.contain(point))
                    return point;
            } else {
                return point;
            }

        }
        return null;
    }

    @Override
    public void rotate(Point centrePoint, int degrees) {
        throw new RuntimeException("nonsupport");

    }

    @Override
    public void translation(Point point) {
        throw new RuntimeException("nonsupport");
    }

    public boolean contain(Point point) {
//        boolean contain = super.contain(point);
//        if (!contain)ic_return false;

        boolean b1 = (point.getLongitude() - start.getLongitude()) * (point.getLongitude() - end.getLongitude()) <= 0
                || TQPathMath.equals(point.getLongitude(), start.getLongitude())
                || TQPathMath.equals(point.getLongitude(), end.getLongitude());
        boolean b2 = (point.getLatitude() - start.getLatitude()) * (point.getLatitude() - end.getLatitude()) <= 0
                || TQPathMath.equals(point.getLatitude(), start.getLatitude())
                || TQPathMath.equals(point.getLatitude(), end.getLatitude());
        return b1 && b2;
    }

    public boolean containExact(Point point) {
        return contain(point) && super.contain(point);
    }

    @Override
    protected Segment clone() {
        try {
            Segment clone = (Segment) super.clone();
            clone.setStart(start.clone());
            clone.setEnd(end.clone());
            return clone;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String toString() {
        return "[start:" + start + " end:" + end + " slope:" + getSlope() + " y:" + getInterceptY() + "]";
    }

    public Segment inversion() {
        return new Segment(end, start);
    }
}
