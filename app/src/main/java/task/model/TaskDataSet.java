package task.model;


import com.skylin.uav.drawforterrain.nofly.Point;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import task.core.Roadblocks;
import task.core.Task;
import task.util.TQMath;

/**
 * Created by sjj on 2017/4/17.
 */

public class TaskDataSet implements Serializable {
    private String type = "border";
    public int mode = Task.mode_normal;
    private boolean clockwise = false;
    /**
     * 偏转角度
     */
    private double deflectionDegrees;
    private List<Roadblocks.RoadblockInterface> roadblocks = new ArrayList<>();
    private List<Point> framePoints;
    private Point home;
    private double targetLineSpacing = 3;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isClockwise() {
        return clockwise;
    }

    public void setClockwise(boolean clockwise) {
        this.clockwise = clockwise;
    }

    public double getDeflectionDegrees() {
        return deflectionDegrees;
    }

    public void setDeflectionDegrees(double deflectionDegrees) {
        this.deflectionDegrees = TQMath.wrap(deflectionDegrees, 0, 360);
    }

    public List<Roadblocks.RoadblockInterface> getRoadblocks() {
        return roadblocks;
    }

    public List<Point> getFramePoints() {
        return framePoints;
    }

    public void setFramePoints(List<Point> framePoints) {
        this.framePoints = framePoints;
    }

    public Point getHome() {
        return home;
    }

    public void setHome(Point start) {
        this.home = start;
    }

    public double getTargetLineSpacing() {
        return targetLineSpacing;
    }

    public void setTargetLineSpacing(double targetLineSpacing) {
        this.targetLineSpacing = targetLineSpacing;
    }

    @Override
    public String toString() {
        return "TaskDataSet{" +
                "deflectionDegrees=" + deflectionDegrees +
                ", roadblocks=" + roadblocks +
                ", framePoints=" + framePoints +
                ", home=" + home +
                ", targetLineSpacing=" + targetLineSpacing +
                '}';
    }
}
