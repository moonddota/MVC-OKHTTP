package com.skylin.uav.drawforterrain.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.GpsStatus;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.baidu.mapapi.utils.DistanceUtil;
import com.baidu.mapapi.utils.SpatialRelationUtil;
import com.skylin.uav.drawforterrain.BaseActivity;
import com.skylin.uav.drawforterrain.nofly.Point;
import com.skylin.uav.drawforterrain.select.home.HomeActivity;
import com.skylin.uav.drawforterrain.select.home.ReplyActuvuty;
import com.skylin.uav.drawforterrain.setting_channel.DotActivity;
import com.skylin.uav.drawforterrain.setting_channel.DownMappingBan;
import com.skylin.uav.drawforterrain.setting_channel.GGABan;
import com.skylin.uav.drawforterrain.setting_channel.ObsMasterBan;
import com.skylin.uav.drawforterrain.setting_channel.ParticularsActivity;
import com.skylin.uav.drawforterrain.util.GPSUtil;
import com.skylin.uav.drawforterrain.util.ToastUtil;
import com.skylin.uav.R;

import org.osmdroid.api.IMapController;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.bing.BingMapTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import kotlin.jvm.JvmField;

import static com.blankj.utilcode.util.AppUtils.launchApp;

public class OsmFragment extends Fragment {
    private String[] mNeedPermissionsList = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    private MapView mapView;
    private BingMapTileSource bing;
    private ExecutorService threadPool;
    private Future<?> submit;
    private Boolean b = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return LayoutInflater.from(getActivity()).inflate(R.layout.fragent_osnmap, container, false);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        threadPool = Executors.newSingleThreadExecutor();


        mapView = view.findViewById(R.id.osm_map);
        BingMapTileSource.setBingKey("AkVvxfMfxw9gmNr7L75HDuOrqos2OppZk_iIsxpmy5OOYo5bs7U_E_tgOZwi6Oa-");
        bing = new BingMapTileSource(null);
        bing.setStyle(BingMapTileSource.IMAGERYSET_AERIALWITHLABELS);
        mapView.setTileSource(bing);
        mapView.setBuiltInZoomControls(false);
        mapView.setMultiTouchControls(true);
        IMapController mapController = mapView.getController();
        mapController.setZoom(19);
        mapController.setCenter(new GeoPoint(30.5082683633, 104.1176718517));


