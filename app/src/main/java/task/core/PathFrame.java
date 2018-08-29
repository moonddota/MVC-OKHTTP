package task.core;


import com.skylin.uav.drawforterrain.nofly.Point;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import task.model.Pair;
import task.model.PolygonVirtual;
import task.model.Segment;
import task.util.TQMath;

/**
 * Created by SJJ on 2017/2/24.
 * 作业区域边框
 */

public class PathFrame implements Serializable {
    public final List<Point> realFramePoints;
    public final List<Point> realFramePointsDeDuplicate = new ArrayList<>();
    public final List<Segment> realFramePointsSegment = new ArrayList<>();
    public final List<Point> realFramePointsSafe = new ArrayList<>();
    public final List<Segment> realFramePointsSafeSegment = new ArrayList<>();
    public final List<Point> realFramePointsSafeLess = new ArrayList<>();
    public final List<Point> virtualFramePoints = new ArrayList<>();
    public final List<Segment> virtualFramePointsSegment = new ArrayList<>();
    public final List<Point> virtualFramePointsSafe = new ArrayList<>();
    public final List<Segment> virtualFramePointsSafeSegment = new ArrayList<>();
    private final double padding;
    private final Point centrePoint;
    private final List<Double> degrees;
    private final double slope;
    public final boolean isClockwise;
    public final List<PolygonVirtual> virtualPolygons = new ArrayList<>();

    public PathFrame(List<Point> points) {
        this(points, Constant.DEFAULT_PADDING);
    }

    private PathFrame(List<Point> points, double padding) {
        int size = points.size();
        if (size < 3) throw new IllegalArgumentException("边框点数必须大于等于3");
        this.realFramePoints = points;
        this.padding = padding;
        centrePoint = TQMath.getCentrePoint(points);
        isClockwise = TQMath.isClockwise(0, points);
        TQMath.createSegment(realFramePointsSegment, points);
        for (Segment segment : realFramePointsSegment) {
            realFramePointsDeDuplicate.add(segment.getStart());
        }
        convertConvexPolygon(realFramePointsDeDuplicate);

        TQMath.createSegment(virtualFramePointsSegment, virtualFramePoints);
        slope = initFrameDegree(degrees = new ArrayList<>(size)).getSlope();
        virtualFramePointsSafe.addAll(TQMath.createSafePoint(virtualFramePointsSegment, padding, isClockwise, false));
        TQMath.removeDuplicatePoint(virtualFramePointsSafe);
        TQMath.createSegment(virtualFramePointsSafeSegment, virtualFramePointsSafe);
        realFramePointsSafeLess.addAll(TQMath.createSafePoint(realFramePointsSegment, padding + Constant.DEFAULT_LESS, isClockwise, false));
        TQMath.removeDuplicatePoint(realFramePointsSafeLess);
        realFramePointsSafe.addAll(TQMath.createSafePoint(realFramePointsSegment, padding, isClockwise, false));
        TQMath.removeDuplicatePoint(realFramePointsSafe);
        TQMath.createSegment(realFramePointsSafeSegment,realFramePointsSafe);
    }

    private void convertConvexPolygon(List<Point> points) {
        List<Pair<Integer, Point>> framePoints = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            framePoints.add(new Pair<>(i, points.get(i)));
        }
        List<Pair<Integer, Point>> array = new ArrayList<>();
        int oldSize = -1;
        List<Integer> list = new ArrayList<>();
        while (oldSize != array.size()) {
            oldSize = array.size();
            list.clear();
            for (int i = 0; i < framePoints.size(); i++) {
                double cross = vecCross(framePoints, i);
                if (isClockwise && cross > 0) {
                    list.add(i);
                } else if (!isClockwise && cross < 0) {
                    list.add(i);
                }
            }
            for (int i = list.size() - 1; i >= 0; i--) {
                array.add(framePoints.remove(list.get(i).intValue()));
            }
        }

