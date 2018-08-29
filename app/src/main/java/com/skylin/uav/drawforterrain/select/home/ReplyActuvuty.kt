package com.skylin.uav.drawforterrain.select.home

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.support.annotation.RequiresApi
import android.support.v4.view.ViewPager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.*
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.utils.DistanceUtil
import com.blankj.utilcode.util.ThreadPoolUtils
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton
import com.skylin.mavlink.MAVLinkClient
import com.skylin.mavlink.model.UsbConnectionParameter
import com.skylin.uav.R
import com.skylin.uav.drawforterrain.APP
import com.skylin.uav.drawforterrain.BaseActivity
import com.skylin.uav.drawforterrain.HttpUrlTool
import com.skylin.uav.drawforterrain.adapter.BarrierAdapter
import com.skylin.uav.drawforterrain.adapter.BoundaryAdapter
import com.skylin.uav.drawforterrain.adapter.MPagerAdapter
import com.skylin.uav.drawforterrain.fragment.MapEventsOverlay
import com.skylin.uav.drawforterrain.fragment.OsmFragment
import com.skylin.uav.drawforterrain.nofly.Point
import com.skylin.uav.drawforterrain.service.BluetoothLeService
import com.skylin.uav.drawforterrain.setting_channel.DownMappingBan
import com.skylin.uav.drawforterrain.setting_channel.GGABan
import com.skylin.uav.drawforterrain.setting_channel.ObsMasterBan
import com.skylin.uav.drawforterrain.setting_channel.db.DbBan
import com.skylin.uav.drawforterrain.setting_channel.db.DbSQL
import com.skylin.uav.drawforterrain.util.*
import com.skylin.uav.drawforterrain.views.MarqueeTextView
import com.zhy.adapter.recyclerview.CommonAdapter
import com.zhy.adapter.recyclerview.base.ViewHolder
import org.json.JSONArray
import org.json.JSONObject
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import sjj.alog.Log
import task.model.Pair
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

class ReplyActuvuty : BaseActivity(), View.OnClickListener {

    object statrc_list {
        var RECOVER = "recover"
        var INFOBEAN = "infobean"
        var mapResum: Boolean = false
    }

    var replyBan = ReplyBan()
    var ggaBan: GGABan? = null
    val old_ggaBan = GGABan()

    var is_recover = false
    var infoBean: DownMappingBan.DataBean.InfoBean? = null

    var threadPool: ExecutorService? = null
    var draw_submit: Future<*>? = null
    var sql: DbSQL? = null

    var mapFragment: OsmFragment? = null

    var fragment_commit: LinearLayout? = null
    var fragment_record: LinearLayout? = null

    var icon_reply: LinearLayout? = null

    var tipDialog: QMUITipDialog? = null

    var blueConnectPopu: BlueConnectPopu? = null

    var commit_name: EditText? = null
    var commit_save: Button? = null
    var but_commit: QMUIRoundButton? = null
    var commit_out: TextView? = null
    var but_backout: QMUIRoundButton? = null
    var record_point: Button? = null
    var draw_show: ImageView? = null
    var selext_connect: MarqueeTextView? = null
    var mode_name: TextView? = null
    var satellites_number: TextView? = null
    var accuracy: TextView? = null
    var rtk_tv: TextView? = null
    var viewpager: ViewPager? = null
    var ll_boundary: LinearLayout? = null
    var ll_obstacle: LinearLayout? = null
    var tv_obstacle: TextView? = null
    var group_obstacle: View? = null
    var tv_area: TextView? = null
    var tv1_area: TextView? = null
    var tv_boundary: TextView? = null
    var group_boundary: View? = null

    val backout_list = ArrayList<Pair<String, Point>>()

    val boundary_List = ArrayList<Point>()
    var recycle_view_boundary: RecyclerView? = null
    var boundaryAdapter: BoundaryAdapter? = null
    var null_view_boundary: TextView? = null
    var last_bounary = -1                 //上一次点击的边界点

    var recycle_view_obstacie: RecyclerView? = null
    var null_view_obstacie: TextView? = null
    var obsAdapter: CommonAdapter<ObsMasterBan>? = null
    val obs_list = ArrayList<ObsMasterBan>()
    var last_obs = "-1"                 //上一次点击的障碍点
    var insert_position = -1           //  插入障碍点  内 list  的 position
    var insert_index = -1             //   插入障碍点  外 list  的 position

    var addPoint = false

    var type: String = "border"

    companion object {
        @JvmStatic
        fun startReplyActivity(activity: Activity, recover: Boolean, infoBean: DownMappingBan.DataBean.InfoBean?) {
            val intent = Intent(activity, ReplyActuvuty::class.java)
            intent.putExtra(statrc_list.RECOVER, recover)   //控制是否展示之前数据
            intent.putExtra(statrc_list.INFOBEAN, infoBean)  //  细节展示需要的数据
            activity.startActivity(intent)
            activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_still)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sql = DbSQL(baseContext, "teamid" + BaseActivity.teamid, null, 1)
        if (sql == null) {
            ToastUtil.show(getString(R.string.toast_spl_un))
            onFinish()
        }

        registerReceiver(mGattUpdateReceiver, BlueBaseActivity.makeGattUpdateIntentFilter())

        setContentView(R.layout.activity_reply)

        threadPool = Executors.newSingleThreadExecutor()

        val intent = intent
        is_recover = intent.getBooleanExtra(statrc_list.RECOVER, false)
        if (is_recover) {
            infoBean = intent.getSerializableExtra(statrc_list.INFOBEAN) as DownMappingBan.DataBean.InfoBean
        }

        initViews()

        if (is_recover) run {

            commit_name?.setText(infoBean?.title)
            replyBan.commie_name = infoBean?.title.toString()

            if (!TextUtils.isEmpty(commit_name?.text.toString())) {
                commit_name?.isFocusable = false
                commit_name?.isFocusableInTouchMode = false
            }

            infoBean?.mappingBorder?.forEach {
                boundary_List.add(Point(it[1], it[0]))
            }
            boundaryAdapter?.notifyDataSetChanged()

            infoBean?.obstacleBorder?.forEachIndexed { index, mutableList ->
                val arrayList = ArrayList<Point>()
                mutableList.forEach {
                    arrayList.add(Point(it[1], it[0]))
                }
                obs_list.add(ObsMasterBan(arrayList, index, baseContext))
            }
            obsAdapter?.notifyDataSetChanged()

            if (boundary_List.size == 0) {
                null_view_boundary?.visibility = View.VISIBLE
                recycle_view_boundary?.visibility = View.GONE
            } else {
                null_view_boundary?.visibility = View.GONE
                recycle_view_boundary?.visibility = View.VISIBLE
            }

            if (obs_list.size == 1 && obs_list[0].list.size == 0) {
                null_view_obstacie?.visibility = View.VISIBLE
                recycle_view_obstacie?.visibility = View.GONE
            } else if (obs_list.size == 0) {
                null_view_obstacie?.visibility = View.VISIBLE
                recycle_view_obstacie?.visibility = View.GONE
            } else {
                null_view_obstacie?.visibility = View.GONE
                recycle_view_obstacie?.visibility = View.VISIBLE
            }
            matchArea(false)
        }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)

