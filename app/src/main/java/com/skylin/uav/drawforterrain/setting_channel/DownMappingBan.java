package com.skylin.uav.drawforterrain.setting_channel;

import android.text.TextUtils;

import com.skylin.uav.drawforterrain.setting_channel.db.DbBan;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Moon on 2018/3/3.
 */

public class DownMappingBan {

    /**
     * ret : 200
     * data : {"info":[{"Id":"241081","title":"测试","province":"四川","city":"成都","district":"双流","mappingBorder":[[30.508348333333,104.11828333333],[30.508318335333,104.11813335333],[30.50815,104.11808333333],[30.50815,104.11808336333]],"obstacleBorder":null,"startBorder":[[30.508348333333,104.11828333333]],"area":"50","parentId":"0","splitStatus":"-1","type":"border","lat":"0.0000000000","lng":"0.0000000000","create_uid":"0","create_name":"接口查阅","create_time":"2018-02-28 11:18:29","update_uid":"0","update_name":"接口查阅","update_time":"2018-02-28 11:18:29","status":"1"},{"Id":"241080","title":"测试","province":"四川","city":"成都","district":"双流","mappingBorder":[[30.508348333333,104.11828333333],[30.508318333333,104.11813333333],[30.50815,104.11808333333],[30.50815,104.11808313333]],"obstacleBorder":null,"startBorder":[[30.508348333333,104.11828333333]],"area":"50","parentId":"0","splitStatus":"-1","type":"border","lat":"0.0000000000","lng":"0.0000000000","create_uid":"0","create_name":"接口查阅","create_time":"2018-02-28 03:22:25","update_uid":"0","update_name":"接口查阅","update_time":"2018-02-28 11:22:24","status":"1"}]}
     * msg :
     */

    private int ret;
    private DataBean data;
    private String msg;

    @Override
    public String toString() {
        return "DownMappingBan{" +
                "ret=" + ret +
                ", data=" + data +
                ", msg='" + msg + '\'' +
                '}';
    }

    public int getRet() {
        return ret;
    }

