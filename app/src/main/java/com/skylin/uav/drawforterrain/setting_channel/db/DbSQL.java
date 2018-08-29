package com.skylin.uav.drawforterrain.setting_channel.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.text.TextUtils;
import android.util.Log;

import com.skylin.uav.drawforterrain.util.LogUtils;


import java.util.ArrayList;

/**
 * Created by Moon on 2018/3/5.
 */

public class DbSQL extends SQLiteOpenHelper {

    /**
     * @param context 上下文
     * @param name    数据库名称
     * @param factory 游标工厂
     * @param version 数据库版本
     */
    private String name;

    public DbSQL(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.name = name;
    }

    // 创建数据库
    @Override
    public void onCreate(SQLiteDatabase db) {

        String sql = "create table " + name + "(_id integer Primary Key autoincrement" +
                ", Id varchar(20)" +
                ", tid varchar(20)" +
                ", title varchar(20)" +
                ", province varchar(20)" +
                ", city varchar(20)" +
                ", district varchar(20)" +
                ", area varchar(20)" +
                ", parentId varchar(20)" +
                ", splitStatus varchar(20)" +
                ", type varchar(20)" +
                ", lat varchar(20)" +
                ", lng varchar(20)" +
                ", create_uid varchar(20)" +
                ", create_name varchar(20)" +
                ", create_time varchar(20)" +
                ", update_uid varchar(20)" +
                ", update_name varchar(20)" +
                ", update_time varchar(20)" +
                ", status varchar(20)" +
                ", mappingBorder varchar(20)" +
                ", obstacleBorder varchar(20)" +
                ", startBorder varchar(20)" +
                ", sync varchar(20)" +
                ",InsertTime varchar(20))";
        db.execSQL(sql);
    }

    public static String sqliteEscape(String keyWord) {

        if (TextUtils.isEmpty(keyWord)) return keyWord;
        keyWord = keyWord.replace("/", "//");
        keyWord = keyWord.replace("'", "''");
        keyWord = keyWord.replace("[", "/[");
        keyWord = keyWord.replace("]", "/]");
        keyWord = keyWord.replace("%", "/%");
        keyWord = keyWord.replace("&", "/&");
        keyWord = keyWord.replace("_", "/_");
        keyWord = keyWord.replace("(", "/(");
        keyWord = keyWord.replace(")", "/)");
        return keyWord;
    }

    // 数据库更新
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        if (oldVersion == 1 && newVersion == 2) {//升级判断,如果再升级就要再加两个判断,从1到3,从2到3  
//            Log.e("SqliteHelper", "数据库更新");
//            db.execSQL("ALTER TABLE "+name+" ADD phone varchar(20)");
//        }
    }

