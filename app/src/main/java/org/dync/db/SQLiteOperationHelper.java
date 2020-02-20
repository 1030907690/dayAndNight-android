package org.dync.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/***
 * sqlite数据库操作类
 * https://www.jianshu.com/p/5c33be6ce89d
 * */
public class SQLiteOperationHelper extends SQLiteOpenHelper {


    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "history.db";
    public static final String DOWNLOAD_TABLE_NAME = "download_history";

    public static final String WATCH_TABLE_NAME = "watch_history";

    public static final String [] TABLE_COLUMNS = {"id","name","url"};

    public SQLiteOperationHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String sql = "create table if not exists " + DOWNLOAD_TABLE_NAME + " (id integer primary key, name text, url text)";
        String watchSql = "create table if not exists " + WATCH_TABLE_NAME + " (id integer primary key, name text, data_source integer,url text,url_item text,duration integer,create_time text,name_node text)";
        sqLiteDatabase.execSQL(sql);
        sqLiteDatabase.execSQL(watchSql);
    }

    //  数据库升级时才会调用，通过VERSION来判断
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        /*String sql = "DROP TABLE IF EXISTS " + TABLE_NAME;
        sqLiteDatabase.execSQL(sql);
        onCreate(sqLiteDatabase);*/
    }

}
