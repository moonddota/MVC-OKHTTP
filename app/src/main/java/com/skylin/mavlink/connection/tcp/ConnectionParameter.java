package com.skylin.mavlink.connection.tcp;

/**
 * Base type used to pass the drone connection parameters over ipc.
 */
public class ConnectionParameter {
    public static final int tcp = 0;
    public static final int usb = 1;
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
