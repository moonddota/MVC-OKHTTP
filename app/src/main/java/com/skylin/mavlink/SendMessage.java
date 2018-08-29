package com.skylin.mavlink;

/**
 * Created by sjj on 2017/4/13.
 */

public interface SendMessage {
    /**
     * 发送消息
     */
    <T> boolean send(ACKHandler<T> ackHandler);
}
