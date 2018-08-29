package com.skylin.mavlink.handler.ACK;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.common.msg_param_request_read;
import com.MAVLink.common.msg_param_value;
import com.skylin.mavlink.SendMessage;
import com.skylin.mavlink.model.Response;

/**
 * Created by sjj on 2017/4/18.
 */

public class ParamReadACKHandler extends CommonACKHandler<msg_param_value> {

    public ParamReadACKHandler(SendMessage sendMavPack, msg_param_request_read message, com.skylin.mavlink.ACKListener<msg_param_value> responseListener) {
        super(sendMavPack, msg_param_value.MAVLINK_MSG_ID_PARAM_VALUE, message, responseListener);
    }

    @Override
    public boolean handleMsg(MAVLinkMessage message) {
        if (targetMsgId!=message.msgid)
            return false;
        msg_param_value value = (msg_param_value) message;
        msg_param_request_read mavLinkMessage = (msg_param_request_read) getMessage();
        if (mavLinkMessage.getParam_Id().equals(value.getParam_Id())) {
            response(new Response<msg_param_value>().setData(value).setSuccess(true));
            return true;
        }
        return false;
    }
}
