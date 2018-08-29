package com.skylin.uav.drawforterrain.setting_channel;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import com.skylin.uav.drawforterrain.adapter.BarrierAdapter;
import com.skylin.uav.drawforterrain.nofly.Point;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Moon on 2018/2/27.
 */

public class ObsMasterBan implements Serializable {

    private ArrayList<Point> arrayList ;
    private BarrierAdapter barrierAdapter;
    private RecyclerView recyclerView;

    public ObsMasterBan(ArrayList<Point> list, int tag, Context context) {
        this.arrayList = list;
        this.barrierAdapter = new BarrierAdapter(arrayList,tag,context);
    }

    public ArrayList<Point> getList() {
        return arrayList;
    }

    public void setList(ArrayList<Point> arrayList) {
        this.arrayList = arrayList;
    }

    public BarrierAdapter getAdapter() {
        return barrierAdapter;
    }

    public void setAdapter(BarrierAdapter barrierAdapter) {
        this.barrierAdapter = barrierAdapter;
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    public void setRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }
}
