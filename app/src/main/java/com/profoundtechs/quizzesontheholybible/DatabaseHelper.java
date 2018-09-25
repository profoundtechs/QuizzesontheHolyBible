package com.profoundtechs.quizzesontheholybible;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "quizzesontheholybible.db";
    public static final String DB_LOCATION = "/data/data/com.profoundtechs.quizzesontheholybible/databases/";
    private Context context;
    private SQLiteDatabase mDatabase;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, 1);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void openDatabase(){
        String dbPath = context.getDatabasePath(DB_NAME).getPath();
        if (mDatabase!=null && mDatabase.isOpen()){
            return;
        }
        mDatabase = SQLiteDatabase.openDatabase(dbPath,null,SQLiteDatabase.OPEN_READWRITE);
    }

    public void closeDatabase(){
        if (mDatabase!=null){
            mDatabase.close();
        }
    }

    public Content getContent(String tableName, int qNumber){
        Content content = null;
        openDatabase();
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM '" + tableName + "' WHERE id = '" + qNumber + "'",null);
        cursor.moveToFirst();
        content = new Content(cursor.getInt(0),cursor.getString(1),cursor.getString(2),
                cursor.getBlob(3),cursor.getBlob(4));
        cursor.close();
        closeDatabase();
        return content;
    }

    public int getNumberOfRows(String tableName){
        openDatabase();
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM '" + tableName + "'",null);
        int rows = cursor.getCount();
        cursor.close();
        return rows;
    }
}
