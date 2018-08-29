package com.skylin.mavlink.model;

import java.io.Closeable;
import java.io.Serializable;

/**
 * Created by SJJ on 2017/5/7.
 */

public class Progress<T extends Serializable> implements Cloneable,Serializable{
    private boolean finish;
    private int count;
    private int progress;
    private T data;

    public boolean isFinish() {
        return finish;
    }

    public int getCount() {
        return count;
    }

    public int getProgress() {
        return progress;
    }

    public T getData() {
        return data;
    }

    public void setFinish(boolean finish) {
        this.finish = finish;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Progress{" +
                "finish=" + finish +
                ", count=" + count +
                ", progress=" + progress +
                ", data=" + data +
                '}';
    }

    @Override
    public Progress<T> clone() {
        try {
            return (Progress) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