        b = getArguments().getBoolean("is_Click", false);
        setMapClick(b);

//        //比例尺配置
//        final DisplayMetrics dm = getResources().getDisplayMetrics();
//        ScaleBarOverlay  mScaleBarOverlay = new ScaleBarOverlay(mapView);
//        mScaleBarOverlay.setCentred(true);
//        mScaleBarOverlay.setAlignBottom(false); //底部显示
//        mScaleBarOverlay.setScaleBarOffset(dm.widthPixels / 5, 80);
//        mapView.getOverlays().add(mScaleBarOverlay);
    }

    public void setMapCanClick(boolean is_click) {

        if (is_click) {
            MapEventsOverlay mapEventOverlay = new MapEventsOverlay(getActivity(), 100, 100, new MapEventsReceiver() {

                @Override
                public boolean singleTapConfirmedHelper(final GeoPoint p) {
                    if (submit != null) {
                        submit.cancel(true);
                    }
                    submit = threadPool.submit(new Runnable() {
                        @Override
                        public void run() {
                            ArrayList<DownMappingBan.DataBean.InfoBean> new_list = new ArrayList();
                            ArrayList<DownMappingBan.DataBean.InfoBean> point_list = ParticularsActivity.getList();
                            for (int i = 0; i < point_list.size(); i++) {
                                DownMappingBan.DataBean.InfoBean infoBean = point_list.get(i);
                                ArrayList<LatLng> boundary_List = new ArrayList<>();
                                for (int j = 0; j < infoBean.getMappingBorder().size(); j++) {
                                    boundary_List.add(
                                            new LatLng(infoBean.getMappingBorder().get(j).get(0),
                                                    infoBean.getMappingBorder().get(j).get(1)));
                                }
                                boolean contain = SpatialRelationUtil.isPolygonContainsPoint(boundary_List, new LatLng(p.getLatitude(), p.getLongitude()));
                                if (contain) {
                                    new_list.add(infoBean);
                                }
                            }
                            if (new_list.size() > 0) {
                                for (int i = 0; i < new_list.size() - 1; i++) {
                                    for (int j = i + 1; j < new_list.size(); j++) {
                                        int a1 = Integer.parseInt(new_list.get(i).getId());
                                        int a2 = Integer.parseInt(new_list.get(j).getId());
                                        if (a1 < a2) {//如果队前日期靠前，调换顺序  
                                            DownMappingBan.DataBean.InfoBean infoBean = new_list.get(i);
                                            new_list.set(i, new_list.get(j));
                                            new_list.set(j, infoBean);
                                        }
                                    }
                                }
                                DotActivity.startDotActivity(getActivity(), false, true, new_list.get(0));
                                submit.cancel(true);
                            }
                        }
                    });
                    return false;
                }

                @Override
                public boolean longPressHelper(GeoPoint p) {
                    return false;
                }

            });
            mapView.getOverlays().add(mapEventOverlay);
        }
    }


    public void setMapClick(Boolean b) {
        if (b) {
            MapEventsOverlay mapEventOverlay = new MapEventsOverlay(getActivity(), 100, 100, new MapEventsReceiver() {
                @Override
                public boolean singleTapConfirmedHelper(final GeoPoint p) {
                    if (submit != null) {
                        submit.cancel(true);
                    }
                    submit = threadPool.submit(new Runnable() {
                        @Override
                        public void run() {
                            ArrayList<DownMappingBan.DataBean.InfoBean> new_list = new ArrayList();
                            ArrayList<DownMappingBan.DataBean.InfoBean> point_list = HomeActivity.getList();
                            if (point_list == null) {
                                return;
                            }
                            for (int i = 0; i < point_list.size(); i++) {
                                DownMappingBan.DataBean.InfoBean infoBean = point_list.get(i);
                                ArrayList<LatLng> boundary_List = new ArrayList<>();
                                for (int j = 0; j < infoBean.getMappingBorder().size(); j++) {
                                    boundary_List.add(
                                            new LatLng(infoBean.getMappingBorder().get(j).get(0),
                                                    infoBean.getMappingBorder().get(j).get(1)));
                                }
                                boolean contain = SpatialRelationUtil.isPolygonContainsPoint(boundary_List, new LatLng(p.getLatitude(), p.getLongitude()));
                                if (contain) {
                                    new_list.add(infoBean);
                                }
                            }
                            if (new_list.size() > 0) {
                                for (int i = 0; i < new_list.size() - 1; i++) {
                                    for (int j = i + 1; j < new_list.size(); j++) {
                                        int a1 = Integer.parseInt(new_list.get(i).getId());
                                        int a2 = Integer.parseInt(new_list.get(j).getId());
                                        if (a1 < a2) {//如果队前日期靠前，调换顺序  
                                            DownMappingBan.DataBean.InfoBean infoBean = new_list.get(i);
                                            new_list.set(i, new_list.get(j));
                                            new_list.set(j, infoBean);
                                        }
                                    }
                                }
                                if (mapClickListener != null) {
                                    mapClickListener.onMapClickListener(new_list, mapView);
                                }
                                submit.cancel(true);
                            }
                        }
                    });
                    return false;
                }

                @Override
                public boolean longPressHelper(GeoPoint p) {
                    return false;
                }

            });
            mapView.getOverlays().add(mapEventOverlay);
        }
    }


    public MapView getMap() {
        return mapView;
    }


    @Override
    public void onStart() {
        super.onStart();
//        sjj.alog.Log.e("onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
//        sjj.alog.Log.e("onResume");

        HomeActivity.statrc_list.INSTANCE.setMapResum(true);
        ReplyActuvuty.statrc_list.INSTANCE.setMapResum(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onResume();
//        sjj.alog.Log.e("onPause");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDetach();
        mapView = null;
        LocationUtils.getInstance(getActivity()).removeLocationUpdatesListener();
//        sjj.alog.Log.e("onDestroy");
    }


    public void setLoaction(GGABan ggaBan) {
        try {
            if (ggaBan != null) {
                if (ggaBan.isEmpty()) {
                    setLM();
                } else
                    mapView.getController().setCenter(toGeoPoint(new Point(ggaBan.getLon(), ggaBan.getLat())));
            } else {
                setLM();
            }
        } catch (Exception e) {
        }
    }

    private void setLM() {
        if (EasyPermissionsEx.hasPermissions(getActivity(), mNeedPermissionsList)) {
            initLocation();
        } else {
            EasyPermissionsEx.requestPermissions(getActivity(), getString(R.string.OsmFragment_tv1), 1, mNeedPermissionsList);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initLocation();
                } else {
                    if (EasyPermissionsEx.somePermissionPermanentlyDenied(this, mNeedPermissionsList)) {
                        EasyPermissionsEx.goSettings2Permissions(this, getString(R.string.OsmFragment_tv2)
                                , getString(R.string.OsmFragment_tv3), 1);
                    }
                }
            }
            break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 1) {
            ToastUtil.show("settings");
        }
    }

    private void initLocation() {

        boolean oPen = GPSUtil.isOPen();
//        sjj.alog.Log.e(oPen);

        if (!oPen) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent, 1315);
