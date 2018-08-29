package task.model;

import com.skylin.uav.drawforterrain.nofly.Point;

import java.io.Serializable;

import task.util.TQMath;

/**
 * Created by SJJ on 2017/2/25.
 * 直线
 * ax + by + c = 0
 * kx - y  + b = 0
 */

public class StraightLine implements Serializable {
    private double a;
    private double b;
    private double c;

    public StraightLine(double k, double b) {
        this.a = k;
        this.b = -1;
        this.c = b;
    }

    public StraightLine(Point start, Point end) {
        this.a = end.getLatitude() - start.getLatitude();
        this.b = start.getLongitude() - end.getLongitude();
        this.c = end.getLongitude() * start.getLatitude() - start.getLongitude() * end.getLatitude();
    }

    public StraightLine(double slope, Point point) {
        init(slope, point);
    }

    protected void init(double slope, Point point) {
        if (Double.isInfinite(slope)) {
            this.a = 1;
            this.b = 0;
            this.c = -point.getLongitude();
        } else {
            this.a = slope;
            this.b = -1;
            this.c = point.getLatitude() - slope * point.getLongitude();
        }
    }

    protected double slope(double degrees) {
        degrees = TQMath.mod(degrees, 180);
        if (degrees == 90) {
            return Double.POSITIVE_INFINITY;
        }
        return Math.tan(Math.toRadians(degrees));
    }

    protected double slope(Point start, Point end) {
        double dLong = end.getLongitude() - start.getLongitude();
        if (dLong == 0) {
            return end.getLatitude() > start.getLatitude() ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
        }
        return (end.getLatitude() - start.getLatitude()) / dLong;
    }

    /**
     * 使直线以给定点为中心旋转一定角度
     *
     * @param centrePoint 旋转中心点，如果点不在直线上，则直线在旋转角度之后将会被平移到给定点
     * @param degrees     旋转角度
     */
    public void rotate(Point centrePoint, double degrees) {
        degrees = TQMath.mod(degrees, 180);
        if (b == 0) {//(-c/a , 0)
            init(slope(90 + degrees), centrePoint);
        } else {
            double slope1 = slope(Math.toDegrees(Math.atan(-a / b)) + degrees);
            init(slope1, centrePoint);
        }
    }

    /**
     * 指定点到直线的距离|ax-y+b|/√（a*a+1）
     *
     * @param point
     * @return
     */
    public double distance(Point point) {
        return Math.abs(a * point.getLongitude() + b * point.getLatitude() + c) / Math.hypot(a, b);
    }

    /**
     * 将直线平移到指定点上
     *
     * @param point
     */
    public void translation(Point point) {
        init(getSlope(), point);
    }

    public double getSlope() {
        return b == 0 ? Double.POSITIVE_INFINITY : -a / b;
    }

    public double getY(double x) {
        if (b == 0) {
            return Double.NaN;
        }
        return -(c + a * x) / b;
    }

    public double getX(double y) {
        if (a == 0) {
            return Double.NaN;
        }
        return -(c + b * y) / a;
    }

    public double getY(StraightLine straightLine) {
        if (TQMath.equals(getSlope(), straightLine.getSlope())) {
            return Double.NaN;
        } else if (a == 0) {
            return -c / b;
        } else if (straightLine.a == 0) {
            return -straightLine.c / straightLine.b;
        } else {
            return (a * straightLine.c - straightLine.a * c) / (straightLine.a * b - straightLine.b * a);
        }
    }

    public double getX(StraightLine straightLine) {
        if (TQMath.equals(getSlope(), straightLine.getSlope())) {
            return Double.NaN;
        } else if (b == 0) {
            return -c / a;
        } else if (straightLine.b == 0) {
            return -straightLine.c / straightLine.a;
        } else {
            return (b * straightLine.c - c * straightLine.b) / (a * straightLine.b - b * straightLine.a);
        }
    }

    public boolean contain(Point point) {
        if (a == 0 && b == 0) {
            return false;
        }
        if (a == 0) {
            return TQMath.equalsCoordinate(-c / b, point.getLatitude());
        }
        if (b == 0) {
            return TQMath.equalsCoordinate(-c / a, point.getLongitude());
        }
        double y = getY(point.getLongitude());
        return TQMath.equalsCoordinate(y, point.getLatitude());
    }

    public double checkDiff(Point point) {
        if (a == 0 && b == 0) {
            return 0;
        }
        if (a == 0) {
            return Math.abs(-c / b - point.getLatitude());
        }
        if (b == 0) {
            return Math.abs(-c / a - point.getLongitude());
        }
        double y = getY(point.getLongitude());
        return Math.abs(y - point.getLatitude());
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
        return "StraightLine{" +
                "a=" + a +
                ", b=" + b +
                ", c=" + c +
                '}';
    }
}
