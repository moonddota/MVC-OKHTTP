package task.core;

import android.support.annotation.NonNull;

import com.skylin.uav.drawforterrain.nofly.Point;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import task.model.Segment;
import task.model.StraightLine;
import task.util.TQMath;

import static task.util.TQMath.searchMaxDistancePoint;


/**
 * Created by SJJ on 2017/2/24.
 */

public class PathProgramming implements Serializable {
    private double deflectionDegrees;
    private final Point home;
    private final Roadblocks roadblocks;
    private final double targetLineSpacing;
    private double realityLineSpacing;
    private PathFrame frame;
    private List<Segment> pathSegments = new ArrayList<>();
    private List<Point> taskPoints;

    PathProgramming(PathFrame frame, double targetLineSpacing, double deflectionDegrees, Point home, Roadblocks roadblocks) throws PathNotFoundException, BadPathException {
        this.frame = frame;
        this.targetLineSpacing = targetLineSpacing;
        this.deflectionDegrees = TQMath.wrap(deflectionDegrees, 0, 360);
        this.home = home;
        this.roadblocks = roadblocks;
        initStartSegment();
    }

    public PathProgramming(PathFrame frame, Point home, Roadblocks roadblocks) {
        this.home = home;
        this.targetLineSpacing = 0;
        this.roadblocks = roadblocks;
        this.frame = frame;
        generateSurroundPath();
    }

    private void generateSurroundPath() {
        List<Point> points = new ArrayList<>(frame.realFramePointsSafe);
        if (!frame.isClockwise) {
            Collections.reverse(points);
        }
        int index = 0;
        for (int i = 1; i < points.size(); i++) {
            if (home.distance(points.get(i)) < home.distance(points.get(index))) {
                index = i;
            }
        }
        if (index > 0) {
            List<Point> tmp = new ArrayList<>(points.size());
            for (int i = index; i < points.size(); i++) {
                tmp.add(points.get(i));
            }
            for (int i = 0; i < index; i++) {
                tmp.add(points.get(i));
            }
            points = tmp;
        }
        TQMath.createSegment(pathSegments, points);
        roadblocks.init(0);
        roadblocks.cut(pathSegments, roadblocks.realRoadblocksMerge);
        taskPoints = generateTaskPoint2();

    }

    public double getRealityLineSpacing() {
        return realityLineSpacing;
    }

    public double getDeflectionDegrees() {
        return deflectionDegrees;
    }

    public List<Segment> getPathSegments() {
        return pathSegments;
    }

    public List<Point> getTaskPoints() {
        return taskPoints;
    }

