package task.model;


import com.skylin.uav.drawforterrain.nofly.Point;

import java.util.ArrayList;
import java.util.List;

import task.core.Constant;
import task.util.TQMath;

import static task.util.TQMath.createSafePoint;

/**
 * Created by Administrator on 2017/3/17.
 */

public class Polygon extends AbstractRoadblock {
    private List<Point> framePoints;
    private double margin;
    protected List<Segment> frame = new ArrayList<>();
    private List<Segment> safeFrame = new ArrayList<>();
    private List<Point> safeFramePoint = new ArrayList<>();
    private List<Point> safeFramePointLess = new ArrayList<>();
    private boolean isClockwise;

    public Polygon(List<Point> framePoints) {
        this(framePoints, Constant.DEFAULT_MARGIN);
    }

    public Polygon(List<Point> framePoints, double margin) {
        if (framePoints.size() < 3) throw new IllegalArgumentException("必须超过3个点才能围成一个多边形");
        this.framePoints = framePoints;
        this.margin = margin;
        TQMath.createSegment(frame = new ArrayList<>(), framePoints);
        isClockwise = TQMath.isClockwise(0, framePoints);
    }

    @Override
    public List<Point> getFramePoint() {
        return framePoints;
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

    @Override
    public void init(double slope) {
        safeFramePoint.clear();
        safeFramePoint.addAll(createSafePoint(frame, margin, isClockwise,true));
        TQMath.removeDuplicatePoint(safeFramePoint);
        safeFrame.clear();
        TQMath.createSegment(safeFrame, safeFramePoint);

        safeFramePointLess.clear();
        safeFramePointLess.addAll(createSafePoint(frame, margin + Constant.DEFAULT_LESS, isClockwise,true));
        TQMath.removeDuplicatePoint(safeFramePointLess);
    }

    @Override
    public String toString() {
        return "Polygon{" +
                "framePoints=" + framePoints +
                ", margin=" + margin +
                ", frame=" + frame +
                ", safeFrame=" + safeFrame +
                ", safeFramePoint=" + safeFramePoint +
                '}';
    }
}

