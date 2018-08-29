package task.core;


import android.util.SparseArray;

import com.skylin.task.model.PolygonMerge;
import com.skylin.uav.drawforterrain.nofly.EmptyUtils;
import com.skylin.uav.drawforterrain.nofly.Point;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import task.model.Pair;
import task.model.PolygonVirtual;
import task.model.Segment;
import task.util.Maps;
import task.util.TQMath;

/**
 * Created by SJJ on 2017/3/1.
 * 路障
 * 做圆 (x－a)²+(y－b)²=r²
 * a = center.getLongitude()
 * b = center.getLatitude()
 */

public class Roadblocks implements Serializable {
    public final List<RoadblockInterface> realRoadblocks = new ArrayList<>();
    public final List<RoadblockInterface> realRoadblocksMerge = new ArrayList<>();
    public final List<RoadblockInterface> realRoadblocksMergeLess = new ArrayList<>();
    public final List<RoadblockInterface> virtualRoadblocks = new ArrayList<>();
    //    public final List<RoadblockInterface> virtualRoadblocksMerge = new ArrayList<>();
    public final List<RoadblockInterface> allRoadblocksMerge = new ArrayList<>();
    private PathFrame frame;

    public Roadblocks(PathFrame frame) {
        this.frame = frame;
    }

    private void setPathFrameToRoadblock(List<? extends RoadblockInterface> roadblockPairs) {
        for (RoadblockInterface roadblockInterface : roadblockPairs) {
            roadblockInterface.setFrame(frame);
        }
    }

    private RoadblockInterface merge(RoadblockInterface r1, RoadblockInterface r2) {
        List<Point> list = merge(r1.getSafeFrameSegment(), r2.getSafeFrameSegment());
        if (list == null) {
            if (contain(r1.getSafeFramePoint(), r2.getSafeFramePoint())) {
                return r1;
            }
            if (contain(r2.getSafeFramePoint(), r1.getSafeFramePoint())) {
                return r2;
            }
            return null;
        }
        PolygonMerge merge = new PolygonMerge(list);
        merge.setFrame(frame);
        return merge;
    }

    private List<Point> merge(List<Segment> s1, List<Segment> s2) {
        toClockwise(s1);
        toClockwise(s2);
        sortLng(s1);
        sortLng(s2);
        boolean sssss = s1.get(0).getStart().getLongitude() <= s2.get(0).getStart().getLongitude();
        if (!sssss) {
            List<Segment> t = s2;
            s2 = s1;
            s1 = t;
        }
        Map<String, List<Pair<Pair<Integer, Integer>, Point>>> intersect = new HashMap<>();
        recordIntersection(s1, s2, intersect);
        if (intersect.size() == 0) {


            return null;
        }
        return route(s1, s2);
    }

    private boolean contain(List<Point> frame, List<Point> a) {
        for (Point point : a) {
            if (TQMath.containExact(frame, point) < 0) {
                return false;
            }
        }
        return true;
    }

    private List<Point> route(List<Segment> source1, List<Segment> source2) {
        List<Point> points = new ArrayList<>();
        List<Segment> current = source1;
        List<Segment> other = source2;
        for (int i = 0; true; i++) {
            i %= current.size();
            Segment nextSeg = current.get(i);
            points.add(nextSeg.getStart());
            while (true) {
                Pair<Point, Integer> next = next(nextSeg, other);
                if (next != null) {
                    points.add(next.first);
                    current = other;
                    other = other == source1 ? source2 : source1;
                    i = next.second;
                    nextSeg = new Segment(next.first, current.get(i).getEnd());
                } else {
                    break;
                }
            }

            if (i == current.size() - 1 && current == source1) break;
        }

        return points;
    }

    private Pair<Point, Integer> next(final Segment segment, List<Segment> source) {
        List<Pair<Point, Integer>> list = new ArrayList<>();
        for (int i = 0; i < source.size(); i++) {
            Segment seg = source.get(i);
            Point point = segment.intersection(seg);
            if (point != null && isLeft(segment, seg)) {
                list.add(new Pair<>(point, i));
            }
        }
        Collections.sort(list, new Comparator<Pair<Point, Integer>>() {
            @Override
            public int compare(Pair<Point, Integer> o1, Pair<Point, Integer> o2) {
                return Double.compare(segment.getStart().distance(o1.first), segment.getStart().distance(o2.first));
            }
        });
        return list.size() > 0 ? list.get(0) : null;
    }

