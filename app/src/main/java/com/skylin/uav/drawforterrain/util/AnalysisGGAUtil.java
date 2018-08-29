package com.skylin.uav.drawforterrain.util;

import java.util.HashMap;

import sjj.alog.Log;

/**
 * Created by moon on 2018/1/2.
 */

public class AnalysisGGAUtil {

    public static HashMap<String,String> analysisGGA(String s) {
//        Log.e("analysisGGA  "+s.length()+"  "+s);
        if (s.length() > 0) {
            try {
                String des = ",";
                int cnt = 0;
                int offset = 0;
                while ((offset = s.indexOf(des, offset)) != -1) {
                    offset = offset + des.length();
                    cnt++;
                }
//                Log.e("11 "+( s.startsWith("$GPGGA") )+""+( s.startsWith("$GNGGA")) +" "+(s.startsWith("$GNGNS")));
                if (s.startsWith("$GPGGA") |s.startsWith("$GNGNS") | s.startsWith("$GNGGA")) {
//                if (s.startsWith("$GPGGA") ) {
//                    Log.e("22 "+(s.length() > 10) +" "+( s.length() < 2000) +" "+(cnt >= 10)+ cnt );

                    if (s.length() > 10 && s.length() < 2000 && cnt >= 10) {
//                    if (s.length() > 30 && s.length() < 2000 && cnt ==14) {
                        String[] s2 = s.split(",");
//                        String rtk = s2[6];
//                        String satellites = s2[7];
//                        String hdop = s2[8];
                        double lat;
                        double lon;
                        if (s2[3].equals("N")) {
                            lat = gpsToGoogler(s2[2]);
                        } else {
                            lat = -gpsToGoogler(s2[2]);
                        }
                        if (s2[5].equals("E")) {
                            lon = gpsToGoogler(s2[4]);
                        } else {
                            lon = -gpsToGoogler(s2[4]);
                        }
//                        double value = Double.parseDouble(s2[9]);
//                        double temp = Math.pow(10, 4);
//                        double alt = (Math.round(temp * value)) / temp;

                        HashMap<String,String> hashMap = new HashMap<>();
                        hashMap.put("satellites",s2[7]);
                        hashMap.put("rtk",s2[6]);
                        hashMap.put("hdop",s2[8]);
                        hashMap.put("lon",lon+"");
                        hashMap.put("lat",lat+"");
//                        hashMap.put("alt",alt+"");
                        hashMap.put("alt",s2[9]);

                        return hashMap;
                    }else {
                        return null;
                    }
                }else {
                    return null;
                }
            } catch (Exception e) {
//                Log.e("",e);
                return null;
            }
        }
        return null;
    }




    //转换经纬度HH.DD.MM格式
    public static double gpsToGoogler(String data) {
        int i = data.indexOf(".");
        String substring = data.substring(0, i - 2);
        System.out.println(substring);
        String substring1 = data.substring(i - 2, data.length());
        System.out.println(substring1);
        return Double.parseDouble(substring) + Double.parseDouble(substring1) / 60;
    }
}
