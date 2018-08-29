package com.skylin.uav.drawforterrain.util;

/**
 * Created by moon on 2017/11/8.
 */

public class Putils {


    public static boolean isNumeric(String str){
        for (int i = 0; i < str.length(); i++){
            System.out.println(str.charAt(i));
            if (!Character.isDigit(str.charAt(i))){
                return false;
            }
        }
        return true;
    }
}
