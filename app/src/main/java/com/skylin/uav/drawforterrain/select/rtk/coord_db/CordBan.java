package com.skylin.uav.drawforterrain.select.rtk.coord_db;

/**
 * Created by moon on 2017/11/20.
 */

public class CordBan {


    public CordBan() {
    }

    public CordBan(SyncBan syncBan){
        this._id = Integer.parseInt(syncBan.getId());
       this.name=syncBan.getTitle();
        this.time=syncBan.getCreateTime();
        this.lat=syncBan.getLat();
        this.lon=syncBan.getLng();
        this.alt=syncBan.getHeight();
        this.city=syncBan.getCity();
        this.province=syncBan.getProvince();
        this.district=syncBan.getDistrict();
    }

    public CordBan(int _id, String time, String name, String lat, String lon, String alt, String isSync, String province, String city, String district) {
        this._id = _id;
        this.time = time;
        this.name = name;
        this.lat = lat;
        this.lon = lon;
        this.alt = alt;
        this.isSync = isSync;
        this.province = province;
        this.city = city;
        this.district = district;
    }

    private String NULL = "";

    private int _id ;
    //时间
    private String time= "";
    //名字
    private String name= "";
    //纬度
    private String lat= "";
    //经度
    private String lon= "";
    //高度
    private String alt= "";
    //是否同步了
    private String isSync= "";
    //省
    private  String province= "";
    //市
    private  String city= "";
    //县
    private  String district= "";

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public String getAlt() {
        return alt;
    }

    public void setAlt(String alt) {
        this.alt = alt;
    }

    public String getIsSync() {
        return isSync;
    }

    public void setIsSync(String isSync) {
        this.isSync = isSync;
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

    @Override
    public String toString() {
        return "Coord_Ban{" +
                "_id=" + _id +
                ", time='" + time + '\'' +
                ", name='" + name + '\'' +
                ", lat='" + lat + '\'' +
                ", lon='" + lon + '\'' +
                ", alt='" + alt + '\'' +
                ", isSync='" + isSync + '\'' +
                ", province='" + province + '\'' +
                ", city='" + city + '\'' +
                ", district='" + district + '\'' +
                '}';
    }
}
