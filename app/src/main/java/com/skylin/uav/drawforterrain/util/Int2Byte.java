package com.skylin.uav.drawforterrain.util;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

/**
 * Created by moon on 2017/10/31.
 */

public class Int2Byte {
    //byte 数组与 int 的相互转换
    public static int byteArrayToInt(byte[] b) {
        return b[3] & 0xFF |
                (b[2] & 0xFF) << 8 |
                (b[1] & 0xFF) << 16 |
                (b[0] & 0xFF) << 24;
    }

    public static byte[] intToByteArray(int a) {
        return new byte[]{
                (byte) (a & 0xFF),
                (byte) ((a >> 8) & 0xFF),
                (byte) ((a >> 16) & 0xFF),
                (byte) ((a >> 24) & 0xFF)
        };
    }


    public static byte[] hex2byte(String hex) {
        String digital = "0123456789ABCDEF";
        char[] hex2char = hex.toCharArray();
        byte[] bytes = new byte[hex.length() / 2];
        int temp;
        for (int i = 0; i < bytes.length; i++) {
            temp = digital.indexOf(hex2char[2 * i]) * 16;
            temp += digital.indexOf(hex2char[2 * i +  1]);
            bytes[i] = (byte) (temp & 0xff);
        }
        return bytes;
    }


    public static int  to2(int args) {
        int num=123; //初始值，也是商值
        int a;  //获取余数
        int ch=0; //存储二进制
        int i=0;  //存储除了几次
        while(num!=0){
            a=num%2;
            num=num/2;
            ch+=a*(Math.pow(10, i));
            i++;
        }
        return ch;
    }


    public static byte[] bytesCopy(int i, int i1, int i2) {
        try {
            byte[] bi = Int2Byte.intToByteArray(i);
            byte[] bi1 = Int2Byte.intToByteArray(i1);
            byte[] bi2 = Int2Byte.intToByteArray(i2);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write("#".getBytes());
            out.write(bi);
            out.write(bi1);
            out.write(bi2);
            out.write("@".getBytes());
            return out.toByteArray();
        }catch (Exception e){
           return null;
        }
    }

    public static byte[] bytesCopyRtcm(int i, int i1, int i2, byte[] rtcm) {
        try{
            byte[] bi = Int2Byte.intToByteArray(i);
            byte[] bi1 = Int2Byte.intToByteArray(i1);
            byte[] bi2 = Int2Byte.intToByteArray(i2);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write("#".getBytes());
            out.write(bi);
            out.write(bi1);
            out.write(bi2);
            out.write(rtcm);
            out.write("@".getBytes());
            return out.toByteArray();
        }catch (Exception e){
            return null;
        }
    }


  //反转译
    public static byte[] translateDatas(byte[] bytes) {
        byte[] byt = new byte[bytes.length];
        int num = 0;
        for (int i = 0; i < bytes.length; i++, num++) {
            if (bytes[i] == 0x7D) {
                i += 1;
                int i1 = bytes[i] ^ 0X20;
                byt[num] = (byte) i1;
            } else {
                byt[num] = bytes[i];
            }
        }
        return Arrays.copyOf(bytes, num);
    }

    //转译
    public static byte[] translationAanEnd(byte[] buffer){
        int lengh = buffer.length;
        byte[] newbuf = new byte[lengh * 2];
        int num = 0;
        for (int j = 0; j < lengh; j++, num++) {
            if (buffer[j] == 35 | buffer[j] == 0X7D |buffer[j] == 0X64 ) {
                int i1 = buffer[j] ^ 0X20;
                newbuf[num] = 0X7D;
                num += 1;
                newbuf[num] = (byte) i1;
            } else {
                newbuf[num] = buffer[j];
            }
        }
        return Arrays.copyOf(newbuf, num);
    }

}
