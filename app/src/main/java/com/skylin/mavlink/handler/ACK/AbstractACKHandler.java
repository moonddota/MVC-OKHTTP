package com.skylin.mavlink.handler.ACK;

import android.os.Looper;

import com.MAVLink.Messages.MAVLinkMessage;
import com.skylin.mavlink.ACKHandler;
import com.skylin.mavlink.ACKListener;
import com.skylin.mavlink.SendMessage;
import com.skylin.mavlink.model.Response;

import io.reactivex.android.schedulers.AndroidSchedulers;
import sjj.alog.Log;
import sjj.schedule.Pool;

/**
 * Created by sjj on 2017/4/11.
 */

public abstract class AbstractACKHandler<T> implements ACKHandler<T> {
    private static final int retryCount = 3;
    private SendMessage sendMessage;
    private MAVLinkMessage message;
    private ACKListener<T> ACKListener;
    /**
     * 重试次数
     */
    private int retry;
    /**
     * 是否需要等待应答
     */
    private boolean ack;
    /**
     * 每次消息超时时间 2 S
     */
    private long timeout;
    /**
     * 超時時間
     */
    private long time;
    protected boolean active = true;

    public AbstractACKHandler(SendMessage sendMavPack, MAVLinkMessage message, ACKListener<T> responseListener) {
        this(sendMavPack, message, responseListener, 2000, retryCount, true);
    }

    public AbstractACKHandler(SendMessage sendMessage, MAVLinkMessage message, ACKListener<T> responseListener, long timeout, int retry, boolean ack) {
        this.sendMessage = sendMessage;
        this.message = message;
        this.ACKListener = responseListener;
        this.timeout = timeout;
        time = System.currentTimeMillis() + timeout;
        this.retry = retry;
        this.ack = ack;
    }

    @Override
    public MAVLinkMessage getMessage() {
        return message;
    }

    public void setRetry(int retry) {
        this.retry = retry;
    }

    public int getRetry() {
        return retry;
    }

    public void setAck(boolean ack) {
        this.ack = ack;
    }

    public synchronized void setTimeout(long timeout) {
        this.timeout = timeout;
        time = System.currentTimeMillis() + timeout;
    }


    /**
     * 默认 有返回
     *
     * @return
     */
    @Override
    public boolean ack() {
        return ack;
    }

    @Override
    public boolean requestAck() {
        return sendMessage(this);
    }

    @Override
    public void onSend() {

    }

    @Override
    public synchronized boolean timeout() {
        return !active || System.currentTimeMillis() >= time;
    }

    @Override
    public boolean retry() {
        if (!active) {
            return false;
        }
        if (retry > 0) {
            time = System.currentTimeMillis() + timeout;
            retry--;
            return true;
        }
        response(new Response<T>().setSuccess(false).setErrorMessage("响应超时").setMessage(getMessage()));
        return false;
    }

    void resetRetry() {
        retry = retryCount;
    }

    @Override
    public void cancel() {
        active = false;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void response(final Response<T> response) {
        if (ACKListener != null && active) {
            if (Thread.currentThread().getId() != Looper.getMainLooper().getThread().getId()) {
                Pool.submit(new Runnable() {
                    @Override
                    public void run() {
                        ACKListener.onResponse(response);
                    }
                });
            } else {
                try {
                    ACKListener.onResponse(response);
                } catch (Exception e) {
                    Log.e("ACKListener response", e);
                }
            }
        }
    }

    protected <S> boolean sendMessage(ACKHandler<S> ackHandler) {
        return sendMessage.send(ackHandler);
    }

    @Override
    public void onSendFailed(Exception e) {
        active = false;
        response(new Response<T>().setSuccess(false).setErrorMessage("消息发送失败：" + e.getMessage()));
    }

    @Override
    public void onDisconnect() {
        active = false;
        response(new Response<T>().setSuccess(false).setErrorMessage("连接已断开"));
    }
}
