package com.skylin.mavlink.connection.usb;

import android.content.Context;
import android.hardware.usb.UsbManager;

import com.skylin.mavlink.connection.usb.usbserial.driver.UsbSerialDriver;
import com.skylin.uav.drawforterrain.util.ToastUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import cn.wch.ch34xuartdriver.CH34xUARTDriver;

/**
 * Created by moon on 2017/8/28.
 */

public class USBCH34XConnection extends UsbConnection.UsbConnectionImpl {
    private static final String ACTION_USB_PERMISSION = "cn.wch.wchusbdriver.USB_PERMISSION";
    private CH34xUARTDriver driver;

    protected USBCH34XConnection(Context context, int baudRate) {
        super(context, baudRate);
        driver = new CH34xUARTDriver((UsbManager) context.getSystemService(Context.USB_SERVICE), context, ACTION_USB_PERMISSION);
        if (!driver.UsbFeatureSupported()) {
            ToastUtil.show("您的手机不支持USB HOST，请更换其他手机再试！");
        }
    }

    @Override
    protected void closeUsbConnection() throws IOException {
        driver.CloseDevice();
    }

    @Override
    protected void openUsbConnection() throws IOException {
        int i = driver.ResumeUsbList();
        if (i == -1) {
            throw new IOException("usb 打开失败");
        } else if (i != 0) {
            throw new IOException("usb 未授权");
        }

        boolean r = driver.UartInit();
        if (!r) {
            throw new IOException("usb 打开失败");
        }
        boolean b = driver.SetConfig(mBaudRate, (byte) 8, (byte) UsbSerialDriver.STOPBITS_1, (byte) UsbSerialDriver.PARITY_NONE, (byte) 0);
        if (!b) {
            throw new IOException("usb 设置失败");
        }


    }

    @Override
    protected int readDataBlock(byte[] readData) throws IOException {
        return driver.ReadData(readData, readData.length);
    }

    @Override
    protected void sendBuffer(byte[] buffer) throws IOException {
        int i = driver.WriteData(buffer, buffer.length);
        if (i < 0){
            throw new IOException("写失败");
        }
    }

}
