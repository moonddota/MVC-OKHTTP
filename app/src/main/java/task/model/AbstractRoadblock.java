package task.model;

import com.skylin.uav.drawforterrain.nofly.Point;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import task.core.PathFrame;
import task.core.Roadblocks;
import task.util.TQMath;

/**
 * Created by SJJ on 2017/3/21.
 */
public abstract class AbstractRoadblock implements Roadblocks.RoadblockInterface, Serializable {

    private PathFrame pathFrame;

    @Override
    public void setFrame(PathFrame frame) {
        pathFrame = frame;
    }

    @Override
    public void cut(List<Segment> storage) {
        List<Segment> segmentsTemp = new ArrayList<>();
        for (Segment segment : storage) {
            segmentsTemp.addAll(cut(segment));
        }
        storage.clear();
        storage.addAll(segmentsTemp);
    }

    public List<Segment> cut(final Segment segment) {
        final List<Pair<Point, Segment>> intersection = intersection(segment);
        for (Pair<Point, Segment> pair : intersection) {
            pair.first.setExtra(Point.EXTRA_CUT_POINT);
//            RXBUS.def.push(new BMMapText("C",pair.first));
        }
        if (intersection.size() == 0) {
            int contain = contain(segment);
            if (contain > 0) {
                return Collections.emptyList();
            }
            if (contain == 0 && contain(TQMath.newPoint(segment.getStart(), segment.getEnd(), 0.5)) >= 0) {
                return Collections.emptyList();
            }
            return Collections.singletonList(segment);
        } else {
            Collections.sort(intersection, new Comparator<Pair<Point, Segment>>() {
                @Override
                public int compare(Pair<Point, Segment> o1, Pair<Point, Segment> o2) {
                    double distance0 = segment.getStart().distance(o1.first);
                    double distance1 = segment.getStart().distance(o2.first);
                    return distance0 > distance1 ? 1 : -1;
                }
            });
            List<Segment> segments = new ArrayList<>();
            Segment s = new Segment(segment.getStart(), intersection.get(0).first);
            Point point = TQMath.newPoint(s.getStart(), s.getEnd(), 0.5);
            int contain = contain(point);
//            RXBUS.def.push(new BMMapText(String.valueOf(contain),point));
            if (contain < 0) {
                if (s.getLength() > 1) {
                    segments.add(s);
                }
            }
            for (int i = 0; i < intersection.size() - 1; i++) {
                Segment s2 = new Segment(intersection.get(i).first, intersection.get(i + 1).first);
                point = TQMath.newPoint(s2.getStart(), s2.getEnd(), 0.5);
                contain = contain(point);
//                RXBUS.def.push(new BMMapText(String.valueOf(contain),point));
                if (contain < 0) {
                    if (s2.getLength() > 1) {
                        segments.add(s2);
                    }
                }
            }
            Segment s3 = new Segment(intersection.get(intersection.size() - 1).first, segment.getEnd());
            point = TQMath.newPoint(s3.getStart(), s3.getEnd(), 0.5);
            contain = contain(point);
//            RXBUS.def.push(new BMMapText(String.valueOf(contain),point));
            if (contain < 0) {
                if (s3.getLength() > 1) {
                    segments.add(s3);
                }
            }
            return segments;
        }
    }

    @Override
    public void filter(List<Point> storage, boolean safe) {
        List<Segment> segments = new ArrayList<>(storage.size());
        for (int i = 1; i < storage.size(); i++) {
            segments.add(new Segment(storage.get(i - 1), storage.get(i)));
        }
        storage.clear();
        for (int i = 0; i < segments.size(); i++) {
            Segment segment = segments.get(i);
            storage.add(segment.getStart());
            List<Pair<Point, Segment>> intersection = intersection(segment);
            if (intersection.size() > 1) {
                List<Point> points = searchPathPoint(segment, safe);
                storage.addAll(points);
            }
        }
        storage.add(segments.get(segments.size() - 1).getEnd());
    }

