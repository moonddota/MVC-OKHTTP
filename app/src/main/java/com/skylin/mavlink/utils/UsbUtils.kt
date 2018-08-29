package com.skylin.mavlink.utils

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.hardware.usb.UsbAccessory
import android.content.Context.USB_SERVICE
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import io.reactivex.Observable
import org.xmlpull.v1.XmlPullParser
import android.content.IntentFilter
import android.content.Intent
import com.skylin.uav.R


private val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"
fun usbManager(ctx: Context) = ctx.getSystemService(Context.USB_SERVICE) as UsbManager
fun getAccessoryList(ctx: Context): Array<out UsbAccessory>? {
    val manager = ctx.getSystemService(Context.USB_SERVICE) as UsbManager
    return manager.accessoryList
}

fun requestPermission(ctx: Context): Observable<UsbAccessory> {
    return Observable.create { usbAcc ->
        val manager = ctx.getSystemService(Context.USB_SERVICE) as UsbManager
        val usbAccessory = getAccessory(ctx) ?: throw Exception("未找到USB设备")
        if (manager.hasPermission(usbAccessory)) {
            usbAcc.onNext(usbAccessory)
            usbAcc.onComplete()
        } else {
            ctx.registerReceiver(object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    val action = intent.action
                    if (ACTION_USB_PERMISSION == action) {
                        synchronized(this) {
                            ctx.unregisterReceiver(this)
                            if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                                usbAcc.onNext(usbAccessory)
                                usbAcc.onComplete()
                            } else {
                                usbAcc.tryOnError(Exception("USB使用权限被拒绝"))
                            }
                        }
                    }
                }
            }, IntentFilter(ACTION_USB_PERMISSION))
            manager.requestPermission(usbAccessory, PendingIntent.getBroadcast(ctx, 0, Intent(ACTION_USB_PERMISSION), 0))
        }
    }

}

fun getAccessory(ctx: Context): UsbAccessory? {
    val list = getAccessoryFilter(ctx)
    return getAccessoryList(ctx)?.find { acc ->
        list.find {
            it.manufacturer == acc.manufacturer && it.model == acc.model && it.version == acc.version
        } != null
    }
}

fun getAccessoryFilter(ctx: Context): List<UsbFilterAccessory> {
    val xml = ctx.resources.getXml(R.xml.accessory_filter)
    val list = mutableListOf<UsbFilterAccessory>()
    while (xml.eventType != XmlPullParser.END_DOCUMENT) {
        if (xml.eventType == XmlPullParser.START_TAG && "usb-accessory" == xml.name) {
            list.add(UsbFilterAccessory(xml.getAttributeValue(null, "manufacturer"),
                    xml.getAttributeValue(null, "model"),
                    xml.getAttributeValue(null, "version")))
        }
        xml.next()
    }
    return list
}

class UsbFilterAccessory(val manufacturer: String,val model: String,val version: String)