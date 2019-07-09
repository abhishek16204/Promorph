package com.example.android.promorph;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ProfileDB extends SQLiteOpenHelper {
    public static final String DATABASE="user_details";
    public static final String TABLE_NAME_lst="lst";
    public static final String TABLE_NAME_leave="leave";
    public static final String TABLE_NAME_attendance="attendance";
    public static final String TABLE_NAME_today_work_progress="today_work_progress";


    public ProfileDB(Context context) {

        super(context, DATABASE, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("Create table " + TABLE_NAME_lst + "("
                + "designation" + " TEXT,"
                + "user_id" + " TEXT,"
                + "name" + " TEXT,"
                + "status" + " INTEGER DEFAULT  0,"
                + "role" + " TEXT "
                + ")");
        db.execSQL("Create table " + TABLE_NAME_leave + "("
                + "leave_id" + " TEXT,"
                + "leave_type" + " TEXT "
                + ")");
        db.execSQL("Create table " + TABLE_NAME_today_work_progress + "("
                + "s_no" + " INTEGER PRIMARY KEY AUTOINCREMENT  ,"
                + "user_id" + " TEXT,"
                + "date" + " TEXT,"
                + "no_of_activity" + " TEXT,"
                + "no_of_activity_done" + " TEXT,"
                + "percentage_of_work" + " TEXT,"
                + "work_perfection_rating" + " TEXT, "
                + "remark" + " TEXT "
                + ")");
        db.execSQL("Create table " + TABLE_NAME_attendance + "("
                + "s_no" + " INTEGER PRIMARY KEY AUTOINCREMENT  ,"
                + "user_id" + " TEXT,"
                + "today_date" + " TEXT,"
                + "check_in" + " TEXT,"
                + "check_out" + " TEXT,"
                + "duration" + " TEXT, "
                + "status" + " TEXT "
                + ")");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_lst);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_leave);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_attendance);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_today_work_progress);

    }

    public long lst(String designation,String user_id,String name,String role) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("role", role);
        contentValues.put("designation",designation);
        contentValues.put("name", name);
        contentValues.put("user_id",user_id);
        long id= sqLiteDatabase.insert(TABLE_NAME_lst, null, contentValues);
        sqLiteDatabase.close();
        return id;
    }
    public long today_progress(String user_id,String date,String no_of_activity,String no_of_activity_done,
                               String percentage_of_work,String work_perfection_rating,String remark) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("user_id",user_id);
        contentValues.put("date", date);
        contentValues.put("no_of_activity",no_of_activity);
        contentValues.put("no_of_activity_done",no_of_activity_done);
        contentValues.put("percentage_of_work",percentage_of_work);
        contentValues.put("work_perfection_rating",work_perfection_rating);
        contentValues.put("remark",remark);
        long id= sqLiteDatabase.insert(TABLE_NAME_today_work_progress, null, contentValues);
        sqLiteDatabase.close();
        return id;
    }
    public boolean progress_check(String user_id,String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = null;
        String sql ="SELECT * FROM "+TABLE_NAME_today_work_progress+" WHERE user_id = "+user_id+" AND date = '"+date+"'" ;
        cursor= db.rawQuery(sql,null);
        Log.e("cusor in work progress",""+cursor.getCount());
        if(cursor.getCount()>0){
            cursor.close();
          return true;
        }
        else{
            cursor.close();
           return false;
        }


    }

    public long leave(String leave_id ,String leave_type) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("leave_id",leave_id);
        contentValues.put("leave_type",leave_type);
        long id= sqLiteDatabase.insert(TABLE_NAME_leave, null, contentValues);
        sqLiteDatabase.close();
        return id;
    }
    public boolean status_check_in(String user_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("status", 1);
        Log.e("Value updated ","to 1");
        return db.update(TABLE_NAME_lst, contentValues, " user_id  =" + user_id, null) > 0;

    }
    public boolean status_check_out(String user_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("status", 0);
        Log.e("Value updated ","to 0");
        return db.update(TABLE_NAME_lst, contentValues, "user_id  =" + user_id, null) > 0;

    }

    public boolean first_check_out(String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = null;
        String sql ="SELECT * FROM "+TABLE_NAME_attendance+" WHERE today_date = '"+date+"'" ;
        cursor= db.rawQuery(sql,null);
        Log.e("cusor in first checkout",""+cursor.getCount());
        if(cursor.getCount()>0){
            cursor.close();
            return true;
        }
        else{
            cursor.close();
            return false;
        }



    }


    public boolean only_check_out(String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = null;
        String sql = "SELECT * FROM " + TABLE_NAME_attendance + " WHERE today_date = '" + date + "' AND status = 'PRESENT'";
        cursor = db.rawQuery(sql, null);
        Log.e("cusor in first checkout", "" + cursor.getCount());
        if (cursor.getCount() > 0) {
            cursor.close();
            return true;
        } else {
            cursor.close();
            return false;
        }
    }
    public boolean id_already_exists(String user_id) {
        SQLiteDatabase db = getWritableDatabase();
        String selectString = "SELECT * FROM " + TABLE_NAME_lst + " WHERE  user_id = " + user_id;
        Cursor cursor = db.rawQuery(selectString, null);

        boolean hasObject = false;
        if (cursor.moveToFirst()) {
            hasObject = true;
            int count = 0;
            while (cursor.moveToNext()) {
                count++;
            }
        }

        cursor.close();
        return hasObject;
    }
    public long attendance_check_in(String user_id ,String today_date,String check_in,String status) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("user_id",user_id);
        contentValues.put("today_date",today_date);
        contentValues.put("check_in",check_in);
        contentValues.put("status",status);
        long id= sqLiteDatabase.insert(TABLE_NAME_attendance, null, contentValues);
        sqLiteDatabase.close();
        return id;
    }
    public long check_out(String date,String check_out,String status) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("check_out",check_out);
        contentValues.put("today_date",date);
        contentValues.put("status",status);
        long id= sqLiteDatabase.insert(TABLE_NAME_attendance, null, contentValues);
        sqLiteDatabase.close();
        return id;
    }




    public boolean attendance_check_out(int serial,String check_out,String duration) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("check_out", check_out);
        contentValues.put("duration", duration);
        Log.e("checked-out","in database");
        return db.update(TABLE_NAME_attendance, contentValues, "s_no=" + serial , null) > 0;

    }
    public Cursor getAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + TABLE_NAME_attendance , null);
        return res;
    }
    public Cursor getUserID() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + TABLE_NAME_lst , null);
        return res;
    }

}
