package com.skylin.uav.drawforterrain.nofly;

import android.text.TextUtils;

import com.skylin.uav.drawforterrain.APP;
import com.skylin.uav.drawforterrain.BaseActivity;
import com.skylin.uav.drawforterrain.BlueToochActivity;


import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import task.model.Pair;
import task.model.Polygon;

import static java.lang.Math.asin;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

/**
 * Created by Administrator on 2017/8/24.
 */

public class NoFlyZoneLocalDataSource {
    public void destroy() {

    }

    public Observable<List<CivilAirport>> getCivilAirport() {
        return Observable.fromCallable(new Callable<List<CivilAirport>>() {
            @Override
            public List<CivilAirport> call() throws Exception {
                InputStream stream = APP.getContext().getResources().getAssets().open("civil_aviation_airport.xls");
                Workbook rwb = Workbook.getWorkbook(stream);
                stream.close();
                Sheet sheet = rwb.getSheet(0);
                String district = "";
                List<CivilAirport> airports = new ArrayList<>(sheet.getRows());
                for (int i = 1; i < sheet.getRows(); i++) {
                    Cell[] column = sheet.getRow(i);
                    if (TextUtils.isEmpty(getContents(column, 1))) {
                        district = getContents(column, 0);
                        continue;
                    }
                    CivilAirport airport = new CivilAirport();
                    airport.district = district;
                    airport.name = getContents(column, 0);
                    airport.districtCode = getContents(column, 1);
                    airport.altitude = getContents(column, 2);
                    airport.trackNumber = getContents(column, 3);
                    airport.A1 = getContents(column, 4);
                    airport.A2 = getContents(column, 5);
                    airport.C2 = getContents(column, 6);
                    airport.C2_B2_R = getContentsDouble(column, 7);
                    airport.B2 = getContents(column, 8);
                    airport.B3 = getContents(column, 9);
                    airport.B3_C3_R = getContentsDouble(column, 10);
                    airport.C3 = getContents(column, 11);
                    airport.A3 = getContents(column, 12);
                    airport.A4 = getContents(column, 13);
                    airport.C4 = getContents(column, 14);
                    airport.C4_B4_R = getContentsDouble(column, 15);
                    airport.B4 = getContents(column, 16);
                    airport.B1 = getContents(column, 17);
                    airport.B1_C1_R = getContentsDouble(column, 18);
                    airport.C1 = getContents(column, 19);
                    airport.effectiveDate = getContents(column, 20);
                    airport.revisionNumber = (int) getContentsDouble(column, 21);
                    airport.revisionIssue = getContents(column, 22);
                    if (column.length > 23)
                        airport.remarks = getContents(column, 23);
                    airport.init();
                    airports.add(airport);
                }
                return airports;
            }
        }).subscribeOn(Schedulers.io());
    }

    private String getContents(Cell[] cells, int index) {
        if (cells.length > index) {
            return cells[index].getContents();
        }
        return "";
    }

    private double getContentsDouble(Cell[] cells, int index) {
        if (cells.length > index) {
            String contents = cells[index].getContents();
            if (TextUtils.isEmpty(contents)) {
                return 0;
            }
            return Double.parseDouble(contents);
        }
        return 0;
    }


