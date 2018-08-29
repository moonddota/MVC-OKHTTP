package com.skylin.mavlink.model;

import com.MAVLink.Messages.MAVLinkMessage;

/**
 * Created by SJJ on 2017/3/30.
 */

public class Response<T> implements Cloneable{
    private boolean success;
    private String errorMessage;
    private T data;
    private MAVLinkMessage message;
    public boolean isSuccess() {
        return success;
    }

    public Response<T> setSuccess(boolean success) {
        this.success = success;
        return this;
    }

    public T getData() {
        return data;
    }

    public Response<T> setData(T data) {
        this.data = data;
        return this;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Response<T> setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    public Response<T> setMessage(MAVLinkMessage message) {
        this.message = message;
        return this;
    }


    @Override
    public String toString() {
        return "Response{" +
                "success=" + success +
                ", errorMessage='" + errorMessage + '\'' +
                ", data=" + data +
                ", message=" + message +
                '}';
    }

    @Override
    public Response<T> clone() {
        try {
            return (Response<T>) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
