package com.skylin.uav.drawforterrain.checksupdata;


import com.google.gson.Gson;
import com.zhy.http.okhttp.callback.IGenericsSerializator;

/**
 * Created by Administrator on 2017/3/24.
 */

public class JsonGenericsSerializator implements IGenericsSerializator {
    Gson mGson = new Gson();
    @Override
    public <T> T transform(String response, Class<T> classOfT) {
        return mGson.fromJson(response, classOfT);
    }
}