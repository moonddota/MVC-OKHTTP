package com.skylin.mavlink.listener;


import com.skylin.uav.drawforterrain.util.ToastUtil;

/**
 * Created by sjj on 2017/7/6.
 */

public abstract class AbstractToastACKListener extends AbstractACKListener {
    private final String prefix;

    public AbstractToastACKListener(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public void onFailed(String errorMessage) {
        ToastUtil.show(prefix + "：" + errorMessage);
    }
}
