package com.skylin.mavlink.handler.ACK;

import android.annotation.SuppressLint;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.common.msg_mission_ack;
import com.MAVLink.common.msg_mission_count;
import com.MAVLink.common.msg_mission_item;
import com.MAVLink.common.msg_mission_item_int;
import com.MAVLink.common.msg_mission_request_int;
import com.MAVLink.common.msg_mission_request_list;
import com.MAVLink.enums.MAV_MISSION_RESULT;
import com.skylin.mavlink.ACKListener;
import com.skylin.mavlink.SendMessage;
import com.skylin.mavlink.model.Progress;
import com.skylin.mavlink.model.Response;

import java.util.HashMap;

/**
 * Created by Administrator on 2017/5/4.
 */

public class ReadTaskHandler extends AbstractACKHandler<Progress<HashMap<Integer, MAVLinkMessage>>> {
    private MAVLinkMessage requestMessage;
    private short target_system;
    private short target_component;
    private int waypointCount;
    @SuppressLint("UseSparseArrays")
    private HashMap<Integer, MAVLinkMessage> mission_items = new HashMap<>();
    private Progress<HashMap<Integer, MAVLinkMessage>> progress = new Progress<>();
    private Response<Progress<HashMap<Integer, MAVLinkMessage>>> response = new Response<Progress<HashMap<Integer, MAVLinkMessage>>>().setData(progress).setSuccess(true);

    public ReadTaskHandler(SendMessage sendMavPack, short target_system, short target_component, ACKListener<Progress<HashMap<Integer, MAVLinkMessage>>> responseListener) {
        super(sendMavPack, null, responseListener);
        this.target_system = target_system;
        this.target_component = target_component;
        msg_mission_request_list msg = new msg_mission_request_list();
        msg.target_system = target_system;
        msg.target_component = target_component;
        requestMessage = msg;
        progress.setData(mission_items);
    }

    @Override
    public MAVLinkMessage getMessage() {
        return requestMessage;
    }

    @Override
    public boolean requestAck() {
        return sendMessage(this);
    }

    @Override
    public void onSend() {

    }

    @Override
    public boolean handleMsg(MAVLinkMessage msg) {
        if (msg.msgid == msg_mission_count.MAVLINK_MSG_ID_MISSION_COUNT) {
            waypointCount = ((msg_mission_count) msg).count;
            progress.setCount(waypointCount);
            requestWayPoint(0);
        } else if (msg.msgid == msg_mission_item_int.MAVLINK_MSG_ID_MISSION_ITEM_INT || msg.msgid == msg_mission_item.MAVLINK_MSG_ID_MISSION_ITEM) {
            int seq;
            if (msg.msgid == msg_mission_item_int.MAVLINK_MSG_ID_MISSION_ITEM_INT) {
                seq = ((msg_mission_item_int) msg).seq;
            } else {
                seq = ((msg_mission_item) msg).seq;
            }

            mission_items.put(seq, msg);
            progress.setProgress(seq + 1);
            if (mission_items.size() == waypointCount) {
                progress.setFinish(true);

                msg_mission_ack ack = new msg_mission_ack();
                ack.target_system = target_system;
                ack.target_component = target_component;
                ack.type = MAV_MISSION_RESULT.MAV_MISSION_ACCEPTED;
                requestMessage = ack;
                sendMessage(this);
            } else {
                requestWayPoint(mission_items.size());
            }
            response();
        }
        return progress.isFinish();
    }

    private void requestWayPoint(int index) {
        msg_mission_request_int msg = new msg_mission_request_int();
        msg.target_system = target_system;
        msg.target_component = target_component;
        msg.seq = index;
        requestMessage = msg;
        sendMessage(this);
        setTimeout(3000);
        resetRetry();
    }

    public void response() {
        Response<Progress<HashMap<Integer, MAVLinkMessage>>> clone = response.clone();
        Progress<HashMap<Integer, MAVLinkMessage>> progress = this.progress.clone();
        clone.setData(progress);
        super.response(clone);
    }
}
