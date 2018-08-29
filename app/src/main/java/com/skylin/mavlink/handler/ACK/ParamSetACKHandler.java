package com.skylin.mavlink.handler.ACK;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.common.msg_param_set;
import com.MAVLink.common.msg_param_value;
import com.skylin.mavlink.SendMessage;
import com.skylin.mavlink.model.Response;

/**
 * Created by sjj on 2017/4/18.
 */

public class ParamSetACKHandler extends CommonACKHandler<msg_param_value> {

    public ParamSetACKHandler(SendMessage sendMavPack, msg_param_set message, com.skylin.mavlink.ACKListener<msg_param_value> responseListener) {
        super(sendMavPack, msg_param_value.MAVLINK_MSG_ID_PARAM_VALUE, message, responseListener);
    }

    @Override
    public boolean handleMsg(MAVLinkMessage message) {
        if (targetMsgId != message.msgid)
            return false;
        msg_param_value value = (msg_param_value) message;
        msg_param_set mavLinkMessage = (msg_param_set) getMessage();
        String param_id = mavLinkMessage.getParam_Id();
        if (param_id.equals(value.getParam_Id())) {
            if (mavLinkMessage.param_value == value.param_value) {
                response(new Response<msg_param_value>().setData(value).setSuccess(true));
            } else {
                response(new Response<msg_param_value>().setSuccess(false).setData(value).setErrorMessage(param_id + "设置失败：" + value.param_value));
            }
            return true;
        }
        return false;
    }
}
