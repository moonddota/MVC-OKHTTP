package com.skylin.mavlink.utils;

import io.reactivex.Flowable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

/**
 * Created by sjj on 2018/1/5.
 */

public final class RXBUS {
    public static final RXBUS def = new RXBUS();
    public final FlowableProcessor<Object> processor = PublishProcessor.create().toSerialized();

    public void push(Object o) {
        processor.onNext(o);
    }
    public <T> Flowable<T> ofType(Class<T> clazz) {
        return processor.ofType(clazz);
    }
}
