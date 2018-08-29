package com.skylin.uav.drawforterrain.setting_channel.db;

import com.skylin.uav.drawforterrain.setting_channel.DownMappingBan;

import org.json.JSONArray;

import java.util.List;

import sjj.alog.Log;

/**
 * Created by Moon on 2018/3/5.
 */

public class DbBan {
    private String NULL = "";
    private int _id;
    private String Id = NULL;
    private String tid= NULL ;
    private String title= NULL;
    private String province= NULL;
    private String city= NULL;
    private String district= NULL;
    private String area= NULL;
    private String parentId= NULL;
    private String splitStatus= NULL;
    private String type= NULL;
    private String lat= NULL;
    private String lng= NULL;
    private String create_uid= NULL;
    private String create_name= NULL;
    private String create_time= NULL;
    private String update_uid= NULL;
    private String update_name= NULL;
    private String update_time= "0";
    private String status= NULL;
    private String obstacleBorder= NULL;
    private String mappingBorder= NULL;
    private String startBorder= NULL;
    private String sync = "false";
    private String InsertTime= NULL;

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
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

    public String getObstacleBorder() {
        return obstacleBorder;
    }

    public void setObstacleBorder(String obstacleBorder) {
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

    public String getMappingBorder() {
        return mappingBorder;
    }

    public void setMappingBorder(String mappingBorder) {
        this.mappingBorder = mappingBorder;
    }

    public String getStartBorder() {
        return startBorder;
    }

    public void setStartBorder(String startBorder) {
        this.startBorder = startBorder;
    }

    public String getSync() {
        return sync;
    }

    public void setSync(String sync) {
        this.sync = sync;
    }

    public String getInsertTime() {
        return InsertTime;
    }

    public void setInsertTime(String insertTime) {
        InsertTime = insertTime;
    }

    public DbBan() {
    }

    public DbBan(DownMappingBan.DataBean.InfoBean infoBean, String InsertTime) {

        this.Id = infoBean.getId();
        this.tid = infoBean.getTid();
        this.title = infoBean.getTitle();
        this.province = infoBean.getProvince();
        this.city = infoBean.getCity();
        this.district = infoBean.getDistrict();
        this.area = infoBean.getArea();
        this.parentId = infoBean.getParentId();
        this.splitStatus = infoBean.getSplitStatus();
        this.type = infoBean.getType();
        this.lat = infoBean.getLat();
        this.lng = infoBean.getLng();
        this.create_uid = infoBean.getCreate_uid();
        this.create_name = infoBean.getCreate_name();
        this.create_time = infoBean.getCreate_time();
        this.update_uid = infoBean.getUpdate_uid();
        this.update_name = infoBean.getUpdate_name();
        this.update_time = infoBean.getUpdate_time();
        this.status = infoBean.getStatus();
        this.sync = infoBean.getSync() ? "true" : "false";
        this.InsertTime = InsertTime;


        JSONArray mapping = new JSONArray();
        JSONArray start = new JSONArray();
        JSONArray obstacle = new JSONArray();

        for (int j = 0; j < infoBean.getMappingBorder().size(); j++) {
            mapping.put(new JSONArray().put(infoBean.getMappingBorder().get(j).get(0)).put(infoBean.getMappingBorder().get(j).get(1)));
        }
        for (int j = 0; j < infoBean.getStartBorder().size(); j++) {
            start.put(new JSONArray().put(infoBean.getStartBorder().get(j).get(0)).put(infoBean.getStartBorder().get(j).get(1)));
        }

        List<List<List<Double>>> obs = infoBean.getObstacleBorder();
        if (obs != null) {
            for (int j = 0; j < obs.size(); j++) {
                JSONArray obstacle1 = new JSONArray();
                for (int k = 0; k < obs.get(j).size(); k++) {
                    obstacle1.put(new JSONArray().put(obs.get(j).get(k).get(0)).put(obs.get(j).get(k).get(1)));
                }
                obstacle.put(obstacle1);
            }
        }


        this.obstacleBorder = obstacle.toString();
        this.mappingBorder = mapping.toString();
        this.startBorder = start.toString();
    }


    @Override
    public String toString() {
        return "DbBan{" +
                "_id=" + _id +
                ", Id='" + Id + '\'' +
                ", tid='" + tid + '\'' +
                ", title='" + title + '\'' +
                ", province='" + province + '\'' +
                ", city='" + city + '\'' +
                ", district='" + district + '\'' +
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
                ", obstacleBorder='" + obstacleBorder + '\'' +
                ", mappingBorder='" + mappingBorder + '\'' +
                ", startBorder='" + startBorder + '\'' +
                ", sync='" + sync + '\'' +
                ", InsertTime='" + InsertTime + '\'' +
                '}';
    }
}
