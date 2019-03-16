package com.example.android.skladovypomocnik;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.HashMap;

public class DatabaseAccess {
    private SQLiteOpenHelper openHelper;
    private SQLiteDatabase db;
    private static DatabaseAccess instance;
    private static final String TAG = "DatabaseHelper";


    private DatabaseAccess(Context context) {
        this.openHelper = new DatabaseOpenHelper(context);
    }

    public static DatabaseAccess getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseAccess(context);
        }
        return instance;
    }


    public void open() {
        try {
            this.db = openHelper.getReadableDatabase();
        } catch (Exception e) {
            System.out.println("SQL EXCEPTION in onCreate");
            Log.d(TAG, "SQL EXCEPTION in onCreate");
            e.printStackTrace();
        }
    }


    public void close() {
        if (db != null) {
            this.db.close();
        }
    }

    public long getTableSize() {
        long count = DatabaseUtils.queryNumEntries(db, "articles");
        return count;
    }

    public SQLiteDatabase getDb() {
        return this.db;
    }

    public String getBook(String ean) {
        if (!ean.isEmpty()) {
            Cursor cursor = db.rawQuery("Select NAME from articles where EAN =" + ean, new String[]{});
            StringBuffer buffer = new StringBuffer();
            if (cursor.moveToFirst()) {
                String name = cursor.getString(0);
                buffer.append("" + name);
                cursor.close();
                return buffer.toString();
            }
            cursor.close();
        }
        return "Název není v databázi";
    }

    public HashMap<String, String> allData() {
        HashMap<String, String> map = new HashMap<>();
        Cursor cursor = db.rawQuery("Select EAN, NAME from articles", new String[]{});
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            String ean = cursor.getString(0);
            String name = cursor.getString(1);
            map.put(ean, name);
            cursor.moveToNext();
        }
        return map;
    }


}
