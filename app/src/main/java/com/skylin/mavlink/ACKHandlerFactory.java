package com.skylin.mavlink;

/**
 * Created by Administrator on 2017/4/11.
 */

public class ACKHandlerFactory {
    private SendMessage sendMavPack;

    public ACKHandlerFactory(SendMessage sendMavPack) {
        this.sendMavPack = sendMavPack;
    }
}
