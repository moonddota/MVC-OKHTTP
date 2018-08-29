package com.skylin.task.model

import com.skylin.uav.drawforterrain.nofly.Point
import task.model.AbstractRoadblock
import task.model.Segment


class PolygonMerge(val source: MutableList<Point>) : AbstractRoadblock() {
    private val safeFrame_ by lazy {
        source.mapIndexed { index, point ->
            if (index < source.size - 1) {
                Segment(point, source[index + 1])
            } else {
                Segment(point, source[0])
            }
        }.toMutableList()
    }

    override fun getFramePoint(): MutableList<Point> = safeFramePoint

    override fun getSafeFramePoint(): MutableList<Point> = source

    override fun getSafeFrameSegment(): MutableList<Segment> = safeFrame_

    override fun getSafeFramePointLess(): MutableList<Point> = safeFramePoint

    override fun init(slope: Double) {

    }
}