package com.skylin.mavlink.model;

/**
 * Created by SJJ on 2017/4/1.
 */

public enum UavAttribute {
    version,
    /**
     * 连接断开
     */
    connectionState_disconnected,
    /**
     * 已连接
     */
    connectionState_connected,
    /**
     * 正在连接
     */
    connectionState_connecting,
    parameter,
    roll,
    pitch,
    yaw,
    groundSpeed,
    airSpeed,
    climbSpeed,
    wpDist,
    altError,
    aspdError,
    navPitch,
    navRoll,
    navBearing,
    xmag,
    ymag,
    zmag,
    type,
    systemStatus,
    baseMode,
    mode,
    position,
    batteryState,
    radioState,
    gpsState,
    gps2FixType,
    ekfStatus,
    warning,
    firmwareVersion,
    penSaACK,
    daiSuACK,
    pixstatu,
    curdosage,
    readypower,
    lng,
    lat,
    onekeyfly,
    errorstatu,
    selfInspectionStatus,
    initStatus_complete,
    initStatus_not,
    initStatus_processing, message_refresh, reachedCommand, readypowerText, backStatus, globalPosition, prearmFlag, msgEvent, currentMission, uavStatus
}
