/* AUTO-GENERATED FILE.  DO NOT MODIFY.
 *
 * This class was automatically generated by the
 * java mavlink generator tool. It should not be modified by hand.
 */

// MESSAGE GIMBAL_AXIS_CALIBRATION_PROGRESS PACKING
package com.MAVLink.ardupilotmega;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;

/**
* 
            Reports progress and success or failure of gimbal axis calibration procedure
        
*/
public class msg_gimbal_axis_calibration_progress extends MAVLinkMessage {

    public static final int MAVLINK_MSG_ID_GIMBAL_AXIS_CALIBRATION_PROGRESS = 203;
    public static final int MAVLINK_MSG_LENGTH = 3;
    private static final long serialVersionUID = MAVLINK_MSG_ID_GIMBAL_AXIS_CALIBRATION_PROGRESS;


    
    /**
    * Which gimbal axis we're reporting calibration progress for
    */
    public short calibration_axis;
    
    /**
    * The current calibration progress for this axis, 0x64=100%
    */
    public short calibration_progress;
    
    /**
    * The status of the running calibration
    */
    public short calibration_status;
    

    /**
    * Generates the payload for a mavlink message for a message of this type
    * @return
    */
    public MAVLinkPacket pack(){
        MAVLinkPacket packet = new MAVLinkPacket(MAVLINK_MSG_LENGTH);
        packet.sysid = 255;
        packet.compid = 190;
        packet.msgid = MAVLINK_MSG_ID_GIMBAL_AXIS_CALIBRATION_PROGRESS;
        
        packet.payload.putUnsignedByte(calibration_axis);
        
        packet.payload.putUnsignedByte(calibration_progress);
        
        packet.payload.putUnsignedByte(calibration_status);
        
        return packet;
    }

    /**
    * Decode a gimbal_axis_calibration_progress message into this class fields
    *
    * @param payload The message to decode
    */
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        
        this.calibration_axis = payload.getUnsignedByte();
        
        this.calibration_progress = payload.getUnsignedByte();
        
        this.calibration_status = payload.getUnsignedByte();
        
    }

    /**
    * Constructor for a new message, just initializes the msgid
    */
    public msg_gimbal_axis_calibration_progress(){
        msgid = MAVLINK_MSG_ID_GIMBAL_AXIS_CALIBRATION_PROGRESS;
    }

    /**
    * Constructor for a new message, initializes the message with the payload
    * from a mavlink packet
    *
    */
    public msg_gimbal_axis_calibration_progress(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_GIMBAL_AXIS_CALIBRATION_PROGRESS;
        unpack(mavLinkPacket.payload);
    }

          
    /**
    * Returns a string with the MSG name and data
    */
    public String toString(){
        return "MAVLINK_MSG_ID_GIMBAL_AXIS_CALIBRATION_PROGRESS -"+" calibration_axis:"+calibration_axis+" calibration_progress:"+calibration_progress+" calibration_status:"+calibration_status+"";
    }
}
        