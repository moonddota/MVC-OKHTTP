package com.skylin.uav.drawforterrain.views;

import com.skylin.uav.drawforterrain.nofly.Point;

/**
 * Created by Moon on 2018/3/14.
 */

public class Line {
    Point start;
    Point end;

    public Line(Point start, Point end) {
        this.start = start;
        this.end = end;
    }

    public Point getStart() {
        return start;
    }

    public void setStart(Point start) {
        this.start = start;
    }

    public Point getEnd() {
        return end;
    }

    public void setEnd(Point end) {
        this.end = end;
    }
}
