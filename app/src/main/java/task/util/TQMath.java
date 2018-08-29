package task.util;


import com.skylin.uav.drawforterrain.nofly.EmptyUtils;
import com.skylin.uav.drawforterrain.nofly.Point;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import common.Assert;
import io.reactivex.functions.BiFunction;
import task.model.Pair;
import task.model.Segment;
import task.model.StraightLine;

import static java.lang.Math.abs;
import static java.lang.Math.asin;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

/**
 * Created by SJJ on 2017/2/25.
 */

public class TQMath {
    private static final double EARTH_RADIUS = 6378137;
    public static final double error = 0.00000002;

    /**
     * 经纬度允许误差范围内认为相等
     *
     * @param v1
     * @param v2
     * @return
     */
    public static boolean equals(double v1, double v2) {
        if (v1 == v2) return true;
        return Math.abs(v1 - v2) <= error;
    }

    public static boolean equalsCoordinate(double d1, double d2) {
        return abs(d1 - d2) <= 1E-7;//1e-7 deg
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
        return new Point(weight(start.getLongitude(), end.getLongitude(), weight), weight(start.getLatitude(), end.getLatitude(), weight), weight(start.getAltitude(), end.getAltitude(), weight));
    }

    private static double weight(double start, double end, double weight) {
        return (end - start) * weight + start;
    }

