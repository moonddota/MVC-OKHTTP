package task.model;


import com.skylin.uav.drawforterrain.nofly.Point;

import java.io.Serializable;

/**
 * Created by SJJ on 2017/2/23.
 * 线段
 */

public class Segment extends StraightLine implements Cloneable, Serializable {
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
        init(slope, start);
        this.start = start;
        return this;
    }

    public Point getStart() {
        return start;
    }

    public Segment setEnd(Point end) {
        double slope = slope(start, end);
        init(slope, start);
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
    public void rotate(Point centrePoint, double degrees) {
        throw new RuntimeException("nonsupport");

    }

    @Override
    public void translation(Point point) {
        throw new RuntimeException("nonsupport");
    }

    public boolean contain(Point point) {
        if (start.equals(point) || end.equals(point)) {
            return true;
        }
        double d1 = point.distance(start);
        double d2 = point.distance(end);
        boolean lng = point.getLongitude() > start.getLongitude() && point.getLongitude() < end.getLongitude() ||
                point.getLongitude() > end.getLongitude() && point.getLongitude() < start.getLongitude();
        boolean lat = point.getLatitude() > start.getLatitude() && point.getLatitude() < end.getLatitude() ||
                point.getLatitude() > end.getLatitude() && point.getLatitude() < start.getLatitude();
        double length = getLength();
        double abs = Math.abs(length - d1 - d2);
        return (lng || lat) && (abs < 0.01 || abs / length < 0.01) && super.contain(point);

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
        return "[start:" + start + " end:" + end + " :" + super.toString() + "]";
    }

    public Segment inversion() {
        return new Segment(end, start);
    }
}
