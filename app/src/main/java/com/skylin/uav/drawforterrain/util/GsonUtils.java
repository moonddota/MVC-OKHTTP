package com.skylin.uav.drawforterrain.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by liuxuan on 2017/4/19.
 */

public class GsonUtils {
    public static String createGsonString(Object object) {
        Gson gson = new Gson();
        String gsonString = gson.toJson(object);
        return gsonString;
    }

    public static <T> T changeGsonToBean(String gsonString, Class<T> cls) {
        Gson gson = new Gson();
        T t = gson.fromJson(gsonString, cls);
        return t;
    }


//    public static <T> GeneralModel<GeneralModelData<T>> changeGsonToModelData(String gsonString, Class<T> cls) {
//        Gson gson = new Gson();
//        GeneralModel<GeneralModelData<T>> model = gson.fromJson(gsonString, new TypeToken<GeneralModel<GeneralModelData<T>>>() {
//        }.getType());
//
//        ic_return model;
//    }
//
//    public static GeneralModel<GeneralModelData<ServerLoginBackSub2>> changeGsonToLoginModel(String gsonString) {
//        Gson gson = new Gson();
//        GeneralModel<GeneralModelData<ServerLoginBackSub2>> model = gson.fromJson(gsonString, new TypeToken<GeneralModel<GeneralModelData<ServerLoginBackSub2>>>() {
//        }.getType());
//
//        ic_return model;
//    }


    public static <T> List<T> changeGsonToList(String gsonString, Class<T> cls) {
        Gson gson = new Gson();
        List<T> list = gson.fromJson(gsonString, new TypeToken<List<T>>() {
        }.getType());

        return list;
    }

    public static <T> List<T> jsonToList(String json, Class<T> cls) {
        Gson gson = new Gson();
        List<T> list = new ArrayList<T>();
        JsonArray array = new JsonParser().parse(json).getAsJsonArray();
        for (final JsonElement elem : array) {
            list.add(gson.fromJson(elem, cls));
        }
        return list;
    }

    public static <T> List<List<T>> changeGsonToListList(String gsonString, Class<T> cls) {
        List<List<T>> list = null;
        Gson gson = new Gson();
        list = gson.fromJson(gsonString, new TypeToken<List<List<T>>>() {
        }.getType());
        return list;
    }

    public static <T> List<List<List<T>>> changeGsonToListListList(String gsonString, Class<T> cls) {
        List<List<List<T>>> list = null;
        Gson gson = new Gson();
        list = gson.fromJson(gsonString, new TypeToken<List<List<List<T>>>>() {
        }.getType());
        return list;
    }


    public static <T> List<Map<String, T>> changeGsonToListMaps(
            String gsonString) {
        List<Map<String, T>> list = null;
        Gson gson = new Gson();
        list = gson.fromJson(gsonString, new TypeToken<List<Map<String, T>>>() {
        }.getType());
        return list;
    }

    public static <T> Map<String, T> changeGsonToMaps(String gsonString) {
        Map<String, T> map = null;
        Gson gson = new Gson();
        map = gson.fromJson(gsonString, new TypeToken<Map<String, T>>() {
        }.getType());


        return map;
    }
}
