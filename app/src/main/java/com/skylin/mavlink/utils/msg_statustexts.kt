package com.skylin.mavlink.utils

import com.MAVLink.common.msg_statustext
import com.MAVLink.enums.MAV_SEVERITY
import java.util.regex.Pattern

val msg_statustext.warning: String
    get() {
        val s = getText()
        if (severity.toInt() == MAV_SEVERITY.MAV_SEVERITY_NOTICE) {
            s
            val matcher = Pattern.compile("\\d+").matcher(s)
            val b0 = matcher.find()
            if (!b0) return s
            val group = matcher.group()
            val b1 = matcher.find()
            if (!b1) return s
            val group1 = matcher.group()

            return MAV_ERROR_SUBSYSTEM.valueOf(java.lang.Short.parseShort(group)).des + " " + MAV_ERROR_CODE.valueOf(java.lang.Short.parseShort(group1)).des
        }
        return s
    }

val msg_statustext.isTakeoffNotice: Boolean
    get() = severity.toInt() == MAV_SEVERITY.MAV_SEVERITY_NOTICE && getText().startsWith("S0")


private enum class MAV_ERROR_SUBSYSTEM(val value: Int, val des: String) {
    MAV_ERROR_SUBSYSTEM_TK_CHECK(0, "起飞"),
    MAV_ERROR_SUBSYSTEM_END(99, "未知");


    companion object {

        fun valueOf(severity: Short): MAV_ERROR_SUBSYSTEM {
            for (subsystem in values()) {
                if (subsystem.value == severity.toInt()) {
                    return subsystem
                }
            }
            return MAV_ERROR_SUBSYSTEM_END
        }
    }
}

private enum class MAV_ERROR_CODE(val value: Int, val des: String) {
    MAV_ERROR_CODE_LOW_BATTERY(0, "低电"),
    MAV_ERROR_CODE_LOW_YW(1, "低药"),
    MAV_ERROR_CODE_LOW_RTK_ERROR(2, "RTK错误"),
    MAV_ERROR_CODE_EM_BATTERY(3, "备电错误"),
    MAV_ERROR_CODE_ARM_MOTOR_FAILED(4, "电机启动失败"),
    MAV_ERROR_CODE_SET_STABLIZE_FAILED(5, "Stablize设置失败"),
    MAV_ERROR_CODE_SET_AUTO_FAILED(6, "Auto设置失败"),
    MAV_ERROR_CODE_NO_GPS(7, "无GPS"),
    MAV_ERROR_CODE_ARMED(8, "已经解锁"),
    MAV_ERROR_CODE_NOSD(9, "无SD卡"),
    MAV_ERROR_CODE_END(99, "未知");


    companion object {

        fun valueOf(severity: Short): MAV_ERROR_CODE {
            for (subsystem in values()) {
                if (subsystem.value == severity.toInt()) {
                    return subsystem
                }
            }
            return MAV_ERROR_CODE_END
        }
    }
}