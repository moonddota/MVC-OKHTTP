package com.skylin.uav.drawforterrain.select.rtk

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.*
import android.support.annotation.RequiresApi
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.InputType
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.widget.*
import com.blankj.utilcode.util.ThreadPoolUtils
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog
import com.skylin.uav.R
import com.skylin.uav.drawforterrain.APP
import com.skylin.uav.drawforterrain.BaseActivity
import com.skylin.uav.drawforterrain.HttpUrlTool
import com.skylin.uav.drawforterrain.select.home.BlueBaseActivity
import com.skylin.uav.drawforterrain.select.home.BlueConnectPopu
import com.skylin.uav.drawforterrain.select.rtk.coord_db.Coord_SQL
import com.skylin.uav.drawforterrain.select.rtk.coord_db.CordBan
import com.skylin.uav.drawforterrain.service.BluetoothLeService
import com.skylin.uav.drawforterrain.setting_channel.GGABan
import com.skylin.uav.drawforterrain.util.OrderUtils
import com.skylin.uav.drawforterrain.util.ToastUtil
import com.skylin.uav.drawforterrain.views.MarqueeTextView
import org.json.JSONObject
import sjj.alog.Log
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Future

class RTKSrtting : BaseActivity(), View.OnClickListener {

    var draw_show: ImageView? = null
    var selext_connect: MarqueeTextView? = null

    private var set_base: FrameLayout? = null
    private var set_hand: FrameLayout? = null
    private var is_base: ImageView? = null
    private var is_hand: ImageView? = null

    private var connectble_recycle: RecyclerView? = null
    private var channelAdapter: ChannelAdapter? = null
    private val list = ArrayList<Int>()

    var tipDialog: QMUITipDialog? = null
    var blueConnectPopu: BlueConnectPopu? = null
    var waitdialog: Waitdialog? = null

    var wait_isShow = false
    var waitPopu: QMUITipDialog? = null

    val threadPoolUtils = ThreadPoolUtils(ThreadPoolUtils.SingleThread, 0)
    var submit: Future<*>? = null
    var submit1: Future<*>? = null
    var submit2: Future<*>? = null

    var base_history: TextView? = null
    var base_bt: LinearLayout? = null
    var set_self_location: Button? = null
    var save_location: Button? = null

    var rtk_state: TextView? = null
    var tev_satellites: TextView? = null
    var tev_hdop: TextView? = null
    var tev_lon: TextView? = null
    var tev_lat: TextView? = null
    var tev_alt: TextView? = null
    var ggaBan: GGABan? = null