//            GPSUtil.openGPS();
            return;
        }

        LocationUtils.getInstance(getActivity()).removeLocationUpdatesListener();
        LocationUtils.getInstance(getActivity()).initLocation(new LocationHelper() {
            @Override
            public void UpdateLocation(Location location) {
            }

            @Override
            public void UpdateStatus(String provider, int status, Bundle extras) {
            }

            @Override
            public void UpdateGPSStatus(GpsStatus pGpsStatus) {

            }

            @Override
            public void UpdateLastLocation(Location location) {
                GeoPoint mPoint = new GeoPoint(location.getLatitude(), location.getLongitude());

                mapView.getController().setCenter(mPoint);
                LocationUtils.getInstance(getActivity()).removeLocationUpdatesListener();
            }
        });
    }

    private Marker L_marker;

    public void setLoaction(double lat, double lon, int i) {
        try {
            switch (i) {
                case 0:
                    mapView.getController().setCenter(new GeoPoint(lat, lon));
                    break;
                case 1:
                    if (L_marker != null) mapView.getOverlays().remove(L_marker);
                    Marker marker = new Marker(mapView);
                    marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(Marker marker, MapView mapView) {
                            return false;
                        }
                    });
                    marker.setIcon(getActivity().getResources().getDrawable(R.mipmap.icon_location));
                    marker.setPosition(new GeoPoint(lat, lon));
                    mapView.getOverlays().add(marker);
                    mapView.invalidate();
                    L_marker = marker;
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
        }

    }

    public void setZoom(ArrayList<Point> boundary_List, Point centerPoint) {
        if (boundary_List.size() < 2) {
            return;
        }
        ArrayList<Double> distance_list = new ArrayList<>();
        for (Point point : boundary_List) {
            distance_list.add(DistanceUtil.getDistance(
                    new LatLng(centerPoint.getLatitude(), centerPoint.getLongitude()),
                    new LatLng(point.getLatitude(), point.getLongitude())));
        }
        for (int i = 0; i < distance_list.size(); i++) {
            for (int j = 1; j < distance_list.size(); j++) {
                double a1 = distance_list.get(i);
                double a2 = distance_list.get(j);
                if (a1 < a2) {//如果队前日期靠前，调换顺序  
                    double a3 = distance_list.get(i);
                    distance_list.set(i, distance_list.get(j));
                    distance_list.set(j, a3);
                }
            }
        }

        double i = distance_list.get(0);

        if (i < 36) {
            mapView.getController().setZoom(20);
        } else if (i >= 36 && i < 71) {
            mapView.getController().setZoom(19);
        } else if (i >= 71 && i < 142) {
            mapView.getController().setZoom(18);
        } else if (i >= 142 && i < 300) {
            mapView.getController().setZoom(17);
        } else if (i >= 300 && i < 600) {
            mapView.getController().setZoom(16);
        } else {
            mapView.getController().setZoom(15);
        }
    }


    public int setMapMopType() {
        String style = bing.getStyle();
        if (style.equals(BingMapTileSource.IMAGERYSET_AERIALWITHLABELS)) {
            bing.setStyle(BingMapTileSource.IMAGERYSET_ROAD);
            invalidate();
            return 1;
        } else {
            bing.setStyle(BingMapTileSource.IMAGERYSET_AERIALWITHLABELS);
            invalidate();
            return 0;
        }
    }

    private List<Overlay> overlay_boundary = new ArrayList<>();

    public void addBoundary(ArrayList<Point> list, int index) {

        for (Overlay overlay : overlay_boundary) {
            mapView.getOverlays().remove(overlay);
        }
        overlay_boundary.clear();

        List<GeoPoint> pts = new ArrayList<>();
        if (list.size() == 2) {
            pts.add(toGeoPoint(list.get(0)));
            pts.add(toGeoPoint(list.get(1)));
            Polyline polyline = new Polyline();
            polyline.setWidth(5);
            polyline.setColor(0xffdbad89);
            polyline.setPoints(pts);
            mapView.getOverlays().add(polyline);
            overlay_boundary.add(polyline);
        } else if (list.size() > 2) {
            for (Point point : list) {
                pts.add(toGeoPoint(point));
            }
            Polygon polygon = new Polygon();
            polygon.setFillColor(0x88ede4dc);
            polygon.setStrokeColor(0xffdbad89);
            polygon.setStrokeWidth(5);
            polygon.setPoints(pts);
            mapView.getOverlays().add(polygon);
            overlay_boundary.add(polygon);
        }

        for (int i = 0; i < list.size(); i++) {
            Marker marker = new Marker(mapView);
            marker.setId("boundary," + i);

            if (i == index)
                marker.setIcon(getResources().getDrawable(R.mipmap.icon_selected));
            else
                marker.setIcon(getResources().getDrawable(R.mipmap.icon_dot));

            marker.setPosition(toGeoPoint(list.get(i)));
            marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker, MapView mapView) {
                    if (markerClickListener != null) {
                        markerClickListener.onMarkerClickListener(marker, mapView);
                    }
                    return false;
                }
            });
            mapView.getOverlays().add(marker);
            overlay_boundary.add(marker);

        }
        invalidate();
    }

    private List<Overlay> overlay_obstacie = new ArrayList<>();

    private void addObstacie(ArrayList<ObsMasterBan> obs_list, int index, int position) {
        for (Overlay overlay : overlay_obstacie) {
            mapView.getOverlays().remove(overlay);
        }
        overlay_obstacie.clear();

        for (int i = 0; i < obs_list.size(); i++) {
            if (obs_list.get(i).getList().size() == 2) {
                List<GeoPoint> points = new ArrayList<>();
                points.add(toGeoPoint(obs_list.get(i).getList().get(0)));
                points.add(toGeoPoint(obs_list.get(i).getList().get(1)));
                Polyline polyline = new Polyline();
                polyline.setWidth(5);
                polyline.setColor(0xffed6262);
                polyline.setPoints(points);
                mapView.getOverlays().add(polyline);
                overlay_obstacie.add(polyline);
            } else if (obs_list.get(i).getList().size() > 2) {
//                if (i != index) {
                List<GeoPoint> pts11 = new ArrayList<>();
                for (Point point : obs_list.get(i).getList()) {
                    pts11.add(toGeoPoint(point));
                }
                Polygon polygon = new Polygon();
                polygon.setFillColor(0x88eccac4);
                polygon.setStrokeColor(0xffed6262);
                polygon.setStrokeWidth(5);
                polygon.setPoints(pts11);
                mapView.getOverlays().add(polygon);
                overlay_obstacie.add(polygon);
//                }
//                else {
//                    List<GeoPoint> pts11 = new ArrayList<>();
//                    for (Point point : obs_list.get(i).getList()) {
//                        pts11.add(toGeoPoint(point));
//                    }
//                    Polygon polygon = new Polygon();
//                    polygon.setFillColor(0xffeccac4);
//                    polygon.setStrokeColor(0xffed6262);
//                    polygon.setStrokeWidth(5);
//                    polygon.setPoints(pts11);
//                    mapView.getOverlays().add(polygon);
//                    overlay_obstacie.add(polygon);
//                }
            }
            if (obs_list.get(i).getList().size() >= 0) {
                for (int j = 0; j < obs_list.get(i).getList().size(); j++) {
                    Marker marker = new Marker(mapView);
                    marker.setId("obs," + i + "," + j);
                    marker.setPosition(toGeoPoint(obs_list.get(i).getList().get(j)));

                    if (index == i && position == j)
                        marker.setIcon(getActivity().getResources().getDrawable(R.mipmap.icon_selected));
                    else
                        marker.setIcon(getActivity().getResources().getDrawable(R.mipmap.icon_obs));

                    marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(Marker marker, MapView mapView) {
                            if (markerClickListener != null) {
                                markerClickListener.onMarkerClickListener(marker, mapView);
                            }
                            return false;
                        }
                    });

                    mapView.getOverlays().add(marker);
                    overlay_obstacie.add(marker);
                }
            }
        }
        invalidate();
    }


    private List<Overlay> ss_overlay_boundary = new CopyOnWriteArrayList<>();

    public void drawList(ArrayList<ArrayList<Point>> draw_list) {
        for (Overlay overlay : ss_overlay_boundary) {
            mapView.getOverlays().remove(overlay);
        }
        ss_overlay_boundary.clear();
        for (int i = 0; i < draw_list.size(); i++) {
            if (i > 100) {
                return;
            }
            ArrayList<Point> list = draw_list.get(i);
            if (list.size() > 2) {
                List<GeoPoint> pts = new ArrayList<>();
                for (Point point : list) {
                    pts.add(toGeoPoint(point));
                }
                Polygon polygon = new Polygon();
                polygon.setFillColor(0x88ede4dc);
                polygon.setStrokeColor(0xffdbad89);
                polygon.setStrokeWidth(5);
                polygon.setPoints(pts);
                mapView.getOverlays().add(polygon);
                ss_overlay_boundary.add(polygon);
            }
        }
        invalidate();
    }

    public void DrawNoFly() {
        List<List<Point>> noFlyList = BaseActivity.second;
        for (int i = 0; i < noFlyList.size(); i++) {
            if (noFlyList.get(i).size() > 0) {
                List<GeoPoint> pts = new ArrayList<>();
                for (int j = 0; j < noFlyList.get(i).size(); j++) {
                    pts.add(toGeoPoint(noFlyList.get(i).get(j)));
                }
                Polygon polygon = new Polygon();
                polygon.setFillColor(0x88ff0000);
                polygon.setStrokeColor(0xFFff0000);
                polygon.setStrokeWidth(5);
                polygon.setPoints(pts);
                mapView.getOverlays().add(polygon);
            }
        }
        invalidate();
    }

    public void DrawNoFly(List<LatLng> latLngs) {
        ArrayList<GeoPoint> pts = new ArrayList<>();
        for (LatLng latLng : latLngs) {
            pts.add(new GeoPoint(latLng.latitude, latLng.longitude));
        }
        Polygon polygon = new Polygon();
        polygon.setFillColor(0x88ff0000);
        polygon.setStrokeColor(0xFFff0000);
        polygon.setStrokeWidth(5);
        polygon.setPoints(pts);
        mapView.getOverlays().add(polygon);
        invalidate();
    }


    public void DrawMapping(ArrayList<Point> boundary_List, int b_index,
                            ArrayList<ObsMasterBan> obs_list, int obs_index, int obs_position) {
        addBoundary(boundary_List, b_index);
        addObstacie(obs_list, obs_index, obs_position);
    }

    private void invalidate() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mapView.invalidate();
            }
        });
    }


    public void clearMap() {
        mapView.getOverlays().clear();
        overlay_boundary.clear();
        invalidate();
    }


    public static GeoPoint toGeoPoint(Point point) {
        return new GeoPoint(point.getLatitude(), point.getLongitude());
    }

    public static Point toPoint(GeoPoint geoPoint) {
        return new Point(geoPoint.getLongitude(), geoPoint.getLatitude());
    }

    public static GeoPoint ConverterToGPS(double lat, double lng) {

//         将GPS设备采集的原始GPS坐标转换成百度坐标
        CoordinateConverter converter = new CoordinateConverter();
        converter.from(CoordinateConverter.CoordType.GPS);
        // sourceLatLng待转换坐标
        converter.coord(new LatLng(lat, lng));
        LatLng desLatLng = converter.convert();
        return new GeoPoint(desLatLng.latitude, desLatLng.longitude);

//
//        double x_pi = 3.14159265358979324 * 3000.0 / 180.0;
//        double x = lng - 0.0065, y = lat - 0.006;
//        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * x_pi);
//        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * x_pi);
//        lng = z * Math.cos(theta);
//        lat = z * Math.sin(theta);
//
//       return new GeoPoint(lat,lng);
    }


    private MarkerClickListener markerClickListener;

    public interface MarkerClickListener {
        void onMarkerClickListener(Marker marker, MapView mapView);

    }

    //设置点击回调接口
    public void setMarkerClickListener(MarkerClickListener markerClickListener) {
        this.markerClickListener = markerClickListener;
    }


    private MapClickListener mapClickListener;

    public interface MapClickListener {
        void onMapClickListener(ArrayList<DownMappingBan.DataBean.InfoBean> list, MapView mapView);
    }

    //设置点击回调接口
    public void setMapClickListener(MapClickListener mapClickListener) {
        this.mapClickListener = mapClickListener;
    }


}
