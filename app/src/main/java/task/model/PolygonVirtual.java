package task.model;


import com.skylin.uav.drawforterrain.nofly.Point;

import java.util.List;

import task.core.Constant;

/**
 * Created by Administrator on 2017/3/17.
 */

public class PolygonVirtual extends Polygon {

    public PolygonVirtual(List<Point> framePoints) {
        this(framePoints, Constant.DEFAULT_MARGIN);
    }

    public PolygonVirtual(List<Point> framePoints, double margin) {
        super(framePoints,margin);
    }
}

