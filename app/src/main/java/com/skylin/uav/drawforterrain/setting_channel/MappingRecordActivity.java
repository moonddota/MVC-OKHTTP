package com.skylin.uav.drawforterrain.setting_channel;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.andview.refreshview.XRefreshView;
import com.skylin.uav.drawforterrain.APP;
import com.skylin.uav.drawforterrain.BaseActivity;
import com.skylin.uav.drawforterrain.BlueToochActivity;
import com.skylin.uav.drawforterrain.HttpUrlTool;
import com.skylin.uav.drawforterrain.adapter.RecordAdapter;
import com.skylin.uav.drawforterrain.checksupdata.JsonGenericsSerializator;

import com.skylin.uav.drawforterrain.setting_channel.db.DbBan;
import com.skylin.uav.drawforterrain.setting_channel.db.DbSQL;
import com.skylin.uav.drawforterrain.util.DateUtil;
import com.skylin.uav.R;
import com.skylin.uav.drawforterrain.util.ToastUtil;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.GenericsCallback;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

/**
 * Created by Moon on 2018/2/26.
 */

public class MappingRecordActivity extends BaseActivity implements View.OnClickListener {
    public static MappingRecordActivity activity;
    private RecordAdapter adapter;
    private RecyclerView mapping_recycle;
    private ArrayList<DownMappingBan.DataBean.InfoBean> list = new ArrayList<>();
    private XRefreshView mapping_refresh;
    private int page = 0;                      //  数据分页 从0开始