    private List<Point> searchPathPoint(Segment segment, boolean safe) {
        List<Pair<Point, Segment>> pairs = intersection(segment);
        if (pairs.size() <= 1) return Collections.emptyList();
        Collections.sort(pairs, new Comparator<Pair<Point, Segment>>() {
            @Override
            public int compare(Pair<Point, Segment> o1, Pair<Point, Segment> o2) {
                return o1.first.compareTo(o2.first);
            }
        });
        Pair<Point, Segment> pair0 = pairs.get(0);
        Pair<Point, Segment> pair1 = pairs.get(pairs.size() - 1);

        List<Segment> segments = new ArrayList<>(getSafeFrameSegment());
        int indexOf0 = segments.indexOf(pair0.second);
        int indexOf1 = segments.indexOf(pair1.second);
        int startIndex;
        int endIndex;
        if (indexOf0 < indexOf1) {
            startIndex = indexOf0;
            endIndex = indexOf1;
        } else {
            startIndex = indexOf1;
            endIndex = indexOf0;
            Pair<Point, Segment> pair0Temp = pair0;
            pair0 = pair1;
            pair1 = pair0Temp;
        }
        List<Segment> pathSeg0 = new ArrayList<>(segments.subList(startIndex, endIndex + 1));
        List<Segment> pathSeg1 = new ArrayList<>(segments.subList(0, startIndex + 1));
        Collections.reverse(pathSeg1);
        List<Segment> pathSeg1Temp = new ArrayList<>(segments.subList(endIndex, segments.size()));
        Collections.reverse(pathSeg1Temp);
        pathSeg1.addAll(pathSeg1Temp);

        List<Point> path0 = new ArrayList<>(4);
        path0.add(pair0.first);
        for (int i = 1; i < pathSeg0.size(); i++) {
            path0.add(pathSeg0.get(i).getStart());
        }
        path0.add(pair1.first);
        TQMath.removeDuplicatePoint(path0);

        List<Point> path1 = new ArrayList<>(4);
        path1.add(pair0.first);
        for (int i = 1; i < pathSeg1.size(); i++) {
            path1.add(pathSeg1.get(i).getEnd());
        }
        path1.add(pair1.first);
        TQMath.removeDuplicatePoint(path1);
        boolean b0 = safe(path0);
        boolean b1 = safe(path1);
        if (!b0 && !b1) {

            throw new IllegalArgumentException("障碍物贯穿测绘区域");
        }

        if ((!(b0 || b1)) && safe) {
            double lv0 = lv(path0);
            double lv1 = lv(path1);
            if (lv0 > lv1) {
                b1 = true;
            } else if (lv1 > lv0) {
                b0 = true;
            }
        }
        double distance0 = b0 | !safe ? distance(path0) : Integer.MAX_VALUE;
        double distance1 = b1 | !safe ? distance(path1) : Integer.MAX_VALUE;
        List<Point> path = distance0 < distance1 ? path0 : path1;
        if (segment.getStart().distance(path.get(0)) > segment.getStart().distance(path.get(path.size() - 1))) {
            Collections.reverse(path);
        }
        boolean remove = path.remove(segment.getStart());//移除可能存在的相同的点（交点在线段顶端不处理）
        boolean remove1 = path.remove(segment.getEnd());
//        if (intersection(intersection(segment))) {
//            Collections.reverse(path);
//        }
        return path;

    }

    private double distance(List<Point> points) {
        double distance = 0;
        for (int i = 1; i < points.size(); i++) {
            distance += points.get(i - 1).distance(points.get(i));
        }
        return distance;
    }

    /**
     * 判断 指定点 是否在障碍物内
     */
    public int contain(Point point) {
        return TQMath.containExact(getSafeFramePoint(), point);
    }

    private int contain(Segment segment) {
        int contain = contain(segment.getStart());
        if (contain < 0) {
            return contain;
        }
        int contain2 = contain(segment.getEnd());
        if (contain2 < 0) {
            return contain2;
        }
        if (contain == 0 && contain2 == 0) {
            return 0;
        }
        return 1;
    }

    /**
     * 线段与障碍物边框交点
     */
    protected List<Pair<Point, Segment>> intersection(Segment segment) {
        return intersection(segment, getSafeFrameSegment());
    }

    /**
     * 线段与边框交点
     */
    private List<Pair<Point, Segment>> intersection(StraightLine straightLine, List<Segment> segments) {
        List<Pair<Point, Segment>> pairs = new ArrayList<>(2);
        for (Segment seg : segments) {
            Point point = seg.intersection(straightLine);
            if (point != null) {
                pairs.add(new Pair<>(point, seg));
            }
        }
        TQMath.removeDuplicate(pairs);
        return pairs;
    }

    /**
     * 检查路径是否与边框线相交
     */
    private boolean safe(List<Point> points) {
        List<Segment> safeFrame = pathFrame.virtualFramePointsSafeSegment;
        Point start = points.get(0);
        Point end = points.get(points.size() - 1);
        for (int i = 1; i < points.size(); i++) {
            Segment segment1 = new Segment(points.get(i - 1), points.get(i));
            for (Segment segment : safeFrame) {
                Point intersection = segment.intersection(segment1);
                if (intersection != null && !start.equals(intersection) && !end.equals(intersection)) {
                    return false;
                }
            }
        }
        for (Point p : points) {
            int contain = TQMath.containExact(pathFrame.virtualFramePointsSafe, p);
            if (contain < 0) {
                return false;
            }
        }
        return true;
    }

    private double lv(List<Point> points) {
        double dis = 0;
        for (Point p : points) {
            int contain = TQMath.containExact(pathFrame.virtualFramePointsSafe, p);
            if (contain < 0) {
                dis = dis + 1;
            }
        }
        return dis;
    }
}