        for (int i = 0; i < framePoints.size(); i++) {
            this.virtualFramePoints.add(framePoints.get(i).second);
        }

        if (array.isEmpty()) {
            return;
        }

        Collections.sort(array, new Comparator<Pair<Integer, Point>>() {
            @Override
            public int compare(Pair<Integer, Point> o1, Pair<Integer, Point> o2) {
                return o1.first - o2.first;
            }
        });

        List<List<Pair<Integer, Point>>> lists = new ArrayList<>();
        List<Pair<Integer, Point>> last = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            Pair<Integer, Point> pair = array.get(i);
            if (last.isEmpty()) {
                last.add(pair);
            } else if (last.get(last.size() - 1).first == pair.first - 1) {
                last.add(pair);
            } else {
                lists.add(last);
                last = new ArrayList<>();
                last.add(pair);
            }
        }
//        lists.add(last);
        if (lists.isEmpty()) {
            lists.add(last);
        } else {
            List<Pair<Integer, Point>> firsts = lists.get(0);
            if (lists.size() > 0 && firsts.get(0).first == 0 && last.get(last.size() - 1).first == points.size() - 1) {
                last.addAll(firsts);
                firsts.clear();
                firsts.addAll(last);
            } else {
                lists.add(last);
            }
        }

        for (List<Pair<Integer, Point>> pairs : lists) {
            List<Point> temp = new ArrayList<>(pairs.size() + 2);
            Pair<Integer, Point> pair = pairs.get(0);
            Point firstP = points.get(pair.first == 0 ? points.size() - 1 : pair.first - 1).clone();
            firstP.setExtra(Point.EXTRA_FRAME_POINT);
            temp.add(firstP);
            for (Pair<Integer, Point> pair1 : pairs) {
                temp.add(pair1.second);
            }

            Pair<Integer, Point> lastPair = pairs.get(pairs.size() - 1);
            Point lastP = points.get(lastPair.first == points.size() - 1 ? 0 : lastPair.first + 1).clone();
            lastP.setExtra(Point.EXTRA_FRAME_POINT);
            temp.add(lastP);
            virtualPolygons.add(new PolygonVirtual(temp));
        }

    }

    private double vecCross(List<Pair<Integer, Point>> points, int index) {
        if (points.size() < 3) return 1;
        Point p0 = points.get(index == 0 ? points.size() - 1 : index - 1).second;
        Point p1 = points.get(index).second;
        Point p2 = points.get(index == points.size() - 1 ? 0 : index + 1).second;
        Point a = new Point(p1.getLongitude() - p0.getLongitude(), p1.getLatitude() - p0.getLatitude());
        Point b = new Point(p2.getLongitude() - p1.getLongitude(), p2.getLatitude() - p1.getLatitude());
        return a.getLongitude() * b.getLatitude() - b.getLongitude() * a.getLatitude();
    }

    public List<Double> getDegrees() {
        return degrees;
    }

    public double getSlope() {
        return slope;
    }

    public Point getCentrePoint() {
        return centrePoint;
    }



    private Segment initFrameDegree(List<Double> degrees) {
        Segment maxLengthSegment = realFramePointsSegment.get(0);
        int index = 0;
        degrees.add(maxLengthSegment.getSlope());
        int size = realFramePointsSegment.size();
        for (int i = 1; i < size; i++) {
            Segment segment = realFramePointsSegment.get(i);
            degrees.add(segment.getSlope());
            if (segment.getLength() > maxLengthSegment.getLength()) {
                maxLengthSegment = segment;
                index = i;
            }
        }
        double baseDeg = Math.toDegrees(Math.atan(degrees.get(index)));
        for (int i = 0; i < degrees.size(); i++) {
            degrees.set(i, TQMath.wrap(Math.toDegrees(Math.atan(degrees.get(i))) - baseDeg, 0, 360));
        }
        Collections.sort(degrees);
        return maxLengthSegment;
    }
}