    public void setRet(int ret) {
        this.ret = ret;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public static class DataBean {

        @Override
        public String toString() {
            return "DataBean{" +
                    "info=" + info +
                    '}';
        }

        private List<InfoBean> info;

        public List<InfoBean> getInfo() {
            return info;
        }

        public void setInfo(List<InfoBean> info) {
            this.info = info;
        }

        public static class InfoBean implements Serializable {

            @Override
            public String toString() {
                return "InfoBean{" +
                        "Id='" + Id + '\'' +
                        ", tid='" + tid + '\'' +
                        ", title='" + title + '\'' +
                        ", province='" + province + '\'' +
                        ", city='" + city + '\'' +
                        ", district='" + district + '\'' +
                        ", obstacleBorder=" + obstacleBorder +
                        ", area='" + area + '\'' +
                        ", parentId='" + parentId + '\'' +
                        ", splitStatus='" + splitStatus + '\'' +
                        ", type='" + type + '\'' +
                        ", lat='" + lat + '\'' +
                        ", lng='" + lng + '\'' +
                        ", create_uid='" + create_uid + '\'' +
                        ", create_name='" + create_name + '\'' +
                        ", create_time='" + create_time + '\'' +
                        ", update_uid='" + update_uid + '\'' +
                        ", update_name='" + update_name + '\'' +
                        ", update_time='" + update_time + '\'' +
                        ", status='" + status + '\'' +
                        ", mappingBorder=" + mappingBorder +
                        ", startBorder=" + startBorder +
                        ", insertTime=" + insertTime +
                        ", sync=" + sync +
                        '}';
            }

            /**
             * Id : 241081
             * title : 测试
             * province : 四川
             * city : 成都
             * district : 双流
             * mappingBorder : [[30.508348333333,104.11828333333],[30.508318335333,104.11813335333],[30.50815,104.11808333333],[30.50815,104.11808336333]]
             * obstacleBorder : null
             * startBorder : [[30.508348333333,104.11828333333]]
             * area : 50
             * parentId : 0
             * splitStatus : -1
             * type : border
             * lat : 0.0000000000
             * lng : 0.0000000000
             * create_uid : 0
             * create_name : 接口查阅
             * create_time : 2018-02-28 11:18:29
             * update_uid : 0
             * update_name : 接口查阅
             * update_time : 2018-02-28 11:18:29
             * status : 1
             */

            private String Id;
            private String tid;
            private String title;
            private String province;
            private String city;
            private String district;
            private String area;
            private String parentId;
            private String splitStatus;
            private String type;
            private String lat;
            private String lng;
            private String create_uid;
            private String create_name;
            private String create_time;
            private String update_uid;
            private String update_name;
            private String update_time;
            private String status;
            private String insertTime;
            private List<List<Double>> mappingBorder;
            private List<List<Double>> startBorder;
            private List<List<List<Double>>> obstacleBorder;
            private Boolean sync = false;

            public String getInsertTime() {
                return insertTime;
            }

            public void setInsertTime(String insertTime) {
                this.insertTime = insertTime;
            }

            public String getId() {
                return Id;
            }

            public void setId(String Id) {
                this.Id = Id;
            }

            public String getTid() {
                return tid;
            }

            public void setTid(String tid) {
                this.tid = tid;
            }

            public String getTitle() {
                return title;
            }

            public void setTitle(String title) {
                this.title = title;
            }

            public String getProvince() {
                return province;
            }

            public void setProvince(String province) {
                this.province = province;
            }

            public String getCity() {
                return city;
            }

            public void setCity(String city) {
                this.city = city;
            }

            public String getDistrict() {
                return district;
            }

            public void setDistrict(String district) {
                this.district = district;
            }

            public List<List<List<Double>>> getObstacleBorder() {
                return obstacleBorder;
            }

            public void setObstacleBorder(List<List<List<Double>>> obstacleBorder) {
                this.obstacleBorder = obstacleBorder;
            }

            public String getArea() {
                return area;
            }

            public void setArea(String area) {
                this.area = area;
            }

            public String getParentId() {
                return parentId;
            }

            public void setParentId(String parentId) {
                this.parentId = parentId;
            }

            public String getSplitStatus() {
                return splitStatus;
            }

            public void setSplitStatus(String splitStatus) {
                this.splitStatus = splitStatus;
            }

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            public String getLat() {
                return lat;
            }

            public void setLat(String lat) {
                this.lat = lat;
            }

            public String getLng() {
                return lng;
            }

            public void setLng(String lng) {
                this.lng = lng;
            }

            public String getCreate_uid() {
                return create_uid;
            }

            public void setCreate_uid(String create_uid) {
                this.create_uid = create_uid;
            }

            public String getCreate_name() {
                return create_name;
            }

            public void setCreate_name(String create_name) {
                this.create_name = create_name;
            }

            public String getCreate_time() {
                return create_time;
            }

            public void setCreate_time(String create_time) {
                this.create_time = create_time;
            }

            public String getUpdate_uid() {
                return update_uid;
            }

            public void setUpdate_uid(String update_uid) {
                this.update_uid = update_uid;
            }

            public String getUpdate_name() {
                return update_name;
            }

            public void setUpdate_name(String update_name) {
                this.update_name = update_name;
            }

            public String getUpdate_time() {
                return update_time;
            }

            public void setUpdate_time(String update_time) {
                this.update_time = update_time;
            }

            public String getStatus() {
                return status;
            }

            public void setStatus(String status) {
                this.status = status;
            }

            public List<List<Double>> getMappingBorder() {
                return mappingBorder;
            }

            public void setMappingBorder(List<List<Double>> mappingBorder) {
                this.mappingBorder = mappingBorder;
            }

            public List<List<Double>> getStartBorder() {
                return startBorder;
            }

            public void setStartBorder(List<List<Double>> startBorder) {
                this.startBorder = startBorder;
            }

            public Boolean getSync() {
                return sync;
            }

            public void setSync(Boolean sync) {
                this.sync = sync;
            }

            public InfoBean() {
            }

            public InfoBean(DbBan dbBan) {
                try {
                    this.Id = dbBan.getId();
                    this.tid = dbBan.getTid();
                    this.title = dbBan.getTitle();
                    this.province = dbBan.getProvince();
                    this.city = dbBan.getCity();
                    this.district = dbBan.getDistrict();
                    this.area = dbBan.getArea();
                    this.parentId = dbBan.getParentId();
                    this.splitStatus = dbBan.getSplitStatus();
                    this.type = dbBan.getType();
                    this.lat = dbBan.getLat();
                    this.lng = dbBan.getLng();
                    this.create_uid = dbBan.getCreate_uid();
                    this.create_name = dbBan.getCreate_name();
                    this.create_time = dbBan.getCreate_time();
                    this.update_uid = dbBan.getUpdate_uid();
                    this.update_name = dbBan.getUpdate_name();
                    this.update_time = dbBan.getUpdate_time();
                    this.status = dbBan.getStatus();
                    this.insertTime = dbBan.getInsertTime();
                    this.sync = dbBan.getSync().equals("true") ? true : false;

                    if (!TextUtils.isEmpty(dbBan.getMappingBorder()) && !"null".equals(dbBan.getMappingBorder())) {
                        JSONArray mapping = new JSONArray(dbBan.getMappingBorder());
                        List<List<Double>> mappinglist = new ArrayList<List<Double>>();
                        for (int j = 0; j < mapping.length(); j++) {
                            ArrayList<Double> p = new ArrayList<Double>();
                            p.add(mapping.getJSONArray(j).getDouble(0));
                            p.add(mapping.getJSONArray(j).getDouble(1));
                            mappinglist.add(p);
                        }
                        this.mappingBorder = mappinglist;
                    }

                    if (!TextUtils.isEmpty(dbBan.getStartBorder()) && !"null".equals(dbBan.getStartBorder())) {
                        JSONArray start = new JSONArray(dbBan.getStartBorder());
                        List<List<Double>> startlist = new ArrayList<List<Double>>();
                        for (int j = 0; j < start.length(); j++) {
                            ArrayList<Double> p = new ArrayList<Double>();
                            p.add(start.getJSONArray(j).getDouble(0));
                            p.add(start.getJSONArray(j).getDouble(1));
                            startlist.add(p);
                        }
                        this.startBorder= startlist;
                    }
                    if (!TextUtils.isEmpty(dbBan.getObstacleBorder()) && !"null".equals(dbBan.getObstacleBorder())) {
                        JSONArray obstacle = new JSONArray(dbBan.getObstacleBorder());
                        List<List<List<Double>>> obstaclelist = new ArrayList<>();
                        for (int j = 0; j < obstacle.length(); j++) {
                            List<List<Double>> p = new ArrayList<>();
                            JSONArray obstacle1 = obstacle.getJSONArray(j);
                            for (int k = 0; k < obstacle1.length(); k++) {
                                List<Double> p1 = new ArrayList<Double>();
                                p1.add(obstacle1.getJSONArray(k).getDouble(0));
                                p1.add(obstacle1.getJSONArray(k).getDouble(1));
                                p.add(p1);
                            }
                            obstaclelist.add(p);
                        }
                        this.obstacleBorder = obstaclelist;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