    private void initStartSegment() throws PathNotFoundException, BadPathException {
        StraightLine straightLine = new StraightLine(frame.getSlope(), frame.getCentrePoint());
        straightLine.rotate(frame.getCentrePoint(), deflectionDegrees);
        Point maxDistancePoint1 = searchMaxDistancePoint(straightLine,frame.realFramePoints);
        straightLine.translation(maxDistancePoint1);
        Point maxDistancePoint2 = searchMaxDistancePoint(straightLine,frame.realFramePoints);
        double radians = TQMath.rotateDegrees(straightLine.getSlope(), 90);
        Point pedal = new StraightLine(Math.tan(radians), maxDistancePoint2).intersection(straightLine);
        double distance = pedal.distance(maxDistancePoint2);

        int pathNum = (int) (distance / targetLineSpacing + 0.5);
        realityLineSpacing = distance / pathNum;
        pathSegments.clear();
        if (pathNum < 1) {
            throw new PathNotFoundException("作业区域过窄，无法规划路径");
        } else if (pathNum == 1) {
            Segment segment = createPathSegments(straightLine, TQMath.newPoint(maxDistancePoint1, maxDistancePoint2, 0.5), 0);
            if (segment != null)
                pathSegments.add(segment);
        } else {
            int startIndex = 0;
            Object[] startSegment = createAcme(maxDistancePoint1, maxDistancePoint2, targetLineSpacing / 2, distance, targetLineSpacing / 2, straightLine, startIndex);
            maxDistancePoint1 = (Point) startSegment[1];
            startIndex = 1 - startIndex;
            pathSegments.add((Segment) startSegment[0]);

            Object[] endSegment = createAcme(maxDistancePoint2, maxDistancePoint1, targetLineSpacing / 2, distance, targetLineSpacing / 2, straightLine, 0);
            maxDistancePoint2 = (Point) endSegment[1];

            if (pathNum > 2) {
                int newPathNum = pathNum * 2;
                for (int i = 3; i < newPathNum - 1; i = i + 2) {
                    Segment pathSegments = createPathSegments(straightLine, TQMath.newPoint(maxDistancePoint1, maxDistancePoint2, i * 1.0 / newPathNum), startIndex);
                    if (pathSegments != null) {
                        startIndex = 1 - startIndex;
                        this.pathSegments.add(pathSegments);
                    }
                }
            }
            Segment segment = (Segment) endSegment[0];
            this.pathSegments.add(startIndex == 0 ? segment : segment.inversion());
        }

        double distance0 = home.distance(pathSegments.get(0).getStart());
        double distance1 = home.distance(pathSegments.get(0).getEnd());
        double distance2 = home.distance(pathSegments.get(pathSegments.size() - 1).getStart());
        double distance3 = home.distance(pathSegments.get(pathSegments.size() - 1).getEnd());
        if (distance0 <= distance1 && distance0 <= distance2 && distance0 <= distance3) {
        } else if (distance1 < distance0 && distance1 < distance2 && distance1 < distance3) {
            List<Segment> segments = new ArrayList<>(pathSegments);
            pathSegments.clear();
            for (Segment segment : segments) {
                pathSegments.add(segment.inversion());
            }
        } else if (distance2 < distance0 && distance2 < distance1 && distance2 < distance3) {
            Collections.reverse(pathSegments);
        } else {
            List<Segment> segments = new ArrayList<>(pathSegments);
            pathSegments.clear();
            for (int i = segments.size() - 1; i >= 0; i--) {
                pathSegments.add(segments.get(i).inversion());
            }
        }
//        for (int i = 0; i < pathSegments.size(); i++) {
//            BMMapText text = new BMMapText(String.valueOf(i), TQMath.newPoint(pathSegments.get(i).getStart(), pathSegments.get(i).getEnd(), 0.5));
//            text.color = Color.GREEN;
//            RXBUS.def.push(text);
//        }
        roadblocks.init(straightLine.getSlope());
        roadblocks.cut(pathSegments, roadblocks.allRoadblocksMerge);
//        for (int i = 0; i < pathSegments.size(); i++) {
//            BMMapText text = new BMMapText(String.valueOf(i), TQMath.newPoint(pathSegments.get(i).getStart(), pathSegments.get(i).getEnd(), 0.5));
//            text.color = Color.YELLOW;
//            RXBUS.def.push(text);
//        }
        taskPoints = generateTaskPoint();
    }

    /**
     * 创建航线目的地点
     */
    @NonNull
    private List<Point> generateTaskPoint2() {
        List<Point> points = new ArrayList<>(pathSegments.size() * 2 + 5);
        //添加起点
        Point lastEnd = null;
        for (Segment segment : pathSegments) {
            Point start0 = segment.getStart();
            Point end0 = segment.getEnd();
            start0.setStatus(Point.OPEN);
//            end0.setStatus(Point.IDLING);
            if (lastEnd != null) {
                if (lastEnd.equals(start0)) {
                    points.remove(points.size() - 1);
                }
                List<Point> inspect = roadblocks.filter(lastEnd, start0, true, roadblocks.realRoadblocksMerge);
                inspect.remove(0);
                points.addAll(inspect);
//                if (lastEnd.getExtra() != Point.EXTRA_CUT_POINT) {
//                    lastEnd.setExtra(Point.EXTRA_TOGGLE_LINE);
//                }
//                if (inspect.size() > 1) {
//                    if (inspect.get(inspect.size() - 1).getExtra() != Point.EXTRA_CUT_POINT) {
//                        inspect.get(inspect.size() - 2).setExtra(Point.EXTRA_TOGGLE_LINE);
//                    }
//                }
            } else {
                points.add(start0);
            }
            points.add(end0);
            lastEnd = end0;
        }
        Point clone = lastEnd.clone();
        points.remove(points.size() - 1);
        points.add(clone);
        clone.setStatus(Point.TURNOFF);

        initCorners(points);

        return points;
    }

    private void initCorners(List<Point> points) {
        Point point = new Point(0, 1);
        for (int i = 0, j = 1; j < points.size(); i = j, j++) {
            Point start = points.get(i);
            Point end = points.get(j);
            if (start.getStatus() != Point.OPEN) {
                continue;
            }
            double v = TQMath.computeHeading(start, end);
            start.corner = TQMath.mod(v, 360);
        }

    }

