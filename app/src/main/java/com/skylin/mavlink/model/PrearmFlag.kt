package com.skylin.mavlink.model

import com.MAVLink.ardupilotmega.msg_prearm_flag
import com.skylin.mavlink.model.PrearmFlag.Companion.check

data class PrearmFlag(var isChecked: Boolean,
                      var barometer: Boolean,
                      var rcCalibration: Boolean,
                      var compass: Boolean,
                      var gps: Boolean,
                      var fence: Boolean,
                      var ins: Boolean,
                      var board: Boolean,
                      var logging: Boolean,
                      var parameter: Boolean,
                      var motor: Boolean,
                      var leaning: Boolean,
                      var throttle: Boolean) {
    fun des(): String {
        if (isChecked) {
            return "自检通过"
        }
        return StringBuilder()
                .check(barometer,"气压计异常、")
                .check(rcCalibration,"遥控器异常、")
                .check(compass,"罗盘异常、")
                .check(gps,"GPS自检未通过、")
                .check(fence,"电子围栏检测异常、")
                .check(ins,"加速度计或陀螺仪异常、")
                .check(board,"电池供电不足、")
                .check(logging,"SD卡读写异常，禁止解锁、")
                .check(parameter,"飞控关键参数异常、")
                .check(motor,"电机控制异常、")
                .check(leaning,"飞机倾斜超过设定角度、")
                .check(throttle,"起飞时油门应在中位、")
                .toString()
    }

    private fun StringBuilder.check(c: Boolean, des: String): StringBuilder {
        if (!c) {
            append(des)
        }
        return this
    }

    companion object {
        //气压计
        val BAROMETER = 1L
        //遥控器
        val RC_CALIBRATION = 1L shl 1
        //磁罗盘
        val COMPASS = 1L shl 2
        //GPS
        val GPS = 1L shl 3
        //地理围栏
        val FENCE = 1L shl 4
        //惯导
        val INS = 1L shl 5
        val BOARD = 1L shl 6
        val LOGGING = 1L shl 7
        val PARAMETER = 1L shl 8
        val MOTOR = 1L shl 9
        val LEANING = 1L shl 10
        val THROTTLE = 1L shl 11
        @JvmStatic
        fun create(msg: msg_prearm_flag): PrearmFlag {
            return PrearmFlag(msg.value == 0L, msg.value check BAROMETER,msg.value check RC_CALIBRATION,msg.value check COMPASS,
                    msg.value check GPS,msg.value check FENCE,msg.value check INS,msg.value check BOARD,msg.value check LOGGING,msg.value check PARAMETER,
                    msg.value check MOTOR,msg.value check LEANING,msg.value check THROTTLE)
        }

        private infix fun Long.check(flag: Long): Boolean {
            return this and flag == 0L
        }
    }
}