    private FrameLayout lift_topbar;
    private TextView middle_topbar;
    public DbSQL sql;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mappingrecord);
        activity = this;
        BlueToochActivity.fullScreen(this);
        sql = new DbSQL(getBaseContext(), "teamid" + BaseActivity.teamid, null, 1);
        if (sql == null) {
            ToastUtil.show(getString(R.string.toast_spl_un));
            finish();
        }

        initAdapter();
        initTopbar();
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    adapter.notifyDataSetChanged();
                    break;
                    default:
                        break;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        new Thread(new Runnable() {
            @Override
            public void run() {
                readSQL();
                getMappingList(false, "1");
            }
        }).start();
    }

    private void initTopbar() {
        lift_topbar = findViewById(R.id.lift_topbar);
        lift_topbar.setOnClickListener(this);
        middle_topbar = findViewById(R.id.middle_topbar);
        middle_topbar.setText(getString(R.string.MappingRecordActivity_tv1));
    }

    private void initAdapter() {
        adapter = new RecordAdapter(this, list);
        mapping_recycle = findViewById(R.id.mapping_recycle);
        mapping_recycle.setLayoutManager(new LinearLayoutManager(this));
        mapping_recycle.setAdapter(adapter);
        mapping_recycle.setHasFixedSize(true);

        adapter.setOnTiemClickListener(new RecordAdapter.PopuClick() {
            @Override
            public void onPopuClick(View view, DownMappingBan.DataBean.InfoBean infoBean) {
//                DemoActivity.startDemoActivity(MappingRecordActivity.this,infoBean);

                if (DotActivity.dotActivity != null) DotActivity.dotActivity.finish();
                DotActivity.startDotActivity(MappingRecordActivity.this, false, true, infoBean);
            }
        });

        mapping_refresh = findViewById(R.id.mapping_refresh);
        // 设置静默加载模式
        mapping_refresh.setSilenceLoadMore();
        //设置刷新完成以后，headerview固定的时间
        mapping_refresh.setPinnedTime(1000);
        mapping_refresh.setMoveForHorizontal(true);
        mapping_refresh.setPullLoadEnable(true);
        mapping_refresh.setAutoLoadMore(false);
//        adapter.setCustomLoadMoreView(new XRefreshViewFooter(this));
        mapping_refresh.enableReleaseToLoadMore(true);
        mapping_refresh.enableRecyclerViewPullUp(true);
        mapping_refresh.enablePullUpWhenLoadCompleted(true);
        //设置静默加载时提前加载的item个数
//        xefreshView1.setPreLoadCount(4);
        //设置Recyclerview的滑动监听
        mapping_refresh.setOnRecyclerViewScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
//                Toast.makeText(getBaseContext(), "11", Toast.LENGTH_SHORT).show();
//                ToastUtil.show("onScrollStateChanged");
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
//                ToastUtil.show("onScrolled");
            }
        });
        mapping_refresh.setXRefreshViewListener(new XRefreshView.SimpleXRefreshListener() {

            @Override
            public void onRefresh(boolean isPullDown) {
                upMappingList();
            }

            @Override
            public void onLoadMore(boolean isSilence) {

                getMappingList(true, String.valueOf(page + 1));
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lift_topbar:
                finish();
                overridePendingTransition(R.anim.slide_still, R.anim.slide_out_right);
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {//当keycode等于退出事件值时
            finish();
            overridePendingTransition(R.anim.slide_still, R.anim.slide_out_right);
            return super.onKeyDown(keyCode, event);
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    private void readSQL() {
        try {
            list.clear();
            ArrayList<DbBan> dbBans = sql.queryAll();   // 查询所有的DbBan
            for (DbBan dbBan:dbBans ) {
                if (dbBan.getType().equals("border"))
                    list.add(new DownMappingBan.DataBean.InfoBean(dbBan));
            }
            sort();
        } catch (Exception e) {
//          Log.e("readSQL  error  ",e);
            handler.sendEmptyMessage(0);
        }
    }

    private void sort() {
      try{
          for (int i = 0; i < list.size() - 1; i++) {
              for (int j = i + 1; j < list.size(); j++) {
                  int a1 = Integer.parseInt(list.get(i).getId());
                  int a2 = Integer.parseInt(list.get(j).getId());
                  if (a1 < a2) {//如果队前日期靠前，调换顺序  
                      DownMappingBan.DataBean.InfoBean infoBean = list.get(i);
                      list.set(i, list.get(j));
                      list.set(j, infoBean);
                  }
              }
          }
          handler.sendEmptyMessage(0);
      }catch (Exception e){

      }
    }

    private void upMappingList() {
        final String url = APP.url + "/work2.0/Public/?service=Mapping.upMapping&token=" + BaseActivity.TOKEN;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ArrayList<DbBan> dbBans = sql.queryAll();
                    for (int i = 0; i < dbBans.size(); i++) {
                        DbBan dbBan = dbBans.get(i);
                        if (dbBan.getSync().equals("false")) {
                            Map<String, String> params = new HashMap<>();
                            if (!TextUtils.isEmpty(dbBan.getId())) {
                                params.put("Id", dbBan.getId());
                            }
                            params.put("title", dbBan.getTitle());
                            params.put("tid", dbBan.getTid());
                            params.put("province", dbBan.getProvince());
                            params.put("city", dbBan.getCity());
                            params.put("district", dbBan.getDistrict());
                            params.put("mappingBorder", dbBan.getMappingBorder());
                            params.put("obstacleBorder", dbBan.getObstacleBorder());
                            params.put("startBorder", dbBan.getStartBorder());
                            params.put("area", dbBan.getArea());
                            params.put("parentId", dbBan.getParentId());
                            params.put("type", dbBan.getType());
                            String result = HttpUrlTool.submitPostData(url, params, "utf-8");
                            JSONObject json = null;
//                            Log.e("pp "+result);
                            json = new JSONObject(result);
                            String msg = json.getString("msg");
                            String ret = json.getString("ret");
                            String data = json.getString("data");
//                            Log.e(msg + "   " + ret + "  " + data);
                            if (ret.equals("200")) {
                                if (data.equals("[1]")) {
//                                    Log.e("修改成功");
                                } else if (data.equals("[0]")) {
//                                    Log.e("修改失败");
                                } else {
//                                    Log.e("上传成功  Id=" + data);
                                    String[] split = data.split("\"");
                                    dbBan.setId(split[1]);
                                }
                                dbBan.setSync("true");
                                sql.updateBan(dbBan);
                            } else {
                                if (ret.equals("409")) {
                                    BaseActivity.TOKEN = null;
                                } else if (ret.equals("408")) {
//                                    Log.e("尚未登陆");
                                }
                            }
                        }
                    }
                    getMappingList(false, "1");
                } catch (Exception e) {
                    getMappingList(false, "1");
                }
            }
        }).start();
    }

    private void getMappingList(final boolean is_up, String page_tag) {
        final String url = APP.url + "/work2.0/Public/?service=Mapping.getMappingList&token=" + BaseActivity.TOKEN;
        OkHttpUtils
                .post()//
                .url(url)//
                .addParams("type", "all")  //all  self   团队 自己
                .addParams("page", page_tag)
                .addParams("tid", BaseActivity.teamid + "")
                .build()//
                .execute(new GenericsCallback<DownMappingBan>(new JsonGenericsSerializator()) {
                    @Override
                    public void onError(Call call, Exception e, int id) {
//                        Log.e("联网失败 " + call.toString() + " e " + e.toString() + " id " + id);
                        mapping_refresh.stopRefresh();
                        mapping_refresh.stopLoadMore();
                        if (!is_up) readSQL();
                    }

                    @Override
                    public void onResponse(DownMappingBan ban, int id) {
//                        Log.e(ban);
                        List<DownMappingBan.DataBean.InfoBean> info = ban.getData().getInfo();
                        if (ban.getRet() == 200) {
                            if (is_up) {
                                if (info.size() == 20)
                                    page++;
                            } else {
                                page = 1;
                            }

                            for (int i = 0; i < info.size(); i++) {
                                DownMappingBan.DataBean.InfoBean infoBean = info.get(i);
//                                Log.e(infoBean);
                                infoBean.setSync(true);
                                DbBan n_dbBan1 = sql.queryPointById(infoBean.getId());
                                if (n_dbBan1 == null) {
                                    sql.addBan(new DbBan(infoBean, DateUtil.getCurDate("yyyy-MM-dd HH:mm:ss")));
                                } else {
                                    if (!n_dbBan1.getUpdate_time().equals(infoBean.getUpdate_time()))
                                        sql.updateBan(new DbBan(infoBean, n_dbBan1.getInsertTime()));
                                }
                            }
                        }
                        mapping_refresh.stopRefresh();
                        mapping_refresh.stopLoadMore();
                        readSQL();
                    }
                });
    }
}