    private Callable<List<Pair<Pair<Integer, Integer>, Point>>> create() {
        return new Callable<List<Pair<Pair<Integer, Integer>, Point>>>() {
            @Override
            public List<Pair<Pair<Integer, Integer>, Point>> call() {
                return new ArrayList<>();
            }
        };
    }

    private void recordIntersection(List<Segment> s1, List<Segment> s2, Map<String, List<Pair<Pair<Integer, Integer>, Point>>> intersect) {
        for (int i = 0; i < s1.size(); i++) {
            final Segment segment1 = s1.get(i);
            for (int j = 0; j < s2.size(); j++) {
                final Segment segment2 = s2.get(j);
                Point point = segment1.intersection(segment2);
                if (point != null) {
                    if (isLeft(segment1, segment2)) {
                        List<Pair<Pair<Integer, Integer>, Point>> list1 = Maps.getOrPut(intersect, "s1:" + i, create());
                        list1.add(new Pair<>(new Pair<>(i, j), point));
                    }
                    if (isLeft(segment2, segment1)) {
                        List<Pair<Pair<Integer, Integer>, Point>> list2 = Maps.getOrPut(intersect, "s2:" + j, create());
                        list2.add(new Pair<>(new Pair<>(i, j), point));
                    }
                }
            }
        }
    }

    private boolean isLeft(Segment origin, Segment target) {
        Point vector1 = new Point(origin.getEnd().getLongitude() - origin.getStart().getLongitude(), origin.getEnd().getLatitude() - origin.getStart().getLatitude());
        Point vector2 = new Point(target.getEnd().getLongitude() - target.getStart().getLongitude(), target.getEnd().getLatitude() - target.getStart().getLatitude());
        return vector1.getLongitude() * vector2.getLatitude() - vector2.getLongitude() * vector1.getLatitude() > 0;
    }

    private void sortLng(List<Segment> list) {
        int index = 0;
        double lng = list.get(0).getStart().getLongitude();
        for (int i = 1; i < list.size(); i++) {
            double longitude = list.get(i).getStart().getLongitude();
            if (lng > longitude) {
                index = i;
                lng = longitude;
            }
        }
        if (index == 0) {
            return;
        }
        List<Segment> segments = new ArrayList<>(list);
        list.clear();
        list.addAll(segments.subList(index, segments.size()));
        list.addAll(segments.subList(0, index));
    }

    private void toClockwise(List<Segment> list) {
        if (TQMath.isClockwise2(list)) {
            return;
        }
        List<Segment> segments = new ArrayList<>(list);
        list.clear();
        for (int i = segments.size() - 1; i >= 0; i--) {
            list.add(segments.get(i).inversion());
        }
    }


    /**
     * 检查路线，保证所有路线不经过障碍物
     *
     * @param pathSegments 作业路径
     */
    public void cut(List<Segment> pathSegments, List<RoadblockInterface> roadblocks) {
        for (RoadblockInterface roadblock : roadblocks) {
            roadblock.cut(pathSegments);
        }
    }

    public List<Point> filter(Point start, Point target, boolean safe, boolean ignoredV, List<RoadblockInterface> roadblocks) {
        List<Point> points = new ArrayList<>();
        points.add(start);
        points.add(target);
        for (RoadblockInterface roadblock : roadblocks) {
            if (ignoredV && roadblock instanceof PolygonVirtual) {
                continue;
            }
            roadblock.filter(points, safe);
        }
        return points;
    }

    public List<Point> filter(Point start, Point target, boolean safe, List<RoadblockInterface> roadblocks) {
        return filter(start, target, safe, false, roadblocks);
    }

