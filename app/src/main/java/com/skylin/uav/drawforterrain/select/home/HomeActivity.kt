package com.skylin.uav.drawforterrain.select.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.support.annotation.RequiresApi
import android.support.v4.content.FileProvider
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.*
import android.widget.LinearLayout.*
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton
import com.skylin.uav.R
import com.skylin.uav.R.layout.*
import com.skylin.uav.drawforterrain.APP
import com.skylin.uav.drawforterrain.BaseActivity
import com.skylin.uav.drawforterrain.BlueToochActivity
import com.skylin.uav.drawforterrain.HttpUrlTool
import com.skylin.uav.drawforterrain.checksupdata.Ban
import com.skylin.uav.drawforterrain.checksupdata.JsonGenericsSerializator
import com.skylin.uav.drawforterrain.fragment.MapEventsOverlay
import com.skylin.uav.drawforterrain.fragment.OsmFragment
import com.skylin.uav.drawforterrain.nofly.Point
import com.skylin.uav.drawforterrain.select.rtk.RTKSrtting
import com.skylin.uav.drawforterrain.service.BluetoothLeService
import com.skylin.uav.drawforterrain.setting_channel.DownMappingBan
import com.skylin.uav.drawforterrain.setting_channel.GGABan
import com.skylin.uav.drawforterrain.setting_channel.db.DbBan
import com.skylin.uav.drawforterrain.setting_channel.db.DbSQL
import com.skylin.uav.drawforterrain.util.DateUtil
import com.skylin.uav.drawforterrain.util.DensityUtil
import com.skylin.uav.drawforterrain.util.OrderUtils
import com.skylin.uav.drawforterrain.util.ToastUtil
import com.skylin.uav.drawforterrain.views.MarqueeTextView
import com.skylin.uav.drawforterrain.views.Pair
import com.zhy.http.okhttp.OkHttpUtils
import com.zhy.http.okhttp.callback.FileCallBack
import com.zhy.http.okhttp.callback.GenericsCallback
import okhttp3.Call
import okhttp3.Request
import org.json.JSONObject
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import sjj.alog.Log
import java.io.File
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

class HomeActivity : BlueBaseActivity(), View.OnClickListener {

    var sql: DbSQL? = null

    var isClick: Boolean = true

    var downWit: QMUITipDialog? = null

    var threadPool: ExecutorService? = null
    var submit: Future<*>? = null

    var clickList = java.util.ArrayList<DownMappingBan.DataBean.InfoBean>()
    var historyPopu: HistoryPopu? = null

    object statrc_list {
        var hand_list = java.util.ArrayList<DownMappingBan.DataBean.InfoBean>()
        var rtkhand_list = java.util.ArrayList<DownMappingBan.DataBean.InfoBean>()
        var gpshand_list = java.util.ArrayList<DownMappingBan.DataBean.InfoBean>()
        var gpsuav_list = java.util.ArrayList<DownMappingBan.DataBean.InfoBean>()
        var homeBan: HomeBan = HomeBan()
        var mapResum: Boolean = false
    }

    var drawerlayout: DrawerLayout? = null
    var navigatio_ch_rtk: FrameLayout? = null
    var navigatio_ch_gps: FrameLayout? = null
    var navigatio_ch_uav: FrameLayout? = null
    var navigatio_ch_hand: FrameLayout? = null

    var navigatio_text_rtk: TextView? = null
    var navigatio_text_gps: TextView? = null
    var navigatio_text_uav: TextView? = null
    var navigatio_text_hand: TextView? = null

    var navigatio_im_rtk: ImageView? = null
    var navigatio_im_gps: ImageView? = null
    var navigatio_im_uav: ImageView? = null
    var navigatio_im_hand: ImageView? = null

    var navigatio_tv_rtk: ImageButton? = null
    var map_model: ImageButton? = null
    var map_location: ImageButton? = null

    var hand_mode1: TextView? = null
    var device_channel: TextView? = null
    var icon_rtk: LinearLayout? = null
    var mode_name: TextView? = null
    var mapFragment: OsmFragment? = null
    var start_work: QMUIRoundButton? = null
    var lldeleat: LinearLayout? = null
    var deleat_title: TextView? = null
    var start_amend: TextView? = null
    var selext_connect: MarqueeTextView? = null
    var draw_show: ImageView? = null
    var versionNmae_home: TextView? = null
    var renewal: TextView? = null
    var renewal_show: View? = null
    var blueConnectPopu: BlueConnectPopu? = null

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sql = DbSQL(baseContext, "teamid" + BaseActivity.teamid, null, 1)
        if (sql == null) {
            ToastUtil.show(getString(R.string.toast_spl_un))
            finish()
        }

