package com.skylin.uav.drawforterrain.select.rtk.coord_db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据库创建、更新
 */
public class Coord_SQL extends SQLiteOpenHelper {
    /**
     * @param context
     *            上下文
     * @param name
     *            数据库名称
     * @param factory
     *            游标工厂
     * @param version
     *            数据库版本
     */
    private String name;
    public Coord_SQL(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.name = name;
    }

    // 创建数据库
    @Override
    public void onCreate(SQLiteDatabase db) {
//        Log.e("SqliteHelper", "数据库创建");
        String sql = "create table "+name+"(_id integer Primary Key autoincrement,lat varchar(20),lon varchar(20), alt varchar(20), time varchar(20) , name varchar(20), issync varchar(20), province varchar(20), city varchar(20), district varchar(20))";
        db.execSQL(sql);
    }

    // 数据库更新
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        Log.e("SqliteHelper", "数据库更新");
    }


//      添加Person到数据库

    public void addCoord(CordBan coord_ban) {
//        sjj.alog.Log.e("SqliteHelper"+"插入");
        SQLiteDatabase db = getWritableDatabase(); // 以读写的形式打开数据库
//		db.execSQL("insert into person(name,age) values("
//				+ String.format("'%s'", person.getName()) + ","
//				+ person.getAge() + ");"); // 插入数据库

        // insert into person(name,age,sex) values('liudehua',50,'man')
//		db.execSQL(
//				"insert into person(name,age,sex) values("
//				+ String.format("'%s'", person.getName()) + ","
//				+ person.getAge() + ","
//				+ String.format("'%s'", person.getSex()) +
//				");"
//		); // 插入数据库

//        db.execSQL(
//                "insert into "+name+"(lat,lon,alt,time,name,issync,province,city,district) values("
//                        + String.format("'%s'", coord_ban.getLat()) + ","
//                        + String.format("'%s'", coord_ban.getLon()) + ","
//                        + String.format("'%s'", coord_ban.getAlt()) + ","
//                        + String.format("'%s'", coord_ban.getTime()) + ","
//                        + String.format("'%s'", coord_ban.getName()) + ","
//                        + String.format("'%s'", coord_ban.getIsSync()) + ","
//                        + String.format("'%s'", coord_ban.getProvince()) + ","
//                        + String.format("'%s'", coord_ban.getCity()) + ","
//                        + String.format("'%s'", coord_ban.getDistrict()) +
//                        ");"
//        ); // 插入数据库
//
//        db.close(); // 关闭数据库连接

        StringBuffer sql_insert = new StringBuffer();
        sql_insert.append("insert into " + name + "(lat,lon,alt,time,name,issync,province,city,district)");
        sql_insert.append(" VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        SQLiteStatement statement = db.compileStatement(sql_insert.toString());
        statement.bindString(1, coord_ban.getLat());
        statement.bindString(2, coord_ban.getLon());
        statement.bindString(3, coord_ban.getAlt());
        statement.bindString(4, coord_ban.getTime());
        statement.bindString(5, coord_ban.getName());
        statement.bindString(6, coord_ban.getIsSync());
        statement.bindString(7, coord_ban.getProvince());
        statement.bindString(8, coord_ban.getCity());
        statement.bindString(9, coord_ban.getDistrict());
        statement.executeInsert();
        db.close(); // 关闭数据库连接
    }

    /**
     * Coord_Ban
     */
    public void updateCoord(CordBan coord_ban) {
//        Log.e("SqliteHelper", "更新");
        SQLiteDatabase db = getWritableDatabase(); // 以读写的形式打开数据库
//		String sql = "update person set name="
//				+ String.format("'%s'", person.getName()) + ",age="
//				+ person.getAge() + " where _id=" + person.get_id();

//        String sql = "update "+name+" set lat="
//                + String.format("'%s'", coord_ban.getLat())
//                + ",lon=" + String.format("'%s'", coord_ban.getLon())
//                + ",alt=" + String.format("'%s'", coord_ban.getAlt())
//                + ",time=" + String.format("'%s'", coord_ban.getTime())
//                + ",issync=" + String.format("'%s'", coord_ban.getIsSync())
//                + ",province=" + String.format("'%s'", coord_ban.getProvince())
//                + ",city=" + String.format("'%s'", coord_ban.getCity())
//                + ",district=" + String.format("'%s'", coord_ban.getDistrict())
//                + " where name=" + String.format("'%s'", coord_ban.getName());

//        Log.e("updatePerson", sql);
//        try {
//            db.execSQL(sql); // 更新数据库
//            db.close(); // 关闭数据库连接
//        }catch (Exception e){
//            e.printStackTrace();
//        }
        String sql = "update "+name
                +" set lat=?"
                + ",lon=?"
                + ",alt=?"
                + ",time=?"
                + ",issync=?"
                + ",province=?"
                + ",city=?"
                + ",district=?"
                + " where name=?";
        SQLiteStatement statement = db.compileStatement(sql);
        statement.bindString(1, coord_ban.getLat());
        statement.bindString(2, coord_ban.getLon());
        statement.bindString(3, coord_ban.getAlt());
        statement.bindString(4, coord_ban.getTime());
        statement.bindString(5, coord_ban.getIsSync());
        statement.bindString(6, coord_ban.getProvince());
        statement.bindString(7, coord_ban.getCity());
        statement.bindString(8, coord_ban.getDistrict());
        statement.bindString(9, coord_ban.getName());
        statement.executeUpdateDelete();
        db.close(); // 关闭数据库连接
    }

    public void deleteCoord(String alt) {
//        Log.e("SqliteHelper", "删除");
        SQLiteDatabase db = getWritableDatabase(); // 以读写的形式打开数据库
        String sql = "_id = ?";
        String wheres[] = { String.valueOf(alt) };
        db.delete(name, sql, wheres); // 数据库删除
        db.close(); // 关闭数据库
    }


    public List<String> qure(){
        List<String> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase(); // 以只读的方式打开数据库
        String sql = " select * from sqlite_master;";
        Cursor cursor = db.rawQuery(sql, null);

        while (cursor.moveToNext()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < cursor.getColumnCount(); i++) {

                String string = cursor.getString(i);
                String name = cursor.getColumnName(i);
                sb.append(name).append("==>").append(string).append("     ");

                if (name.equals("name")){
                    list.add(string);
                }

            }
//            sjj.alog.Log.e(sb);
        }
        return list;
    }


    public ArrayList<CordBan> queryAllCoord_Ban() {
        ArrayList<CordBan> list = new ArrayList<CordBan>();
        SQLiteDatabase db = getReadableDatabase(); // 以只读的方式打开数据库
        String sql = "select * from "+name+";";
        Cursor cursor = db.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            int _id = cursor.getInt(cursor.getColumnIndex("_id"));
            String lat = cursor.getString(cursor.getColumnIndex("lat"));
            String lon = cursor.getString(cursor.getColumnIndex("lon"));
            String alt = cursor.getString(cursor.getColumnIndex("alt"));
            String time = cursor.getString(cursor.getColumnIndex("time"));
            String name = cursor.getString(cursor.getColumnIndex("name"));
            String issync = cursor.getString(cursor.getColumnIndex("issync"));
            String province = cursor.getString(cursor.getColumnIndex("province"));
            String city = cursor.getString(cursor.getColumnIndex("city"));
            String district = cursor.getString(cursor.getColumnIndex("district"));

            CordBan coord_ban = new CordBan();
            coord_ban.set_id(_id);
            coord_ban.setLat(lat);
            coord_ban.setLon(lon);
            coord_ban.setAlt(alt);
            coord_ban.setTime(time);
            coord_ban.setName(name);
            coord_ban.setIsSync(issync);
            coord_ban.setProvince(province);
            coord_ban.setCity(city);
            coord_ban.setDistrict(district);
//            LogUtils.e(" ---- alt = " + alt);
            list.add(coord_ban); // 添加到数组
//            LogUtils.e("ban :"+coord_ban);
        }
        cursor.close(); // 关闭游标
        db.close(); // 关闭数据库
        return list;
    }

    public CordBan queryCoord_BanByName(String name) {
//        sjj.alog.Log.e("查询 ");
        CordBan coord_ban = null;
        SQLiteDatabase db = getReadableDatabase(); // 以只读方式打开数据库
//		String[] columns = { "_id", "name", "age" };
        String[] columns = { "_id", "lat", "lon", "alt", "time", "name", "issync", "province", "city", "district"};
        String selection = "name=?";
        String[] selectionArgs = { String.valueOf(name) };
        Cursor cursor = db.query(this.name, columns, selection, selectionArgs, null, null, null);
        if (cursor.moveToNext()) {
            coord_ban = new CordBan();
            coord_ban.set_id(cursor.getInt(cursor.getColumnIndex("_id")));
            coord_ban.setLat(cursor.getString(cursor.getColumnIndex("lat")));
            coord_ban.setLon(cursor.getString(cursor.getColumnIndex("lon")));
            coord_ban.setAlt(cursor.getString(cursor.getColumnIndex("alt")));
            coord_ban.setTime(cursor.getString(cursor.getColumnIndex("time")));
            coord_ban.setName(cursor.getString(cursor.getColumnIndex("name")));
            coord_ban.setIsSync(cursor.getString(cursor.getColumnIndex("issync")));
            coord_ban.setProvince(cursor.getString(cursor.getColumnIndex("province")));
            coord_ban.setCity(cursor.getString(cursor.getColumnIndex("city")));
            coord_ban.setDistrict(cursor.getString(cursor.getColumnIndex("district")));
        }
        return coord_ban;
    }

    public CordBan queryCoord_BanByTime(String time) {
//        sjj.alog.Log.e("查询 ");
        CordBan coord_ban = null;
        SQLiteDatabase db = getReadableDatabase(); // 以只读方式打开数据库
//		String[] columns = { "_id", "name", "age" };
        String[] columns = { "_id", "lat", "lon", "alt", "time", "name", "issync", "province", "city", "district"};
        String selection = "time=?";
        String[] selectionArgs = { String.valueOf(time) };
        Cursor cursor = db.query(this.name, columns, selection, selectionArgs, null, null, null);
        if (cursor.moveToNext()) {
            coord_ban = new CordBan();
            coord_ban.set_id(cursor.getInt(cursor.getColumnIndex("_id")));
            coord_ban.setLat(cursor.getString(cursor.getColumnIndex("lat")));
            coord_ban.setLon(cursor.getString(cursor.getColumnIndex("lon")));
            coord_ban.setAlt(cursor.getString(cursor.getColumnIndex("alt")));
            coord_ban.setTime(cursor.getString(cursor.getColumnIndex("time")));
            coord_ban.setName(cursor.getString(cursor.getColumnIndex("name")));
            coord_ban.setIsSync(cursor.getString(cursor.getColumnIndex("issync")));
            coord_ban.setProvince(cursor.getString(cursor.getColumnIndex("province")));
            coord_ban.setCity(cursor.getString(cursor.getColumnIndex("city")));
            coord_ban.setDistrict(cursor.getString(cursor.getColumnIndex("district")));
        }
        return coord_ban;
    }
}