    private Object[] createAcme(Point start, Point end, double d1, double d2, double d3, StraightLine baseLine, int startIndex) throws BadPathException {
        if (d1 >= d2) {
            throw new RuntimeException("顶点O创建失败");
        }
        Point point = TQMath.newPoint(start, end, d1 / d2);
        Segment segment = createPathSegments(baseLine, point, startIndex);
        if (segment != null) {
            return new Object[]{segment, TQMath.newPoint(start, end, (d1 - d3) / d2)};
        } else {
            return createAcme(start, end, d1 + 0.05, d2, d3, baseLine, startIndex);
        }
    }

    /**
     * @param baseLine
     * @param point
     * @param start    0 or 1
     * @return
     * @throws BadPathException
     */
    private Segment createPathSegments(StraightLine baseLine, Point point, int start) throws BadPathException {
        baseLine.translation(point);
        List<Point> points = searchIntersection(baseLine);
        int size = points.size();
        if (size <= 1) {
            return null;
        } else if (size == 2) {
            TQMath.sort(points);
            return new Segment(points.get(start), points.get(1 - start));
        } else {
            Segment segment = null;
            for (int i = 0; i < points.size() - 1; i++) {
                Point newPoint = TQMath.newPoint(points.get(i), points.get(i + 1), 0.5);
                if (TQMath.containExact(frame.virtualFramePointsSafe, newPoint) > 0) {
                    Segment segment1 = new Segment(points.get(i + start), points.get(i + 1 - start));
                    if (segment == null) {
                        segment = segment1;
                    } else {
                        if (segment1.getLength() > segment.getLength()) {
                            segment = segment1;
                        }
                    }
                }
            }
            return segment;
        }
    }

    /**
     * 查找给定直线与内框的交点
     *
     * @param straightLine
     * @return
     */
    private List<Point> searchIntersection(StraightLine straightLine) {
        List<Point> points = new ArrayList<>(2);
        for (Segment segment : frame.virtualFramePointsSafeSegment) {
            Point point = segment.intersection(straightLine);
            if (point != null) {
                points.add(point);
            }
        }
        TQMath.removeDuplicatePoint(points);
        Collections.sort(points);
        return points;
    }


    /**
     * 创建航线目的地点
     */
    @NonNull
    private List<Point> generateTaskPoint() {

        List<Point> points = new ArrayList<>(pathSegments.size() * 2 + 5);
        //添加起点

        Point lastEnd = null;
        for (Segment segment : pathSegments) {
            Point start0 = segment.getStart();
            Point end0 = segment.getEnd();
            start0.setStatus(Point.OPEN);
            end0.setStatus(Point.IDLING);
            if (lastEnd != null) {
                List<Point> inspect = roadblocks.filter(lastEnd, start0, true, roadblocks.allRoadblocksMerge);
                inspect.remove(0);
                points.addAll(inspect);
                if (lastEnd.getExtra() != Point.EXTRA_CUT_POINT) {
                    lastEnd.setExtra(Point.EXTRA_TOGGLE_LINE);
                }
                if (inspect.size() > 1) {
                    if (inspect.get(inspect.size() - 1).getExtra() != Point.EXTRA_CUT_POINT) {
                        inspect.get(inspect.size() - 2).setExtra(Point.EXTRA_TOGGLE_LINE);
                    }
                }
            } else {
                points.add(start0);
            }
            points.add(end0);
            lastEnd = end0;
        }
        points.get(points.size() - 1).setStatus(Point.TURNOFF);
//        StraightLine straightLine = new StraightLine(points.get(0), points.get(1));
//        double degrees = 90 - Math.toDegrees(Math.atan(straightLine.getSlope()));
//        double dLongitude = points.get(1).getLongitude() - points.get(0).getLongitude();
//        points.get(0).corner = dLongitude >= 0 ? degrees : (degrees + 180);

        initCorners(points);


        for (int i = 0, j = 1; j < points.size(); i++, j++) {
            Point point = points.get(i);
            Point point1 = points.get(j);
            boolean b = point.distance(point1) > realityLineSpacing + 1;
            if (b && point.getExtra() == Point.EXTRA_TOGGLE_LINE) {
                point.setStatus(Point.OPEN);
            }
            if (b && point1.getStatus() == Point.OPEN && point.getStatus() == Point.TURNOFF) {
                point.setStatus(Point.OPEN);
            }
        }

        return points;
    }

    private class PathNotFoundException extends Exception {
        PathNotFoundException(String message) {
            super(message);
        }
    }

    private class BadPathException extends Exception {
        BadPathException(String message) {
            super(message);
        }
    }

}