    public  Pair<List<CivilAirport>, List<List<Point>>> call() throws Exception {
        InputStream stream = APP.getContext().getResources().getAssets().open("civil_aviation_airport.xls");
        Workbook rwb = Workbook.getWorkbook(stream);
        stream.close();
        Sheet sheet = rwb.getSheet(0);
        String district = "";
        List<CivilAirport> airports = new ArrayList<>(sheet.getRows());
        List<List<Point>> listPoint = new ArrayList<>();
//        LogUtils.e("   000  " + sheet.getRows());
        for (int i = 1; i < sheet.getRows(); i++) {
            Cell[] column = sheet.getRow(i);
            if (TextUtils.isEmpty(getContents(column, 1))) {
                district = getContents(column, 0);
                continue;
            }
            CivilAirport airport = new CivilAirport();
            airport.district = district;
            airport.name = getContents(column, 0);
            airport.districtCode = getContents(column, 1);
            airport.altitude = getContents(column, 2);
            airport.trackNumber = getContents(column, 3);
            airport.A1 = getContents(column, 4);
            airport.A2 = getContents(column, 5);
            airport.C2 = getContents(column, 6);
            airport.C2_B2_R = getContentsDouble(column, 7);
            airport.B2 = getContents(column, 8);
            airport.B3 = getContents(column, 9);
            airport.B3_C3_R = getContentsDouble(column, 10);
            airport.C3 = getContents(column, 11);
            airport.A3 = getContents(column, 12);
            airport.A4 = getContents(column, 13);
            airport.C4 = getContents(column, 14);
            airport.C4_B4_R = getContentsDouble(column, 15);
            airport.B4 = getContents(column, 16);
            airport.B1 = getContents(column, 17);
            airport.B1_C1_R = getContentsDouble(column, 18);
            airport.C1 = getContents(column, 19);
            airport.effectiveDate = getContents(column, 20);
            airport.revisionNumber = (int) getContentsDouble(column, 21);
            airport.revisionIssue = getContents(column, 22);
            if (column.length > 23)
                airport.remarks = getContents(column, 23);
            airport.init();

            String a1 = airport.getA1();
            String a2 = airport.getA2();
            String a3 = airport.getA3();
            String a4 = airport.getA4();
            String b1 = airport.getB1();
            String b2 = airport.getB2();
            String b3 = airport.getB3();
            String b4 = airport.getB4();


            List<Point> list = addList(a1, a2, b2, b3, a3, a4, b4, b1);
            if (list != null && list.size()== 8){
                Polygon polygon = new Polygon(list,500);
                polygon.init(0);
                List<Point> safeFramePoint = polygon.getSafeFramePoint();

//                TQMath.computeHeading();
                listPoint.add(safeFramePoint);
            }
            airports.add(airport);
        }
        Pair<List<CivilAirport>, List<List<Point>>> pair = new Pair<>(airports,listPoint);
        return pair;
    }

    private List<Point> addList(String... strings) {
        List<Point> list = new ArrayList<>();
        for (String s : strings) {
            if (EmptyUtils.isEmpty(s)) continue;
            Point e = new Point(s);
            list.add(e);
        }
        return list;
    }

    public static Point computeOffset(Point from, double distance, double heading) {
        distance /= BaseActivity.EARTH_RADIUS;
        heading = toRadians(heading);
        double fromLat = toRadians(from.getLatitude());
        double fromLng = toRadians(from.getLongitude());
        double cosDistance = cos(distance);
        double sinDistance = sin(distance);
        double sinFromLat = sin(fromLat);
        double cosFromLat = cos(fromLat);
        double sinLat = cosDistance * sinFromLat + sinDistance * cosFromLat * cos(heading);
        double dLng = atan2(
                sinDistance * cosFromLat * sin(heading),
                cosDistance - sinFromLat * sinLat);
        return new Point(toDegrees(fromLng + dLng), toDegrees(asin(sinLat)));
    }


//    flyZoneRepository.getCivilAirport()
//            .observeOn(Schedulers.computation())
//            .doOnNext(new Consumer<List<CivilAirport>>() {
//        @Override
//        public void accept(List<CivilAirport> civilAirports) throws Exception {
//            for (CivilAirport airport : civilAirports) {
//                boolean intersect = airport.intersect(flightTask.getDataSet().getFramePoints());
//                if (intersect) {
//                    throw new RuntimeException("作业区域 进入机场");
//                }
//            }
//        }
//    })
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe(new Consumer<List<CivilAirport>>() {
//        @Override
//        public void accept(List<CivilAirport> civilAirports) throws Exception {
//            flightTask.setAllow(true);
//        }
//    }, new Consumer<Throwable>() {
//        @Override
//        public void accept(Throwable throwable) throws Exception {
//            Log.e("ex:" + Thread.currentThread().getName());
//            showException(throwable);
//        }
//    });
}
