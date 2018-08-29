package com.skylin.mavlink.listener;


/**
 * Created by sjj on 2017/7/6.
 */

public class CommonNotificationACKListener<T> extends AbstractACKListener<T> {
    private final String prefix;

    public CommonNotificationACKListener(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public void onSuccess(T t) {
        notification(prefix, "成功");
    }

    @Override
    public void onFailed(String errorMessage) {
        notification(prefix, errorMessage);
    }
}
