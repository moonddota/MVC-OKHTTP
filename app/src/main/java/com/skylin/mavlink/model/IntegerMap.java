package com.skylin.mavlink.model;

import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

import java.util.HashMap;

/**
 * Created by SJJ on 2017/5/4.
 */

public class IntegerMap extends HashMap<Integer, Integer> implements Cloneable{
    private int minKey = Integer.MAX_VALUE;
    private int maxKey = Integer.MIN_VALUE;
    private int minValue = Integer.MAX_VALUE;
    private int maxValue = Integer.MIN_VALUE;
    @Override
    public Integer get(Object key) {
        if (key instanceof Integer) {
            Integer intKey = (Integer) key;
            if (intKey < minKey) return minValue - 1;
            if (intKey > maxKey) return maxValue + 1;
            Integer integer = super.get(key);
            if (integer == null) {
                return get(++intKey);
            }
            return integer;
        }
        throw new IllegalArgumentException();
    }

    @Override
    public Integer put(@NonNull Integer key, @NonNull Integer value) {
        minKey = Math.min(key, minKey);
        minValue = Math.min(value, minValue);
        maxKey = Math.max(key, maxKey);
        maxValue = Math.max(value, maxValue);
        return super.put(key, value);
    }

    @Override
    public void clear() {
        super.clear();
        minKey = Integer.MAX_VALUE;
        maxKey = Integer.MIN_VALUE;
        minValue = Integer.MAX_VALUE;
        maxValue = Integer.MIN_VALUE;
    }

    public int getMinKey() {
        return minKey;
    }

    public int getMaxKey() {
        return maxKey;
    }

    public int getMinValue() {
        return minValue;
    }

    public int getMaxValue() {
        return maxValue;
    }

    @Override
    public String toString() {
        return "IntegerMap{" +
                "minKey=" + minKey +
                ", maxKey=" + maxKey +
                ", minValue=" + minValue +
                ", maxValue=" + maxValue +
                '}'+super.toString();
    }
}
