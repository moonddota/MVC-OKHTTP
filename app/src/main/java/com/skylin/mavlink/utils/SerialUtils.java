package com.skylin.mavlink.utils;

import java.io.DataOutputStream;
import java.io.OutputStream;

import sjj.alog.Log;

/**
 * Created by sjj on 2017/7/10.
 * <p>
 * switch (v.getId()) {
 * case R.id.button3:
 * execShellCmd("echo 1 > sys/class/switch/xpand/switch_power");
 * break;
 * case R.id.button8:
 * execShellCmd("echo 0 > sys/class/switch/xpand/switch_power");
 * break;
 * case R.id.button7:
 * execShellCmd("echo 1 > sys/class/switch/xpand/switch_io");
 * break;
 * case R.id.button6:
 * execShellCmd("echo 0 > sys/class/switch/xpand/switch_io");
 * break;
 * case R.id.button5:
 * execShellCmd("echo 1 > sys/class/switch/xpand/switch_irq");
 * break;
 * case R.id.button4:
 * execShellCmd("echo 0 > sys/class/switch/xpand/switch_irq");
 * break;
 * <p>
 * <p>
 * default:
 * break;
 * }
 */

public class SerialUtils {
    public static boolean powerOn() {
        return execShellCmd("echo 1 > sys/class/switch/xpand/switch_power");
    }
    public static boolean powerOff(){
        return execShellCmd("echo 0 > sys/class/switch/xpand/switch_power");
    }
    private static boolean execShellCmd(String cmd) {
        try {
            Process process = Runtime.getRuntime().exec("sh");
            OutputStream outputStream = process.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
            dataOutputStream.writeBytes(cmd);
            dataOutputStream.flush();
            dataOutputStream.close();
            outputStream.close();
            return true;
        } catch (Throwable t) {
            Log.e("execShellCmd",t);
        }
        return false;
    }
}
