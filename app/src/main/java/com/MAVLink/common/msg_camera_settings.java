/* AUTO-GENERATED FILE.  DO NOT MODIFY.
 *
 * This class was automatically generated by the
 * java mavlink generator tool. It should not be modified by hand.
 */

// MESSAGE CAMERA_SETTINGS PACKING
package com.MAVLink.common;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;

/**
* WIP: Settings of a camera, can be requested using MAV_CMD_REQUEST_CAMERA_SETTINGS and written using MAV_CMD_SET_CAMERA_SETTINGS
*/
public class msg_camera_settings extends MAVLinkMessage {

    public static final int MAVLINK_MSG_ID_CAMERA_SETTINGS = 260;
    public static final int MAVLINK_MSG_ID_CAMERA_SETTINGS_CRC = 8;
    public static final int MAVLINK_MSG_LENGTH = 28;
    private static final long serialVersionUID = MAVLINK_MSG_ID_CAMERA_SETTINGS;


      
    /**
    * Timestamp (milliseconds since system boot)
    */
    public long time_boot_ms;
      
    /**
    * Aperture is 1/value
    */
    public float aperture;
      
    /**
    * Shutter speed in s
    */
    public float shutter_speed;
      
    /**
    * ISO sensitivity
    */
    public float iso_sensitivity;
      
    /**
    * Color temperature in degrees Kelvin
    */
    public float white_balance;
      
    /**
    * Camera ID if there are multiple
    */
    public short camera_id;
      
    /**
    * Aperture locked (0: auto, 1: locked)
    */
    public short aperture_locked;
      
    /**
    * Shutter speed locked (0: auto, 1: locked)
    */
    public short shutter_speed_locked;
      
    /**
    * ISO sensitivity locked (0: auto, 1: locked)
    */
    public short iso_sensitivity_locked;
      
    /**
    * Color temperature locked (0: auto, 1: locked)
    */
    public short white_balance_locked;
      
    /**
    * Reserved for a camera mode ID
    */
    public short mode_id;
      
    /**
    * Reserved for a color mode ID
    */
    public short color_mode_id;
      
    /**
    * Reserved for image format ID
    */
    public short image_format_id;
    

    /**
    * Generates the payload for a mavlink message for a message of this type
    * @return
    */
    public MAVLinkPacket pack(){
        MAVLinkPacket packet = new MAVLinkPacket(MAVLINK_MSG_LENGTH);
        packet.sysid = 255;
        packet.compid = 190;
        packet.msgid = MAVLINK_MSG_ID_CAMERA_SETTINGS;
        packet.crc_extra = MAVLINK_MSG_ID_CAMERA_SETTINGS_CRC;
              
        packet.payload.putUnsignedInt(time_boot_ms);
              
        packet.payload.putFloat(aperture);
              
        packet.payload.putFloat(shutter_speed);
              
        packet.payload.putFloat(iso_sensitivity);
              
        packet.payload.putFloat(white_balance);
              
        packet.payload.putUnsignedByte(camera_id);
              
        packet.payload.putUnsignedByte(aperture_locked);
              
        packet.payload.putUnsignedByte(shutter_speed_locked);
              
        packet.payload.putUnsignedByte(iso_sensitivity_locked);
              
        packet.payload.putUnsignedByte(white_balance_locked);
              
        packet.payload.putUnsignedByte(mode_id);
              
        packet.payload.putUnsignedByte(color_mode_id);
              
        packet.payload.putUnsignedByte(image_format_id);
        
        return packet;
    }

    /**
    * Decode a camera_settings message into this class fields
    *
    * @param payload The message to decode
    */
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
              
        this.time_boot_ms = payload.getUnsignedInt();
              
        this.aperture = payload.getFloat();
              
        this.shutter_speed = payload.getFloat();
              
        this.iso_sensitivity = payload.getFloat();
              
        this.white_balance = payload.getFloat();
              
        this.camera_id = payload.getUnsignedByte();
              
        this.aperture_locked = payload.getUnsignedByte();
              
        this.shutter_speed_locked = payload.getUnsignedByte();
              
        this.iso_sensitivity_locked = payload.getUnsignedByte();
              
        this.white_balance_locked = payload.getUnsignedByte();
              
        this.mode_id = payload.getUnsignedByte();
              
        this.color_mode_id = payload.getUnsignedByte();
              
        this.image_format_id = payload.getUnsignedByte();
        
    }

    /**
    * Constructor for a new message, just initializes the msgid
    */
    public msg_camera_settings(){
        msgid = MAVLINK_MSG_ID_CAMERA_SETTINGS;
    }

    /**
    * Constructor for a new message, initializes the message with the payload
    * from a mavlink packet
    *
    */
    public msg_camera_settings(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_CAMERA_SETTINGS;
        unpack(mavLinkPacket.payload);
    }

                              
    /**
    * Returns a string with the MSG name and data
    */
    public String toString(){
        return "MAVLINK_MSG_ID_CAMERA_SETTINGS - sysid:"+sysid+" compid:"+compid+" time_boot_ms:"+time_boot_ms+" aperture:"+aperture+" shutter_speed:"+shutter_speed+" iso_sensitivity:"+iso_sensitivity+" white_balance:"+white_balance+" camera_id:"+camera_id+" aperture_locked:"+aperture_locked+" shutter_speed_locked:"+shutter_speed_locked+" iso_sensitivity_locked:"+iso_sensitivity_locked+" white_balance_locked:"+white_balance_locked+" mode_id:"+mode_id+" color_mode_id:"+color_mode_id+" image_format_id:"+image_format_id+"";
    }
}
        