    // 添加到数据库
    public void addBan(DbBan dbBan) {
        SQLiteDatabase db = getWritableDatabase(); // 以读写的形式打开数据库
//        String sql = "insert into " + name + "(Id,tid,title,province,city,district,area,parentId" +
//                ",splitStatus,type,lat,lng,create_uid,create_name,create_time" +
//                ",update_uid,update_name,update_time,status,mappingBorder,obstacleBorder" +
//                ",startBorder,sync,InsertTime) values("
//                + String.format("'%s'", sqliteEscape(dbBan.getId())) + ","
//                + String.format("'%s'", sqliteEscape(dbBan.getTid())) + ","
//                + String.format("'%s'", sqliteEscape(dbBan.getTitle())) + ","
//                + String.format("'%s'", sqliteEscape(dbBan.getProvince())) + ","
//                + String.format("'%s'", sqliteEscape(dbBan.getCity())) + ","
//                + String.format("'%s'", sqliteEscape(dbBan.getDistrict())) + ","
//                + String.format("'%s'", sqliteEscape(dbBan.getArea())) + ","
//                + String.format("'%s'", sqliteEscape(dbBan.getParentId())) + ","
//                + String.format("'%s'", sqliteEscape(dbBan.getSplitStatus())) + ","
//                + String.format("'%s'", sqliteEscape(dbBan.getType())) + ","
//                + String.format("'%s'", sqliteEscape(dbBan.getLat())) + ","
//                + String.format("'%s'", sqliteEscape(dbBan.getLng())) + ","
//                + String.format("'%s'", sqliteEscape(dbBan.getCreate_uid())) + ","
//                + String.format("'%s'", sqliteEscape(dbBan.getCreate_name())) + ","
//                + String.format("'%s'", sqliteEscape(dbBan.getCreate_time())) + ","
//                + String.format("'%s'", sqliteEscape(dbBan.getUpdate_uid())) + ","
//                + String.format("'%s'", sqliteEscape(dbBan.getUpdate_name())) + ","
//                + String.format("'%s'", sqliteEscape(dbBan.getUpdate_time())) + ","
//                + String.format("'%s'", sqliteEscape(dbBan.getStatus())) + ","
//                + String.format("'%s'", sqliteEscape(dbBan.getMappingBorder())) + ","
//                + String.format("'%s'", sqliteEscape(dbBan.getObstacleBorder())) + ","
//                + String.format("'%s'", sqliteEscape(dbBan.getStartBorder())) + ","
//                + String.format("'%s'", sqliteEscape(dbBan.getSync())) + ","
//                + String.format("'%s'", sqliteEscape(dbBan.getInsertTime())) +
//                ");";
//
//        db.execSQL(sql); // 插入数据库
////        LogUtils.e("SqliteHelper" + "插入:"+dbBan.toString());
//        db.close(); // 关闭数据库连接


        StringBuffer sql_insert = new StringBuffer();
        sql_insert.append("insert into " + name + "(Id,tid,title,province,city,district,area,parentId" +
                ",splitStatus,type,lat,lng,create_uid,create_name,create_time" +
                ",update_uid,update_name,update_time,status,mappingBorder,obstacleBorder" +
                ",startBorder,sync,InsertTime)");
        sql_insert.append(" VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        SQLiteStatement statement = db.compileStatement(sql_insert.toString());
        statement.bindString(1, dbBan.getId());
        statement.bindString(2, dbBan.getTid());
        statement.bindString(3, dbBan.getTitle());
        statement.bindString(4, dbBan.getProvince());
        statement.bindString(5, dbBan.getCity());
        statement.bindString(6, dbBan.getDistrict());
        statement.bindString(7, dbBan.getArea());
        statement.bindString(8, dbBan.getParentId());
        statement.bindString(9, dbBan.getSplitStatus());
        statement.bindString(10, dbBan.getType());
        statement.bindString(11, dbBan.getLat());
        statement.bindString(12, dbBan.getLng());
        statement.bindString(13, dbBan.getCreate_uid());
        statement.bindString(14, dbBan.getCreate_name());
        statement.bindString(15, dbBan.getCreate_time());
        statement.bindString(16, dbBan.getUpdate_uid());
        statement.bindString(17, dbBan.getUpdate_name());
        statement.bindString(18, dbBan.getUpdate_time());
        statement.bindString(19, dbBan.getStatus());
        statement.bindString(20, dbBan.getMappingBorder());
        statement.bindString(21, dbBan.getObstacleBorder());
        statement.bindString(22, dbBan.getStartBorder());
        statement.bindString(23, dbBan.getSync());
        statement.bindString(24, dbBan.getInsertTime());

        statement.executeInsert();
        db.close(); // 关闭数据库连接
    }

    //更改条目
    public void updateBan(DbBan dbBan) {
        SQLiteDatabase db = getWritableDatabase(); // 以读写的形式打开数据库
//        String sql = "update " + name
//                + " set Id=" + String.format("'%s'", dbBan.getId())
//                + ",tid=" + String.format("'%s'", dbBan.getTid())
//                + ",title=" + String.format("'%s'", sqliteEscape(dbBan.getTitle()))
//                + ",province=" + String.format("'%s'", dbBan.getProvince())
//                + ",city=" + String.format("'%s'", (dbBan.getCity())
//                + ",district=" + String.format("'%s'", (dbBan.getDistrict())
//                + ",area=" + String.format("'%s'", (dbBan.getArea())
//                + ",parentId=" + String.format("'%s'", (dbBan.getParentId())
//                + ",splitStatus=" + String.format("'%s'", (dbBan.getSplitStatus())
//                + ",type=" + String.format("'%s'", (dbBan.getType())
//                + ",lat=" + String.format("'%s'", (dbBan.getLat())
//                + ",lng=" + String.format("'%s'", (dbBan.getLng())
//                + ",create_uid=" + String.format("'%s'", (dbBan.getCreate_uid())
//                + ",create_name=" + String.format("'%s'", (dbBan.getCreate_name())
//                + ",create_time=" + String.format("'%s'", (dbBan.getCreate_time())
//                + ",update_uid=" + String.format("'%s'", (dbBan.getUpdate_uid())
//                + ",update_name=" + String.format("'%s'", (dbBan.getUpdate_name())
//                + ",update_time=" + String.format("'%s'", (dbBan.getUpdate_time())
//                + ",status=" + String.format("'%s'", (dbBan.getStatus())
//                + ",mappingBorder=" + String.format("'%s'", (dbBan.getMappingBorder())
//                + ",obstacleBorder=" + String.format("'%s'", (dbBan.getObstacleBorder())
//                + ",startBorder=" + String.format("'%s'", (dbBan.getStartBorder())
//                + ",sync=" + String.format("'%s'", (dbBan.getSync())
//                + ",InsertTime=" + String.format("'%s'", (dbBan.getInsertTime())
//                + " where InsertTime=" + String.format("'%s'", (dbBan.getInsertTime())
//                + "and title=" + String.format("'%s'", (dbBan.getTitle())));
//        LogUtils.e("SqliteHelper ,更新  :" + dbBan.toString());
//        try {
//            db.execSQL(sql); // 更新数据库
//            db.close(); // 关闭数据库连接
//        } catch (Exception e) {
//            e.printStackTrace();
//            sjj.alog.Log.e("", e);
//        }


        String sql = "update " + name
                + " set Id= ?"
                + ",tid=?"
                + ",title=?"
                + ",province=?"
                + ",city=?"
                + ",district=?"
                + ",area=?"
                + ",parentId=?"
                + ",splitStatus=?"
                + ",type=?"
                + ",lat=?"
                + ",lng=?"
                + ",create_uid=?"
                + ",create_name=?"
                + ",create_time=?"
                + ",update_uid=?"
                + ",update_name=?"
                + ",update_time=?"
                + ",status=?"
                + ",mappingBorder=?"
                + ",obstacleBorder=?"
                + ",startBorder=?"
                + ",sync=?"
                + ",InsertTime=?"
                + " where InsertTime=?"
                + "and title=?";

        SQLiteStatement statement = db.compileStatement(sql);
        statement.bindString(1, dbBan.getId());
        statement.bindString(2, dbBan.getTid());
        statement.bindString(3, dbBan.getTitle());
        statement.bindString(4, dbBan.getProvince());
        statement.bindString(5, dbBan.getCity());
        statement.bindString(6, dbBan.getDistrict());
        statement.bindString(7, dbBan.getArea());
        statement.bindString(8, dbBan.getParentId());
        statement.bindString(9, dbBan.getSplitStatus());
        statement.bindString(10, dbBan.getType());
        statement.bindString(11, dbBan.getLat());
        statement.bindString(12, dbBan.getLng());
        statement.bindString(13, dbBan.getCreate_uid());
        statement.bindString(14, dbBan.getCreate_name());
        statement.bindString(15, dbBan.getCreate_time());
        statement.bindString(16, dbBan.getUpdate_uid());
        statement.bindString(17, dbBan.getUpdate_name());
        statement.bindString(18, dbBan.getUpdate_time());
        statement.bindString(19, dbBan.getStatus());
        statement.bindString(20, dbBan.getMappingBorder());
        statement.bindString(21, dbBan.getObstacleBorder());
        statement.bindString(22, dbBan.getStartBorder());
        statement.bindString(23, dbBan.getSync());
        statement.bindString(24, dbBan.getInsertTime());
        statement.bindString(25, dbBan.getInsertTime());
        statement.bindString(26, dbBan.getTitle());
        statement.executeUpdateDelete();
        db.close(); // 关闭数据库连接
    }

    //删除  按照InsertTime
    public void deleteTime(String time) {
//        Log.e("SqliteHelper", "删除");
        SQLiteDatabase db = getWritableDatabase(); // 以读写的形式打开数据库
        String sql = "InsertTime = ?";
        String wheres[] = {String.valueOf(time)};
        db.delete(name, sql, wheres); // 数据库删除
        db.close(); // 关闭数据库
    }

    //删除  按照Id
    public void deleteId(String alt) {
//        Log.e("SqliteHelper", "删除");
        SQLiteDatabase db = getWritableDatabase(); // 以读写的形式打开数据库
        String sql = "Id = ?";
        String wheres[] = {String.valueOf(alt)};
        db.delete(name, sql, wheres); // 数据库删除
        db.close(); // 关闭数据库
    }

    public ArrayList<DbBan> queryAll() {
        ArrayList<DbBan> list = new ArrayList<DbBan>();
        SQLiteDatabase db = getReadableDatabase(); // 以只读的方式打开数据库
        String sql = "select * from " + name + ";";
        Cursor cursor = db.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            DbBan dbBan = new DbBan();

            dbBan.set_id(cursor.getInt(cursor.getColumnIndex("_id")));
            dbBan.setId(cursor.getString(cursor.getColumnIndex("Id")));
            dbBan.setTid(cursor.getString(cursor.getColumnIndex("tid")));
            dbBan.setTitle(cursor.getString(cursor.getColumnIndex("title")));
            dbBan.setProvince(cursor.getString(cursor.getColumnIndex("province")));
            dbBan.setCity(cursor.getString(cursor.getColumnIndex("city")));
            dbBan.setDistrict(cursor.getString(cursor.getColumnIndex("district")));
            dbBan.setArea(cursor.getString(cursor.getColumnIndex("area")));
            dbBan.setParentId(cursor.getString(cursor.getColumnIndex("parentId")));
            dbBan.setSplitStatus(cursor.getString(cursor.getColumnIndex("splitStatus")));
            dbBan.setType(cursor.getString(cursor.getColumnIndex("type")));
            dbBan.setLat(cursor.getString(cursor.getColumnIndex("lat")));
            dbBan.setLng(cursor.getString(cursor.getColumnIndex("lng")));
            dbBan.setCreate_uid(cursor.getString(cursor.getColumnIndex("create_uid")));
            dbBan.setCreate_name(cursor.getString(cursor.getColumnIndex("create_name")));
            dbBan.setCreate_time(cursor.getString(cursor.getColumnIndex("create_time")));
            dbBan.setUpdate_uid(cursor.getString(cursor.getColumnIndex("update_uid")));
            dbBan.setUpdate_name(cursor.getString(cursor.getColumnIndex("update_name")));
            dbBan.setUpdate_time(cursor.getString(cursor.getColumnIndex("update_time")));
            dbBan.setStatus(cursor.getString(cursor.getColumnIndex("status")));
            dbBan.setObstacleBorder(cursor.getString(cursor.getColumnIndex("obstacleBorder")));
            dbBan.setMappingBorder(cursor.getString(cursor.getColumnIndex("mappingBorder")));
            dbBan.setStartBorder(cursor.getString(cursor.getColumnIndex("startBorder")));
            dbBan.setSync(cursor.getString(cursor.getColumnIndex("sync")));
            dbBan.setInsertTime(cursor.getString(cursor.getColumnIndex("InsertTime")));
//            LogUtils.e(" ---- alt = " + point);
            list.add(dbBan); // 添加到数组
        }
        cursor.close(); // 关闭游标
        db.close(); // 关闭数据库
        return list;
    }

