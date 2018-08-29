package com.skylin.uav.drawforterrain.select.rtk

import android.annotation.TargetApi
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.support.annotation.RequiresApi
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.blankj.utilcode.util.ThreadPoolUtils
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog
import com.qmuiteam.qmui.widget.pullRefreshLayout.QMUIPullRefreshLayout
import com.skylin.uav.R
import com.skylin.uav.drawforterrain.APP
import com.skylin.uav.drawforterrain.BaseActivity
import com.skylin.uav.drawforterrain.HttpUrlTool
import com.skylin.uav.drawforterrain.select.home.BlueBaseActivity
import com.skylin.uav.drawforterrain.select.home.BlueConnectPopu
import com.skylin.uav.drawforterrain.select.home.LayoutManager
import com.skylin.uav.drawforterrain.select.rtk.coord_db.Coord_SQL
import com.skylin.uav.drawforterrain.select.rtk.coord_db.CordBan
import com.skylin.uav.drawforterrain.select.rtk.coord_db.SyncBan
import com.skylin.uav.drawforterrain.service.BluetoothLeService
import com.skylin.uav.drawforterrain.setting_channel.GGABan
import com.skylin.uav.drawforterrain.util.GsonUtil
import com.skylin.uav.drawforterrain.util.OrderUtils
import com.skylin.uav.drawforterrain.util.ToastUtil
import com.skylin.uav.drawforterrain.views.MarqueeTextView
import com.zhy.adapter.recyclerview.CommonAdapter
import com.zhy.adapter.recyclerview.base.ViewHolder
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import sjj.alog.Log
import java.sql.Array
import java.util.*
import java.util.concurrent.Future

class RTKHistoryActivity : BaseActivity(), View.OnClickListener {

    private var recyclerView: RecyclerView? = null
    private var refresh: QMUIPullRefreshLayout? = null
    private var commonAdapter: CommonAdapter<CordBan>? = null
    private val list = ArrayList<CordBan>()

    var draw_show: ImageView? = null
    var selext_connect: MarqueeTextView? = null
    var blueConnectPopu: BlueConnectPopu? = null


    private var tipDialog: QMUITipDialog? = null

    val threadPoolUtils = ThreadPoolUtils(ThreadPoolUtils.SingleThread, 0)
    var submit: Future<*>? = null
    var submit1: Future<*>? = null
    var submit2: Future<*>? = null

    var ggaBan = GGABan()
    var handler = Handler() {
        when (it.what) {
        }
    }

    var runnable = object :Runnable{
        override fun run() {
            OrderUtils.requestGGA()

            handler.postDelayed(this,1000)
        }

    }

