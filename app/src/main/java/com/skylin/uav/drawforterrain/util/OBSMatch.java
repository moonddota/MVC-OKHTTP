package com.skylin.uav.drawforterrain.util;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.SpatialRelationUtil;
import com.skylin.uav.drawforterrain.nofly.Point;
import com.skylin.uav.drawforterrain.setting_channel.ObsMasterBan;

import java.util.ArrayList;
import java.util.List;

import task.model.Polygon;

/**
 * Created by moon on 2017/12/15.
 */

public class OBSMatch {

    /*
     * 障碍向外扩展4米
     * */
    public static void match(ArrayList<ObsMasterBan> listgg, int index) throws Exception {
        int size = listgg.get(index).getList().size();
        int size1 = listgg.size();
//        Log.e(size+"   "+(size < 3) + "  " + (listgg.size() < 2) + "  " + (gglist == null));
        if (size1 < 1 | size < 1) return;  //  最少两组障碍    最后一组至少一个障碍点

        List<List<Point>> amplifyList = new ArrayList<>();

        for (int i = 0; i < size1; i++) {
            if (i != index) {
                Polygon polygon = new Polygon(listgg.get(i).getList(), 4.0);
                polygon.init(0);
                List<Point> safeFramePoint = polygon.getSafeFramePoint();
                amplifyList.add(safeFramePoint);
            }
        }

        isNode_(amplifyList, index, listgg);
    }

    private static void isNode_(List<List<Point>> amplifyList, int index, ArrayList<ObsMasterBan> listgg) throws Exception {
        for (Point point : listgg.get(index).getList()) {
            if (judgeNoFly(point.getLatitude(), point.getLongitude(), amplifyList) != null)
                throw new RuntimeException("在机场禁飞区，不能打点");
        }
    }

    /*
     * 判断点是否在一个区域内
     * */
    public static List<LatLng> judgeNoFly(Double lat, Double lon, List<List<Point>> second) throws Exception {
        if (second == null) return null;
        for (List<Point> ss : second) {
            List<LatLng> latlngs = new ArrayList<>();
            for (Point point : ss) {
                latlngs.add(new LatLng(point.getLatitude(), point.getLongitude()));
            }
            LatLng latlng = new LatLng(lat, lon);
            boolean contain = SpatialRelationUtil.isPolygonContainsPoint(latlngs, latlng);
            if (contain) return latlngs;
        }
        return null;
    }

}
