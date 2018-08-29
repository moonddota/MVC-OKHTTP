package com.skylin.uav.drawforterrain.util;

import android.os.Build;
import android.support.annotation.RequiresApi;

import com.skylin.uav.drawforterrain.BlueToochActivity;
import com.skylin.uav.drawforterrain.select.home.BlueBaseActivity;

/**
 * Created by moon on 2017/11/22.
 */

public class OrderUtils {

    //  请求GGA
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static void requestGGA() {
        int gga = 0;
        gga |= (1 << 3);
        byte[] ggas = Int2Byte.bytesCopy(gga, 0, 0);
        BlueToochActivity.writeBlue(ggas);
        BlueBaseActivity.writeBlue(ggas);
    }

    //  请求信道
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static void requestChannel() {
        int channel = 0;
        channel |= (1 << 4);
        byte[] channels = Int2Byte.bytesCopy(channel, 0, 0);
        BlueToochActivity.writeBlue(channels);
        BlueBaseActivity.writeBlue(channels);
    }

    //  请求id
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static void requestID() {
        int id = 0;
        id |= (1 << 2);
        byte[] idss = Int2Byte.bytesCopy(id, 0, 0);
        BlueToochActivity.writeBlue(idss);
        BlueBaseActivity.writeBlue(idss);
    }

    //  请求工作状态
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static void requestSystems() {
        int system = 0;
        system |= (1 << 5);
        byte[] systems = Int2Byte.bytesCopy(system, 0, 0);
        BlueToochActivity.writeBlue(systems);
        BlueBaseActivity.writeBlue(systems);
    }


    //设置信道
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static void setChnnel(byte[] bDatas) {
        try {
            int i = 0;
            i |= (1 << 0);
            i |= (1 << 2);
            byte[] translation = Int2Byte.translationAanEnd(bDatas);
            byte[] bytes = Int2Byte.bytesCopyRtcm(i, bDatas.length, translation.length, translation);
//            Log.e(Arrays.toString(bytes));
            BlueToochActivity.writeBlue(bytes);
            BlueBaseActivity.writeBlue(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //设置  RTK自矫正
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static void setRtkCorrect() {
        try {
            int i = 0;
            i |= (1 << 0);
            i |= (1 << 5);
            byte[] buf = Int2Byte.bytesCopy(i, 0, 0);
            BlueToochActivity.writeBlue(buf);
            BlueBaseActivity.writeBlue(buf);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    //设置  经纬度高度写入    数据部分(纬度,经度,高度)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static void setDatas(byte[] bytes) {
        try {
            int i = 0;
            i |= (1 << 0);
            i |= (1 << 6);
            byte[] translation = Int2Byte.translationAanEnd(bytes);
            byte[] buf = Int2Byte.bytesCopyRtcm(i, bytes.length, translation.length, translation);
//            Log.e(Arrays.toString(buf));
            BlueToochActivity.writeBlue(buf);
            BlueBaseActivity.writeBlue(buf);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //设置   基站手持杖切换
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static void setBastOrHand(byte[] bytes) {
        try {
            int i = 0;
            i |= (1 << 0);
            i |= (1 << 3);
            byte[] translation = Int2Byte.translationAanEnd(bytes);
            byte[] buf = Int2Byte.bytesCopyRtcm(i, bytes.length, translation.length, translation);
            BlueToochActivity.writeBlue(buf);
            BlueBaseActivity.writeBlue(buf);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
