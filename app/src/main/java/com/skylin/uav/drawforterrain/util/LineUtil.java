package com.skylin.uav.drawforterrain.util;

import com.skylin.uav.drawforterrain.nofly.Point;
import com.skylin.uav.drawforterrain.nofly.Segment;
import com.skylin.uav.drawforterrain.setting_channel.ObsMasterBan;

import java.util.ArrayList;
import java.util.List;

import sjj.alog.Log;
import task.model.Polygon;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Created by Moon on 2018/3/14.
 * 判断两条直线是否相交
 */

public class LineUtil {

    public static void judge(ArrayList<ObsMasterBan> obs_list, int index, int position) throws Exception {
//        Log.e("1");
        ArrayList<Point> list = obs_list.get(index).getList();
        if (obs_list.size() > 1) {
            if (list.size() > 1) {
                ArrayList<Segment> list_line = new ArrayList<>();
                for (int i = 0; i < obs_list.size(); i++) {
                    if (i != index) {
                        ArrayList<Point> list1 = obs_list.get(i).getList();
                        if (list1.size() > 1) {

                            Polygon polygon = new Polygon(list1, 4.0);     //向外扩大4.0
                            polygon.init(0);
                            List<Point> safeFramePoint = polygon.getSafeFramePoint();

                            for (int j = 0; j < safeFramePoint.size() - 1; j++) {
                                list_line.add(new Segment(safeFramePoint.get(j), safeFramePoint.get(j + 1)));
                                if (j == 0) {
                                    list_line.add(new Segment(safeFramePoint.get(j), safeFramePoint.get(safeFramePoint.size() - 1)));
                                }
                            }
                        }
                    }
                }
                ArrayList<Segment> list_line1 = new ArrayList<>();
                if (list.size() == 2) {
                    list_line1.add(new Segment(list.get(0), list.get(1)));
                } else if (list.size() > 2) {
                    if (position == 0) {
                        list_line1.add(new Segment(list.get(0), list.get(1)));
                        list_line1.add(new Segment(list.get(0), list.get(list.size() - 1)));
                    } else if (position == list.size() - 1) {
                        list_line1.add(new Segment(list.get(0), list.get(list.size() - 1)));
                        list_line1.add(new Segment(list.get(list.size() - 2), list.get(list.size() - 1)));
                    } else {
                        list_line1.add(new Segment(list.get(position), list.get(position - 1)));
                        list_line1.add(new Segment(list.get(position), list.get(position + 1)));
                    }
                }
                nod(list_line, list_line1);

            }
        }
    }

    public static void nod(ArrayList<Segment> lines, ArrayList<Segment> new_lines) throws Exception {
        for (int i = 0; i < new_lines.size(); i++) {
            for (int j = 0; j < lines.size(); j++) {
                if (new_lines.get(i).intersection(lines.get(j)) != null)
                    throw new RuntimeException("障碍相交，不能生成，建议合并");
            }
        }
    }

    public static void judgeList(ArrayList<Point> list) throws Exception {
        ArrayList<Segment> list_line = new ArrayList<>();
        ArrayList<Point> list_point = new ArrayList<>();
        for (int i = 0; i < list.size() - 1; i++) {
            if (i == 0) list_line.add(new Segment(list.get(i), list.get(list.size() - 1)));
            list_line.add(new Segment(list.get(i), list.get(i + 1)));
        }
        nodList(list_line);
    }

    public static void nodList(ArrayList<Segment> lines) throws Exception {
        for (int i = 0; i < lines.size(); i++) {
            for (int j = 0; j < lines.size(); j++) {

                if (i == 0) {
                    if (j != 0 && j != 1 && j != lines.size() - 1) {
                        Point intersection = lines.get(i).intersection(lines.get(j));
                        if (intersection != null) {
                            Log.e(i + "  " + j);
                            throw new RuntimeException("图形交叉了，请检查后重新打点");
                        }
                    }
                } else if (i == lines.size() - 1) {
                    if (j != lines.size() - 1 && j != 0 && j != lines.size() - 2) {
                        Point intersection = lines.get(i).intersection(lines.get(j));
                        if (intersection != null) {
                            Log.e(i + "  " + j);
                            throw new RuntimeException("图形交叉了，请检查后重新打点");
                        }
                    }
                } else {
                    if (j != i && j != i + 1 && j != i - 1) {
                        Point intersection = lines.get(i).intersection(lines.get(j));
                        if (intersection != null) {
                            Log.e(i + "  " + j);
                            throw new RuntimeException("图形交叉了，请检查后重新打点");
                        }
                    }

                }
            }
        }
    }

}
