package com.skylin.mavlink.model;

import java.io.Serializable;

/**
 * Base type used to pass the drone connection parameters over ipc.
 */
public class ConnectionParameter implements Serializable {
    public static final int serial = 0;
    public static final int usb = 1;
    public static final int tcp = 2;
    @Deprecated
    public static final int bluetooth = 2;
    private final int connectionType;

    public ConnectionParameter(int connectionType) {
        this.connectionType = connectionType;
    }

    /**
     * {@link #tcp}
     * {@link #usb}
     */
    public int getConnectionType() {
        return connectionType;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConnectionParameter)) return false;

        ConnectionParameter that = (ConnectionParameter) o;

        return getConnectionType() == that.getConnectionType();

    }

    @Override
    public int hashCode() {
        return getConnectionType();
    }
}