    private var coord_sql: Coord_SQL? = null


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rtksetting)

        registerReceiver(mGattUpdateReceiver, BlueBaseActivity.makeGattUpdateIntentFilter())

        coord_sql = Coord_SQL(baseContext, "tid" + BaseActivity.teamid, null, 1)
        for (i in 0..7) {
            list.add(i)
        }

        initViews()

        waitPopu = QMUITipDialog.Builder(this@RTKSrtting)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord(getString(R.string.ConnectBLEActivity_tv16))
                .create()
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)

        setContentView(R.layout.activity_rtksetting)
        initViews()
        recover()
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun initViews() {
        draw_show = findViewById(R.id.draw_show)
        draw_show?.setImageResource(R.mipmap.ic_return)
        draw_show?.setOnClickListener(this)
        selext_connect = findViewById(R.id.selext_connect)
        selext_connect?.setOnClickListener(this)
        findViewById<LinearLayout>(R.id.icon_rtk).visibility = View.GONE
        blueConnectPopu = BlueConnectPopu(this@RTKSrtting, selext_connect, R.id.selext_connect)

        initRecycle()

        set_base = findViewById(R.id.set_base)
        set_base?.setOnClickListener(this)
        set_hand = findViewById(R.id.set_hand)
        set_hand?.setOnClickListener(this)

        is_base = findViewById(R.id.is_base)
        is_hand = findViewById(R.id.is_hand)

        base_history = findViewById(R.id.base_history)
        base_history?.setOnClickListener(this)
        base_bt = findViewById(R.id.base_bt)
        set_self_location = findViewById(R.id.set_self_location)
        set_self_location?.setOnClickListener(this)
        save_location = findViewById(R.id.save_location)
        save_location?.setOnClickListener(this)

        rtk_state = findViewById(R.id.rtk_state)
        tev_satellites = findViewById(R.id.tev_satellites)
        tev_hdop = findViewById(R.id.tev_hdop)
        tev_lon = findViewById(R.id.tev_lon)
        tev_lat = findViewById(R.id.tev_lat)
        tev_alt = findViewById(R.id.tev_alt)

    }

    fun initRecycle() {

        connectble_recycle = findViewById(R.id.connectble_recycle)
        connectble_recycle?.setLayoutManager(GridLayoutManager(this, 4))
        channelAdapter = ChannelAdapter(this, list)
        connectble_recycle?.setAdapter(channelAdapter)
        connectble_recycle?.setHasFixedSize(true)

        channelAdapter?.getView(object : ChannelAdapter.OnClick {
            @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
            override fun getClick(view: View?, i: Int) {
                val workmoder = BaseActivity.workmoder
                if (workmoder == "基站") {
                    QMUIDialog.MessageDialogBuilder(this@RTKSrtting)
                            .setTitle(getString(R.string.continuePopu_tv1))
                            .setMessage(getString(R.string.ConnectBLEActivity_tv8))
                            .addAction(getString(R.string.DotActivity_tv9)) { dialog, index ->
                                dialog.dismiss()
                            }
                            .addAction(getString(R.string.continuePopu_tv9)) { dialog, index ->
                                dialog.dismiss()
                                setChannle(i)
                            }
                            .show()
                } else if (workmoder == "手持杖") {
                    setChannle(i)
                }
            }
        })
    }

    fun recover() {
        if (BlueBaseActivity.mConnected) {
            selext_connect?.text = BlueBaseActivity.device.name

            if (workmoder == "基站") {
                setIsBase()
            } else if (workmoder == "手持杖") {
                setisHand()
            } else {
                setNull()
            }

            if (ggaBan != null) {
                rtk_state?.text = ggaBan?.rtk
                tev_satellites?.text = ggaBan?.satellites
                tev_hdop?.text = ggaBan?.hdop.toString()
                tev_lon?.text = ggaBan?.lon.toString()
                tev_lat?.text = ggaBan?.lat.toString()
                tev_alt?.text = ggaBan?.alt.toString()
            }

            channelAdapter?.setIndex(BaseActivity.channle)
            channelAdapter?.notifyDataSetChanged()
        }else{
            setNull()
            rtk_state?.text = ""
            tev_satellites?.text = ""
            tev_hdop?.text = ""
            tev_lon?.text = ""
            tev_lat?.text = ""
            tev_alt?.text = ""
        }
    }

    override fun onResume() {
        super.onResume()
        if (BlueBaseActivity.mConnected) {
            selext_connect?.text = BlueBaseActivity.device.name
            handler.removeCallbacks(runnable)
            handler.post(runnable)


            waitPopu?.show()
            wait_isShow = true
            handler.postDelayed({
                if (waitPopu!!.isShowing() && wait_isShow) {
                    ToastUtil.showLong(getString(R.string.ConnectBLEActivity_tv17))
                    waitPopu?.dismiss()
                    wait_isShow = false
                }
            }, 50000)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mGattUpdateReceiver)
        handler.removeCallbacks(runnable)

    }

    override fun onKeyLongPress(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {//当keycode等于退出事件值时
            onFinish()
            return false
        } else {
            return super.onKeyDown(keyCode, event)
        }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private fun setChannle(i: Int) {
        OrderUtils.setChnnel((i.toString() + "").toByteArray())
        waitdialog = Waitdialog(
                this@RTKSrtting
                , R.style.MDialog
                , 20000
                , getString(R.string.ConnectBLEActivity_tv9) + " " + i + " " + getString(R.string.ConnectBLEActivity_tv10)
                , 0.8
                , 0.33)
        waitdialog?.setCanceledOnTouchOutside(false)
        waitdialog?.setCancelable(false)
        waitdialog?.show()
        checkChannel(i)
    }

    private fun checkChannel(i: Int) {
        if (submit1 != null) {
            submit1?.cancel(true)
        }
        submit1 = threadPoolUtils.submit(Runnable {
            val timeOut = SystemClock.uptimeMillis() + 20000
            var is_chake = true
            while (is_chake) {
                if (SystemClock.uptimeMillis() > timeOut) {
                    handler.post {
                        waitdialog?.dismiss()
                    }
                    ToastUtil.show(getString(R.string.ConnectBLEActivity_tv11))
                    is_chake = false
                    submit1?.cancel(true)
                }
                if (i == BaseActivity.channle) {
                    ToastUtil.show(getString(R.string.ConnectBLEActivity_tv12))
                    handler.post {
                        channelAdapter?.setIndex(i)
                        channelAdapter?.notifyDataSetChanged()
                        waitdialog?.dismiss()
                    }
                    is_chake = false
                    submit1?.cancel(true)
                }
            }
        })
    }

    fun onFinish() {
        finish()
        overridePendingTransition(R.anim.slide_still, R.anim.slide_out_right)
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.base_history -> {
                if (!BlueBaseActivity.mConnected) {
                    ToastUtil.show(getString(R.string.topbar_tv1))
                }
                startActivity(Intent(baseContext, RTKHistoryActivity::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_still)
            }
            R.id.draw_show -> {
                onFinish()
            }
            R.id.selext_connect -> {
                blueConnectPopu?.show()
            }
            R.id.set_base -> {
                if (!BaseActivity.workmoder.equals("手持杖")) {
                    return
                }
                if (!BlueBaseActivity.mConnected) {
                    ToastUtil.show(getString(R.string.topbar_tv1))
                    return
                }

                QMUIDialog.MessageDialogBuilder(this@RTKSrtting)
                        .setTitle(getString(R.string.continuePopu_tv1))
                        .setMessage(getString(R.string.ConnectBLEActivity_tv19))
                        .addAction(getString(R.string.DotActivity_tv9)) { dialog, index ->
                            dialog.dismiss()
                        }
                        .addAction(getString(R.string.continuePopu_tv9)) { dialog, index ->
                            dialog.dismiss()
                            val bytes = byteArrayOf(1)
                            OrderUtils.setBastOrHand(bytes)           //设置到基站
                            waitdialog = Waitdialog(this@RTKSrtting
                                    , R.style.MDialog
                                    , 60000
                                    , getString(R.string.ConnectBLEActivity_tv20)
                                    , 0.8
                                    , 0.33)
                            waitdialog?.setCanceledOnTouchOutside(false)
                            waitdialog?.setCancelable(false)
                            waitdialog?.show()
                            checkWorkModel(0)
                        }
                        .show()
            }
            R.id.set_hand -> {
                if (!BaseActivity.workmoder.equals("基站")) return

                if (!BlueBaseActivity.mConnected) {
                    ToastUtil.show(getString(R.string.topbar_tv1))
                    return
                }
                QMUIDialog.MessageDialogBuilder(this@RTKSrtting)
                        .setTitle(getString(R.string.continuePopu_tv1))
                        .setMessage(getString(R.string.ConnectBLEActivity_tv21))
                        .addAction(getString(R.string.DotActivity_tv9)) { dialog, index ->
                            dialog.dismiss()
                        }
                        .addAction(getString(R.string.continuePopu_tv9)) { dialog, index ->
                            dialog.dismiss()
                            val bytes1 = byteArrayOf(0)
                            OrderUtils.setBastOrHand(bytes1)    //设置到手持杖
                            waitdialog = Waitdialog(this@RTKSrtting
                                    , R.style.MDialog
                                    , 60000
                                    , getString(R.string.ConnectBLEActivity_tv22)
                                    , 0.8
                                    , 0.33)
                            waitdialog?.setCanceledOnTouchOutside(false)
                            waitdialog?.setCancelable(false)
                            waitdialog?.show()
                            checkWorkModel(1)
                        }
                        .show()
            }
            R.id.set_self_location -> {

                if (!BlueBaseActivity.mConnected) {
                    ToastUtil.show(getString(R.string.topbar_tv1))
                    return
                }
                QMUIDialog.MessageDialogBuilder(this@RTKSrtting)
                        .setTitle(getString(R.string.continuePopu_tv1))
                        .setMessage(getString(R.string.ConnectBLEActivity_tv21))
                        .addAction(getString(R.string.DotActivity_tv9)) { dialog, index ->
                            dialog.dismiss()
                        }
                        .addAction(getString(R.string.continuePopu_tv9)) { dialog, index ->
                            dialog.dismiss()
                            getSelfLocation()
                        }
                        .show()
            }
            R.id.save_location -> {
                if (!rtk_state!!.text.equals("7")) {
                    ToastUtil.show(getString(R.string.toast_12))
                    return
                }

                val salt = tev_alt?.text.toString()
                val slat = tev_lat?.text.toString()
                val slon = tev_lon?.text.toString()
                if (TextUtils.isEmpty(slat) or TextUtils.isEmpty(slon) or TextUtils.isEmpty(salt)) {
                    ToastUtil.show(getString(R.string.LocationActivity_tv10))
                    return
                }

                val builder = QMUIDialog.EditTextDialogBuilder(this@RTKSrtting)
                builder.setTitle(getString(R.string.LocationActivity_tv6))
                        .setPlaceholder(getString(R.string.toast_17))
                        .setInputType(InputType.TYPE_CLASS_TEXT)
                        .addAction("取消", { dialog, index ->
                            dialog.dismiss()
                        })
                        .addAction(0, "确定", QMUIDialogAction.ACTION_PROP_NEGATIVE, { dialog, index ->
                            var text = builder.editText.text.toString();
                            if (!TextUtils.isEmpty(text) && text.length > 0) {
                                dialog.dismiss();
                                upload(text, slat, slon, salt)
                            } else {
                                ToastUtil.show(getString(R.string.toast_17))
                            }

                        })
                        .show();
            }

        }
    }

    private fun upload(name: String, lat: String, lon: String, alt: String) {
        submit2 = threadPoolUtils.submit(Runnable {
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val curDate = Date(System.currentTimeMillis())//获取当前时间       
            val time = formatter.format(curDate)
            val url = APP.url + "mapping/?service=Mapping.upBaseStation&token=" + BaseActivity.TOKEN
            val cordBan = CordBan()
            val params = HashMap<String, String>()
            params["tid"] = BaseActivity.teamid.toString()
            params["title"] = name
            params["lat"] = lat
            params["lng"] = lon
            params["height"] = alt
            params["time"] = time
            try {
                val result = HttpUrlTool.submitPostData(url, params, "utf-8")
                Log.e("aa  " + result.toString())
                var json = JSONObject(result)
                val msg = json.getString("msg")
                val ret = json.getString("ret")
                Log.e(msg)
                if (ret != "200") {
                    cordBan.setIsSync("no")
                } else {
                    val data = json.getString("data")
                    val json_data = JSONObject(data)
                    val code = json_data.getString("code")
                    if (code == "0")
                        cordBan.setIsSync("yes")
                    else
                        cordBan.setIsSync("no")
                }

                cordBan.setIsSync("no")
                cordBan.setLat(lat)
                cordBan.setLon(lon)
                cordBan.setAlt(alt)
                cordBan.setTime(time)
                cordBan.setName(name)
                //        Log.e(cordBan.toString());
                coord_sql?.addCoord(cordBan)
            } catch (e: Exception) {
                //            Log.e("", e);
                cordBan.setIsSync("no")
                cordBan.setLat(lat)
                cordBan.setLon(lon)
                cordBan.setAlt(alt)
                cordBan.setTime(time)
                cordBan.setName(name)

                //        Log.e(cordBan.toString());
                coord_sql?.addCoord(cordBan)
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private fun getSelfLocation() {
        tipDialog = QMUITipDialog.Builder(this@RTKSrtting)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord(getString(R.string.SetLocationActivity_tv9))
                .create()
        tipDialog?.show()
        Thread(Runnable {
            OrderUtils.setRtkCorrect()
            SystemClock.sleep(20)
            OrderUtils.setRtkCorrect()
            SystemClock.sleep(1000)
            ggaBan?.setEmpty()
            checkCorrect()
        }).start()
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private fun checkCorrect() {
        OrderUtils.requestGGA()
        val timeOut = SystemClock.uptimeMillis() + 80000
        while (true) {
            if (SystemClock.uptimeMillis() > timeOut) {
                ToastUtil.show(getString(R.string.SetLocationActivity_tv13))
                handler.post({
                    tipDialog?.dismiss()

                })
                return
            }
//            Log.e(ggaBan?.rtk  +"   AA   "+BaseActivity.locationModer)
//            if (ggaBan?.rtk.equals("7") && BaseActivity.locationModer.equals("自校准")) {
            if (ggaBan?.rtk.equals("7")) {
                ToastUtil.show(getString(R.string.SetLocationActivity_tv14))
                handler.post({
                    tipDialog?.dismiss()
                })
                return
            }
            SystemClock.sleep(1000)
        }
    }

    private fun checkWorkModel(i: Int) {
        if (submit != null) {
            submit?.cancel(true)
        }
        submit = threadPoolUtils.submit {
            val timeOut = SystemClock.uptimeMillis() + 60000
            var is_chake = true
            while (is_chake) {
                if (SystemClock.uptimeMillis() > timeOut) {
                    ToastUtil.show(getString(R.string.ConnectBLEActivity_tv13))
                    handler.post({
                        waitdialog?.dismiss()
                    })
                    is_chake = false
                    submit?.cancel(true)
                }
                val workmoder = BaseActivity.workmoder
                if (workmoder == "基站" && i == 0) {
                    ToastUtil.show(getString(R.string.ConnectBLEActivity_tv14))
                    handler.post {
                        waitdialog?.dismiss()
                        setIsBase()
                    }
                    is_chake = false
                    submit?.cancel(true)
                } else if (workmoder == "手持杖" && i == 1) {
                    ToastUtil.show(getString(R.string.ConnectBLEActivity_tv15))
                    handler.post {
                        waitdialog?.dismiss()
                        setisHand()
                    }
                    is_chake = false
                    submit?.cancel(true)
                }
            }
        }
    }

    var handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message?) {
            when (msg?.what) {
                1 -> {

                }
                2 -> {

                }

            }
        }
    }

    var runnable = object : Runnable {
        @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
        override fun run() {
            if (BlueBaseActivity.mConnected) {
                OrderUtils.requestSystems()
                SystemClock.sleep(20)
                OrderUtils.requestChannel()
                SystemClock.sleep(20)
                OrderUtils.requestGGA()

                if (BaseActivity.channle !== -1 && !TextUtils.isEmpty(BaseActivity.workmoder) && waitPopu!!.isShowing() && wait_isShow) {
                    waitPopu?.dismiss()
                    wait_isShow = false
                }

                channelAdapter?.setIndex(BaseActivity.channle)
                channelAdapter?.notifyDataSetChanged()

                if (workmoder == "基站") {
                    setIsBase()
                } else if (workmoder == "手持杖") {
                    setisHand()
                } else {
                    setNull()
                }

                if (ggaBan != null) {
                    rtk_state?.text = ggaBan?.rtk
                    tev_satellites?.text = ggaBan?.satellites
                    tev_hdop?.text = ggaBan?.hdop.toString()
                    tev_lon?.text = ggaBan?.lon.toString()
                    tev_lat?.text = ggaBan?.lat.toString()
                    tev_alt?.text = ggaBan?.alt.toString()
                }
            }

            handler.postDelayed(this, 1000)
        }
    }

    private fun setisHand() {
        set_base?.setClickable(true)
        is_base?.setVisibility(View.INVISIBLE)
        set_hand?.setClickable(false)
        is_hand?.setVisibility(View.VISIBLE)

        base_history?.visibility = View.GONE
        base_bt?.visibility = View.GONE
    }

    private fun setIsBase() {
        set_base?.setClickable(false)
        is_base?.setVisibility(View.VISIBLE)
        set_hand?.setClickable(true)
        is_hand?.setVisibility(View.INVISIBLE)

        base_history?.visibility = View.VISIBLE
        base_bt?.visibility = View.VISIBLE
    }

    private fun setNull() {
        set_base?.setClickable(true)
        is_base?.setVisibility(View.INVISIBLE)
        set_hand?.setClickable(true)
        is_hand?.setVisibility(View.INVISIBLE)

        base_history?.visibility = View.GONE
        base_bt?.visibility = View.GONE
    }


    private val mGattUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            try {
                if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(intent.action)) { //连接一个GATT服务
                    handler.removeCallbacks(runnable)
                    handler.post(runnable)
                    selext_connect?.text = BlueBaseActivity.device.name
                } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(intent.action)) {  //从GATT服务中断开连接
                    handler.removeCallbacks(runnable)
                    selext_connect?.text = getString(R.string.EquipmentActivity_tv5)
                    setNull()
                    channelAdapter?.index = -1
                    channelAdapter?.notifyDataSetChanged()


                    rtk_state?.text = ""
                    tev_satellites?.text = ""
                    tev_hdop?.text = ""
                    tev_lon?.text = ""
                    tev_lat?.text = ""
                    tev_alt?.text = ""

                } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(intent.action)) {   //从服务中接受数据
                    ggaBan = intent.getSerializableExtra(BluetoothLeService.EXTRA_DATA) as GGABan
//                    Log.e(ggaBan.toString())
                }
            } catch (e: Exception) {
            }

        }
    }

}