package com.skylin.uav.drawforterrain.select.home


import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.os.*
import android.support.annotation.RequiresApi
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.DisplayMetrics
import android.view.*
import android.widget.*
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog
import com.skylin.uav.R
import com.skylin.uav.drawforterrain.BaseActivity
import com.skylin.uav.drawforterrain.service.BluetoothLeService
import com.skylin.uav.drawforterrain.util.ToastUtil
import com.zhy.adapter.recyclerview.CommonAdapter
import com.zhy.adapter.recyclerview.base.ViewHolder
import java.util.ArrayList

class BlueConnectPopu : View.OnClickListener {

    private var device_rv: RecyclerView? = null
    private val mLeDevices = ArrayList<BluetoothDevice>()
    private var mScanning: Boolean = false
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var search_position = -1
    private var tipDialog: QMUITipDialog? = null
    private var mdevice: BluetoothDevice? = null
    private var blueconnect: Button? = null
    private var refresh_blue: ImageView? = null


    private var view: View? = null
    private var popupWindow: PopupWindow? = null
    private var mactivity: Activity? = null
    private var id: Int? = null

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    constructor(mactivity: Activity?, view: View?, id: Int?) {
        this.view = view
        this.mactivity = mactivity
        this.id = id
        init()
    }


    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun init() {
        val dm = DisplayMetrics()
        mactivity!!.getWindowManager().getDefaultDisplay().getMetrics(dm)//获取屏幕信息
        val inflater = LayoutInflater.from(mactivity)
        var popView = inflater.inflate(R.layout.popu_blueconnect, null)

        val bluetoothManager = mactivity!!.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = bluetoothManager.adapter

        initViews(popView)
        tipDialog = QMUITipDialog.Builder(mactivity)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord(mactivity?.getString(R.string.EquipmentActivity_tv2))
                .create()
    }


    private fun initViews(popView: View) {

        blueconnect = popView.findViewById(R.id.blueconnect)
        blueconnect!!.setOnClickListener(this)


        initAdapter(popView)

        initPopu(popView)
    }


    private fun initAdapter(popView: View) {
        refresh_blue = popView.findViewById(R.id.refresh_blue)
        refresh_blue?.setOnClickListener(this)

        device_rv = popView.findViewById(R.id.blueconnect_rc)
        device_rv!!.layoutManager = LayoutManager(mactivity!!)
        device_rv!!.setHasFixedSize(true)

        device_rv!!.adapter = object : CommonAdapter<BluetoothDevice>(mactivity!!, R.layout.item_device, mLeDevices) {
            override fun convert(holder: ViewHolder, bluetoothDevice: BluetoothDevice?, position: Int) {

                if (bluetoothDevice != null) {
                    val deviceName = bluetoothDevice.name
                    val textView = holder.getView<TextView>(R.id.device_isconnect)

                    val name = holder.getView<TextView>(R.id.device_name)
                    if (deviceName != null && deviceName.length > 0) {
                        name.text = deviceName
                    } else {
                        name.text = "Unknown device"
                    }

                    val checkBox = holder.getView<CheckBox>(R.id.device_check)
                    if (search_position == position) {
                        checkBox.isChecked = true
                    } else {
                        checkBox.isChecked = false
                    }
                    if (BlueBaseActivity.device != null) {
                        if (BlueBaseActivity.mConnected) {
                            if (bluetoothDevice.address == BlueBaseActivity.device.address) {
                                checkBox.visibility = View.GONE
                                textView.visibility = View.VISIBLE
                                textView.text = mactivity?.getString(R.string.EquipmentActivity_tv3)
                                name.setTextColor(mactivity!!.resources!!.getColor(R.color.green3))
                                textView.setTextColor(mactivity!!.resources!!.getColor(R.color.green3))
                            } else {
                                name.setTextColor(mactivity?.resources!!.getColor(R.color.black))
                                textView.visibility = View.GONE
                                checkBox.visibility = View.VISIBLE
                            }
                        } else {
                            name.setTextColor(mactivity!!.resources.getColor(R.color.black))
                            textView.visibility = View.GONE
                            checkBox.visibility = View.VISIBLE
                        }
                    }

                    val frameLayout = holder.getView<FrameLayout>(R.id.device_cardview)
                    frameLayout.setOnClickListener(View.OnClickListener {
                        val device = mLeDevices[position] ?: return@OnClickListener
                        mdevice = device
                        search_position = position
                        blueconnect!!.isClickable = true
                        blueconnect!!.setBackgroundResource(R.color.green3)
                        device_rv!!.adapter!!.notifyDataSetChanged()
                    })
                }
            }
        }
    }

