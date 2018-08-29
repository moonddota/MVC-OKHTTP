package com.skylin.mavlink.connection;

import com.MAVLink.MAVLinkPacket;

/**
 * Provides updates about the mavlink connection.
 */
public interface MavLinkConnectionListener {

//	/**
//	 * Called when the mavlink connection is established.
//	 */
//	public void onConnect();

	/**
	 * Called when data is received via the mavlink connection.
	 * 
	 * @param packet
	 *            received data
	 */
    void onReceivePacket(MAVLinkPacket packet);

	/**
	 * Called when the mavlink connection is disconnected.
	 */
    void onConnectionStateChange(int state);

	/**
	 * Provides information about communication error.
	 *
	 * @param errMsg
	 *            error information
	 */
    void onComError(Throwable errMsg);

}
