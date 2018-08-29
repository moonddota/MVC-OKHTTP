package com.skylin.uav.drawforterrain.fragment;

import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;


public class MapEventsOverlay extends Overlay {

    private Context context;
    private MapEventsReceiver mReceiver;
    private int x;
    private int y;

    /**
     * @param ctx the context
     * @param receiver the object that will receive/handle the events.
     * It must implement MapEventsReceiver interface.
     */
    public MapEventsOverlay(Context ctx, int x, int y,MapEventsReceiver receiver) {
        super(ctx);
        this.context = ctx;
        this.x = x;
        this.y = y;
        mReceiver = receiver;
    }

    @Override
    public void draw(Canvas c, MapView osmv, boolean shadow) {

    }

    @Override public boolean onSingleTapUp(MotionEvent e, MapView mapView){
        Projection proj = mapView.getProjection();
//        IGeoPoint p = proj.fromPixels(e.getX(), e.getY());

        final Projection pj = mapView.getProjection();
        GeoPoint p = (GeoPoint) pj.fromPixels((int) e.getX(), (int) e.getY());


        mapView.getController().animateTo(p);

        return mReceiver.singleTapConfirmedHelper(p);
    }

    @Override public boolean onLongPress(MotionEvent e, MapView mapView) {
        Projection proj = mapView.getProjection();
//        IGeoPoint p = proj.fromPixels(e.getX(), e.getY());

        final Projection pj = mapView.getProjection();
        GeoPoint p = (GeoPoint) pj.fromPixels((int) e.getX(), (int) e.getY());

        //throw event to the receiver:
        return mReceiver.longPressHelper(p);
    }
}