    //查询   按照Id
    public DbBan queryPointById(String id) {
        DbBan dbBan = null;
        SQLiteDatabase db = getReadableDatabase(); // 以只读方式打开数据库
        String[] columns = {"_id", "Id", "tid", "title", "province", "city", "district", "area", "parentId"
                , "splitStatus", "type", "lat", "lng", "create_uid", "create_name", "create_time"
                , "update_uid", "update_name", "update_time", "status", "obstacleBorder", "mappingBorder"
                , "startBorder", "sync", "InsertTime"};
        String selection = "Id=?";
        String[] selectionArgs = {String.valueOf(id)};
        Cursor cursor = db.query(name, columns, selection, selectionArgs, null, null, null);
        if (cursor.moveToNext()) {
            dbBan = new DbBan();
            dbBan.set_id(cursor.getInt(cursor.getColumnIndex("_id")));
            dbBan.setId(cursor.getString(cursor.getColumnIndex("Id")));
            dbBan.setTid(cursor.getString(cursor.getColumnIndex("tid")));
            dbBan.setTitle(cursor.getString(cursor.getColumnIndex("title")));
            dbBan.setProvince(cursor.getString(cursor.getColumnIndex("province")));
            dbBan.setCity(cursor.getString(cursor.getColumnIndex("city")));
            dbBan.setDistrict(cursor.getString(cursor.getColumnIndex("district")));
            dbBan.setArea(cursor.getString(cursor.getColumnIndex("area")));
            dbBan.setParentId(cursor.getString(cursor.getColumnIndex("parentId")));
            dbBan.setSplitStatus(cursor.getString(cursor.getColumnIndex("splitStatus")));
            dbBan.setType(cursor.getString(cursor.getColumnIndex("type")));
            dbBan.setLat(cursor.getString(cursor.getColumnIndex("lat")));
            dbBan.setLng(cursor.getString(cursor.getColumnIndex("lng")));
            dbBan.setCreate_uid(cursor.getString(cursor.getColumnIndex("create_uid")));
            dbBan.setCreate_name(cursor.getString(cursor.getColumnIndex("create_name")));
            dbBan.setCreate_time(cursor.getString(cursor.getColumnIndex("create_time")));
            dbBan.setUpdate_uid(cursor.getString(cursor.getColumnIndex("update_uid")));
            dbBan.setUpdate_name(cursor.getString(cursor.getColumnIndex("update_name")));
            dbBan.setUpdate_time(cursor.getString(cursor.getColumnIndex("update_time")));
            dbBan.setStatus(cursor.getString(cursor.getColumnIndex("status")));
            dbBan.setObstacleBorder(cursor.getString(cursor.getColumnIndex("obstacleBorder")));
            dbBan.setMappingBorder(cursor.getString(cursor.getColumnIndex("mappingBorder")));
            dbBan.setStartBorder(cursor.getString(cursor.getColumnIndex("startBorder")));
            dbBan.setSync(cursor.getString(cursor.getColumnIndex("sync")));
            dbBan.setInsertTime(cursor.getString(cursor.getColumnIndex("InsertTime")));
        }
        return dbBan;
    }


}
