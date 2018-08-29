/* AUTO-GENERATED FILE.  DO NOT MODIFY.
 *
 * This class was automatically generated by the
 * java mavlink generator tool. It should not be modified by hand.
 */

// MESSAGE DATA16 PACKING
package com.MAVLink.ardupilotmega;
import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;

/**
* Data packet, size 16
*/
public class msg_data16 extends MAVLinkMessage {

    public static final int MAVLINK_MSG_ID_DATA16 = 169;
    public static final int MAVLINK_MSG_ID_DATA16_CRC = 234;
    public static final int MAVLINK_MSG_LENGTH = 18;
    private static final long serialVersionUID = MAVLINK_MSG_ID_DATA16;


      
    /**
    * data type
    */
    public short type;
      
    /**
    * data length
    */
    public short len;
      
    /**
    * raw data
    */
    public short data[] = new short[16];
    

    /**
    * Generates the payload for a mavlink message for a message of this type
    * @return
    */
    public MAVLinkPacket pack(){
        MAVLinkPacket packet = new MAVLinkPacket(MAVLINK_MSG_LENGTH);
        packet.sysid = 255;
        packet.compid = 190;
        packet.msgid = MAVLINK_MSG_ID_DATA16;
        packet.crc_extra = MAVLINK_MSG_ID_DATA16_CRC;
              
        packet.payload.putUnsignedByte(type);
              
        packet.payload.putUnsignedByte(len);
              
        
        for (int i = 0; i < data.length; i++) {
            packet.payload.putUnsignedByte(data[i]);
        }
                    
        
        return packet;
    }

    /**
    * Decode a data16 message into this class fields
    *
    * @param payload The message to decode
    */
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
              
        this.type = payload.getUnsignedByte();
              
        this.len = payload.getUnsignedByte();
              
         
        for (int i = 0; i < this.data.length; i++) {
            this.data[i] = payload.getUnsignedByte();
        }
                
        
    }

    /**
    * Constructor for a new message, just initializes the msgid
    */
    public msg_data16(){
        msgid = MAVLINK_MSG_ID_DATA16;
    }

    /**
    * Constructor for a new message, initializes the message with the payload
    * from a mavlink packet
    *
    */
    public msg_data16(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_DATA16;
        unpack(mavLinkPacket.payload);
    }

          
    /**
    * Returns a string with the MSG name and data
    */
    public String toString(){
        return "MAVLINK_MSG_ID_DATA16 - sysid:"+sysid+" compid:"+compid+" type:"+type+" len:"+len+" data:"+data+"";
    }
}
        