    public static void sort(List<Point> points) {
        Collections.sort(points);
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

    public static int containExact(List<Point> frame, Point target) {
        for (int i = 0; i < frame.size() - 1; i++) {
            Segment segment = new Segment(frame.get(i), frame.get(i + 1));
            if (segment.contain(target)) {
                return 0;
            }
        }

        Segment segment = new Segment(frame.get(frame.size() - 1), frame.get(0));
        if (segment.contain(target)) {
            return 0;
        }

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
        return b ? 1 : -1;
    }

    public static void createSegment(List<Segment> segments, List<Point> points) {
        segments.clear();
        for (int i = 0; i < points.size() - 1; i++) {
            Point start = points.get(i);
            Point end = points.get(i + 1);
            segments.add(new Segment(start, end));
        }
        segments.add(new Segment(points.get(points.size() - 1), points.get(0)));
        filter(segments);
    }

    private static void filter(List<Segment> segments) {
        if (segments.size() < 2) return;
        for (int i = 0, j = segments.size() - 1; i < segments.size(); j = i, i++) {
            Segment segment1 = segments.get(j);
            Segment segment2 = segments.get(i);
            Point vector1 = vector(segment1);
            Point vector2 = vector(segment2);
            double cosa = (vector1.getLongitude() * vector2.getLongitude() + vector1.getLatitude() * vector2.getLatitude())
                    / (Math.hypot(vector1.getLongitude(), vector1.getLatitude()) * Math.hypot(vector2.getLongitude(), vector2.getLatitude()));
            if (Math.abs(1 - Math.abs(cosa)) < 1e-7) {
                segment1.setEnd(segment2.getEnd());
                segments.remove(segment2);
                filter(segments);
                break;
            }
        }
    }

    private static Point vector(Segment segment) {
        Point start = segment.getStart();
        Point end = segment.getEnd();
        return new Point(end.getLongitude() - start.getLongitude(), end.getLatitude() - start.getLatitude());
    }

    public static double wrap(double n, double min, double max) {
        return (n >= min && n < max) ? n : (mod(n - min, max - min) + min);
    }

    public static double mod(double x, double m) {
        return ((x % m) + m) % m;
    }

    public static double offsetAngle(double plane1, double air1) {
        double mod = mod(plane1, 360);
        double air = mod(air1, 360);
        double diff = abs(mod - air);
        if (diff > 180) {
            return 360 - diff;
        }
        return diff;
    }

    public static double calculateYawAngle(double plane1, double air1) {
        double mod = mod(plane1, 360);
        double air = mod(air1, 360);
        double diff = mod(mod - air, 360);
        if (diff > 180) {
            diff = 360 - diff;
        }
        if (diff == 0) {
            return air;
        }
        if (diff > 90) {
            return mod(mod + 180, 360);
        }
        return plane1;
    }

    public static Point computeOffset(Point from, double distance, double heading) {
        distance /= EARTH_RADIUS;
        heading = toRadians(heading);
        // http://williams.best.vwh.net/avform.htm#LL
        double fromLat = toRadians(from.getLatitude());
        double fromLng = toRadians(from.getLongitude());
        double cosDistance = cos(distance);
        double sinDistance = sin(distance);
        double sinFromLat = sin(fromLat);
        double cosFromLat = cos(fromLat);
        double sinLat = cosDistance * sinFromLat + sinDistance * cosFromLat * cos(heading);
        double dLng = atan2(
                sinDistance * cosFromLat * sin(heading),
                cosDistance - sinFromLat * sinLat);
        return new Point(toDegrees(fromLng + dLng), toDegrees(asin(sinLat)));
    }

    public static List<Point> computeOffset(List<Point> src, double distance, double heading) {
        if (src == null || src.isEmpty()) {
            return Collections.emptyList();
        }
        List<Point> points = new ArrayList<>(src.size());
        for (Point point : src) {
            points.add(computeOffset(point, distance, heading));
        }
        return points;
    }

    /**
     * Returns the heading from one LatLng to another LatLng. Headings are
     * expressed in degrees clockwise from North within the range [-180,180).
     *
     * @return The heading in degrees clockwise from north.
     */
    public static double computeHeading(Point from, Point to) {
        // http://williams.best.vwh.net/avform.htm#Crs
        double fromLat = toRadians(from.getLatitude());
        double fromLng = toRadians(from.getLongitude());
        double toLat = toRadians(to.getLatitude());
        double toLng = toRadians(to.getLongitude());
        double dLng = toLng - fromLng;
        double heading = atan2(
                sin(dLng) * cos(toLat),
                cos(fromLat) * sin(toLat) - sin(fromLat) * cos(toLat) * cos(dLng));
        return wrap(toDegrees(heading), -180, 180);
    }

    public static void setPointAlt(Point point, List<Segment> frame, Point center) {
        if (point.getAltitude() != 0) return;
        if (EmptyUtils.isEmpty(frame)) return;
        if (contain(point, frame) >= 0) {
            setPointAlt(point, frame);
            return;
        }
        setPointAlt(center, frame);
        Segment target = new Segment(center, point);
        List<Point> points = new ArrayList<>();
        for (Segment segment : frame) {
            Point intersection = target.intersection(segment);
            if (intersection != null) {
                alt(intersection, segment);
                points.add(intersection);
            }
        }
        Collections.sort(points, new Comparator<Point>() {
            @Override
            public int compare(Point o1, Point o2) {
                double altitude = o1.getAltitude();
                double altitude2 = o2.getAltitude();
                double dif = altitude - altitude2;
                return dif > 0 ? 1 : dif < 0 ? -1 : 0;
            }
        });
        Point point1 = points.get(points.size() - 1);
        double d1 = center.distance(point1);
        double d2 = point1.distance(point);
        double dif1 = point1.getAltitude() - center.getAltitude();
        double alt = (d1 + d2) / d1 * dif1 + center.getAltitude();
        point.setAltitude(alt);
    }

    public static void setPointAlt(Point point, List<Segment> frame) {
        if (point.getAltitude() != 0) return;
        if (EmptyUtils.isEmpty(frame)) return;
        if (point.getAltitude() != 0) return;
        if (contain(point, frame) < 0) {
            double lng = 0;
            double lat = 0;
            for (Segment segment : frame) {
                lat += segment.getStart().getLatitude();
                lng += segment.getStart().getLongitude();
            }
            Point center = new Point(lng / frame.size(), lat / frame.size());
            if (contain(center, frame) < 0) {
                Point nearest = null;
                double dis = Double.MAX_VALUE;
                for (Segment segment : frame) {
                    double distance = segment.getStart().distance(center);
                    if (distance < dis) {
                        dis = distance;
                        nearest = segment.getStart();
                    }
                }
                assert nearest != null;
                center.setAltitude(nearest.getAltitude());
                if (center.equals(point)) {
                    point.setAltitude(center.getAltitude());
                    return;
                }
            }
            setPointAlt(point, frame, center);
            return;
        }
        double alt0 = alt(point, intersection(point, 0, frame));
        double alt1 = alt(point, intersection(point, Math.tan(Math.toRadians(90)), frame));
        point.setAltitude((float) ((alt0 + alt1) / 2));
    }

    private static Pair<Point, Point> intersection(Point anchor, double slope, List<Segment> segments) {
        StraightLine straightLine = new StraightLine(slope, anchor);
        List<Point> points = new ArrayList<>();
        for (Segment segment : segments) {
            Point point = segment.intersection(straightLine);
            if (point != null) {
                alt(point, segment);
                points.add(point);
            }
        }
        removeDuplicatePoint(points);
        points.remove(anchor);
        points.add(anchor);
        Collections.sort(points);
        int indexOf = points.indexOf(anchor);

        if (points.size() < 3 || (indexOf == 0 || indexOf >= points.size() - 1)) {
            points.remove(anchor);
            return new Pair<>(points.get(0), points.get(points.size() - 1));
        }
        return new Pair<>(points.get(indexOf - 1), points.get(indexOf + 1));
    }

    private static int contain(Point point, List<Segment> segments) {
        List<Point> points = new ArrayList<>(segments.size());
        for (Segment segment : segments) {
            points.add(segment.getStart());
        }
        return containExact(points, point);
    }

    private static double alt(Point target, Pair<Point, Point> pair) {
        double f = pair.first.distance(target);
        double s = pair.second.distance(target);
        return weight(pair.first.getAltitude(), pair.second.getAltitude(), f / (f + s));
    }

    private static void alt(Point target, Segment segment) {
        target.setAltitude((float) alt(target, new Pair<>(segment.getStart(), segment.getEnd())));
    }

    public static boolean isClockwise2(List<Segment> points) {
        List<Point> list = new ArrayList<>(points.size());
        for (Segment segment : points) {
            list.add(segment.getStart());
        }
        return isClockwise(0, list);
    }

    public static boolean isClockwise(int index, List<Point> points) {
        BiFunction<Point, Point, Point> function;
        if (index == 0) {
            function = east();
        } else if (index == 1) {
            function = west();
        } else if (index == 2) {
            function = south();
        } else if (index == 3) {
            function = north();
        } else {//不会这么倒霉的
            throw new IllegalArgumentException(String.valueOf(index));
        }
        Point[] point = get3Point(function, points);
        double xxx = xxx(point[0], point[1], point[2]);
        return !(xxx > 0) && (xxx < 0 || isClockwise(++index, points));
    }

    private static BiFunction<Point, Point, Point> east() {
        return new BiFunction<Point, Point, Point>() {
            @Override
            public Point apply(Point point, Point point2) throws Exception {
                if (point2.getLongitude() - point.getLongitude() > 0) {
                    return point2;
                }
                return point;
            }
        };
    }

    private static BiFunction<Point, Point, Point> west() {
        return new BiFunction<Point, Point, Point>() {
            @Override
            public Point apply(Point point, Point point2) throws Exception {
                if (point2.getLongitude() - point.getLongitude() < 0) {
                    return point2;
                }
                return point;
            }
        };
    }

    private static BiFunction<Point, Point, Point> south() {
        return new BiFunction<Point, Point, Point>() {
            @Override
            public Point apply(Point point, Point point2) throws Exception {
                if (point2.getLatitude() - point.getLatitude() < 0) {
                    return point2;
                }
                return point;
            }
        };
    }

    private static BiFunction<Point, Point, Point> north() {
        return new BiFunction<Point, Point, Point>() {
            @Override
            public Point apply(Point point, Point point2) throws Exception {
                if (point2.getLatitude() - point.getLatitude() > 0) {
                    return point2;
                }
                return point;
            }
        };
    }

    private static Point[] get3Point(BiFunction<Point, Point, Point> function, List<Point> points) {
        Point point = points.get(0);
        for (int i = 1; i < points.size(); i++) {
            try {
                point = function.apply(point, points.get(i));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        int i = points.indexOf(point);
        if (i == 0) {
            return new Point[]{points.get(points.size() - 1), points.get(0), points.get(1)};
        } else if (i == points.size() - 1) {
            return new Point[]{points.get(i - 2), points.get(i), points.get(0)};
        } else {
            return new Point[]{points.get(i - 1), points.get(i), points.get(i + 1)};
        }
    }

    private static double xxx(Point p1, Point p2, Point p3) {
        return (p1.getLongitude() - p3.getLongitude()) * (p2.getLatitude() - p3.getLatitude()) -
                (p1.getLatitude() - p3.getLatitude()) * (p2.getLongitude() - p3.getLongitude());
    }

    public static Point createSafePointBase(Segment segment, boolean isClockwise, boolean out) {
        Point start = segment.getStart();
        Point end = segment.getEnd();
        Point center = new Point((start.getLongitude() + end.getLongitude()) / 2, (end.getLatitude() + start.getLatitude()) / 2);
        Point centerVector = new Point(center.getLongitude() - start.getLongitude(), center.getLatitude() - start.getLatitude());
        boolean anticlockwise = (isClockwise && out) || (!isClockwise && !out);
        if (anticlockwise) {
            return new Point(center.getLongitude() - centerVector.getLatitude(), center.getLatitude() + centerVector.getLongitude());
        } else {
            return new Point(center.getLongitude() + centerVector.getLatitude(), center.getLatitude() - centerVector.getLongitude());
        }
    }

    public static List<Point> createSafePoint(List<Segment> frame, double margin, boolean isClockwise, boolean out) {
        List<StraightLine> straightLines = new ArrayList<>(frame.size());
        for (int i = 0; i < frame.size(); i++) {
            Segment segment = frame.get(i);
            Point currentSegmentCenter = TQMath.createSafePointBase(segment, isClockwise, out);
            StraightLine straightLine = new StraightLine(Math.tan(TQMath.rotateDegrees(segment.getSlope(), 90)), currentSegmentCenter);
            Point point = straightLine.intersection(segment);
            Point target = TQMath.newPoint(point, currentSegmentCenter, margin / point.distance(currentSegmentCenter));
            straightLine.rotate(target, 90);
            straightLines.add(straightLine);
        }
        List<Point> points = new ArrayList<>(frame.size());
        StraightLine lastLine = straightLines.get(straightLines.size() - 1);
        for (StraightLine currentLine : straightLines) {
            Point intersection = lastLine.intersection(currentLine);
            if (intersection != null)
                points.add(intersection);
            lastLine = currentLine;
        }
        Assert.verify(points.size() == frame.size(), "障碍物安全区域创建失败");
        List<Point> points2 = new ArrayList<>(points.size());
        for (int i = 0; i < points.size(); i++) {
//            if (points.get(i).distance(framePoints.get()))

//            RXBUS.def.push(new BMMapText(framePoints.get(i).toString(),i+"",framePoints.get(i), Color.RED,48));
            Point p0 = frame.get(i).getStart();
            Point p1 = points.get(i);
            double cross = vecCross(frame, i);
            boolean concave = (isClockwise && cross > 0) || (!isClockwise && cross < 0);
            if (((out && !concave) || (!out && concave)) && p0.distance(p1) >= Math.sqrt(2) * margin) {
                Point point = TQMath.newPoint(p0, p1, margin / p0.distance(p1));
//                Point point = TQMath.computeOffset(p0, margin, TQMath.computeHeading(p0, p1));
                StraightLine line = new StraightLine(p0, p1);
                line.rotate(point, 90);
                Segment left = new Segment(i > 0 ? points.get(i - 1) : points.get(points.size() - 1), p1);
                Segment right = new Segment(p1, i < points.size() - 1 ? points.get(i + 1) : points.get(0));
                Point lp = left.intersection(line);
                Point rp = right.intersection(line);
                if (lp != null) {
                    points2.add(lp);
                }
                if (rp != null) {
                    points2.add(rp);
                }
            } else {
                points2.add(p1);
            }
        }
        return points2;
    }

    private static double vecCross(List<Segment> points, int index) {
        if (points.size() < 3) return 1;
        Point p0 = points.get(index == 0 ? points.size() - 1 : index - 1).getStart();
        Point p1 = points.get(index).getStart();
        Point p2 = points.get(index == points.size() - 1 ? 0 : index + 1).getStart();
        Point a = new Point(p1.getLongitude() - p0.getLongitude(), p1.getLatitude() - p0.getLatitude());
        Point b = new Point(p2.getLongitude() - p1.getLongitude(), p2.getLatitude() - p1.getLatitude());
        return a.getLongitude() * b.getLatitude() - b.getLongitude() * a.getLatitude();
    }

    public static Point getCentrePoint(List<Point> points) {
        double lat = 0;
        double lon = 0;
        for (Point point : points) {
            lat += point.getLatitude();
            lon += point.getLongitude();
        }
        int size = points.size();
        return new Point(lon / size, lat / size);
    }

    public static Point searchMaxDistancePoint(StraightLine straightLine, List<Point> points) {
        double distanceTemp = 0;
        Point point = null;
        for (Point framePoint : points) {
            double distance1 = straightLine.distance(framePoint);
            if (distance1 > distanceTemp) {
                distanceTemp = distance1;
                point = framePoint;
            }
        }
        return point;
    }

    public static boolean contain(List<Point> frame, Point target) {
        for (int i = 0, j = frame.size() - 1; i < frame.size(); j = i, i++) {
            Point lp = frame.get(j);
            Point cp = frame.get(i);
            if ((lp.getLongitude() > target.getLongitude() || equals(lp.getLongitude(), target.getLongitude())) !=
                    (cp.getLongitude() > target.getLongitude() || equals(lp.getLongitude(), target.getLongitude()))) {
                StraightLine straightLine = new StraightLine(lp, cp);
                double y = straightLine.getY(target.getLongitude());
                if (target.getLatitude() < y || equals(y, target.getLatitude())) {
                    return true;
                }
            }
        }
        return false;
    }

}
