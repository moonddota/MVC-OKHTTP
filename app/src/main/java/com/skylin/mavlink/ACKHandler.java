package com.skylin.mavlink;

import com.MAVLink.Messages.MAVLinkMessage;
import com.skylin.mavlink.connection.MavLinkConnection;
import com.skylin.mavlink.model.Response;

/**
 * Created by sjj on 2017/4/11.
 */

public interface ACKHandler<T> {
    MAVLinkMessage getMessage();
    /**
     * @return true 需要等待响应
     */
    boolean ack();

    /**
     *读取 飞控参数 或者 等待响应
     */
    boolean requestAck();

    /**
     * 发送消息时调用，读取配置之类
     * @return 返回null 的时候说明不需要处理
     */
    void onSend();
    /**
     *
     * @param message 飞控返回消息
     * @return  true 获得响应消息
     */
    boolean handleMsg(MAVLinkMessage message);
    /**
     * 超时时间
     * @return true 超时
     */
    boolean timeout();

    /**
     * @return true 重试
     */
    boolean retry();

    void cancel();

    boolean isActive();

    /**
     * @param response 消息响应
     */
    void response(Response<T> response);

    void onSendFailed(Exception e);

    void onDisconnect();
}