        setContentView(activity_home)

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter())

        initViews()

        downWit = QMUITipDialog.Builder(this@HomeActivity)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord(getString(R.string.LoginActivity_tv1))
                .create()

        getUser(false)
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)

        statrc_list.mapResum = false

        isClick = false
        val map = mapFragment!!.getMap()
        map?.dispatchTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, (map.width / 2).toFloat(), (map.height / 2).toFloat(), 0))
        map?.dispatchTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_UP, (map.width / 2).toFloat(), (map.height / 2).toFloat(), 0))

        setContentView(activity_home)
        initViews()
        recover()
        isClick = true
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private fun initViews() {

        threadPool = Executors.newSingleThreadExecutor()

        findViewById<TextView>(R.id.navigatio_name).setText(BaseActivity.username)
        findViewById<TextView>(R.id.navigatio_tid).setText(BaseActivity.teamName)

        draw_show = findViewById(R.id.draw_show)
        draw_show?.setOnClickListener(this)

        selext_connect = findViewById(R.id.selext_connect)
        selext_connect?.setOnClickListener(this)

        lldeleat = findViewById(R.id.lldeleat)
        deleat_title = findViewById(R.id.deleat_title)
        start_amend = findViewById(R.id.start_amend)
        start_amend?.setOnClickListener(this)

        mapFragment = OsmFragment()
        val bundle = Bundle()
        bundle.putBoolean("is_Click", true);
        mapFragment?.setArguments(bundle);
        val ft = fragmentManager.beginTransaction()
        ft.replace(R.id.home_map_replace, mapFragment)
        ft.commitAllowingStateLoss()
        mapFragment!!.setMapClickListener(object : OsmFragment.MapClickListener {
            override fun onMapClickListener(list: java.util.ArrayList<DownMappingBan.DataBean.InfoBean>?, mapView: MapView?) {
                if (isClick) {
                    clickList.clear()
                    if (list?.size == 0) return
                    list?.forEach {
                        clickList.add(it)
                    }
//                    Log.e(" sss " + clickList.size)
                    runOnUiThread({
                        if (clickList.size == 1) {
                            lldeleat?.visibility = View.VISIBLE

                            if (!BaseActivity.isScreenOriatationPortrait(this@HomeActivity)) {
                                var la = LayoutParams(DensityUtil.dip2px(100f), -1)
                                la.setMargins(DensityUtil.dip2px(10f), 0, 0, 0)
                                start_work?.setLayoutParams(la)
                            }

                            historyPopu?.dismiss()

                            val locale = Locale.getDefault().toString()
                            if (locale.equals("zh_CN")) {
                                deleat_title?.text = clickList[0].area + getString(R.string.recordadapter_tv1) + "  " + clickList[0].title
                            } else {
                                val df = DecimalFormat("#0.000")
                                val format = df.format((clickList[0].area.toDouble() / 15))
                                deleat_title?.text = format + getString(R.string.recordadapter_tv1) + "  " + clickList[0].title
                            }

                            statrc_list.homeBan.pair = Pair<Boolean, DownMappingBan.DataBean.InfoBean>(true, clickList[0])
                        } else {
                            lldeleat?.visibility = View.GONE

                            if (!BaseActivity.isScreenOriatationPortrait(this@HomeActivity)) {
                                start_work?.setLayoutParams(LayoutParams(DensityUtil.dip2px(300f), -1))
                            }

                            historyPopu?.show(clickList)
                            statrc_list.homeBan.pair = Pair<Boolean, DownMappingBan.DataBean.InfoBean>(false, null)

                        }
                    })
                }
            }
        })
        handler.postDelayed({
            mapFragment!!.getMap()?.overlays?.add(MapEventsOverlay(this, 100, 100, object : MapEventsReceiver {

                override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                    //            sjj.alog.Log.e(p.getLatitude() + "  " + p.getLongitude() + "  " + p.getAltitude());
                    statrc_list.homeBan.pp = p
                    return false
                }

                override fun longPressHelper(p: GeoPoint): Boolean {
                    return false
                }

            }))
        }, 2000)


        drawerlayout = findViewById(R.id.drawerlayout)
        drawerlayout?.setDrawerListener(drawListener)

        navigatio_ch_rtk = findViewById(R.id.navigatio_ch_rtk)
        navigatio_ch_gps = findViewById(R.id.navigatio_ch_gps)
        navigatio_ch_uav = findViewById(R.id.navigatio_ch_uav)
        navigatio_ch_hand = findViewById(R.id.navigatio_ch_hand)
        navigatio_ch_rtk?.setOnClickListener(navigatioClick)
        navigatio_ch_gps?.setOnClickListener(navigatioClick)
        navigatio_ch_uav?.setOnClickListener(navigatioClick)
        navigatio_ch_hand?.setOnClickListener(navigatioClick)

        navigatio_text_rtk = findViewById(R.id.navigatio_text_rtk)
        navigatio_text_gps = findViewById(R.id.navigatio_text_gps)
        navigatio_text_uav = findViewById(R.id.navigatio_text_uav)
        navigatio_text_hand = findViewById(R.id.navigatio_text_hand)

        navigatio_im_rtk = findViewById(R.id.navigatio_im_rtk)
        navigatio_im_gps = findViewById(R.id.navigatio_im_gps)
        navigatio_im_uav = findViewById(R.id.navigatio_im_uav)
        navigatio_im_hand = findViewById(R.id.navigatio_im_hand)

        mode_name = findViewById(R.id.mode_name)

        icon_rtk = findViewById(R.id.icon_rtk)
        hand_mode1 = findViewById(R.id.hand_mode1)
        device_channel = findViewById(R.id.device_channel)

        versionNmae_home = findViewById(R.id.versionNmae_home)
        try {
            val manager = APP.getContext().packageManager
            val info = manager.getPackageInfo(APP.getContext().packageName, 0)
            versionNmae_home?.setText(getString(R.string.app_name) + " " + info.versionName)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        renewal_show = findViewById(R.id.renewal_show)
        renewal = findViewById(R.id.renewal)
        renewal?.setOnClickListener(this)

        navigatio_tv_rtk = findViewById(R.id.navigatio_tv_rtk)
        navigatio_tv_rtk?.setOnClickListener(this)
        map_model = findViewById(R.id.map_model)
        map_model?.setOnClickListener(this)
        map_location = findViewById(R.id.map_location)
        map_location?.setOnClickListener(this)

        start_work = findViewById(R.id.start_work)
        start_work?.setOnClickListener(this)

        historyPopu = HistoryPopu(this, start_work, R.id.start_work, sql)
        historyPopu?.setPopuCloseListener(object : HistoryPopu.HistoryPopuClick {
            override fun onPopuClick(view: View?, id: Int, bean: DownMappingBan.DataBean.InfoBean?) {
                if (view?.id == R.id.history_refresh) {
                    getMappingList()
                } else {
                    lldeleat?.visibility = View.VISIBLE

                    if (!BaseActivity.isScreenOriatationPortrait(this@HomeActivity)) {
                        var la = LayoutParams(DensityUtil.dip2px(100f), -1)
                        la.setMargins(DensityUtil.dip2px(10f), 0, 0, 0)
                        start_work?.setLayoutParams(la)
                    }

                    historyPopu?.dismiss()
                    val locale = Locale.getDefault().toString()
                    if (locale.equals("zh_CN")) {
                        deleat_title?.text = bean?.area + getString(R.string.recordadapter_tv1) + "  " + bean?.title
                    } else {
                        val df = DecimalFormat("#0.000")
                        val format = df.format((bean!!.area.toDouble() / 15))
                        deleat_title?.text = format + getString(R.string.recordadapter_tv1) + "  " + bean?.title
                    }
                    HomeActivity.statrc_list.homeBan.pair = Pair<Boolean, DownMappingBan.DataBean.InfoBean>(true, bean);
                }
            }
        })

        blueConnectPopu = BlueConnectPopu(this@HomeActivity, start_work, R.id.start_work)

    }


    private fun recover() {
        Thread {
            while (!statrc_list.mapResum) {
                SystemClock.sleep(5)
            }
            runOnUiThread {
                when (statrc_list.homeBan.mode) {
                    0 -> {
                        modeChange(navigatio_ch_rtk, navigatio_text_rtk, navigatio_im_rtk)
                        mode_name?.text = getString(R.string.startActivity_tab_tv1)
                        navigatio_tv_rtk?.visibility = View.VISIBLE
                        icon_rtk?.visibility = View.VISIBLE
                        selext_connect?.visibility = View.VISIBLE
                        if (BlueBaseActivity.mConnected) {
                            selext_connect?.text = BlueBaseActivity.device.name
                        }
                        drawList(statrc_list.rtkhand_list)
                    }
                    1 -> {
                        modeChange(navigatio_ch_gps, navigatio_text_gps, navigatio_im_gps)
                        mode_name?.text = getString(R.string.startActivity_tab_tv2)
                        navigatio_tv_rtk?.visibility = View.INVISIBLE
                        icon_rtk?.visibility = View.INVISIBLE
                        selext_connect?.visibility = View.VISIBLE
                        if (BlueBaseActivity.mConnected) {
                            selext_connect?.text = BlueBaseActivity.device.name
                        }
                        drawList(statrc_list.gpshand_list)
                    }
                    2 -> {
                        modeChange(navigatio_ch_uav, navigatio_text_uav, navigatio_im_uav)
                        mode_name?.text = getString(R.string.startActivity_tab_tv3)
                        navigatio_tv_rtk?.visibility = View.INVISIBLE
                        icon_rtk?.visibility = View.INVISIBLE
                        selext_connect?.visibility = View.VISIBLE
                        drawList(statrc_list.gpsuav_list)
                    }
                    3 -> {
                        modeChange(navigatio_ch_hand, navigatio_text_hand, navigatio_im_hand)
                        mode_name?.text = getString(R.string.startActivity_tab_tv4)
                        navigatio_tv_rtk?.visibility = View.INVISIBLE
                        icon_rtk?.visibility = View.INVISIBLE
                        selext_connect?.visibility = View.GONE
                        drawList(statrc_list.hand_list)
                    }
                }
                if (statrc_list.homeBan.draw_open) {
                    drawerlayout?.openDrawer(Gravity.START)
                } else {
                    drawerlayout?.closeDrawer(Gravity.START)
                }

                if (statrc_list.homeBan.pair.first) {
                    lldeleat?.visibility = View.VISIBLE

                    if (!BaseActivity.isScreenOriatationPortrait(this@HomeActivity)) {
                        start_work?.setLayoutParams(LayoutParams(DensityUtil.dip2px(100f), -1))
                    }

                    historyPopu?.dismiss()
                    deleat_title?.text = statrc_list.homeBan.pair.second?.area + getString(R.string.recordadapter_tv1) + "  " + statrc_list.homeBan.pair.second?.title
                }

                if (statrc_list.homeBan.pp != null) {
                    mapFragment?.setLoaction(statrc_list.homeBan.pp!!.latitude, statrc_list.homeBan.pp!!.longitude, 0)
                }

                if (statrc_list.homeBan.is_up) {
                    renewal_show?.visibility = View.VISIBLE
                } else {
                    renewal_show?.visibility = View.INVISIBLE
                }
            }
        }.start()

    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
        upMappingList()
        mapFragment!!.setLoaction(null)
        lldeleat?.visibility = View.GONE
        statrc_list.homeBan.pair = Pair<Boolean, DownMappingBan.DataBean.InfoBean>(false, null)


        if (!BaseActivity.isScreenOriatationPortrait(this@HomeActivity)) {
            start_work?.setLayoutParams(LayoutParams(DensityUtil.dip2px(300f), -1))
        }

        if (statrc_list.homeBan.mode == 0 && BlueBaseActivity.mConnected) {
            handler.removeCallbacks(runnbale)
            handler.post(runnbale)
        }
    }

    override fun onPause() {
        super.onPause()

        handler.removeCallbacks(runnbale)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mGattUpdateReceiver)
    }

    var drawListener = object : DrawerLayout.DrawerListener {
        override fun onDrawerStateChanged(newState: Int) {
        }

        override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
        }

        override fun onDrawerClosed(drawerView: View) {
            statrc_list.homeBan.draw_open = false
        }

        override fun onDrawerOpened(drawerView: View) {
            statrc_list.homeBan.draw_open = true
        }

    }

    var navigatioClick = object : View.OnClickListener {
        override fun onClick(v: View?) {
            when (v) {
                navigatio_ch_rtk -> {
                    statrc_list.homeBan.mode = 0
                    modeChange(navigatio_ch_rtk, navigatio_text_rtk, navigatio_im_rtk)
                    mode_name?.text = getString(R.string.startActivity_tab_tv1)
                    navigatio_tv_rtk?.visibility = View.VISIBLE
                    icon_rtk?.visibility = View.VISIBLE
                    drawList(statrc_list.rtkhand_list)
                    selext_connect?.visibility = View.VISIBLE
                    selext_connect?.text = getString(R.string.EquipmentActivity_tv5)

                    disConnectUav()
                    disConnectBlueFour()
                    lldeleat?.visibility = View.GONE

                    if (!BaseActivity.isScreenOriatationPortrait(this@HomeActivity)) {
                        start_work?.setLayoutParams(LayoutParams(DensityUtil.dip2px(300f), -1))
                    }
                    statrc_list.homeBan.pair = Pair<Boolean, DownMappingBan.DataBean.InfoBean>(false, null)

                }
                navigatio_ch_gps -> {
                    statrc_list.homeBan.mode = 1
                    modeChange(navigatio_ch_gps, navigatio_text_gps, navigatio_im_gps)
                    mode_name?.text = getString(R.string.startActivity_tab_tv2)
                    navigatio_tv_rtk?.visibility = View.INVISIBLE
                    icon_rtk?.visibility = View.INVISIBLE
                    drawList(statrc_list.gpshand_list)
                    selext_connect?.visibility = View.VISIBLE
                    selext_connect?.text = getString(R.string.EquipmentActivity_tv5)


                    disConnectUav()
                    disConnectBlueFour()
                    lldeleat?.visibility = View.GONE

                    if (!BaseActivity.isScreenOriatationPortrait(this@HomeActivity)) {
                        start_work?.setLayoutParams(LayoutParams(DensityUtil.dip2px(300f), -1))
                    }
                    statrc_list.homeBan.pair = Pair<Boolean, DownMappingBan.DataBean.InfoBean>(false, null)


                }
                navigatio_ch_uav -> {
                    statrc_list.homeBan.mode = 2
                    modeChange(navigatio_ch_uav, navigatio_text_uav, navigatio_im_uav)
                    mode_name?.text = getString(R.string.startActivity_tab_tv3)
                    navigatio_tv_rtk?.visibility = View.INVISIBLE
                    icon_rtk?.visibility = View.INVISIBLE
                    drawList(statrc_list.gpsuav_list)
                    selext_connect?.visibility = View.VISIBLE
                    selext_connect?.text = getString(R.string.EquipmentActivity_tv5)


                    disConnectUav()
                    disConnectBlueFour()
                    lldeleat?.visibility = View.GONE

                    if (!BaseActivity.isScreenOriatationPortrait(this@HomeActivity)) {
                        start_work?.setLayoutParams(LayoutParams(DensityUtil.dip2px(300f), -1))
                    }
                    statrc_list.homeBan.pair = Pair<Boolean, DownMappingBan.DataBean.InfoBean>(false, null)


                }
                navigatio_ch_hand -> {
                    statrc_list.homeBan.mode = 3
                    modeChange(navigatio_ch_hand, navigatio_text_hand, navigatio_im_hand)
                    mode_name?.text = getString(R.string.startActivity_tab_tv4)
                    navigatio_tv_rtk?.visibility = View.INVISIBLE
                    icon_rtk?.visibility = View.INVISIBLE
                    drawList(statrc_list.hand_list)
                    selext_connect?.visibility = View.GONE

                    disConnectUav()
                    disConnectBlueFour()
                    lldeleat?.visibility = View.GONE

                    if (!BaseActivity.isScreenOriatationPortrait(this@HomeActivity)) {
                        start_work?.setLayoutParams(LayoutParams(DensityUtil.dip2px(300f), -1))
                    }
                    statrc_list.homeBan.pair = Pair<Boolean, DownMappingBan.DataBean.InfoBean>(false, null)


                }
            }
            clickList.clear()
            historyPopu?.dismiss()
        }
    }

    private fun modeChange(layout: FrameLayout?, text: TextView?, im: ImageView?) {

        navigatio_ch_rtk?.isClickable = true
        navigatio_ch_gps?.isClickable = true
        navigatio_ch_uav?.isClickable = true
        navigatio_ch_hand?.isClickable = true
        navigatio_ch_rtk?.setBackgroundResource(R.color.green3)
        navigatio_ch_gps?.setBackgroundResource(R.color.green3)
        navigatio_ch_uav?.setBackgroundResource(R.color.green3)
        navigatio_ch_hand?.setBackgroundResource(R.color.green3)

        navigatio_text_rtk?.setTextColor(resources.getColor(R.color.white))
        navigatio_text_gps?.setTextColor(resources.getColor(R.color.white))
        navigatio_text_uav?.setTextColor(resources.getColor(R.color.white))
        navigatio_text_hand?.setTextColor(resources.getColor(R.color.white))


        navigatio_im_rtk?.setImageResource(R.mipmap.rtk_device)
        navigatio_im_gps?.setImageResource(R.mipmap.gps_device)
        navigatio_im_uav?.setImageResource(R.mipmap.gps_plane)
        navigatio_im_hand?.setImageResource(R.mipmap.manual)

        layout?.isClickable = false
        layout?.setBackgroundResource(R.color.green4)
        text?.setTextColor(resources.getColor(R.color.green3))
        when (im) {
            navigatio_im_rtk -> im?.setImageResource(R.mipmap.rtk_device_green)
            navigatio_im_gps -> im?.setImageResource(R.mipmap.gps_device_green)
            navigatio_im_uav -> im?.setImageResource(R.mipmap.gps_plane_green)
            navigatio_im_hand -> im?.setImageResource(R.mipmap.manual_green)
        }
        hand_mode1?.setText(BaseActivity.workmoder)
        device_channel?.setText(BaseActivity.channle.toString())


    }

    fun onDeleatBackClick(v: View?) {
        setHistory()
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    override fun onClick(v: View?) {
        when (v) {
            renewal -> {
                getUser(true)
            }
            draw_show -> {
                drawerlayout?.openDrawer(Gravity.START)
            }
            start_amend -> {
                when (statrc_list.homeBan.mode) {
                    0 -> {
                        if (!BlueBaseActivity.mConnected) {
                            ToastUtil.show(getString(R.string.ParticularsActivity_tv3))
                            return
                        }
                        if (!BaseActivity.workmoder.equals("手持杖")) {
                            ToastUtil.show(getString(R.string.ParticularsActivity_tv2))
                            return
                        }
                        ReplyActuvuty.startReplyActivity(this@HomeActivity, true, statrc_list.homeBan.pair.second)
                    }
                    1 -> {
                        if (!BlueBaseActivity.mConnected) {
                            ToastUtil.show(getString(R.string.ParticularsActivity_tv3))
                            return
                        }
                        ReplyActuvuty.startReplyActivity(this@HomeActivity, true, statrc_list.homeBan.pair.second)
                    }
                    2 -> {
                        if (!BlueBaseActivity.mConnected) {
                            ToastUtil.show(getString(R.string.UAVActivity_tv13))
                            return
                        }
                        ReplyActuvuty.startReplyActivity(this@HomeActivity, true, statrc_list.homeBan.pair.second)
                    }
                    3 -> {
                        ReplyActuvuty.startReplyActivity(this@HomeActivity, true, statrc_list.homeBan.pair.second)
                    }
                }

            }
            map_model -> {
                mapFragment!!.setMapMopType()
            }
            map_location -> {
                mapFragment!!.setLoaction(null)
            }
            navigatio_tv_rtk -> {
                if (!BlueBaseActivity.mConnected) {
                    ToastUtil.show(getString(R.string.ParticularsActivity_tv3))
                    return
                }
                startActivity(Intent(this@HomeActivity, RTKSrtting::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_still)
            }
            start_work -> {
                when (statrc_list.homeBan.mode) {
                    0 -> {
                        if (!BlueBaseActivity.mConnected) {
                            ToastUtil.show(getString(R.string.ParticularsActivity_tv3))
                            return
                        }
                        if (!BaseActivity.workmoder.equals("手持杖")) {
                            ToastUtil.show(getString(R.string.ParticularsActivity_tv2))
                            return
                        }
                        ReplyActuvuty.startReplyActivity(this@HomeActivity, false, null)
                    }
                    1 -> {
                        if (!BlueBaseActivity.mConnected) {
                            ToastUtil.show(getString(R.string.ParticularsActivity_tv3))
                            return
                        }
                        ReplyActuvuty.startReplyActivity(this@HomeActivity, false, null)
                    }
                    2 -> {
                        if (!BlueBaseActivity.mConnected) {
                            ToastUtil.show(getString(R.string.UAVActivity_tv13))
                            return
                        }
                        ReplyActuvuty.startReplyActivity(this@HomeActivity, false, null)
                    }
                    3 -> {
                        ReplyActuvuty.startReplyActivity(this@HomeActivity, false, null)
                    }
                }
            }
            selext_connect -> {

                when (statrc_list.homeBan.mode) {
                    0 -> {
                        blueConnectPopu!!.show()
                    }
                    1 -> {
                        blueConnectPopu!!.show()
                    }
                    2 -> {
                        connectUav()
                    }
                    3 -> {
                    }
                }
            }
        }
    }

    private fun setHistory() {
        statrc_list.homeBan.pair = Pair<Boolean, DownMappingBan.DataBean.InfoBean>(false, null)
        lldeleat?.visibility = View.GONE

        if (!BaseActivity.isScreenOriatationPortrait(this@HomeActivity)) {
            start_work?.setLayoutParams(LayoutParams(DensityUtil.dip2px(300f), -1))
        }

        statrc_list.homeBan.pair = Pair<Boolean, DownMappingBan.DataBean.InfoBean>(false, null)

        clickList.clear()
        when (statrc_list.homeBan.mode) {
            0 -> {
                statrc_list.rtkhand_list.forEach { it ->
                    clickList.add(it)
                }
            }
            1 -> {
                statrc_list.gpshand_list.forEach { it ->
                    clickList.add(it)
                }
            }
            2 -> {
                statrc_list.gpsuav_list.forEach { it ->
                    clickList.add(it)
                }
            }
            3 -> {
                statrc_list.hand_list.forEach { it ->
                    clickList.add(it)
                }
            }
        }
        historyPopu?.show(clickList)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {//当keycode等于退出事件值时
            if (drawerlayout!!.isDrawerOpen(Gravity.START)) {
                drawerlayout?.closeDrawer(Gravity.START)
            } else {
                toQuitTheApp()
            }
            return false
        } else {
            return super.onKeyDown(keyCode, event)
        }
    }


    var isExit = false
    fun toQuitTheApp() {
        if (isExit) {
            System.exit(0)// 使虚拟机停止运行并退出程序
        } else {
            isExit = true
            handler.postDelayed(Runnable { isExit = false }, 2000)// 2秒后发送消息
            ToastUtil.show(getString(R.string.LoginActivity_tv14))
        }
    }

    var handler = Handler() {
        when (it.what) {

        }
    }

    var runnbale = object : Runnable {
        @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
        override fun run() {
            if (statrc_list.homeBan.mode == 0 && BlueBaseActivity.mConnected) {
                OrderUtils.requestChannel()
                SystemClock.sleep(10)
                OrderUtils.requestSystems()
                SystemClock.sleep(10)

                if (BaseActivity.workmoder.equals("手持杖")) {
                    hand_mode1?.setText(getString(R.string.hand_mode1))
                } else if (BaseActivity.workmoder.equals("基站")) {
                    hand_mode1?.setText(getString(R.string.hand_mode2))
                } else {
                    hand_mode1?.setText(BaseActivity.workmoder)
                }

                device_channel?.setText(BaseActivity.channle.toString())
            }
            handler.postDelayed(this, 1000)
        }

    }


    private fun upMappingList() {
        val url = APP.url + "/work2.0/Public/?service=Mapping.upMapping&token=" + BaseActivity.TOKEN
        Thread(Runnable {
            try {
                sql?.queryAll()?.forEach {
                    if (it.sync == "false") {
                        val params = HashMap<String, String>()
                        if (!TextUtils.isEmpty(it.id)) {
                            params["Id"] = it.id
                        }
                        params["title"] = it.title
                        params["tid"] = it.tid
                        params["province"] = it.province
                        params["city"] = it.city
                        params["district"] = it.district
                        params["mappingBorder"] = it.mappingBorder
                        params["obstacleBorder"] = it.obstacleBorder
                        params["startBorder"] = it.startBorder
                        params["area"] = it.area
                        params["parentId"] = it.parentId
                        params["type"] = it.type
                        val result = HttpUrlTool.submitPostData(url, params, "utf-8")
                        var json: JSONObject? = null
                        //                            Log.e("pp " + result);
                        json = JSONObject(result)
                        val msg = json.getString("msg")
                        val ret = json.getString("ret")
                        val data = json.getString("data")
                        //                            Log.e(msg + "   " + ret + "  " + data);
                        if (ret == "200") {
                            if (data == "[1]") {
                                //                                    Log.e("修改成功");
                            } else if (data == "[0]") {
                                //                                    Log.e("修改失败");
                            } else {
                                //                                    Log.e("上传成功  Id=" + data);
                                val split = data.split("\"".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                                it.id = split[1]
                            }
                            it.sync = "true"
                            sql?.updateBan(it)
                        }
                    }
                }
                getMappingList()
            } catch (e: Exception) {
                getMappingList()
            }
        }).start()
    }

    fun getMappingList() = try {
        val url = APP.url + "/work2.0/Public/?service=Mapping.getMappingList&token=" + BaseActivity.TOKEN
        OkHttpUtils
                .post()//
                .url(url)//
                .addParams("type", "all")  //all  self   团队 自己
                .addParams("page", "1")
                .addParams("tid", BaseActivity.teamid.toString() + "")
                .build()//
                .execute(object : GenericsCallback<DownMappingBan>(JsonGenericsSerializator()) {
                    override fun onError(call: Call, e: Exception, id: Int) {
//                        Log.e("联网失败 " + call.toString() + " e " + e.toString() + " id " + id)
                        sort()
                    }

                    override fun onResponse(ban: DownMappingBan, id: Int) {
//                        Log.e(ban)
                        val info = ban.data.info
                        if (ban.ret == 200) {
                            info.forEach {
                                it.sync = true
                                val n_dbBan1 = sql!!.queryPointById(it.id)
                                if (n_dbBan1 == null) {
                                    sql?.addBan(DbBan(it, DateUtil.getCurDate("yyyy-MM-dd HH:mm:ss")))
                                } else {
                                    if (n_dbBan1.getUpdate_time() != it.update_time)
                                        sql?.updateBan(DbBan(it, n_dbBan1.getInsertTime()))
                                }
                            }
                        }

                        sort()
                    }
                })
    } catch (e: Exception) {
//        Log.e("", e)
    }

    private fun sort() {
        statrc_list.hand_list.clear()
        statrc_list.rtkhand_list.clear()
        statrc_list.gpshand_list.clear()
        statrc_list.gpsuav_list.clear()

        sql?.queryAll()?.forEach {

            when (it.getType()) {
                "hand" -> statrc_list.hand_list.add(DownMappingBan.DataBean.InfoBean(it))
                "border" -> statrc_list.rtkhand_list.add(DownMappingBan.DataBean.InfoBean(it))
                "gps_hand" -> statrc_list.gpshand_list.add(DownMappingBan.DataBean.InfoBean(it))
                "gps_uav" -> statrc_list.gpsuav_list.add(DownMappingBan.DataBean.InfoBean(it))
                else -> {
                }
            }
        }
        listsort(statrc_list.hand_list)
        listsort(statrc_list.rtkhand_list)
        listsort(statrc_list.gpshand_list)
        listsort(statrc_list.gpsuav_list)

        when (statrc_list.homeBan.mode) {
            0 -> {
                drawList(statrc_list.rtkhand_list)
            }
            1 -> {
                drawList(statrc_list.gpshand_list)
            }
            2 -> {
                drawList(statrc_list.gpsuav_list)
            }
            3 -> {
                drawList(statrc_list.hand_list)
            }
        }
        if (historyPopu?.isShow()!!) {
            setHistory()
        }

    }

    private fun listsort(list: ArrayList<DownMappingBan.DataBean.InfoBean>) {
        try {
            for (i in 0 until list.size - 1) {
                for (j in i + 1 until list.size) {
                    val a1 = Integer.parseInt(list[i].id)
                    val a2 = Integer.parseInt(list[j].id)
                    if (a1 < a2) {//如果队前日期靠前，调换顺序  
                        val infoBean = list[i]
                        list[i] = list[j]
                        list[j] = infoBean
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("", e)
        }
    }

    fun drawList(point_list: ArrayList<DownMappingBan.DataBean.InfoBean>) {

        if (submit != null) {
            submit?.cancel(true)
        }
        submit = threadPool?.submit(Runnable {
            try {
                val draw_list = java.util.ArrayList<java.util.ArrayList<Point>>()
                point_list.forEach { it ->
                    val boundary_List = java.util.ArrayList<Point>()
                    it.mappingBorder.forEach {
                        boundary_List.add(Point(it[1], it[0]))
                    }
                    draw_list.add(boundary_List)
                }
                mapFragment?.drawList(draw_list)
            } catch (e: Exception) {
                //      Log.e("", e);
            }
        })
    }

    companion object {
        @JvmStatic
        fun getList(): java.util.ArrayList<DownMappingBan.DataBean.InfoBean>? {
            when (statrc_list.homeBan.mode) {

                0 -> {
                    return statrc_list.rtkhand_list
                }
                1 -> {
                    return statrc_list.gpshand_list
                }
                2 -> {
                    return statrc_list.gpsuav_list
                }
                3 -> {
                    return statrc_list.hand_list
                }
                else -> {
                    return null
                }
            }
        }
    }

    private val mGattUpdateReceiver = object : BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        override fun onReceive(context: Context, intent: Intent) {
            if (BluetoothLeService.ACTION_GATT_CONNECTED == intent.action) { //连接一个GATT服务

                when (statrc_list.homeBan.mode) {
                    0 -> {
                        handler.removeCallbacks(runnbale)
                        handler.post(runnbale)
                        selext_connect?.text = BlueBaseActivity.device.name
                    }
                    1 -> {
                        selext_connect?.text = BlueBaseActivity.device.name
                    }
                    2 -> {
                        selext_connect?.text = getString(R.string.EquipmentActivity_tv3)
                    }
                }


            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED == intent.action) {  //从GATT服务中断开连接
                handler.removeCallbacks(runnbale)
                selext_connect?.text = getString(R.string.EquipmentActivity_tv5)
                BaseActivity.channle = -1
                BaseActivity.workmoder = ""
                hand_mode1?.setText("")
                device_channel?.setText("")

            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED == intent.action) {  //发现有可支持的服务

            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE == intent.action) {   //从服务中接受数据
            }
        }
    }


    fun getUser(b: Boolean) {
        val url = APP.url + "/work2.0/Public/store/?service=App.getAppInfo&id=2"
        OkHttpUtils
                .post()
                .url(url)
                //                .addParams("name","植保测绘")
                .build()
                .execute(object : GenericsCallback<Ban>(JsonGenericsSerializator()) {
                    override fun onError(call: Call, e: Exception, id: Int) {
                    }

                    override fun onResponse(response: Ban, id: Int) {
                        if (response.ret == 200) {
                            //                            LogUtils.e(response);
                            for (i in 0 until response.data.size) {
                                val versionCode = response.data[i].versionCode
                                if (hasUpdate(Integer.parseInt(versionCode))) {
                                    try {
                                        renewal_show?.visibility = View.VISIBLE
                                        statrc_list.homeBan.is_up = true
                                        if (b) {
                                            val dialog = AlertDialog.Builder(this@HomeActivity)
                                                    .setTitle(getString(R.string.LoginActivity_tv9))
                                                    .setMessage(getString(R.string.LoginActivity_tv10))
                                                    .setPositiveButton(getString(R.string.continuePopu_tv9)) { dialog, which ->
                                                        dialog.dismiss()
                                                        downFile()
                                                    }
                                                    .setNegativeButton(getString(R.string.DotActivity_tv9)) { dialog, which ->
                                                        dialog.dismiss()
                                                    }
                                                    .create()
                                            dialog.show()
                                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(resources.getColor(R.color.qmui_config_color_blue))
                                            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(resources.getColor(R.color.qmui_config_color_blue))
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                    return
                                } else if (i == response.data.size - 1) {
                                    renewal_show?.visibility = View.INVISIBLE
                                    return
                                }
                            }
                        }
                    }
                })
    }

    fun hasUpdate(version: Int): Boolean {
        try {
            val manager = APP.getContext().packageManager
            val info = manager.getPackageInfo(APP.getContext().packageName, 0)
            return version > info.versionCode
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun downFile() {
        downWit?.show()
        val url = APP.url + "/work2.0/Public/store/?service=App.downApp&id=2"
        val patch = "sdcard/Android/data/com.skylin.uav.nongtiancehui/cache/download/"
        OkHttpUtils
                .get()
                .url(url)
                .build()
                .execute(object : FileCallBack(patch, "农田测绘.apk") {

                    override fun onBefore(request: Request, id: Int) {}

                    override fun inProgress(progress: Float, total: Long, id: Int) {
                        //                        mProgressBar.setProgress((int) progress*-1);
                        //                        Log.e("ok", "inProgress :" + (progress * 100) + "  " + total);
                        val f = progress * 100
                        val df = DecimalFormat("#0.0")
                        val s = df.format(f.toDouble()) + "%"
                    }

                    override fun onError(call: Call, e: Exception, id: Int) {
                        //                        sjj.alog.Log.e("error  " + e.getMessage());
                    }

                    override fun onResponse(file: File, id: Int) {
                        try {
                            downWit?.dismiss()
                            file.canRead()
                            val intent = Intent(Intent.ACTION_VIEW)
                            val data: Uri
                            // 判断版本大于等于7.0
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                // 是在清单文件中配置的authorities
                                data = FileProvider.getUriForFile(baseContext, "com.skylin.uav.nongtiancehui.fileprovider", file)
                                // 给目标应用一个临时授权
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            } else {
                                data = Uri.fromFile(file)
                            }
                            intent.setDataAndType(data, "application/vnd.android.package-archive")
                            startActivity(intent)
                            System.exit(0)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    }
                })
    }

}