package com.skylin.uav.drawforterrain.select.rtk.coord_db;

/**
 * Created by wh on 2017/3/28.
 */

public class SyncBan {


    /**
     * id : 1730
     * title : 今天
     * province : 四川省
     * city : 成都市
     * district : 双流县
     * lat : 30.5454540000
     * lng : 141.5656585693
     * height : 500.8559875488
     * createTime : 2017-11-23 15:38:52
     */

    private String id;
    private String title;
    private String province;
    private String city;
    private String district;
    private String lat;
    private String lng;
    private String height;
    private String createTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "SyncBan{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", province='" + province + '\'' +
                ", city='" + city + '\'' +
                ", district='" + district + '\'' +
                ", lat='" + lat + '\'' +
                ", lng='" + lng + '\'' +
                ", height='" + height + '\'' +
                ", createTime='" + createTime + '\'' +
                '}';
    }
}
