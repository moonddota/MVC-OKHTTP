package com.skylin.mavlink.connection.usb

import android.content.Context
import android.os.ParcelFileDescriptor
import com.skylin.mavlink.utils.requestPermission
import com.skylin.mavlink.utils.usbManager
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import kotlin.experimental.and
import kotlin.experimental.or

internal class UsbAccessoryConnection(private val ctx: Context,private val bd: Int) : UsbConnection.UsbConnectionImpl(ctx, bd) {

    private var output: OutputStream? = null
    private var input: InputStream? = null
    private var openAccessory: ParcelFileDescriptor? = null
    private var close = false

    override fun openUsbConnection() {
        close = false
        val accessory = requestPermission(ctx).blockingFirst()
        if (close) return
        openAccessory = usbManager(ctx).openAccessory(accessory) ?: throw Exception("USB设备打开失败")
        output = FileOutputStream(openAccessory?.fileDescriptor)
        input = FileInputStream(openAccessory?.fileDescriptor)

        setConfig(bd, 8, 1, 0, 0)

    }

    override fun readDataBlock(buffer: ByteArray): Int {
        return input?.read(buffer) ?: throw Exception("Uninitialized usb connection.")
    }

    override fun sendBuffer(buffer: ByteArray) {
        output?.write(buffer) ?: throw Exception("Uninitialized usb connection.")
    }

    override fun closeUsbConnection() {
        close = true
        try {
            setConfig(9600,8,1,0,0)
        } catch (e: Exception) {
        }

        try {
            openAccessory?.close()
        } catch (e: Exception) {
        }
        try {
            output?.close()
        } catch (e: Exception) {
        }
        try {
            input?.close()
        } catch (e: Exception) {
        }
        output = null
        input = null
        openAccessory = null
    }

    fun setConfig(baud: Int, dataBits: Byte, stopBits: Byte, parity: Byte, flowControl: Byte) {
        var tmp: Byte = 0x00
        var baudRate_byte: Byte = 0x00
        val writeusbdata = ByteArray(5)
        writeusbdata[0] = 0x30

        when (baud) {
            300 -> baudRate_byte = 0x00
            600 -> baudRate_byte = 0x01
            1200 -> baudRate_byte = 0x02
            2400 -> baudRate_byte = 0x03
            4800 -> baudRate_byte = 0x04
            9600 -> baudRate_byte = 0x05
            19200 -> baudRate_byte = 0x06
            38400 -> baudRate_byte = 0x07
            57600 -> baudRate_byte = 0x08
            115200 -> baudRate_byte = 0x09
            230400 -> baudRate_byte = 0x0A
            460800 -> baudRate_byte = 0x0B
            921600 -> baudRate_byte = 0x0C
            else -> baudRate_byte = 0x05
        }// default baudRate "9600"
        // prepare the baud rate buffer
        writeusbdata[1] = baudRate_byte
        when (dataBits.toInt()) {
            5 -> tmp = tmp or 0x00
            6 -> tmp = tmp or 0x01
            7 -> tmp = tmp or 0x02
            8 -> tmp = tmp or 0x03
            else -> tmp = tmp or 0x03
        }//reserve
        //reserve
        // default data bit "8"
        when (stopBits.toInt()) {
            1 -> tmp = tmp and (1 shl 2).inv().toByte()
            2 -> tmp = tmp or (1 shl 2).toByte()
            else -> tmp = tmp and (1 shl 2).inv().toByte()
        }// default stop bit "1"
        tmp = when (parity.toInt()) {
            0 -> tmp and (1 shl 3 or (1 shl 4) or (1 shl 5)).inv().toByte()
            1 -> tmp or (1 shl 3).toByte()
            2 -> tmp or (1 shl 3 or (1 shl 4)).toByte()
            3 -> tmp or (1 shl 3 or (1 shl 5)).toByte()
            4 -> tmp or (1 shl 3 or (1 shl 4) or (1 shl 5)).toByte()
            else -> tmp and (1 shl 3 or (1 shl 4) or (1 shl 5)).inv().toByte()
        }//none
        //odd
        //event
        //mark
        //space
        //default parity "NONE"

        when (flowControl.toInt()) {
            0 -> tmp = tmp and (1 shl 6).inv().toByte()
            1 -> tmp = tmp or (1 shl 6).toByte()
            else -> tmp = tmp and (1 shl 6).inv().toByte()
        }//default flowControl "NONE"
        // dataBits, stopBits, parity, flowControl
        writeusbdata[2] = tmp

        writeusbdata[3] = 0x00
        writeusbdata[4] = 0x00

        sendBuffer(writeusbdata)
    }
}