package com.example.hongssang.navertest;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by 22 on 2016-09-26.
 */
public class DBAdapter extends SQLiteOpenHelper{
    private static final String DATABASE_NAME = "ggariDB";
    private static final String DATABASE_TABLE = "TB_GGARI";
    private static final int DATABASE_VERSION = 1;

    private SQLiteOpenHelper helper;
    private SQLiteDatabase db;

    public DBAdapter(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 최초에 데이터베이스가 없을 경우 생성을 위해 호출됨
        // 데이블 생성하는 코드 작성
        String sql = "create table mytable(id integer primary key autoincrement, " +
                "name text, callNumber text, menu text );";

        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