        statrc_list.mapResum = false

        val map = mapFragment!!.getMap()
        map?.dispatchTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, (map.width / 2).toFloat(), (map.height / 2).toFloat(), 0))
        map?.dispatchTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_UP, (map.width / 2).toFloat(), (map.height / 2).toFloat(), 0))

        setContentView(R.layout.activity_reply)

        initViews()

        recover()

    }

    override fun onResume() {
        super.onResume()
        if (boundary_List.size > 0) run {
            if (boundary_List.size > 2) {
                val centerPoint = Point.getCenterPoint(boundary_List)
                mapFragment?.setLoaction(centerPoint.latitude, centerPoint.longitude, 0)
                mapFragment?.setZoom(boundary_List, centerPoint)
            } else {
                mapFragment?.setLoaction(boundary_List[0].latitude, boundary_List[0].longitude, 0)
            }
        } else if (obs_list.size > 0) {
            if (obs_list[0].list.size > 0) {
                mapFragment?.setLoaction(obs_list[0].list[0].latitude, obs_list[0].list[0].longitude, 0)
            } else {
                handler.postDelayed({
                    mapFragment?.setLoaction(ggaBan)
                }, 1000)
            }
        } else {
            handler.postDelayed({
                mapFragment?.setLoaction(ggaBan)
            }, 1000)
        }
        drawMap(-1, -1, -1)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mGattUpdateReceiver)
        handler.removeCallbacks(runnbale)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {//当keycode等于退出事件值时
            if (record_point?.text.toString() != getString(R.string.DotActivity_tv6)) {
                obs_list.forEach {
                    it.adapter.indext = -1
                    it.adapter.delete = false
                    it.adapter.notifyDataSetChanged()
                }
                boundaryAdapter?.setDeleat(false)
                boundaryAdapter?.setIndext(-1)
                boundaryAdapter?.notifyDataSetChanged()
                drawMap(-1, -1, -1)
                record_point?.text = getString(R.string.DotActivity_tv6)
                backout_state(false)
                return false
            }
            QMUIDialog.MessageDialogBuilder(this@ReplyActuvuty)
                    .setTitle(getString(R.string.continuePopu_tv10))
                    .setMessage(getString(R.string.continuePopu_tv11))
                    .addAction(getString(R.string.continuePopu_tv12)) { dialog, index ->
                        dialog.dismiss()
                    }
                    .addAction(getString(R.string.continuePopu_tv9)) { dialog, index ->
                        dialog.dismiss()
                        onFinish()
                    }
                    .show()
            return false
        } else {
            return super.onKeyDown(keyCode, event)
        }
    }


    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private fun initViews() {

        tipDialog = QMUITipDialog.Builder(this)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord(getString(R.string.DotActivity_tv14))
                .create()

        initMap()

        initPager()

        fragment_commit = findViewById(R.id.pager_commit)
        fragment_record = findViewById(R.id.pager_record)

        findViewById<LinearLayout>(R.id.icon_rtk).visibility = View.GONE
        icon_reply = findViewById(R.id.icon_reply)

        satellites_number = findViewById(R.id.satellites_number)
        accuracy = findViewById(R.id.accuracy)
        rtk_tv = findViewById(R.id.rtk_tv)

        commit_name = findViewById(R.id.commit_name)
        commit_name?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                replyBan.commie_name = commit_name?.text.toString()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
        commit_save = findViewById(R.id.commit_save)
        commit_save?.setOnClickListener(this)
        but_commit = findViewById(R.id.but_commit)
        but_commit?.setOnClickListener(this)
        commit_out = findViewById(R.id.commit_out)
        commit_out?.setOnClickListener(this)
        but_backout = findViewById(R.id.but_backout)
        but_backout?.setOnClickListener(this)
        record_point = findViewById(R.id.record_point)
        record_point?.setOnClickListener(this)
        tv_area = findViewById(R.id.tv_area)
        tv1_area = findViewById(R.id.tv1_area)
        draw_show = findViewById(R.id.draw_show)
        draw_show?.setImageResource(R.mipmap.ic_return)
        draw_show?.setOnClickListener(this)

        ll_boundary = findViewById(R.id.ll_boundary)
        ll_boundary?.setOnClickListener(this)
        ll_obstacle = findViewById(R.id.ll_obstacle)
        ll_obstacle?.setOnClickListener(this)

        tv_obstacle = findViewById(R.id.tv_obstacle)
        group_obstacle = findViewById(R.id.group_obstacle)
        tv_boundary = findViewById(R.id.tv_boundary)
        group_boundary = findViewById(R.id.group_boundary)

        mode_name = findViewById(R.id.mode_name)
        selext_connect = findViewById(R.id.selext_connect)
        selext_connect?.setOnClickListener(this)
        when (HomeActivity.statrc_list.homeBan.mode) {
            0 -> {
                icon_reply?.visibility = View.VISIBLE
                mode_name?.text = getString(R.string.startActivity_tab_tv1)
                selext_connect?.visibility = View.VISIBLE
                if (BlueBaseActivity.mConnected) {
                    selext_connect?.text = BlueBaseActivity.device.name
                }
                handler.removeCallbacks(runnbale)
                handler.post(runnbale)
                type = "border"
            }
            1 -> {
                mode_name?.text = getString(R.string.startActivity_tab_tv2)
                icon_reply?.visibility = View.VISIBLE
                selext_connect?.visibility = View.VISIBLE
                if (BlueBaseActivity.mConnected) {
                    selext_connect?.text = BlueBaseActivity.device.name
                }
                handler.removeCallbacks(runnbale)
                handler.post(runnbale)
                type = "gps_hand"

            }
            2 -> {
                mode_name?.text = getString(R.string.startActivity_tab_tv3)
                icon_reply?.visibility = View.GONE
                selext_connect?.visibility = View.VISIBLE
                if (BlueBaseActivity.mConnected) {
                    selext_connect?.text = getString(R.string.EquipmentActivity_tv3)
                }
                type = "gps_uav"
            }
            3 -> {
                mode_name?.text = getString(R.string.startActivity_tab_tv4)
                icon_reply?.visibility = View.GONE
                selext_connect?.visibility = View.GONE
                findViewById<View>(R.id.hand_point).visibility = View.VISIBLE
                type = "hand"
            }
        }

        blueConnectPopu = BlueConnectPopu(this@ReplyActuvuty, mode_name, R.id.mode_name)
    }

    fun recover() {

        when (replyBan.MODE) {
            0 -> {
                viewpager?.setCurrentItem(0, true)//参数一是ViewPager的position,参数二为是否有滑动效果
            }
            1 -> {
                viewpager?.setCurrentItem(1, true)//参数一是ViewPager的position,参数二为是否有滑动效果
            }
        }

        boundaryAdapter?.notifyDataSetChanged()
        obsAdapter?.notifyDataSetChanged()
        if (boundary_List.size == 0) {
            null_view_boundary?.visibility = View.VISIBLE
            recycle_view_boundary?.visibility = View.GONE
        } else {
            null_view_boundary?.visibility = View.GONE
            recycle_view_boundary?.visibility = View.VISIBLE
        }

        if (obs_list.size == 1 && obs_list[0].list.size == 0) {
            null_view_obstacie?.visibility = View.VISIBLE
            recycle_view_obstacie?.visibility = View.GONE
        } else if (obs_list.size == 0) {
            null_view_obstacie?.visibility = View.VISIBLE
            recycle_view_obstacie?.visibility = View.GONE
        } else {
            null_view_obstacie?.visibility = View.GONE
            recycle_view_obstacie?.visibility = View.VISIBLE
        }
        matchArea(false)

        commit_name?.setText(replyBan.commie_name)
        if (is_recover) run {
            if (!TextUtils.isEmpty(commit_name?.text.toString())) {
                commit_name?.isFocusable = false
                commit_name?.isFocusableInTouchMode = false
            }
        }

        if (replyBan.is_commit) {
            fragment_commit?.visibility = View.VISIBLE
            but_commit?.visibility = View.GONE
            fragment_record?.visibility = View.GONE
        }

        Thread {
            while (!statrc_list.mapResum) (
                    SystemClock.sleep(5)
                    )
            drawMap(-1, -1, -1)
            if (replyBan.pp != null) {
                handler.post({ mapFragment?.setLoaction(replyBan.pp!!.latitude, replyBan.pp!!.longitude, 0) })
            }
        }.start()
    }

    fun onFinish() {
        finish()
        overridePendingTransition(R.anim.slide_still, R.anim.slide_out_right)
    }

    fun initMap() {
        mapFragment = OsmFragment()
        val bundle = Bundle()
        bundle.putBoolean("is_Click", false);
        mapFragment?.setArguments(bundle);
        val ft = fragmentManager.beginTransaction()
        ft.replace(R.id.map_replace, mapFragment)
        ft.commitAllowingStateLoss()

        findViewById<View>(R.id.location_dot).setOnClickListener(this)
        findViewById<View>(R.id.map_model).setOnClickListener(this)


        handler.postDelayed({
            mapFragment!!.getMap()?.overlays?.add(MapEventsOverlay(this, 100, 100, object : MapEventsReceiver {

                override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                    replyBan.pp = p
                    if (addPoint) {
                        ggaBan = GGABan()
                        ggaBan?.lat = p.getLatitude()
                        ggaBan?.lon = p.getLongitude()
                        recordPoint()
                        addPoint = false
                    }
                    return false
                }

                override fun longPressHelper(p: GeoPoint): Boolean {
                    return false
                }

            }))
        }, 2000)
    }

    fun initPager() {
        viewpager = findViewById(R.id.viewpager_reply)
        val view_boundary = LayoutInflater.from(this).inflate(R.layout.view_boundary, null)
        val view_obstacle = LayoutInflater.from(this).inflate(R.layout.view_obstacle, null)
        val mViewList = ArrayList<View>()//页卡视图集合
        mViewList.add(view_boundary)
        mViewList.add(view_obstacle)
        viewpager?.setAdapter(MPagerAdapter(mViewList))
        viewpager?.setOnPageChangeListener(object : android.support.v4.view.ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        replyBan.MODE = 0
                        selectModeTV(ll_boundary!!)
                    }
                    1 -> {
                        replyBan.MODE = 1
                        selectModeTV(ll_obstacle!!)
                    }
                    else -> {
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
        initBoundaryViews(view_boundary)
        initObsViews(view_obstacle)
    }

    fun initBoundaryViews(view: View) {
        boundaryAdapter = BoundaryAdapter(boundary_List, this)
        null_view_boundary = view.findViewById(R.id.null_view_boundary)
        recycle_view_boundary = view.findViewById(R.id.recycle_view_boundary)
        recycle_view_boundary?.setLayoutManager(LinearLayoutManager(baseContext, LinearLayoutManager.HORIZONTAL, false))
        recycle_view_boundary?.setHasFixedSize(true)
        recycle_view_boundary?.adapter = boundaryAdapter
        boundaryAdapter?.setItemClickListener(object : BoundaryAdapter.ItemClickListener {
            override fun onItemClicked(view: View?, position: Int) {
                when (view?.id) {
                    R.id.boundary_item_delete -> {
                        backout_list.add(Pair("bounary,$position", boundary_List[position]))
                        boundary_List.removeAt(position)
                        drawMap(-1, -1, -1)
                        boundaryAdapter?.notifyDataSetChanged()
                        matchArea(false)
                        backout_state(true)
                        if (boundary_List.size == 0) {
                            null_view_boundary?.visibility = View.VISIBLE
                            recycle_view_boundary?.visibility = View.GONE
                        }
                    }
                    R.id.boundary_item_number -> {
                        mapFragment?.setLoaction(
                                boundary_List[position].latitude, boundary_List[position].longitude, 0)
                        if (!boundaryAdapter!!.isDeleat) {
                            if (last_bounary == position) {
                                record_point?.text = getString(R.string.DotActivity_tv6)
                                backout_state(false)
                                boundaryAdapter?.isDeleat = false
                                boundaryAdapter?.indext = -1
                                last_bounary = -1
                                drawMap(-1, -1, -1)
                            } else {
                                last_bounary = position
                                record_point?.text = getString(R.string.DotActivity_tv16)
                                backout_state(true)
                                boundaryAdapter?.isDeleat = false
                                boundaryAdapter?.indext = position
                                drawMap(position, -1, -1)
                            }
                        } else {
                            boundaryAdapter?.isDelete = position
                            drawMap(position, -1, -1)
                        }
                        boundaryAdapter?.notifyDataSetChanged()
                    }
                }
            }
        })

        boundaryAdapter?.setLongClickListener(object : BoundaryAdapter.LongClickListener {
            override fun onLongClicked(view: View?, position: Int) {
                mapFragment?.setLoaction(
                        boundary_List[position].latitude, boundary_List[position].longitude, 0)
                if (last_bounary == position) {
                    boundaryAdapter?.isDeleat = false
                    last_bounary = -1
                    drawMap(-1, -1, -1)
                    record_point?.text = getString(R.string.DotActivity_tv6)
                    backout_state(false)
                } else {
                    last_bounary = position
                    boundaryAdapter?.isDeleat = true
                    boundaryAdapter?.isDelete = position
                    drawMap(position, -1, -1)
                    record_point?.text = getString(R.string.DotActivity_tv17)
                    backout_state(true)
                }
                boundaryAdapter?.notifyDataSetChanged()
            }
        })
    }

    fun initObsViews(view: View) {
        null_view_obstacie = view.findViewById(R.id.null_view_obstacie)
        recycle_view_obstacie = view.findViewById(R.id.recycle_view_obstacie)
        recycle_view_obstacie?.setLayoutManager(LinearLayoutManager(baseContext))
        recycle_view_obstacie?.setHasFixedSize(true)
        obsAdapter = object : CommonAdapter<ObsMasterBan>(this, R.layout.item_obs_master, obs_list) {
            override fun convert(holder: ViewHolder, obsMasterBan: ObsMasterBan, position: Int) {
                holder.setText(R.id.obs_hande, String(byteArrayOf((position + 65).toByte()), 0, 1))
                val end_view = holder.getView<TextView>(R.id.obs_end)
                if (obsAdapter!!.getDatas().size - 1 == position && obsMasterBan.list.size > 2) {
                    end_view?.visibility = View.VISIBLE
                } else {
                    end_view?.visibility = View.INVISIBLE
                }
                end_view.setOnClickListener {
                    obs_list.forEach {
                        it.adapter.setDelete(false)
                        it.adapter.setIndext(-1)
                        it.adapter.notifyDataSetChanged()
                    }
                    record_point?.text = getString(R.string.DotActivity_tv6)
                    backout_state(false)
                    drawMap(-1, -1, -1)
                    if (obs_list.size < 11) {
                        obs_list.add(ObsMasterBan(ArrayList(), obs_list.size, baseContext))
                        obsAdapter?.notifyDataSetChanged()
                        recycle_view_obstacie?.smoothScrollToPosition(obs_list.size - 1)
                    } else {
                        ToastUtil.show(getString(R.string.toast_8))
                    }
                }

                var obs_recycler = holder.getView<RecyclerView>(R.id.obs_recycyle)
                val linearLayoutManager = LinearLayoutManager(baseContext, LinearLayoutManager.HORIZONTAL, false)
                linearLayoutManager.stackFromEnd = true
                obs_recycler.setLayoutManager(linearLayoutManager)
                obs_recycler.setHasFixedSize(true)
                val adapter = obs_list.get(position).getAdapter()
                obs_recycler.setAdapter(adapter)
                obs_list.get(position).setRecyclerView(obs_recycler)

                adapter.setItemClickListener(BarrierAdapter.ItemClickListener { view, position ->
                    when (view.id) {
                        R.id.barrier_item_delete -> {
                            val deleat_barrier = view.tag as Int
                            val point = obs_list.get(deleat_barrier).getList().get(position)
                            obs_list.get(deleat_barrier).getList().removeAt(position)
                            obs_list.get(deleat_barrier).getAdapter().notifyDataSetChanged()
                            if (obs_list.get(deleat_barrier).getList().size == 0 && deleat_barrier != 0) {
                                obs_list.removeAt(deleat_barrier)
                                for (i in obs_list.indices) {
                                    obs_list.get(i).getAdapter().setTag(i)
                                }
                                notifyDataSetChanged()
                                backout_list.add(Pair("obs,yes,$deleat_barrier,$position", point))
                            } else {
                                backout_list.add(Pair("obs,no,$deleat_barrier,$position", point))
                            }
                            drawMap(-1, -1, -1)
                            backout_state(true)
                            if (obs_list.size == 1 && obs_list.get(0).getList().size == 0) {
                                null_view_obstacie?.visibility = View.VISIBLE
                                recycle_view_obstacie?.visibility = View.GONE
                            }
                            notifyDataSetChanged()
                        }
                        R.id.barrier_item_number -> {
                            val tag = view.tag as Int
                            mapFragment?.setLoaction(obs_list.get(tag).getList().get(position).getLatitude(), obs_list.get(tag).getList().get(position).getLongitude(), 0)
                            if (!obsMasterBan.adapter.delete) {
                                if (last_obs == tag.toString() + "" + position) {
                                    obs_list.forEach {
                                        it.adapter.setIndext(-1)
                                        it.adapter.notifyDataSetChanged()
                                    }
                                    last_obs = (-1).toString() + ""
                                    record_point?.text = getString(R.string.DotActivity_tv6)
                                    backout_state(false)
                                    drawMap(-1, -1, -1)
                                } else {
                                    last_obs = tag.toString() + "" + position
                                    for (ban in obs_list) {
                                        ban.getAdapter().setIndext(-1)
                                        ban.getAdapter().notifyDataSetChanged()
                                    }
                                    insert_position = position
                                    insert_index = tag

                                    record_point?.text = getString(R.string.DotActivity_tv16)
                                    backout_state(true)
                                    obsMasterBan.adapter.indext = position
                                    obsMasterBan.adapter.notifyDataSetChanged()
                                    drawMap(-1, tag, position)
                                }
                            } else {
                                obs_list.forEach {
                                    it.getAdapter().setIndext(-1)
                                    it.getAdapter().notifyDataSetChanged()
                                }
                                obsMasterBan.adapter.deleat = position
                                obsMasterBan.adapter.notifyDataSetChanged()
                                drawMap(-1, tag, position)
                            }
                        }
                        else -> {
                        }
                    }
                })
                adapter.setLongClickListener(BarrierAdapter.LongClickListener { view, position ->
                    val tag = view.tag as Int
                    mapFragment?.setLoaction(obs_list.get(tag).getList().get(position).getLatitude(), obs_list.get(tag).getList().get(position).getLongitude(), 0)
                    if (last_obs == tag.toString() + "" + position) {
                        obs_list.forEach {
                            it.getAdapter().setDelete(false)
                            it.getAdapter().setIndext(-1)
                            it.getAdapter().notifyDataSetChanged()
                        }
                        last_obs = (-1).toString() + ""
                        drawMap(-1, -1, -1)
                        record_point?.text = getString(R.string.DotActivity_tv6)
                        backout_state(false)
                    } else {
                        last_obs = tag.toString() + "" + position
                        obs_list.forEach {
                            it.getAdapter().setIndext(-1)
                            it.getAdapter().setDeleat(-1)
                            it.getAdapter().setDelete(true)
                            it.getAdapter().notifyDataSetChanged()
                        }
                        obsMasterBan.adapter.deleat = position
                        obsMasterBan.adapter.notifyDataSetChanged()
                        drawMap(-1, tag, -1)
                        record_point?.text = getString(R.string.DotActivity_tv17)
                        backout_state(true)
                    }
                })
            }
        }

        recycle_view_obstacie?.adapter = obsAdapter
    }

    var handler = Handler() {
        when (it.what) {
        }
    }

    var runnbale = object : Runnable {
        @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
        override fun run() {
            if (BlueBaseActivity.mConnected) {
                if (HomeActivity.statrc_list.homeBan.mode == 0) {
                    OrderUtils.requestGGA()
                }

                if (ggaBan != null) {
                    satellites_number?.text = ggaBan?.satellites

                    val hdop = ggaBan?.hdop
                    accuracy?.text = hdop.toString()
                    if (hdop!! >= 1.5) {
                        accuracy?.setTextColor(resources.getColor(R.color.red))
                    } else {
                        accuracy?.setTextColor(resources.getColor(R.color.white))
                    }

                    val rtk = ggaBan?.rtk
                    rtk_tv?.text = rtk
                    if (HomeActivity.statrc_list.homeBan.mode == 0 && rtk!! != "4") {
                        rtk_tv?.setTextColor(resources.getColor(R.color.red))
                    } else {
                        rtk_tv?.setTextColor(resources.getColor(R.color.white))
                    }

                    var lat = ggaBan!!.lat
                    var lon = ggaBan!!.lon
                    if (lat != 0.0 && lon != 0.0) {
                        mapFragment?.setLoaction(lat, lon, 1)
                    }
                }
            }
            handler.postDelayed(this, 1000)
        }

    }


    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.commit_save -> {
                upMapping()
            }
            R.id.but_backout -> {
                val size = backout_list.size
                if (size > 0) {
                    val pair = backout_list[size - 1]
                    val split = pair.first.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    if (split[0].startsWith("bounary")) {
                        boundary_List.add(Integer.parseInt(split[1]), pair.second)
                        matchArea(false)
                        boundaryAdapter?.notifyDataSetChanged()
                        if (boundary_List.size != 0) {
                            null_view_boundary?.visibility = View.GONE
                            recycle_view_boundary?.visibility = View.VISIBLE
                        }
                    } else if (split[0].startsWith("obs")) {
                        if (split[1] == "no") {
                            obs_list[Integer.parseInt(split[2])].list.add(Integer.parseInt(split[3]), pair.second)
                            obs_list[Integer.parseInt(split[2])].adapter.notifyDataSetChanged()
                        } else if (split[1] == "yes") {
                            val list = ArrayList<Point>()
                            list.add(pair.second)
                            obs_list.add(Integer.parseInt(split[2]), ObsMasterBan(list, Integer.parseInt(split[2]), baseContext))
                            for (i in obs_list.indices) {
                                obs_list[i].adapter.tag = i
                                obs_list[i].adapter.notifyDataSetChanged()
                            }
                        }

                        if (obs_list.size == 1 && obs_list[0].list.size == 0) {
                            null_view_obstacie?.visibility = View.VISIBLE
                            recycle_view_obstacie?.visibility = View.GONE
                        } else if (obs_list.size == 0) {
                            null_view_obstacie?.visibility = View.VISIBLE
                            recycle_view_obstacie?.visibility = View.GONE
                        } else {
                            null_view_obstacie?.visibility = View.GONE
                            recycle_view_obstacie?.visibility = View.VISIBLE
                        }

                        obsAdapter?.notifyDataSetChanged()
                    }
                    backout_list.removeAt(size - 1)
                    backout_state(true)
                    if (backout_list.size < 1) {
                        record_point?.text = getString(R.string.DotActivity_tv6)
                        obs_list.forEach {
                            it.adapter.indext = -1
                            it.adapter.delete = false
                            it.adapter.notifyDataSetChanged()
                        }
                        boundaryAdapter?.setDeleat(false)
                        boundaryAdapter?.setIndext(-1)
                        boundaryAdapter?.notifyDataSetChanged()
                    }
                    //                    mapFragment.setLoaction(pair.second.getAltitude(), pair.second.getLongitude(), 0);
                    drawMap(-1, -1, -1)
                }
            }
            R.id.draw_show -> {
                if (record_point?.text.toString() != getString(R.string.DotActivity_tv6)) {
                    obs_list.forEach {
                        it.adapter.indext = -1
                        it.adapter.delete = false
                        it.adapter.notifyDataSetChanged()
                    }
                    boundaryAdapter?.setDeleat(false)
                    boundaryAdapter?.setIndext(-1)
                    boundaryAdapter?.notifyDataSetChanged()
                    drawMap(-1, -1, -1)
                    record_point?.text = getString(R.string.DotActivity_tv6)
                    backout_state(false)
                    return
                }
                QMUIDialog.MessageDialogBuilder(this@ReplyActuvuty)
                        .setTitle(getString(R.string.continuePopu_tv10))
                        .setMessage(getString(R.string.continuePopu_tv11))
                        .addAction(getString(R.string.continuePopu_tv12)) { dialog, index ->
                            dialog.dismiss()
                        }
                        .addAction(getString(R.string.continuePopu_tv9)) { dialog, index ->
                            dialog.dismiss()
                            onFinish()
                        }
                        .show()
            }
            R.id.commit_out -> {
                if (fragment_commit?.visibility == View.VISIBLE) {
                    fragment_commit?.visibility = View.GONE
                    fragment_record?.visibility = View.VISIBLE
                    but_commit?.visibility = View.VISIBLE
                    replyBan.is_commit = false
                }
            }
            R.id.but_commit -> {
                if (boundary_List.size < 3) {
                    ToastUtil.show(getString(R.string.toast_9))
                    return
                }
                if (obs_list.size != 0) {
                    for (ban in obs_list) {
                        if (ban.list.size < 3 && ban.list.size > 0) {
                            ToastUtil.show(getString(R.string.toast_10))
                            return
                        }
                    }
                }
                if (boundary_List.size > 3) {
                    try {
                        LineUtil.judgeList(boundary_List)
                    } catch (e: Exception) {
                        e.printStackTrace()
//                        Log.e("", e)
                        ToastUtil.show(e.message)
                        return
                    }

                }
                for (ban in obs_list) {
                    if (ban.list != null)
                        if (ban.list.size > 3) {
                            try {
                                LineUtil.judgeList(ban.list)
                            } catch (e: Exception) {
                                e.printStackTrace()
//                                Log.e("", e)
                                ToastUtil.show(e.message)
                                return
                            }

                        }
                }

                fragment_commit?.visibility = View.VISIBLE
                but_commit?.visibility = View.GONE
                fragment_record?.visibility = View.GONE
                replyBan.is_commit = true
                if (infoBean != null) {
                    commit_name?.setText(infoBean?.title.toString())
                }
            }
            R.id.ll_boundary -> {
                if (replyBan.MODE == 0) {
                    return
                }
                viewpager?.setCurrentItem(0, true)//参数一是ViewPager的position,参数二为是否有滑动效果
            }
            R.id.ll_obstacle -> {
                if (replyBan.MODE == 1) {
                    return
                }
                viewpager?.setCurrentItem(1, true)//参数一是ViewPager的position,参数二为是否有滑动效果
            }
            R.id.map_model -> {
                mapFragment?.setMapMopType()
            }
            R.id.location_dot -> {
                mapFragment?.setLoaction(ggaBan)
            }
            R.id.selext_connect -> {
                when (HomeActivity.statrc_list.homeBan.mode) {
                    0 -> {
                        blueConnectPopu!!.show()
                    }
                    1 -> {
                        blueConnectPopu!!.show()
                    }
                    2 -> {
                        if (!BlueBaseActivity.mConnected) {
                            connectUav()
                        }
                    }
                }
            }
            R.id.record_point -> {
                if (HomeActivity.statrc_list.homeBan.mode == 3) {
                    addPoint = true
                    val map = mapFragment!!.getMap()
                    map?.dispatchTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, (map.width / 2).toFloat(), (map.height / 2).toFloat(), 0))
                    map?.dispatchTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_UP, (map.width / 2).toFloat(), (map.height / 2).toFloat(), 0))

                } else {
                    recordPoint()
                }
            }
        }
    }

    fun recordPoint() {

        val s = record_point?.text.toString()
        if (s == getString(R.string.DotActivity_tv17)) {
            return
        }

        if (ggaBan == null) {
            ToastUtil.show(getString(R.string.StartActivity_tv33))
            return
        }

        if ((HomeActivity.statrc_list.homeBan.mode == 0) or (HomeActivity.statrc_list.homeBan.mode == 1)) {
            if (!BlueBaseActivity.mConnected) {
                ToastUtil.show(getString(R.string.toast_3))
                return
            }

            if (ggaBan!!.isEmpty()) {
                ToastUtil.show(getString(R.string.toast_11))
                return
            }

            if ((HomeActivity.statrc_list.homeBan.mode == 0) && (ggaBan!!.getRtk() != "4")) {
                ToastUtil.show(getString(R.string.toast_12))
                return
            }

            if (ggaBan!!.getHdop() >= 1.5) {
                ToastUtil.show(getString(R.string.toast_12))
                return
            }

            if (DistanceUtil.getDistance(
                            LatLng(old_ggaBan.lat!!, old_ggaBan.lon!!),
                            LatLng(ggaBan!!.getLat()!!, ggaBan!!.getLon()!!)) > 0.2) {
                ToastUtil.show(getString(R.string.toast_12))
                return
            }
        }

        try {
            val latLngs = OBSMatch.judgeNoFly(ggaBan?.getLat(), ggaBan?.getLon(), BaseActivity.second)
            if (latLngs != null) {
                ToastUtil.show(getString(R.string.toast_13))
                mapFragment?.DrawNoFly(latLngs)
                return
            }
        } catch (e: Exception) {
            ToastUtil.show(getString(R.string.toast_13))
            return
        }


        when (replyBan.MODE) {
            0 -> recordBoundarys(s, GGABan(ggaBan))
            1 -> recordObstacle(s, GGABan(ggaBan))
        }
    }


    fun recordBoundarys(s: String, ggaBan: GGABan?) {
        //正常打点
        if (s == getString(R.string.DotActivity_tv6)) {
            boundary_List.add(Point(ggaBan?.getLon()!!, ggaBan.getLat()!!, ggaBan.getAlt()!!))
            if (boundary_List.size >= 2) {
                val b = boundary_List.size
                if (DistanceUtil.getDistance(
                                LatLng(boundary_List[b - 1].latitude, boundary_List[b - 1].longitude),
                                LatLng(boundary_List[b - 2].latitude, boundary_List[b - 2].longitude)) <= 1) {
                    boundary_List.removeAt(b - 1)
                    ToastUtil.show(getString(R.string.toast_14))
                    return
                }
            }

            if (boundary_List.size != 0) {
                null_view_boundary?.visibility = View.GONE
                recycle_view_boundary?.visibility = View.VISIBLE
            }

            val i = boundary_List.size - 1
            recycle_view_boundary?.smoothScrollToPosition(i)
            if (HomeActivity.statrc_list.homeBan.mode != 3)
                mapFragment?.setLoaction(boundary_List[i].latitude, boundary_List[i].longitude, 0)
        } else if (s == getString(R.string.DotActivity_tv16)) {
            val indext = boundaryAdapter?.getIndext()!!.toInt()
            val point = Point(ggaBan?.getLon()!!, ggaBan.getLat()!!, ggaBan.getAlt()!!)
            boundary_List.add(indext, point)

            if (indext == 0) {
                if (DistanceUtil.getDistance(
                                LatLng(point.latitude, point.longitude),
                                LatLng(boundary_List[1].latitude, boundary_List[1].longitude)) <= 1) {
                    boundary_List.removeAt(0)
                    ToastUtil.show(getString(R.string.toast_14))
                    return
                }
            } else if (indext >= 1) {
                if ((DistanceUtil.getDistance(
                                LatLng(point.latitude, point.longitude),
                                LatLng(boundary_List[indext - 1].latitude, boundary_List[indext - 1].longitude)) <= 1) or (DistanceUtil.getDistance(
                                LatLng(point.latitude, point.longitude),
                                LatLng(boundary_List[indext + 1].latitude, boundary_List[indext + 1].longitude)) <= 1)) {
                    boundary_List.removeAt(indext)
                    ToastUtil.show(getString(R.string.toast_14))
                    return
                }
            }
            if (HomeActivity.statrc_list.homeBan.mode != 3)
                mapFragment?.setLoaction(point.latitude, point.longitude, 0)
        }//插入点
        record_point?.text = getString(R.string.DotActivity_tv6)
        backout_state(false)
        boundaryAdapter?.setIndext(-1)
        boundaryAdapter?.notifyDataSetChanged()
        drawMap(-1, -1, -1)
        matchArea(false)
        backout_list.clear()
    }

    fun recordObstacle(s: String, ggaBan: GGABan?) {
        if (s == getString(R.string.DotActivity_tv6)) {
            val point = Point(ggaBan?.getLon()!!, ggaBan.getLat()!!, ggaBan.getAlt()!!)
            if (obs_list.size == 0) {
                obs_list.add(ObsMasterBan(ArrayList(), obs_list.size, baseContext))
                obsAdapter?.notifyDataSetChanged()
                val obsMasterBan = obs_list[obs_list.size - 1]
                obsMasterBan.list.add(point)
                obsMasterBan.adapter.notifyDataSetChanged()
                drawMap(-1, -1, -1)
                if (HomeActivity.statrc_list.homeBan.mode != 3)
                    mapFragment?.setLoaction(point.latitude, point.longitude, 0)

                null_view_obstacie?.visibility = View.GONE
                recycle_view_obstacie?.visibility = View.VISIBLE

                return
            }

            val obsMasterBan = obs_list[obs_list.size - 1]
            val list = obsMasterBan.list
            list.add(point)

            if (list.size >= 2) {
                val b = list.size
                if (DistanceUtil.getDistance(
                                LatLng(list[b - 1].latitude, list[b - 1].longitude),
                                LatLng(list[b - 2].latitude, list[b - 2].longitude)) <= 0.1) {
                    list.removeAt(b - 1)
                    ToastUtil.show(getString(R.string.toast_14))
                    return
                }
            }

            try {
                OBSMatch.match(obs_list, obs_list.size - 1)
            } catch (e: Exception) {
                list.removeAt(list.size - 1)
                ToastUtil.show(getString(R.string.toast_15))
                return
            }

            try {
                LineUtil.judge(obs_list, obs_list.size - 1, list.size - 1)
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("", e)
                ToastUtil.show(getString(R.string.toast_16))
                list.removeAt(list.size - 1)
                return
            }

            null_view_obstacie?.visibility = View.GONE
            recycle_view_obstacie?.visibility = View.VISIBLE

            if (HomeActivity.statrc_list.homeBan.mode != 3)
                mapFragment?.setLoaction(point.latitude, point.longitude, 0)
            obsAdapter?.notifyDataSetChanged()
            obsMasterBan.adapter.notifyDataSetChanged()
            obsMasterBan.recyclerView.smoothScrollToPosition(list.size - 1)
            //            obsMasterBan.getRecyclerView().scrollToPosition(list.size() - 1);
            //            ((LinearLayoutManager) obsMasterBan.getRecyclerView().getLayoutManager()).scrollToPositionWithOffset(list.size() - 1,list.size() - 1);
        } else if (s == getString(R.string.DotActivity_tv16)) {
            val point = Point(ggaBan?.getLon()!!, ggaBan.getLat()!!, ggaBan.getAlt()!!)
            val obsMasterBan = obs_list[insert_index]
            val list = obsMasterBan.list
            list.add(insert_position, point)

            if (insert_position == 0) {
                val b = list.size
                if ((DistanceUtil.getDistance(
                                LatLng(list[b - 1].latitude, list[b - 1].longitude),
                                LatLng(list[0].latitude, list[0].longitude)) <= 0.1) or (DistanceUtil.getDistance(
                                LatLng(list[1].latitude, list[1].longitude),
                                LatLng(list[0].latitude, list[0].longitude)) <= 0.1)) {
                    list.removeAt(insert_position)
                    ToastUtil.show(getString(R.string.toast_14))
                    return
                }
            } else {
                val b = list.size
                if ((DistanceUtil.getDistance(
                                LatLng(list[insert_position - 1].latitude, list[insert_position - 1].longitude),
                                LatLng(list[insert_position].latitude, list[insert_position].longitude)) <= 0.1) or (DistanceUtil.getDistance(
                                LatLng(list[insert_position + 1].latitude, list[insert_position + 1].longitude),
                                LatLng(list[insert_position].latitude, list[insert_position].longitude)) <= 0.1)) {
                    list.removeAt(insert_position)
                    ToastUtil.show(getString(R.string.toast_14))
                    return
                }
            }


            try {
                OBSMatch.match(obs_list, obs_list.size - 1)
            } catch (e: Exception) {
                list.removeAt(insert_position)
                ToastUtil.show(getString(R.string.toast_15))
                return
            }

            try {
                LineUtil.judge(obs_list, obs_list.size - 1, insert_position)
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("", e)
                ToastUtil.show(getString(R.string.toast_16))
                list.removeAt(insert_position)
                return
            }

            if (HomeActivity.statrc_list.homeBan.mode != 3)
                mapFragment?.setLoaction(point.latitude, point.longitude, 0)
            record_point?.text = getString(R.string.DotActivity_tv6)
            backout_state(false)
            obsMasterBan.adapter.indext = -1
            obsMasterBan.adapter.notifyDataSetChanged()
        }//插入点
        drawMap(-1, -1, -1)
        backout_list.clear()
    }

    fun selectModeTV(linear: LinearLayout) {
        if (linear == ll_boundary) {
//            viewpager?.setLayoutParams(LinearLayout.LayoutParams(-1, DensityUtil.dip2px(80f), 1f))

            tv_boundary?.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//加粗
            tv_boundary?.setTextColor(resources.getColor(R.color.black))
            group_boundary?.visibility = View.VISIBLE

            tv_obstacle?.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            tv_obstacle?.setTextColor(resources.getColor(R.color.text_gray))
            group_obstacle?.visibility = View.INVISIBLE

            obs_list.forEach {
                it.getAdapter().setIndext(-1)
                it.getAdapter().setDelete(false)
                it.getAdapter().notifyDataSetChanged()
            }
        } else {
//            viewpager?.setLayoutParams(LinearLayout.LayoutParams(-1, DensityUtil.dip2px(130f), 1f))

            tv_boundary?.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            tv_boundary?.setTextColor(resources.getColor(R.color.text_gray))
            group_boundary?.visibility = View.INVISIBLE

            tv_obstacle?.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            tv_obstacle?.setTextColor(resources.getColor(R.color.black))
            group_obstacle?.visibility = View.VISIBLE

            boundaryAdapter?.setDeleat(false)
            boundaryAdapter?.setIndext(-1)
            boundaryAdapter?.notifyDataSetChanged()
        }
        record_point?.text = getString(R.string.DotActivity_tv6)
        backout_state(false)

        drawMap(-1, -1, -1)
    }

    fun drawMap(b_index: Int, obs_index: Int, obs_position: Int) {
        if (draw_submit != null) {
            draw_submit?.cancel(true)
        }
        draw_submit = threadPool?.submit(Runnable {
            mapFragment?.DrawMapping(
                    boundary_List, b_index,
                    obs_list, obs_index, obs_position)
        })
    }


    fun matchArea(accuracy: Boolean): String? {
        val s = Area.mathArea(boundary_List)
        if (accuracy) {
            return s
        } else {
            val locale = Locale.getDefault().toString()
            if (locale.equals("zh_CN")) {

                val df = DecimalFormat("#0.00")
                val format = df.format(java.lang.Double.parseDouble(s))
                tv_area?.text = format
                tv1_area?.text = format
            } else {
                val df = DecimalFormat("#0.000")
                val format = df.format((s.toDouble() / 15))
                tv_area?.text = format
                tv1_area?.text = format
            }
            return null
        }
    }

    fun backout_state(is_show: Boolean) {
        if (is_show) {
            if (backout_list.size > 0)
                but_backout?.visibility = View.VISIBLE
            else
                but_backout?.visibility = View.GONE
        } else {
            but_backout?.visibility = View.GONE
        }
    }


    val mGattUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (BluetoothLeService.ACTION_GATT_CONNECTED == intent.action) { //连接一个GATT服务
                when (HomeActivity.statrc_list.homeBan.mode) {
                    0, 1 -> {
                        selext_connect?.text = BlueBaseActivity.device.name
                        handler.removeCallbacks(runnbale)
                        handler.post(runnbale)
                    }
                    2 -> {
                        selext_connect?.text = getString(R.string.EquipmentActivity_tv3)
                    }
                }

            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED == intent.action) {  //从GATT服务中断开连接
                ggaBan = null
                selext_connect?.text = getString(R.string.EquipmentActivity_tv5)

                handler.removeCallbacks(runnbale)

            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED == intent.action) {  //发现有可支持的服务
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE == intent.action) {   //从服务中接受数据
                ggaBan = intent.getSerializableExtra(BluetoothLeService.EXTRA_DATA) as GGABan
                if (ggaBan?.time == 3 && !ggaBan!!.isEmpty) {
                    when (HomeActivity.statrc_list.homeBan.mode) {
                        0, 1 -> {
                            old_ggaBan.lat = (ggaBan?.lat)
                            old_ggaBan.lon = (ggaBan?.lon)
                        }
                    }
                }
            }
        }
    }

    private fun upMapping() {
        try {
            val title = commit_name?.text.toString()
            if (TextUtils.isEmpty(title)) {
                ToastUtil.show(getString(R.string.toast_17))
                return
            }

            commit_save?.isClickable = false
            tipDialog?.show()

            val area = matchArea(true)

            val bound = JSONArray()
            val obs = JSONArray()
            val start = JSONArray()

            for (point in boundary_List) {
                bound.put(JSONArray().put(point.latitude).put(point.longitude))
            }

            for (ban in obs_list) {
                val barrier1 = JSONArray()
                for (point in ban.list) {
                    if (ban.list.size != 0) {
                        barrier1.put(JSONArray().put(point.latitude).put(point.longitude))
                    }
                }
                if (barrier1.length() != 0) {
                    obs.put(barrier1)
                }
            }

            start.put(JSONArray().put(boundary_List[0].latitude).put(boundary_List[0].longitude))

            val bound_ss = bound.toString()
            val obs_ss = obs.toString()
            val start_ss = start.toString()

            ThreadPoolUtils(ThreadPoolUtils.SingleThread, 0).execute {
                val url = APP.url + "/work2.0/Public/?service=Mapping.upMapping&token=" + BaseActivity.TOKEN
                val dbBan = DbBan()

                val params = HashMap<String, String>()
                if (is_recover) {
                    if (!TextUtils.isEmpty(infoBean?.id)) {
                        params["Id"] = infoBean?.id!!
                        dbBan.id = infoBean?.id
                    }
                }

                dbBan.create_name = BaseActivity.username
                dbBan.title = title
                dbBan.tid = BaseActivity.teamid.toString()
                dbBan.mappingBorder = bound_ss
                dbBan.obstacleBorder = obs_ss
                dbBan.startBorder = start_ss
                dbBan.area = area
                dbBan.type = type
                dbBan.parentId = "0"

                params["title"] = title
                params["tid"] = BaseActivity.teamid.toString()
                params["mappingBorder"] = bound_ss
                params["obstacleBorder"] = obs_ss
                params["startBorder"] = start_ss
                params["area"] = area!!
                params["parentId"] = "0"
                params["type"] = type
                val result = HttpUrlTool.submitPostData(url, params, "utf-8")
                //                    Log.e("after request");
                var json: JSONObject? = null
                //                    Log.e("doc  "+ result);
                try {
                    json = JSONObject(result)
                    val msg = json.getString("msg")
                    val ret = json.getString("ret")
                    val data = json.getString("data")
                    //                        Log.e(msg + "   " + ret + "  " + data);
                    if (ret == "200") {
                        dbBan.sync = "true"
                        if (data == "[1]") {
                            //                                Log.e("修改成功");
                        } else if (data == "[0]") {
                            //                                Log.e("修改失败");
                            dbBan.sync = "false"
                        } else {
                            val split = data.split("\"".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                            //                                Log.e("上传成功  Id:" + split[1]);
                            dbBan.id = split[1]
                        }
                    } else {
                        dbBan.sync = "false"
                    }
                    upDB(dbBan)
                } catch (e: Exception) {
                    Log.e("", e)
                    handler.post({ tipDialog?.dismiss() })
                    commit_save?.isClickable = true
                    dbBan.sync = "false"
                    upDB(dbBan)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            handler.post({ tipDialog?.dismiss() })
            ToastUtil.show(getString(R.string.toast_18))
            commit_save?.isClickable = true
        }
    }

    private fun upDB(dbBan: DbBan) {
        if (is_recover) {
            if (TextUtils.isEmpty(infoBean?.insertTime)) {
                dbBan.insertTime = DateUtil.getCurDate("yyyy-MM-dd HH:mm:ss")
                sql?.addBan(dbBan)
            } else {
                dbBan.insertTime = infoBean?.insertTime
                sql?.updateBan(dbBan)
            }
        } else {
            Log.e("212121212")
            dbBan.insertTime = DateUtil.getCurDate("yyyy-MM-dd HH:mm:ss")
            sql?.addBan(dbBan)
        }
        if (dbBan.sync == "true") {
            handler.post({ tipDialog?.dismiss() })
            ToastUtil.show(getString(R.string.toast_19))
        } else {
            ToastUtil.show(getString(R.string.toast_19))
        }
        onFinish()
    }

    private val client = MAVLinkClient()
    private val uavClient = client.getUavClient(UsbConnectionParameter(57600), this)

}