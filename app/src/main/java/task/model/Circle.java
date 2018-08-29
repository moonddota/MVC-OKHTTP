package task.model;

import com.skylin.uav.drawforterrain.nofly.Point;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import task.core.Constant;
import task.util.TQMath;

/**
 * Created by SJJ on 2017/3/17.
 */

public class Circle extends AbstractRoadblock {
    private Point center;
    private double radius;
    private double margin;
    private double safeRadius;

    private List<Point> framePoint = new ArrayList<>(4);
    private List<Point> safeFramePoint = new ArrayList<>(4);
    private List<Point> safeFramePointLess = new ArrayList<>(4);
    private List<Segment> safeFrame = new ArrayList<>(4);
    private Point border;

    Circle(Point center) {
        this(center, 3.5);
    }

    public Circle(Point center, Point border) {
        this(center, center.distance(border));
        this.border = border;
    }

    private Circle(Point center, double radius) {
        this(center, radius, Constant.DEFAULT_MARGIN);
    }

    private Circle(Point center, double radius, double margin) {
        this.center = center;
        this.radius = radius;
        this.margin = margin;
        safeRadius = radius + margin;
    }

    public List<Point> getFramePoint() {
        return framePoint;
    }

    @Override
    public List<Point> getSafeFramePoint() {
        return safeFramePoint;
    }

    @Override
    public List<Segment> getSafeFrameSegment() {
        return safeFrame;
    }

    @Override
    public List<Point> getSafeFramePointLess() {
        return safeFramePointLess;
    }

    public Point getCenter() {
        return center;
    }

    public Point getBorder() {
        return border;
    }

    public double getSafeRadius() {
        return safeRadius;
    }


    public void init(double slope) {
        framePoint.clear();
        framePoint.addAll(createFramePoint(slope, radius));//生成障碍物边框顶点
        safeFramePoint.clear();
        safeFramePoint.addAll(createFramePoint(slope, safeRadius));//生成障碍物安全边框
        safeFrame.clear();
        for (int i = 1; i < safeFramePoint.size(); i++) {
            safeFrame.add(new Segment(safeFramePoint.get(i - 1), safeFramePoint.get(i)));
        }
        safeFrame.add(new Segment(safeFramePoint.get(safeFramePoint.size() - 1), safeFramePoint.get(0)));

        safeFramePointLess.clear();
        safeFramePointLess.addAll(createFramePoint(slope, safeRadius + Constant.DEFAULT_LESS));

    }

    /**
     * 根据中心点，斜率，半径，创建障碍物边框
     */
    private List<Point> createFramePoint(double slope, double radius) {
        radius = Math.hypot(radius, radius);
        double r1 = TQMath.rotateDegrees(slope, 45);
        double dLongitude1 = TQMath.getDLongitude(center.getLatitude(), Math.cos(r1) * radius);
        double dLatitude1 = TQMath.getDLatitude(Math.sin(r1) * radius);
        Point p0 = new Point(center.getLongitude() + dLongitude1, center.getLatitude() + dLatitude1);
        Point p2 = new Point(center.getLongitude() - dLongitude1, center.getLatitude() - dLatitude1);

        double r2 = TQMath.rotateDegrees(slope, 135);
        double dLongitude2 = TQMath.getDLongitude(center.getLatitude(), Math.cos(r2) * radius);
        double dLatitude2 = TQMath.getDLatitude(Math.sin(r2) * radius);
        Point p1 = new Point(center.getLongitude() + dLongitude2, center.getLatitude() + dLatitude2);
        Point p3 = new Point(center.getLongitude() - dLongitude2, center.getLatitude() - dLatitude2);
        return Arrays.asList(p0, p1, p2, p3);
    }

    @Override
    public String toString() {
        return "Circle{" +
                "center=" + center +
                ", radius=" + radius +
                ", margin=" + margin +
                ", safeRadius=" + safeRadius +
                ", framePoint=" + framePoint +
                ", safeFramePoint=" + safeFramePoint +
                ", safeFrame=" + safeFrame +
                ", border=" + border +
                '}';
    }
}
