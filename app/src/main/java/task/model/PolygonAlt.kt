package com.skylin.task.model;

import com.skylin.uav.drawforterrain.nofly.Point
import task.model.Polygon
import task.model.Segment
import task.util.TQMath

/**
 * Created by sjj on 2017/9/29.
 */

class PolygonAlt : Polygon {
    constructor(framePoints: MutableList<Point>) : super(framePoints)
    constructor(framePoints: MutableList<Point>, margin: Double) : super(framePoints, margin)

    override fun filter(storage: MutableList<Point>?, safe: Boolean) {
        val segments = MutableList(storage!!.size - 1) {
            return@MutableList Segment(storage[it], storage[it + 1])
        }
        storage.clear()
        segments.forEach {
            storage.add(it.start)
            val intersection = intersection(it)
            if (intersection.size > 1) {
                var list = intersection.map { it.first }.sorted()
                if (list.first().distance(it.start) > list.last().distance(it.start)) {
                    list = list.reversed()
                }
                val first = list.first()
                val cloneFirst = first.clone()
                TQMath.setPointAlt(cloneFirst, frame, initCentrePoint(framePoint))
                val last = list.last()
                val cloneLast = last.clone()
                TQMath.setPointAlt(cloneLast, frame, initCentrePoint(framePoint))
                if (it.start.extra == Point.EXTRA_CUT_POINT) {
                    storage.add(cloneFirst)
                    storage.add(cloneLast)
                    cloneFirst.status = Point.OPEN
                    cloneLast.status = Point.TURNOFF
                } else {
                    storage.add(first)
                    storage.add(cloneFirst)
                    storage.add(cloneLast)
                    storage.add(last)
                }
            } else if (intersection.size == 1) {
                if (TQMath.containExact(safeFramePoint, it.start) >= 0 && TQMath.containExact(safeFramePoint, it.end) < 0) {
                    //返航
                    val first = intersection[0].first
                    val clone = first.clone()
                    TQMath.setPointAlt(clone, frame, initCentrePoint(framePoint))
                    storage.add(clone)
                    storage.add(first)
                }
            }
        }
        storage.add(segments.last().end)
    }

    private fun initCentrePoint(points: List<Point>): Point {
        var lat = 0.0
        var lon = 0.0
        for (point in points) {
            lat += point.latitude
            lon += point.longitude
        }
        val size = points.size
        return Point(lon / size, lat / size)
    }
}
