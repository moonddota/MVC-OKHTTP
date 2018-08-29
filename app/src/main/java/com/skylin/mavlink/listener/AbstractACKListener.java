package com.skylin.mavlink.listener;

import com.skylin.mavlink.ACKListener;
import com.skylin.mavlink.model.Progress;
import com.skylin.mavlink.model.Response;
import com.skylin.uav.drawforterrain.util.ToastUtil;


/**
 * Created by sjj on 2017/6/30.
 */

public abstract class AbstractACKListener<T> implements ACKListener<T> {
    private final Object lock = new Object();
    private boolean complete = false;
    private Progress progress;

    @Override
    public final void onResponse(Response<T> result) {
        synchronized (lock) {
            complete = true;
//            if (progress != null&&progress.isShowing()) {
//                progress.dismiss();
//                progress = null;
//            }
        }
        if (result.isSuccess()) {
            onSuccess(result.getData());
        } else {
            onFailed(result.getErrorMessage());
        }
    }

    public abstract void onSuccess(T t);

    public abstract void onFailed(String errorMessage);

    protected final void notification(String prefix, String message) {
        ToastUtil.show(prefix + ":" + message);
//        App.getApp().getSpeech().notifyNewMessage(prefix + message);
    }

//    public AbstractACKListener<T> showProgress(final String message, final boolean cancelable) {
//        Pool.submit(AndroidSchedulers.mainThread(), new Runnable() {
//            @Override
//            public void run() {
//                synchronized (lock) {
////                    if (progress != null) {
////                        progress.dismiss();
////                    }
////                    if (!complete)
////                        progress = new Progress(App.getApp().getLastActivity()).setMessage(message).setCancelable(cancelable).show();
////                }
//            }
//        });
//        return this;
//    }
}