    private fun initPopu(popView: View) {
        val displayMetrics = mactivity!!.getResources().displayMetrics
        if (BaseActivity.isScreenOriatationPortrait(mactivity)) {
            //竖屏
            popupWindow = PopupWindow(popView, displayMetrics.widthPixels, (displayMetrics.heightPixels * 0.4).toInt())
        } else {
            //横屏
            popupWindow = PopupWindow(popView, (displayMetrics.widthPixels * 0.6).toInt(), (displayMetrics.heightPixels * 0.6).toInt())
        }
        popupWindow?.setAnimationStyle(R.style.dialogstyle)//弹出动画
        popupWindow?.setFocusable(true)//PopupWindow是否具有获取焦点的能力，默认为False。
        popupWindow?.setOutsideTouchable(true)//设置PopupWindow是否响应外部点击事件，默认是true
        popupWindow?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        popupWindow?.setBackgroundDrawable(BitmapDrawable())//使点击popupwindow以外的区域时popupwindow自动消失须放在showAsDropDown之前
        popupWindow?.setOnDismissListener(PopupWindow.OnDismissListener { backgroundAlpha(mactivity!!, 1f) })


    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun show() {

        if (mBluetoothAdapter == null || !mBluetoothAdapter!!.isEnabled) {
            mBluetoothAdapter!!.enable()
        }
        scanLeDevice(true)
        blueconnect!!.text = if (BlueBaseActivity.mConnected) mactivity!!.getString(R.string.EquipmentActivity_tv4) else mactivity!!.getString(R.string.EquipmentActivity_tv5)
        blueconnect!!.setBackgroundResource(if (BlueBaseActivity.mConnected) R.color.green3 else R.color.bbc2bb)
        blueconnect!!.isClickable = if (BlueBaseActivity.mConnected) true else false


        popupWindow!!.showAtLocation(mactivity!!.findViewById<View?>(id!!), Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 0)
        backgroundAlpha(mactivity!!, 0.5f)

        mactivity!!.registerReceiver(mGattUpdateReceiver, BlueBaseActivity.makeGattUpdateIntentFilter())
    }


    fun dismiss() {
        popupWindow?.dismiss()
        mactivity!!.unregisterReceiver(mGattUpdateReceiver)
    }

    fun backgroundAlpha(context: Activity, bgAlpha: Float) {
        val lp = context.window.attributes
        lp.alpha = bgAlpha
        context.window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        context.window.attributes = lp
    }



    @SuppressLint("ObjectAnimatorBinding")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    override fun onClick(v: View) {
        when (v) {

            blueconnect -> {
                if (BlueBaseActivity.mConnected) {
                    BlueBaseActivity.disConnectBlueFour()
                    viewsvisivleT()
                } else {

                    if (mdevice == null) {
                        ToastUtil.show(mactivity!!.getString(R.string.toast_3))
                        return
                    }
                    BlueBaseActivity.device = mdevice
                    if (mScanning) {
                        viewsvisivleF()
                    }

                    BlueBaseActivity.connectBlueFour()
                    tipDialog!!.show()
                    checkConnect()
                }
            }
            refresh_blue -> {
                scanLeDevice(true)
                val anim: ObjectAnimator = ObjectAnimator.ofFloat(refresh_blue!!, "rotation", 0f, 360f)
                anim.setDuration(500)
                anim.setRepeatCount(3)
                anim.setRepeatMode(ObjectAnimator.RESTART)
                anim.start()
            }
            else -> {
            }
        }
    }

    //设备扫描回调。
    private val mLeScanCallback = BluetoothAdapter.LeScanCallback { device, rssi, scanRecord ->
        //	device  搜索到的设备
        mactivity?.runOnUiThread {
            //                    String s = device.getName();
            //                    if (!TextUtils.isEmpty(s)) {
            //                        if (s.startsWith("401") && s.length() == 15) {
            if (!mLeDevices.contains(device)) {
                mLeDevices.add(device)
                device_rv!!.adapter!!.notifyDataSetChanged()
            }
            //                        }
            //                    }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun scanLeDevice(enable: Boolean) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed({ viewsvisivleF() }, 30000)
            viewsvisivleT()
        } else {
            viewsvisivleF()
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private fun viewsvisivleF() {
        mScanning = false
        mBluetoothAdapter!!.stopLeScan(mLeScanCallback)
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private fun viewsvisivleT() {

        mLeDevices.clear()
        search_position = -1
        if (BlueBaseActivity.mConnected) {
            mLeDevices.add(BlueBaseActivity.device)
        }
        mScanning = true
        mBluetoothAdapter!!.startLeScan(mLeScanCallback)
    }

    @SuppressLint("HandlerLeak")
    private val mHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            try {
                when (msg.what) {
                    0 -> {
                        tipDialog!!.dismiss()
                        for (i in mLeDevices.indices) {
                            if (mLeDevices[i].address == BlueBaseActivity.device.address) {
                                mLeDevices.removeAt(i)
                                mLeDevices.add(0, BlueBaseActivity.device)
                            }
                        }
                        search_position = -1
                        device_rv!!.adapter!!.notifyDataSetChanged()
                        device_rv!!.layoutManager.scrollToPosition(0)
                        popupWindow?.dismiss()
                    }
                    1 -> {
                        tipDialog!!.dismiss()
                        BlueBaseActivity.disConnectBlueFour()
                        device_rv!!.adapter!!.notifyDataSetChanged()
                    }
                    else -> {
                    }
                }
            } catch (e: Exception) {

            }

        }
    }


    private fun checkConnect() {
        Thread(Runnable {
            try {
                val timeOut = SystemClock.uptimeMillis() + 10000
                while (!BlueBaseActivity.mConnected) {
                    if (SystemClock.uptimeMillis() > timeOut) {
                        ToastUtil.show(mactivity!!.getString(R.string.toast_5))
                        mHandler.sendEmptyMessage(1)
                        return@Runnable
                    }
                }
                Thread.sleep(1000)
                if (BlueBaseActivity.mConnected) {
                    mHandler.sendEmptyMessage(0)
                } else {
                    mHandler.sendEmptyMessage(1)
                    ToastUtil.show(mactivity!!.getString(R.string.toast_5))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }).start()
    }


    private val mGattUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (BluetoothLeService.ACTION_GATT_CONNECTED == intent.action) { //连接一个GATT服务
                blueconnect!!.text = mactivity!!.getString(R.string.EquipmentActivity_tv4)
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED == intent.action) {  //从GATT服务中断开连接
                blueconnect!!.text = mactivity!!.getString(R.string.EquipmentActivity_tv5)
                blueconnect!!.setBackgroundResource(R.color.bbc2bb)
                blueconnect!!.isClickable = false
                mLeDevices.clear()
                device_rv!!.adapter!!.notifyDataSetChanged()
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED == intent.action) {  //发现有可支持的服务
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE == intent.action) {   //从服务中接受数据
            }
        }
    }


}