package task.core;


import com.skylin.uav.drawforterrain.nofly.Point;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.reactivex.disposables.Disposable;
import sjj.alog.Log;
import task.model.Segment;
import task.model.StraightLine;
import task.model.TaskDataSet;
import task.util.TQMath;

import static task.util.TQMath.searchMaxDistancePoint;

/**
 * Created by SJJ on 2017/2/23.
 * 路径任务规划相关
 */

public class Task implements Serializable, Disposable {
    public static final int mode_normal = 1;
    public static final int mode_surround = 2;
    private PathFrame frame;
    private Roadblocks roadblocks;
    private PathProgramming programming;
    private TaskDataSet dataSet;
    private Exception error;
    private boolean dispose;
    private Point entrancePoint;

    public Task(TaskDataSet dataSet) {
//        Log.e(dataSet);
        this.dataSet = dataSet;
    }

    public void init() {
        try {
            checkDispose();
            frame = new PathFrame(dataSet.getFramePoints());
            checkDispose();
            roadblocks = new Roadblocks(frame);
            roadblocks.realRoadblocks.addAll(dataSet.getRoadblocks());
            roadblocks.virtualRoadblocks.addAll(frame.virtualPolygons);
            checkDispose();
            if (dataSet.mode == mode_surround) {
                programming = new PathProgramming(frame, dataSet.getHome(), roadblocks);
                checkDispose();
            } else {
                if (dataSet.mode == mode_normal) {
                    double lastDeg = -1;
                    List<Double> doubles = new ArrayList<>(frame.getDegrees());
                    for (int i = 0; i < 360; i++) {
                        checkDispose();
                        try {
                            int dif = dataSet.isClockwise() ? -i : i;
                            double deflectionDegrees = dif + dataSet.getDeflectionDegrees();

                            for (int j = 0; j < doubles.size(); j++) {
                                double deg = doubles.get(j);
                                if (lastDeg >= 0 && lastDeg < deg && deflectionDegrees > deg) {
                                    deflectionDegrees = deg;
                                    i--;
                                    doubles.remove(j);
                                    break;
                                } else if (lastDeg >= 0 && lastDeg > deg && deflectionDegrees < deg) {
                                    deflectionDegrees = deg;
                                    i--;
                                    doubles.remove(j);
                                    break;
                                }
                            }
                            lastDeg = deflectionDegrees;
                            programming = new PathProgramming(frame, dataSet.getTargetLineSpacing(), deflectionDegrees, dataSet.getHome(), roadblocks);
                            break;
                        } catch (Exception e) {
                            Log.e(String.valueOf(lastDeg), e);
                            error = e;
                            programming = null;
                        }
                    }
                }
            }
            entrancePoint = getMinDistancePoint(dataSet.getHome());
        } catch (Exception e) {
            error = e;
            programming = null;
        }
    }

    public Point getEntrancePoint() {
        return entrancePoint;
    }

    public PathFrame getFrame() {
        return frame;
    }

    public boolean isSuccess() {
        return programming != null;
    }

    public Exception getError() {
        return error;
    }

    public PathProgramming getProgramming() {
        return programming;
    }

    public Roadblocks getRoadblocks() {
        return roadblocks;
    }

    public TaskDataSet getDataSet() {
        return dataSet;
    }

    private void checkDispose() throws Exception {
        if (dispose) {
            throw new Exception("Disposed");
        }
    }

    @Override
    public void dispose() {
        dispose = true;
    }

    @Override
    public boolean isDisposed() {
        return dispose;
    }

    public Point getMinDistancePoint(final Point point) {
        if (TQMath.containExact(frame.realFramePointsSafe, point) >= 0) {
            return point;
        }
        StraightLine line = new StraightLine(frame.getCentrePoint(), point);
        List<Point> points = new ArrayList<>();

        Point maxDistancePoint1 = searchMaxDistancePoint(line, frame.realFramePointsSafe);
        line.translation(maxDistancePoint1);
        Point maxDistancePoint2 = searchMaxDistancePoint(line, frame.realFramePointsSafe);
        double distance = Math.ceil(maxDistancePoint1.distance(maxDistancePoint2) * 4);
        for (int i = 1; i < distance; i++) {
            Point point1 = TQMath.newPoint(maxDistancePoint1, maxDistancePoint2, i / distance);
            searchIntersection(points, new StraightLine(point, point1));
        }
        Collections.sort(points, new Comparator<Point>() {
            @Override
            public int compare(Point o1, Point o2) {
                return Double.compare(point.distance(o1), point.distance(o2));
            }
        });
        boolean c;
        for (int i = 0; i < points.size(); i++) {
            Point p = points.get(i);
            c = false;
            for (Roadblocks.RoadblockInterface roadblockInterface : roadblocks.realRoadblocksMerge) {
                if (roadblockInterface.contain(p) > 0) {
                    c = true;
                }
            }
            if (!c) {
                return p;
            }
        }
        return null;
    }

    private void searchIntersection(List<Point> points, StraightLine straightLine) {
        for (Segment segment : frame.realFramePointsSafeSegment) {
            Point point = segment.intersection(straightLine);
            if (point != null) {
                points.add(point);
            }
        }
    }

}
