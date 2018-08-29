package com.skylin.mavlink;

import com.skylin.mavlink.model.Response;

/**
 * Created by SJJ on 2017/3/30.
 */

public interface ACKListener<T> {
    void onResponse(Response<T> result);
}
