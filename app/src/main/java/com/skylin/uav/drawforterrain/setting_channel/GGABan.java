package com.skylin.uav.drawforterrain.setting_channel;

import android.text.TextUtils;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by moon on 2018/1/3.
 */

public class GGABan implements Serializable {
    private final String NUll = "";
    private double hdop = 0.0;
    private String rtk = NUll;
    private double lat = 0.0;
    private double lon = 0.0;
    private double alt = 0.0;
    private String satellites = NUll;
    private int channel = -1;
    private String workmoder = NUll;
    private String workstate = NUll;

    @Override
    public String toString() {
        return "GGABan{" +
                "hdop='" + hdop + '\'' +
                ", rtk='" + rtk + '\'' +
                ", lat='" + lat + '\'' +
                ", lon='" + lon + '\'' +
                ", alt='" + alt + '\'' +
                ", satellites='" + satellites + '\'' +
                ", channel='" + channel + '\'' +
                ", workmoder='" + workmoder + '\'' +
                ", workstate='" + workmoder + '\'' +
                '}';
    }

    public GGABan() {
    }
    public GGABan(GGABan ggaBan) {
        this.hdop = ggaBan.getHdop();
        this.rtk = ggaBan.getRtk();
        this.lat = ggaBan.getLat();
        this.lon = ggaBan.getLon();
        this.alt = ggaBan.getAlt();
        this.satellites = ggaBan.getSatellites();
        this.channel = ggaBan.getChannel();
        this.workmoder = ggaBan.getWorkmoder();
        this.workstate = ggaBan.getWorkstate();
    }

    public GGABan(Double hdop, String rtk, Double lat, Double lon, Double alt, String satellites) {
        this.hdop = hdop;
        this.rtk = rtk;
        this.lat = lat;
        this.lon = lon;
        this.alt = alt;
        this.satellites = satellites;
    }

    public void setDatas(HashMap<String, String> HashMap) {
        this.hdop = Double.parseDouble(HashMap.get("hdop"));
        this.rtk = HashMap.get("rtk");
        this.lat = Double.parseDouble(HashMap.get("lat"));
        this.lon = Double.parseDouble(HashMap.get("lon"));
        this.alt = Double.parseDouble(HashMap.get("alt"));
        this.satellites = HashMap.get("satellites");
        setTime();
    }

    public boolean isEmpty() {
        if (hdop==0.0 | TextUtils.isEmpty(rtk) | lat ==0.0
                | lon==0.0 | alt==0.0| TextUtils.isEmpty(satellites))
            return true;
        else
            return false;
    }


    public void setGGAEmpty() {
        hdop = 0.0;
        rtk = NUll;
        lat = 0.0;
        lon = 0.0;
        alt = 0.0;
        satellites = NUll;
    }

    public void setEmpty() {
        hdop = 0.0;
        rtk = NUll;
        lat = 0.0;
        lon = 0.0;
        alt = 0.0;
        satellites = NUll;
        channel = -1;
        workmoder = NUll;
        workstate = NUll;
    }

    public Double getHdop() {
        return hdop;
    }

    public void setHdop(Double hdop) {
        this.hdop = hdop;
    }

    public String getRtk() {
        return rtk;
    }

    public void setRtk(String rtk) {
        this.rtk = rtk;
    }

    public Double getLat() {
        return lat;
    }


    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public Double getAlt() {
        return alt;
    }

    public void setAlt(Double alt) {
        this.alt = alt;
    }

    public String getSatellites() {
        return satellites;
    }

    public void setSatellites(String satellites) {
        this.satellites = satellites;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public String getWorkmoder() {
        return workmoder;
    }

    public void setWorkmoder(String workmoder) {
        this.workmoder = workmoder;
    }

    public String getWorkstate() {
        return workstate;
    }

    public void setWorkstate(String workstate) {
        this.workstate = workstate;
    }


    private int time = 0;

    public int getTime() {
        return time;
    }

    public void setTime() {
        time += 1;
        if (time == 4)
            time = 0;
    }

}
