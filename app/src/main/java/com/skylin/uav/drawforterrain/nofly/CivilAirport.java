package com.skylin.uav.drawforterrain.nofly;


import com.skylin.uav.drawforterrain.util.LogUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import task.model.Pair;
import task.util.TQMath;


/**
 * Created by sjj on 2017/8/24.
 */

public class CivilAirport implements Serializable {
    public String district;
    public String name;
    public String districtCode;
    public String altitude;
    public String trackNumber;
    public String A1;
    public String A2;
    public String C2;
    public double C2_B2_R;
    public String B2;
    public String B3;
    public double B3_C3_R;
    public String C3;
    public String A3;
    public String A4;
    public String C4;
    public double C4_B4_R;
    public String B4;
    public String B1;
    public double B1_C1_R;
    public String C1;
    public String effectiveDate;
    public int revisionNumber;
    public String revisionIssue;
    public String remarks;
    //-------------------
    private final List<Point> border = new ArrayList<>();
    private Point center;
    private double maxR;
    private List<Pair<Point, Double>> centers = new ArrayList<>();

    public void init() {
        border.clear();
        addBorder(A1, A2, C2, B2, B3, C3, A3, A4, C4, B4, B1, C1);
        center = createCenterPoint();
        if (border.size() < 1) return;
        maxR = border.get(0).distance(center);
        centers.clear();
        createCenters(border.get(3), C2_B2_R, border.get(10));
        createCenters(border.get(4), B3_C3_R, border.get(9));
        createCenters(border.get(9), C4_B4_R, border.get(4));
        createCenters(border.get(10), B1_C1_R, border.get(3));
    }

    private void createCenters(Point from, double radius, Point to) {
        centers.add(new Pair<>(TQMath.computeOffset(from, radius, TQMath.computeHeading(from, to)), radius));
    }

    private Point createCenterPoint() {
        double lng = 0;
        double lat = 0;
        for (Point p : border) {
            lng += p.getLongitude();
            lat += p.getLatitude();
        }
        return new Point(lng / border.size(), lat / border.size());
    }

    private void addBorder(String... strings) {
        for (String s : strings) {
            if (EmptyUtils.isEmpty(s)) continue;
            Point e = new Point(s);
            border.add(e);
        }
    }

    public boolean intersect(List<Point> points) {
        if (EmptyUtils.isEmpty(center, border, points)) return false;
        boolean safe = true;
        for (Point point : points) {
            if (center.distance(point) < maxR) {
                safe = false;
                break;
            }
        }
        if (safe) return false;
        for (Point point : points) {
            for (Pair<Point, Double> pair : centers) {
                if (pair.first.distance(point) < pair.second) {
                    return true;
                }
            }
        }
        for (Point point : points) {
            boolean contain = TQMath.contain(border, point);
            if (contain) {
                return true;
            }
        }

        return false;
    }

    public boolean intersect11(Point point) {
        if (EmptyUtils.isEmpty(center, border, point)) return false;
        boolean safe = true;

        if (center.distance(point) < maxR) {
            safe = false;
        }
        LogUtils.e(center.distance(point) + "   " + maxR +"   "+safe);
        if (!safe) {
            return true;
        }
        for (Pair<Point, Double> pair : centers) {
            if (pair.first.distance(point) < pair.second) {
                return true;
            }
        }
//            boolean contain = TQMath.contain(border, point);
//        if (contain) {
//            ic_return true;
//        }
//        LogUtils.e("q11111" + safe);
        return false;
    }

    @Override
    public String toString() {
        return "CivilAirport{" +
                "district='" + district + '\'' +
                ", name='" + name + '\'' +
                ", districtCode='" + districtCode + '\'' +
                ", altitude='" + altitude + '\'' +
                ", trackNumber='" + trackNumber + '\'' +
                ", A1='" + A1 + '\'' +
                ", A2='" + A2 + '\'' +
                ", C2='" + C2 + '\'' +
                ", C2_B2_R=" + C2_B2_R +
                ", B2='" + B2 + '\'' +
                ", B3='" + B3 + '\'' +
                ", B3_C3_R=" + B3_C3_R +
                ", C3='" + C3 + '\'' +
                ", A3='" + A3 + '\'' +
                ", A4='" + A4 + '\'' +
                ", C4='" + C4 + '\'' +
                ", C4_B4_R=" + C4_B4_R +
                ", B4='" + B4 + '\'' +
                ", B1='" + B1 + '\'' +
                ", B1_C1_R=" + B1_C1_R +
                ", C1='" + C1 + '\'' +
                ", effectiveDate='" + effectiveDate + '\'' +
                ", revisionNumber=" + revisionNumber +
                ", revisionIssue='" + revisionIssue + '\'' +
                ", remarks='" + remarks + '\'' +
                ", border=" + border +
                ", center=" + center +
                ", maxR=" + maxR +
                ", centers=" + centers +
                '}';
    }


    public String getA1() {
        return A1;
    }

    public void setA1(String a1) {
        A1 = a1;
    }

    public String getA2() {
        return A2;
    }

    public void setA2(String a2) {
        A2 = a2;
    }

    public String getB2() {
        return B2;
    }

    public void setB2(String b2) {
        B2 = b2;
    }

    public String getB3() {
        return B3;
    }

    public void setB3(String b3) {
        B3 = b3;
    }

    public String getA3() {
        return A3;
    }

    public void setA3(String a3) {
        A3 = a3;
    }

    public String getA4() {
        return A4;
    }

    public void setA4(String a4) {
        A4 = a4;
    }

    public String getB4() {
        return B4;
    }

    public void setB4(String b4) {
        B4 = b4;
    }

    public String getB1() {
        return B1;
    }

    public void setB1(String b1) {
        B1 = b1;
    }

    public Point getCenter() {
        return center;
    }
}
