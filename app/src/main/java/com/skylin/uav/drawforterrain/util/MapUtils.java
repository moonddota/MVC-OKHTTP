package com.skylin.uav.drawforterrain.util;

import com.skylin.uav.drawforterrain.nofly.Point;

public class MapUtils {


    private static double EARTH_RADIUS = 6371.393;
//    private static double EARTH_RADIUS = 6378.137;
//
//    private static double rad(double d) {
//        return d * Math.PI / 180.0;
//    }
//
//    /**
//     * 计算两个经纬度之间的距离
//     * @return
//     */
//    public static double GetDistance(Point point1, Point point2) {
//        double radLat1 = rad(point1.getLatitude());
//        double radLat2 = rad(point2.getLatitude());
//        double a = radLat1 - radLat2;
//        double b = rad(point1.getLatitude()) - rad(point2.getLongitude());
//        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) +
//                Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
//        s = s * EARTH_RADIUS;
//        s = Math.round(s * 10000d) / 10000d;
//        s = s*1000;
//        return s;
//    }


    /**
     * 角度弧度计算公式 rad:(). <br/>
     *
     * 360度=2π π=Math.PI
     *
     * x度 = x*π/360 弧度
     *
     * @author chiwei
     * @return
     * @since JDK 1.6
     */
    private static double getRadian(double degree) {
        return degree * Math.PI / 180.0;
    }

    /**
     * 依据经纬度计算两点之间的距离 GetDistance:(). <br/>
     *
     *
     * @author chiwei
     * @return 距离 单位 米
     * @since JDK 1.6
     */
    public static double getDistance(Point point1, Point point2) {
        double radLat1 = getRadian(point1.getLatitude());
        double radLat2 = getRadian(point2.getLatitude());
        double a = radLat1 - radLat2;// 两点纬度差
        double b = getRadian(point1.getLongitude()) - getRadian(point2.getLongitude());// 两点的经度差
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                + Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        return s * 1000;
    }


}
