package com.directdev.portal.tools.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

import com.directdev.portal.tools.datatype.FinanceData;
import com.directdev.portal.tools.datatype.ScheduleData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class JournalDB extends SQLiteOpenHelper {

    private SQLiteDatabase db;
    private Context context;

    public static final String DATABASE_NAME = "Journal";

    public static final String TABLE_COURSES = "courses";
    public static final String TABLE_DATES = "dates";
    public static final String TABLE_CLASSES = "classes";
    public static final String TABLE_SHIFTS = "shifts";
    public static final String TABLE_ROOM = "room";
    public static final String TABLE_MODE = "mode";
    public static final String TABLE_TYPE = "type";
    public static final String TABLE_FINANCE = "finance";

    public static final String COLUMN_ID = "_id";

    public static final String COLUMN_FINANCE_TYPE = "payment_type";
    public static final String COLUMN_FINANCE_AMOUNT = "amount";
    public static final String COLUMN_FINANCE_DESCRIPTION = "descr";
    public static final String COLUMN_FINANCE_DUE_DATE = "due_date";
    public static final String COLUMN_FINANCE_POSTED_DATE = "posted_date";

    public static final String COLUMN_TYPE_TYPE = "type";

    public static final String COLUMN_MODE_TYPE = "mode";

    public static final String COLUMN_ROOM_NAME = "room";

    public static final String COLUMN_COURSES_NAME = "course";
    public static final String COLUMN_COURSES_CODE = "code_data";

    public static final String COLUMN_DATES_DATE = "date_data";
    public static final String COLUMN_DATES_FINANCE = "finance";
    public static final String COLUMN_DATES_EXAM = "exam";
    public static final String COLUMN_DATES_SCHEDULE = "schedule";

    public static final String COLUMN_CLASSES_COURSE = "course_id";
    public static final String COLUMN_CLASSES_DATE = "date_id";
    public static final String COLUMN_CLASSES_SHIFT = "shift_id";
    public static final String COLUMN_CLASSES_SESSION = "session";
    public static final String COLUMN_CLASSES_ROOM = "room_id";
    public static final String COLUMN_CLASSES_MODE = "mode_id";
    public static final String COLUMN_CLASSES_TYPE = "type_id";

    public static final String COLUMN_SHIFTS_SHIFT_START = "start_data";
    public static final String COLUMN_SHIFTS_SHIFT_END = "end_data";

    private static final String UPGRADE_DB = "drop table if exist schedule."+TABLE_CLASSES+";" +
            "drop table if exist schedule."+TABLE_DATES+";" +
            "drop table if exist schedule."+TABLE_COURSES+";" +
            "drop table if exist schedule."+TABLE_SHIFTS+";";

    private static final String CREATE_TABLE_FINANCE = "create table if not exists "+TABLE_FINANCE+" ("
            +COLUMN_ID+" integer primary key autoincrement,"
            +COLUMN_FINANCE_AMOUNT+" integer,"
            + COLUMN_FINANCE_DUE_DATE +" text,"
            +COLUMN_FINANCE_TYPE+" text,"
            +COLUMN_FINANCE_DESCRIPTION+" text,"
            +COLUMN_FINANCE_POSTED_DATE+" text,"
            +"foreign key("+ COLUMN_FINANCE_POSTED_DATE +") references "+TABLE_DATES+"("+COLUMN_ID+"),"
            +"foreign key("+ COLUMN_FINANCE_DUE_DATE +") references "+TABLE_DATES+"("+COLUMN_ID+"));";

    private static final String CREATE_TABLE_ROOM = "create table if not exists "+TABLE_ROOM+" ("
            +COLUMN_ID+" integer primary key autoincrement,"
            +COLUMN_ROOM_NAME+" text unique);";

    private static final String CREATE_TABLE_SHIFTS = "create table if not exists "+TABLE_SHIFTS+" ("
            +COLUMN_ID+" integer primary key autoincrement,"
            +COLUMN_SHIFTS_SHIFT_START+" text unique,"
            +COLUMN_SHIFTS_SHIFT_END+" text unique);";

    private static final String CREATE_TABLE_TYPE = "create table if not exists "+TABLE_TYPE+" ("
            +COLUMN_ID+" integer primary key autoincrement,"
            +COLUMN_TYPE_TYPE+" text unique);";

    private static final String CREATE_TABLE_CLASSES = "create table if not exists "+TABLE_CLASSES+" ("
            +COLUMN_ID+" integer primary key autoincrement,"
            +COLUMN_CLASSES_COURSE+" integer,"
            +COLUMN_CLASSES_DATE+" integer,"
            +COLUMN_CLASSES_SHIFT+" integer,"
            +COLUMN_CLASSES_SESSION+" integer,"
            +COLUMN_CLASSES_ROOM+" integer,"
            +COLUMN_CLASSES_MODE+" integer,"
            +COLUMN_CLASSES_TYPE+" integer,"
            +"foreign key("+COLUMN_CLASSES_COURSE+") references "+TABLE_COURSES+"("+COLUMN_ID+"),"
            +"foreign key("+COLUMN_CLASSES_DATE+") references "+TABLE_DATES+"("+COLUMN_ID+"),"
            +"foreign key("+COLUMN_CLASSES_ROOM+") references "+TABLE_ROOM+"("+COLUMN_ID+"),"
            +"foreign key("+COLUMN_CLASSES_TYPE+") references "+TABLE_TYPE+"("+COLUMN_ID+"),"
            +"foreign key("+COLUMN_CLASSES_SHIFT+") references "+TABLE_SHIFTS+"("+COLUMN_ID+"));";

    private static final String CREATE_TABLE_DATES = "create table if not exists "+TABLE_DATES+" ("
            +COLUMN_ID+" integer primary key autoincrement,"
            +COLUMN_DATES_FINANCE+" integer,"
            +COLUMN_DATES_EXAM+" integer,"
            +COLUMN_DATES_SCHEDULE+" integer,"
            +COLUMN_DATES_DATE+" text unique);";

    private static final String CREATE_TABLE_MODE = "create table if not exists "+TABLE_MODE+" ("
            +COLUMN_ID+" integer primary key autoincrement,"
            +COLUMN_MODE_TYPE+" text unique);";

    private static final String CREATE_TABLE_COURSES = "create table if not exists "+TABLE_COURSES+" ("
            +COLUMN_ID+" integer primary key autoincrement,"
            +COLUMN_COURSES_NAME+" text unique,"
            +COLUMN_COURSES_CODE+" text unique);";


    public JournalDB(Context ctx){
        super(ctx, DATABASE_NAME, null, 1);
        this.context = ctx;
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL(CREATE_TABLE_DATES);
        db.execSQL(CREATE_TABLE_COURSES);
        db.execSQL(CREATE_TABLE_SHIFTS);
        db.execSQL(CREATE_TABLE_ROOM);
        db.execSQL(CREATE_TABLE_MODE);
        db.execSQL(CREATE_TABLE_TYPE);
        db.execSQL(CREATE_TABLE_CLASSES);
        db.execSQL(CREATE_TABLE_FINANCE);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db = this.getWritableDatabase();
        db.execSQL(UPGRADE_DB);
    }


    public void addScheduleJson(JSONArray data) throws JSONException {
        long rowid;
        JSONObject jObject;
        ContentValues dataHolder;
        db = this.getWritableDatabase();

        db.beginTransaction();
        try {
            for (int i = 1; i <= data.length(); i++) {
                jObject = data.getJSONObject(i - 1);

                dataHolder = shiftExtractor(jObject);
                rowid = db.insertWithOnConflict(TABLE_SHIFTS, null, dataHolder, SQLiteDatabase.CONFLICT_IGNORE);
                dataHolder = classMaker(rowid, COLUMN_CLASSES_SHIFT, TABLE_SHIFTS,dataHolder,COLUMN_SHIFTS_SHIFT_START);
                db.insertWithOnConflict(TABLE_CLASSES, null, dataHolder, SQLiteDatabase.CONFLICT_IGNORE);

                dataHolder = courseExtractor(jObject);
                rowid = db.insertWithOnConflict(TABLE_COURSES, null, dataHolder, SQLiteDatabase.CONFLICT_IGNORE);
                dataHolder = classMaker(rowid, COLUMN_CLASSES_COURSE, TABLE_COURSES,dataHolder,COLUMN_COURSES_CODE);
                db.update(TABLE_CLASSES, dataHolder, "_id = " + i, null);

                dataHolder = datesExtractor(jObject);
                rowid = db.insertWithOnConflict(TABLE_DATES, null, dataHolder, SQLiteDatabase.CONFLICT_IGNORE);
                dataHolder = classMaker(rowid, COLUMN_CLASSES_DATE, TABLE_DATES,dataHolder,COLUMN_DATES_DATE);
                db.update(TABLE_CLASSES, dataHolder, "_id = " + i, null);

                dataHolder = roomExtractor(jObject);
                rowid = db.insertWithOnConflict(TABLE_ROOM, null, dataHolder, SQLiteDatabase.CONFLICT_IGNORE);
                dataHolder = classMaker(rowid, COLUMN_CLASSES_ROOM, TABLE_ROOM,dataHolder,COLUMN_ROOM_NAME);
                db.update(TABLE_CLASSES, dataHolder, "_id = " + i, null);

                dataHolder = modeExtractor(jObject);
                rowid = db.insertWithOnConflict(TABLE_MODE, null, dataHolder, SQLiteDatabase.CONFLICT_IGNORE);
                dataHolder = classMaker(rowid, COLUMN_CLASSES_MODE, TABLE_MODE,dataHolder,COLUMN_MODE_TYPE);
                db.update(TABLE_CLASSES, dataHolder, "_id = " + i, null);

                dataHolder = typeExtractor(jObject);
                rowid = db.insertWithOnConflict(TABLE_TYPE, null, dataHolder, SQLiteDatabase.CONFLICT_IGNORE);
                dataHolder = classMaker(rowid, COLUMN_CLASSES_TYPE, TABLE_TYPE,dataHolder,COLUMN_TYPE_TYPE);
                db.update(TABLE_CLASSES, dataHolder, "_id = " + i, null);

                dataHolder = sessionExtractor(jObject);
                db.update(TABLE_CLASSES, dataHolder, "_id = "+i, null);
            }
            db.setTransactionSuccessful();
        }finally {
            db.endTransaction();
        }
        db.close();
    }

    public void addFinanceJson(JSONObject data) throws JSONException{
        ContentValues dataHolder;
        db = this.getWritableDatabase();
        JSONArray jsonArray = data.getJSONArray("Status");
        db.beginTransaction();
        String dueDate;
        String postedDate;
        try {
            for (int i = 1; i <= jsonArray.length(); i++) {
                data = jsonArray.getJSONObject(i - 1);

                dataHolder = financeExtractor(data);
                db.insertWithOnConflict(TABLE_FINANCE, null, dataHolder, SQLiteDatabase.CONFLICT_IGNORE);
                dataHolder.clear();
                dueDate = data.getString("DUE_DT");
                postedDate = data.getString("ITEM_EFFECTIVE_DT");
                dataHolder.put(COLUMN_DATES_FINANCE, 1);
                db.update(TABLE_DATES, dataHolder, COLUMN_DATES_DATE + "=" + "\"" + dueDate + "\"", null);
                dateFinance(dueDate, i, COLUMN_FINANCE_DUE_DATE);
                dateFinance(postedDate,i,COLUMN_FINANCE_POSTED_DATE);
            }
            db.setTransactionSuccessful();
        }finally {
            db.endTransaction();
        }
        db.close();
    }

    public Integer sumFinance(){
        Integer amount = 0;
        db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select sum(" + COLUMN_FINANCE_AMOUNT + ") from " + TABLE_FINANCE, null);
        if(cursor.moveToFirst()){
            amount = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return amount;
    }

    public List<FinanceData> queryFinance(String type){
        Log.d("query call", "INITIATED");
        db = this.getReadableDatabase();
        Cursor cursor;
        FinanceData financeData;
        List<FinanceData> data = new LinkedList<>();
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        if(type.equals("P")){
            builder.setTables(TABLE_FINANCE + " inner join " + TABLE_DATES + " on " + COLUMN_FINANCE_POSTED_DATE + " = " + TABLE_DATES + "." + COLUMN_ID);
        }else{
            builder.setTables(TABLE_FINANCE + " inner join " + TABLE_DATES + " on " + COLUMN_FINANCE_DUE_DATE + " = " + TABLE_DATES + "." + COLUMN_ID);
        }
        cursor = builder.query(db,null,"payment_type = \""+type+"\"",null,null,null,null);
        cursor.moveToFirst();
        try{
            do{
                financeData = new FinanceData(cursor.getString(cursor.getColumnIndex(COLUMN_FINANCE_DESCRIPTION)),
                        cursor.getInt(cursor.getColumnIndex(COLUMN_FINANCE_AMOUNT)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_DATES_DATE)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_FINANCE_TYPE)));
                data.add(financeData);
            }while (cursor.moveToNext());
        }catch(IndexOutOfBoundsException e){
            db.close();
            return null;
        }
        db.close();
        return data;
    }

    public List<ScheduleData> queryJournal(String whereClause){
        db = this.getReadableDatabase();
        Cursor cursor;
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        ScheduleData scheduleHolder;
        List<ScheduleData> data = new LinkedList<>();
        builder.setTables(TABLE_CLASSES + " inner join " + TABLE_DATES + " on " + COLUMN_CLASSES_DATE + " = " + TABLE_DATES + "." + COLUMN_ID +
                " inner join " + TABLE_COURSES + " on " + COLUMN_CLASSES_COURSE + " = " + TABLE_COURSES + "." + COLUMN_ID +
                " inner join " + TABLE_SHIFTS + " on " + COLUMN_CLASSES_SHIFT + " = " + TABLE_SHIFTS + "." + COLUMN_ID +
                " inner join " + TABLE_ROOM + " on " + COLUMN_CLASSES_ROOM + " = " + TABLE_ROOM + "." + COLUMN_ID +
                " inner join " + TABLE_TYPE + " on " + COLUMN_CLASSES_TYPE + " = " + TABLE_TYPE + "." + COLUMN_ID +
                " inner join " + TABLE_MODE + " on " + COLUMN_CLASSES_MODE + " = " + TABLE_MODE + "." + COLUMN_ID);
        cursor = builder.query(db,null,whereClause,null,null,null,null);
        cursor.moveToFirst();
        try {
            do {
                scheduleHolder = new ScheduleData();
                scheduleHolder.date = cursor.getString(cursor.getColumnIndex(COLUMN_DATES_DATE));
                scheduleHolder.mode = (cursor.getString(cursor.getColumnIndex(COLUMN_MODE_TYPE)));
                scheduleHolder.room = (cursor.getString(cursor.getColumnIndex(COLUMN_ROOM_NAME)));
                scheduleHolder.courseid = (cursor.getString(cursor.getColumnIndex(COLUMN_COURSES_CODE)));
                scheduleHolder.coursename = (cursor.getString(cursor.getColumnIndex(COLUMN_COURSES_NAME)));
                scheduleHolder.shift = (new String[]{cursor.getString(cursor.getColumnIndex(COLUMN_SHIFTS_SHIFT_START)), cursor.getString(cursor.getColumnIndex(COLUMN_SHIFTS_SHIFT_END))});
                scheduleHolder.session = (cursor.getInt(cursor.getColumnIndex(COLUMN_CLASSES_SESSION)));
                scheduleHolder.type = (cursor.getString(cursor.getColumnIndex(COLUMN_TYPE_TYPE)));
                data.add(scheduleHolder);
            } while (cursor.moveToNext());
            cursor.close();
            db.close();
            return data;
        }catch (CursorIndexOutOfBoundsException e){
            return null;
        }
    }

    public void deleteData(){
        db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_FINANCE, null, null);
            db.delete(TABLE_CLASSES, null, null);
            db.delete(TABLE_DATES, null, null);
            db.delete(TABLE_MODE, null, null);
            db.delete(TABLE_COURSES, null, null);
            db.delete(TABLE_ROOM, null, null);
            db.delete(TABLE_SHIFTS, null, null);
            db.delete(TABLE_TYPE, null, null);
            db.delete("sqlite_sequence", null, null);
            db.setTransactionSuccessful();
        }finally {
            db.endTransaction();
        }
        db.close();
    }

    private ContentValues classMaker(long i, String classColumn, String tableName, ContentValues dataHolder, String otherTableColumn) throws JSONException{
        ContentValues cv = new ContentValues();
        String[] searchColumn = new String[2];
        String[] searchAttribute = new String[2];
        Cursor cursor;
        if(i!=-1) {
            cv.put(classColumn, i);
        }else{
            searchColumn[0] = "_id";
            searchAttribute[0] = dataHolder.getAsString(otherTableColumn);
            cursor = db.query(tableName, searchColumn, otherTableColumn + "=\"" + searchAttribute[0] + "\"", null, null, null, null);
            if(cursor.moveToFirst()) {
                i = cursor.getInt(cursor.getColumnIndex("_id"));
            }
            cv.put(classColumn, i);
            cursor.close();
        }
        return cv;
    }

    private void dateFinance(String date, int i,String column) throws JSONException{
        ContentValues cv = new ContentValues();
        Integer data;
        Cursor cursor = db.query(TABLE_DATES,new String[]{COLUMN_ID},COLUMN_DATES_DATE+"="+"\""+date+"\"",null,null,null,null);
        long rowid;
        if (cursor.moveToFirst()) {
            data = cursor.getInt(0);
            cv.put(column, data);
            db.update(TABLE_FINANCE, cv, "_id = " + i, null);
        }else {
            cv.put(COLUMN_DATES_DATE, date);
            if(column.equals(COLUMN_FINANCE_POSTED_DATE)){
                cv.put(COLUMN_DATES_FINANCE,0);
            }else{
                cv.put(COLUMN_DATES_FINANCE,1);
            }
            cv.put(COLUMN_DATES_EXAM,0);
            cv.put(COLUMN_DATES_SCHEDULE,0);
            rowid = db.insertWithOnConflict(TABLE_DATES,null,cv,SQLiteDatabase.CONFLICT_IGNORE);
            cv.clear();
            cv.put(column, rowid);
            db.update(TABLE_FINANCE, cv, "_id = " + i, null);
        }
        cursor.close();
    }

    private ContentValues financeExtractor(JSONObject rawData) throws JSONException{
        ContentValues cv = new ContentValues();
        String stringData;
        String descr;
        Integer integerData;
        try {
            integerData = rawData.getInt("ITEM_AMT");
            stringData = rawData.getString("ITEM_TYPE_CD");
            descr = rawData.getString("DESCR");
            cv.put(COLUMN_FINANCE_AMOUNT, integerData);
            cv.put(COLUMN_FINANCE_TYPE, stringData);
            cv.put(COLUMN_FINANCE_DESCRIPTION, descr);
        }catch (StringIndexOutOfBoundsException e){
            cv.put(COLUMN_FINANCE_DESCRIPTION, "n/a");
            cv.put(COLUMN_FINANCE_AMOUNT, "n/a");
            cv.put(COLUMN_FINANCE_TYPE, "n/a");
        }
        return cv;
    }

    private ContentValues sessionExtractor(JSONObject rawData) throws JSONException{
        ContentValues cv = new ContentValues();
        String stringData;
        try {
            stringData = rawData.getString("Session");
            cv.put(COLUMN_CLASSES_SESSION, stringData);
        }catch (StringIndexOutOfBoundsException e){
            cv.put(COLUMN_CLASSES_SESSION, "n/a");
        }
        return cv;
    }

    private ContentValues typeExtractor(JSONObject rawData) throws JSONException{
        ContentValues cv = new ContentValues();
        String stringData;
        try {
            stringData = rawData.getString("Type");
            cv.put(COLUMN_TYPE_TYPE, stringData);
        }catch (StringIndexOutOfBoundsException e){
            cv.put(COLUMN_TYPE_TYPE, "n/a");
        }
        return cv;
    }

    private ContentValues modeExtractor(JSONObject rawData) throws JSONException{
        ContentValues cv = new ContentValues();
        String stringData;
        try {
            stringData = rawData.getString("Mode");
            cv.put(COLUMN_MODE_TYPE, stringData);
        }catch (StringIndexOutOfBoundsException e){
            cv.put(COLUMN_MODE_TYPE, "n/a");
        }
        return cv;
    }

    private ContentValues roomExtractor(JSONObject rawData) throws JSONException{
        ContentValues cv = new ContentValues();
        String stringData;
        try {
            stringData = rawData.getString("Room");
            cv.put(COLUMN_ROOM_NAME, stringData);
        }catch (StringIndexOutOfBoundsException e){
            cv.put(COLUMN_ROOM_NAME, "n/a");
        }
        return cv;
    }

    private ContentValues datesExtractor(JSONObject rawData) throws JSONException{
        ContentValues cv = new ContentValues();
        String stringData;
        try {
            stringData = rawData.getString("Date");
            cv.put(COLUMN_DATES_DATE, stringData);
        }catch (StringIndexOutOfBoundsException e){
            cv.put(COLUMN_DATES_DATE, "n/a");

        }finally {
            cv.put(COLUMN_DATES_FINANCE, 0);
            cv.put(COLUMN_DATES_EXAM, 0);
            cv.put(COLUMN_DATES_SCHEDULE, 1);
        }
        return cv;
    }

    private ContentValues courseExtractor(JSONObject rawData) throws JSONException{
        ContentValues cv = new ContentValues();
        String[] stringData = new String[3];
        try {
            stringData[0] = rawData.getString("CourseID");
            stringData[1] = rawData.getString("CourseName");
            cv.put(COLUMN_COURSES_CODE, stringData[0]);
            cv.put(COLUMN_COURSES_NAME, stringData[1]);
        }catch (StringIndexOutOfBoundsException e){
            cv.put(COLUMN_COURSES_CODE, "n/a");
            cv.put(COLUMN_COURSES_NAME, "n/a");
        }
        return cv;
    }

    private ContentValues shiftExtractor(JSONObject rawData) throws JSONException{
        ContentValues cv = new ContentValues();
        String[] stringData = new String[3];
        try {
            stringData[0]  = rawData.getString("Shift").substring(0,5);
            stringData[1]  = rawData.getString("Shift").substring(8,13);
            cv.put(COLUMN_SHIFTS_SHIFT_START, stringData[0]);
            cv.put(COLUMN_SHIFTS_SHIFT_END, stringData[1]);
        }catch (StringIndexOutOfBoundsException e){
            cv.put(COLUMN_SHIFTS_SHIFT_START, "n/a");
            cv.put(COLUMN_SHIFTS_SHIFT_END, "n/a");
        }
        return cv;
    }
}
