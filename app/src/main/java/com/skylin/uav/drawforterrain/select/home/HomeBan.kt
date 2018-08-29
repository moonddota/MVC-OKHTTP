package com.skylin.uav.drawforterrain.select.home

import com.skylin.uav.drawforterrain.setting_channel.DownMappingBan
import com.skylin.uav.drawforterrain.views.Pair
import org.osmdroid.util.GeoPoint

class HomeBan {
    var mode: Int = 0
    var draw_open: Boolean = false
    var pair  = Pair<Boolean,DownMappingBan.DataBean.InfoBean>(false,null)
    var pp : GeoPoint? = GeoPoint(0.0,0.0)
    var is_up :Boolean = false


    constructor()


}