    public void init(double slope) {
        setPathFrameToRoadblock(realRoadblocks);
        setPathFrameToRoadblock(virtualRoadblocks);

        for (RoadblockInterface roadblock : realRoadblocks) {
            roadblock.init(slope);
        }
        for (RoadblockInterface roadblock : virtualRoadblocks) {
            roadblock.init(slope);
        }

        realRoadblocksMergeLess.clear();
        for (RoadblockInterface roadblockInterface : realRoadblocks) {
            realRoadblocksMergeLess.add(new PolygonMerge(roadblockInterface.getSafeFramePointLess()));
        }
        merge(realRoadblocksMergeLess);
        realRoadblocksMerge.clear();
        realRoadblocksMerge.addAll(realRoadblocks);
        merge(realRoadblocksMerge);
//        virtualRoadblocksMerge.clear();
//        virtualRoadblocksMerge.addAll(virtualRoadblocks);
//        merge(virtualRoadblocksMerge);

        Set<Integer> set1 = new HashSet<>();
        Set<Integer> set2 = new HashSet<>();
        SparseArray<List<RoadblockInterface>> array = new SparseArray<>();
        for (int i = 0; i < virtualRoadblocks.size(); i++) {
            for (int j = 0; j < realRoadblocksMerge.size(); j++) {
                RoadblockInterface merge = merge(virtualRoadblocks.get(i), realRoadblocksMerge.get(j));
                if (EmptyUtils.notEmpty(merge)) {
                    set1.add(i);
                    set2.add(j);
                    List<RoadblockInterface> list = array.get(j);
                    if (list == null) {
                        list = new ArrayList<>();
                        array.put(j, list);
                    }
                    list.add(merge);
                    merge(list);
                }
            }
        }

        allRoadblocksMerge.clear();
        for (int i = 0; i < realRoadblocksMerge.size(); i++) {
            if (set2.contains(i)) continue;
            allRoadblocksMerge.add(realRoadblocksMerge.get(i));
        }
        for (int i = 0; i < virtualRoadblocks.size(); i++) {
            if (set1.contains(i)) continue;
            allRoadblocksMerge.add(virtualRoadblocks.get(i));
        }
        for (int i = 0; i < array.size(); i++) {
            allRoadblocksMerge.addAll(array.valueAt(i));
        }


//        allRoadblocksMerge.addAll(realRoadblocksMerge);
//        allRoadblocksMerge.addAll(virtualRoadblocksMerge);
//        merge(allRoadblocksMerge);
//        for (RoadblockInterface roadblockInterface : virtualRoadblocksMerge) {
//            BMMapPolygon polygon = new BMMapPolygon(roadblockInterface.getSafeFramePoint());
//            polygon.setColor(Color.RED);
//            polygon.setAltitude(4);
//            RXBUS.def.push(polygon);
////            texts.appendP(roadblockInterface.getSafeFramePoint());
//        }
//        for (RoadblockInterface roadblockInterface : virtualRoadblocks) {
//            BMMapPolygon polygon = new BMMapPolygon(roadblockInterface.getSafeFramePoint());
//            polygon.setColor(Color.YELLOW);
//            polygon.setAltitude(3);
//            RXBUS.def.push(polygon);
//        }
//        RXBUS.def.push(texts);
    }

    private void merge(List<RoadblockInterface> roadblocks) {
        for (int i = 0; i < roadblocks.size(); i++) {
            RoadblockInterface list = roadblocks.get(i);
            for (int j = 0; j < roadblocks.size(); j++) {
                RoadblockInterface list1 = roadblocks.get(j);
                if (i == j) continue;
                RoadblockInterface merge = merge(list, list1);
                if (EmptyUtils.notEmpty(merge)) {
                    roadblocks.remove(i > j ? i : j);
                    roadblocks.remove(i > j ? j : i);
                    roadblocks.add(merge);
                    merge(roadblocks);
                    return;
                }
            }
        }
    }

    public interface RoadblockInterface extends Serializable {
        /**
         * 截断经过障碍的线段
         *
         * @param storage
         */
        void cut(List<Segment> storage);

        void filter(List<Point> storage, boolean safe);

        void setFrame(PathFrame frame);

        List<Point> getFramePoint();

        List<Point> getSafeFramePoint();

        List<Segment> getSafeFrameSegment();

        List<Point> getSafeFramePointLess();

        void init(double slope);

        int contain(Point point);
    }
}