    private var coord_sql: Coord_SQL? = null


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_rtkhistory)

        coord_sql = Coord_SQL(baseContext, "tid" + teamid, null, 1)

        registerReceiver(mGattUpdateReceiver, BlueBaseActivity.makeGattUpdateIntentFilter())


        initViews()

        sort()
    }


    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        setContentView(R.layout.activity_rtkhistory)

        initViews()
    }

    override fun onResume() {
        super.onResume()
        upload()
        handler.post { runnable }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
        unregisterReceiver(mGattUpdateReceiver)

    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun initViews() {
        tipDialog = QMUITipDialog.Builder(this)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord(getString(R.string.HistoryActivity_tv1))
                .create()

        draw_show = findViewById(R.id.draw_show)
        draw_show?.setImageResource(R.mipmap.ic_return)
        draw_show?.setOnClickListener(this)
        selext_connect = findViewById(R.id.selext_connect)
        selext_connect?.setOnClickListener(this)
        if (BlueBaseActivity.mConnected) {
            selext_connect?.text = BlueBaseActivity.device.name
        }
        findViewById<LinearLayout>(R.id.icon_rtk).visibility = View.GONE
        blueConnectPopu = BlueConnectPopu(this@RTKHistoryActivity, selext_connect, R.id.selext_connect)


        initRecycle()

        initRefresh()
    }


    private fun initRecycle() {
        commonAdapter = object : CommonAdapter<CordBan>(this@RTKHistoryActivity, R.layout.item_cordban, list) {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
            @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
            override fun convert(holder: ViewHolder, cordBan: CordBan, position: Int) {
                holder.setText(R.id.item_name, cordBan.name)
                holder.setText(R.id.item_time, cordBan.time)
                holder.setText(R.id.item_adress, cordBan.province + "-" + cordBan.city + "-" + cordBan.district)

                val lon = cordBan.lon
                val lat = cordBan.lat
                val alt = cordBan.alt
                if (!TextUtils.isEmpty(lon))
                    holder.setText(R.id.item_lon, lon)
                if (!TextUtils.isEmpty(lat))
                    holder.setText(R.id.item_lat, lat)
                if (!TextUtils.isEmpty(alt))
                    holder.setText(R.id.item_alt, alt)

                val im = holder.getView<ImageView>(R.id.item_sync_onley)
                if (cordBan.isSync.equals("yes"))
                    im.setVisibility(View.INVISIBLE)
                else
                    im.setVisibility(View.VISIBLE)

                holder.setOnClickListener(R.id.item_setting, View.OnClickListener {
                    if (!BlueBaseActivity.mConnected) {
                        ToastUtil.show(getString(R.string.topbar_tv1))
                        return@OnClickListener
                    }

                    QMUIDialog.MessageDialogBuilder(this@RTKHistoryActivity)
                            .setTitle(getString(R.string.continuePopu_tv1))
                            .setMessage(getString(R.string.HistoryActivity_tv4))
                            .addAction(getString(R.string.DotActivity_tv9)) { dialog, index ->
                                dialog.dismiss()
                            }
                            .addAction(getString(R.string.continuePopu_tv9)) { dialog, index ->
                                dialog.dismiss()
                                val s = "$lat,$lon,$alt,"
                                OrderUtils.setDatas(s.toByteArray())
                                tipDialog?.show()
                                checkGGA(lat, lon, alt)
                            }
                            .show()
                })
            }
        }
        recyclerView = findViewById(R.id.listview_history)
        recyclerView?.setLayoutManager(LayoutManager(this@RTKHistoryActivity))
        recyclerView?.setAdapter(commonAdapter)
        recyclerView?.setHasFixedSize(true)
    }

    private fun initRefresh() {
        refresh = findViewById(R.id.refresh)
        refresh?.setOnPullListener(object : QMUIPullRefreshLayout.OnPullListener {
            override fun onMoveTarget(offset: Int) {

            }

            override fun onMoveRefreshView(offset: Int) {

            }

            override fun onRefresh() {
                upload()
            }
        })

    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.selext_connect -> {
                blueConnectPopu?.show()
            }
            R.id.draw_show -> {
                onFinish()
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return super.onKeyDown(keyCode, event)
    }


    fun onFinish() {
        finish()
        overridePendingTransition(R.anim.slide_still, R.anim.slide_out_right)
    }


    private fun checkGGA(lat: String, lon: String, alt: String) {
        submit = threadPoolUtils.submit {
            val timeOut = SystemClock.uptimeMillis() + 30000
            var is_while = true
            while (is_while) {
                if (SystemClock.uptimeMillis() > timeOut) {
                    handler.post({ tipDialog?.dismiss() })
                    ToastUtil.show(getString(R.string.HistoryActivity_tv5))
                    is_while = false
                    submit?.cancel(true)
                }

                SystemClock.sleep(1000)
//                Log.e(lat +"AA"+lon+"AA"+alt)
//                Log.e(ggaBan?.lat.toString()+"AA"+ggaBan?.lon.toString()+"AA"+ggaBan?.alt.toString())
//Log.e(lat.equals(ggaBan?.lat.toString()))
//Log.e(lon.equals(ggaBan?.lon.toString()))
//Log.e(alt.equals(ggaBan?.alt).toString())
//Log.e("7".equals(ggaBan?.rtk))
                if (ggaBan.getRtk().equals("7")
                        && ggaBan.getLat().toString().equals(lat)
                        && ggaBan.getLon().toString().equals(lon)
                        && ggaBan.getAlt().toString().equals(alt)) {
                    ToastUtil.show(getString(R.string.HistoryActivity_tv6))
                    handler.post({ tipDialog?.dismiss() })
                    onFinish()
                    is_while = false
                    submit?.cancel(true)
                }
            }

        }
    }

    private val mGattUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            try {
                if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(intent.action)) { //连接一个GATT服务

                    selext_connect?.text = BlueBaseActivity.device.name
                } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(intent.action)) {  //从GATT服务中断开连接
                    selext_connect?.text = getString(R.string.EquipmentActivity_tv5)

                } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(intent.action)) {   //从服务中接受数据
                    ggaBan = intent.getSerializableExtra(BluetoothLeService.EXTRA_DATA) as GGABan
                }
            } catch (e: Exception) {
            }

        }
    }

    private fun upload() {

        submit1 = threadPoolUtils.submit{
            val banList = coord_sql?.queryAllCoord_Ban()
//            Log.e((coord_sql == null).toString()+"  "+(banList == null).toString()+"   "+ Arrays.asList(banList))
            if (banList == null) {
                handler.post { download() }
            } else if (banList.size != 0) {

                banList.forEach {
                    //                LogUtils.e(list.get(i).getIsSync().equals("no"));
                    if (it.isSync.equals("no")) {
                        val url = APP.url + "/work2.0/Public/"+"mapping/?service=Mapping.upBaseStation&token=" + BaseActivity.TOKEN
                        val params = HashMap<String, String>()
                        params["tid"] = BaseActivity.teamid.toString()
                        params["title"] = it.name
                        params["lat"] = it.lat
                        params["lng"] = it.lon
                        params["height"] = it.alt
                        params["time"] = it.time
                        val result = HttpUrlTool.submitPostData(url, params, "utf-8")
//                        Log.e(result);
                        try {
                            var json = JSONObject(result)
                            val msg = json.getString("msg")
                            val ret = json.getString("ret")
                            if (ret != "200") {
//                                ToastUtil.show(msg)
                                Log.e(msg);
                            } else {
                                val data = json.getString("data")
                                val json_data = JSONObject(data)
                                val code = json_data.getString("code")
                                if (code == "0") {
                                    val cordBan = it
                                    cordBan.isSync = "yes"
                                    coord_sql?.updateCoord(cordBan)
                                }
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            handler.post { download() }
                        }
                    }
                }
                handler.postDelayed({ download() }, 2000)// 2秒后发送消息
            } else {
                handler.post { download() }
            }
        }
    }

    private fun download() {
        submit2 = threadPoolUtils.submit({
            try {
                val url = APP.url +"/work2.0/Public/"+ "mapping/?service=Mapping.getBaseStationList&token=" + BaseActivity.TOKEN
                val params = HashMap<String, String>()
                params["tid"] = BaseActivity.teamid.toString()
                val result = HttpUrlTool.submitPostData(url, params, "utf-8")
//                Log.e(result)
                val json = JSONObject(result)
                val msg = json.getString("msg")
                val ret = json.getString("ret")
                if (ret != "200") {
//                    ToastUtil.show(msg)
                    Log.e("msg")
                } else {
                    val data = json.getString("data")
                    val jsonData = JSONObject(data)
                    val info = jsonData.getString("info")
                    val jsonInfo = JSONArray(info)
                    for (i in 0 until jsonInfo.length()) {
                        val syncBan = GsonUtil.parseJsonWithGson(jsonInfo.get(i).toString(), SyncBan::class.java)
                        val coord_ban = CordBan(syncBan)
                        coord_ban.isSync = "yes"
                        if (coord_sql?.queryCoord_BanByName(syncBan.getTitle()) == null || coord_sql?.queryCoord_BanByTime(syncBan.getCreateTime()) == null) {
                            coord_sql?.addCoord(coord_ban)
                        } else {
                            val banList = coord_sql!!.queryAllCoord_Ban()
                            banList.forEach {
                                if (it.name.equals(syncBan.getTitle()) && it.time.equals(syncBan.getCreateTime())) {
                                    if (it.isSync.equals("no")) {
                                        coord_sql?.updateCoord(coord_ban)
                                    }
                                }
                            }
                        }
                    }
                }
                handler.post({
                    sort()
                    refresh?.finishRefresh()
                })
            } catch (e: Exception) {
                //                    Log.e("", e);
                handler.post({
                    sort()
                    refresh?.finishRefresh()
                })
            }
        })
    }

    private fun sort() {
        try {
            val banList = coord_sql!!.queryAllCoord_Ban()
            for (i in 0 until banList.size - 1) {
                for (j in i + 1 until banList.size) {
                    val a1 = banList[i]._id
                    val a2 = banList[j]._id
                    if (a1 < a2) {//如果队前日期靠前，调换顺序  
                        val infoBean = banList[i]
                        banList[i] = banList[j]
                        banList[j] = infoBean
                    }
                }
            }
            list.clear()
            banList.forEach {
                list.add(it)
            }
            commonAdapter?.notifyDataSetChanged()
        } catch (e: Exception) {
            Log.e("", e)
        }
    }

}