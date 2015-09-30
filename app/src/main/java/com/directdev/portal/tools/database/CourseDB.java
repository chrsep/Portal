package com.directdev.portal.tools.database;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;

import com.directdev.portal.tools.datatype.ScoreData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CourseDB extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "Course";
    public static final String TABLE_TERM = "terms";
    public static final String TABLE_COURSE = "courses";
    public static final String TABLE_TYPE = "types";
    public static final String TABLE_SCORE = "scores";
    public static final String TABLE_GPA = "gpas";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TERM_TERM = "term";
    public static final String COLUMN_TERM_DESCRIPTION = "description";
    public static final String COLUMN_TYPE_TYPE = "type";
    public static final String COLUMN_GPA_GPA = "gpa";
    public static final String COLUMN_GPA_GPS = "gps";
    public static final String COLUMN_GPA_TERM = "term_id";
    public static final String COLUMN_COURSE_COURSE = "course";
    public static final String COLUMN_COURSE_TERM = "term_id";
    public static final String COLUMN_SCORE_TYPE = "type_id";
    public static final String COLUMN_SCORE_TERM = "term_id";
    public static final String COLUMN_SCORE_COURSE = "course_id";
    public static final String COLUMN_SCORE_SCORE = "score";
    public static final String COLUMN_SCORE_TOTAL = "total";
    public static final String COLUMN_SCORE_GRADE = "grades";
    private static final String CREATE_TABLE_GPA = "create table if not exists " + TABLE_GPA + " ("
            + COLUMN_ID + " integer primary key autoincrement,"
            + COLUMN_GPA_GPS + " integer,"
            + COLUMN_GPA_GPA + " text,"
            + COLUMN_GPA_TERM + " integer,"
            + "foreign key(" + COLUMN_GPA_TERM + ") references " + TABLE_TERM + "(" + COLUMN_ID + "));";
    private static final String CREATE_TABLE_COURSE = "create table if not exists " + TABLE_COURSE + " ("
            + COLUMN_ID + " integer primary key autoincrement,"
            + COLUMN_COURSE_TERM + " integer,"
            + COLUMN_COURSE_COURSE + " text unique,"
            + "foreign key(" + COLUMN_COURSE_TERM + ") references " + TABLE_TERM + "(" + COLUMN_ID + "));";
    private static final String CREATE_TABLE_TERM = "create table if not exists " + TABLE_TERM + " ("
            + COLUMN_ID + " integer primary key autoincrement,"
            + COLUMN_TERM_DESCRIPTION + " text,"
            + COLUMN_TERM_TERM + " text unique);";
    private static final String CREATE_TABLE_TYPE = "create table if not exists " + TABLE_TYPE + " ("
            + COLUMN_ID + " integer primary key autoincrement,"
            + COLUMN_TYPE_TYPE + " text unique);";
    private static final String CREATE_TABLE_SCORE = "create table if not exists " + TABLE_SCORE + " ("
            + COLUMN_ID + " integer primary key autoincrement,"
            + COLUMN_SCORE_TYPE + " integer,"
            + COLUMN_SCORE_COURSE + " integer,"
            + COLUMN_SCORE_TERM + " integer,"
            + COLUMN_SCORE_SCORE + " integer,"
            + COLUMN_SCORE_TOTAL + " integer,"
            + COLUMN_SCORE_GRADE + " string,"
            + "foreign key(" + COLUMN_SCORE_TYPE + ") references " + TABLE_TYPE + "(" + COLUMN_ID + "),"
            + "foreign key(" + COLUMN_SCORE_COURSE + ") references " + TABLE_COURSE + "(" + COLUMN_ID + "),"
            + "foreign key(" + COLUMN_SCORE_TERM + ") references " + TABLE_TERM + "(" + COLUMN_ID + "));";
    private Context context;
    private SQLiteDatabase db;

    public CourseDB(Context ctx) {
        super(ctx, DATABASE_NAME, null, 1);
        this.context = ctx;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_COURSE);
        db.execSQL(CREATE_TABLE_TERM);
        db.execSQL(CREATE_TABLE_TYPE);
        db.execSQL(CREATE_TABLE_SCORE);
        db.execSQL(CREATE_TABLE_GPA);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void deleteData() {
        db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_SCORE, null, null);
            db.delete(TABLE_COURSE, null, null);
            db.delete(TABLE_TERM, null, null);
            db.delete(TABLE_TYPE, null, null);
            db.delete(TABLE_GPA, null, null);
            db.delete("sqlite_sequence", null, null);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        db.close();
    }

    public void addTerms(JSONArray data) throws JSONException {
        ContentValues dataHolder = new ContentValues();
        db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            for (int i = 0; i < data.length(); i++) {
                dataHolder.put(COLUMN_TERM_TERM, data.getJSONObject(i).getString("value"));
                dataHolder.put(COLUMN_TERM_DESCRIPTION, data.getJSONObject(i).getString("field"));
                db.insertWithOnConflict(TABLE_TERM, null, dataHolder, SQLiteDatabase.CONFLICT_IGNORE);
                dataHolder.clear();
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        db.close();
    }

    public List<String> queryTerm() {
        db = this.getReadableDatabase();
        List<String> terms = new ArrayList<>();
        Cursor cursor = db.query(TABLE_TERM, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                terms.add(cursor.getString(cursor.getColumnIndex(COLUMN_TERM_TERM)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return terms;
    }

    public List<String> queryCourse(String term) {
        db = this.getReadableDatabase();
        List<String> courses = new ArrayList<>();
        Cursor cursor = db.query(TABLE_TERM, null, COLUMN_TERM_TERM + " = " + "\"" + term + "\"", null, null, null, null);
        cursor.moveToFirst();
        Cursor courseCursor = db.query(TABLE_COURSE, null, COLUMN_COURSE_TERM + " = " + "\"" + cursor.getString(cursor.getColumnIndex(COLUMN_ID)) + "\"", null, null, null, null);
        if (courseCursor.moveToFirst()) {
            do {
                courses.add(courseCursor.getString(courseCursor.getColumnIndex(COLUMN_COURSE_COURSE)));
            } while (courseCursor.moveToNext());
        }
        cursor.close();
        courseCursor.close();
        db.close();
        return courses;
    }

    public ScoreData queryGrades(String course) {
        db = this.getReadableDatabase();
        ScoreData scoreData = new ScoreData();
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(TABLE_SCORE + " inner join " + TABLE_TYPE + " on " + COLUMN_SCORE_TYPE + " = " + TABLE_TYPE + "." + COLUMN_ID +
                " inner join " + TABLE_COURSE + " on " + COLUMN_SCORE_COURSE + " = " + TABLE_COURSE + "." + COLUMN_ID);
        Cursor cursor = builder.query(db, null, COLUMN_COURSE_COURSE + " = " + "\"" + course + "\"", null, null, null, null);
        cursor.moveToFirst();
        if (cursor.moveToFirst()) {
            scoreData.grade = cursor.getString(cursor.getColumnIndex(COLUMN_SCORE_GRADE));
            do {
                if (cursor.getString(cursor.getColumnIndex(COLUMN_TYPE_TYPE)).equals("ASSIGNMENT")) {
                    scoreData.assignment = cursor.getString(cursor.getColumnIndex(COLUMN_SCORE_SCORE));
                } else if (cursor.getString(cursor.getColumnIndex(COLUMN_TYPE_TYPE)).equals("MID EXAM")) {
                    scoreData.mid = cursor.getString(cursor.getColumnIndex(COLUMN_SCORE_SCORE));
                } else if (cursor.getString(cursor.getColumnIndex(COLUMN_TYPE_TYPE)).equals("FINAL EXAM")) {
                    scoreData.fin = cursor.getString(cursor.getColumnIndex(COLUMN_SCORE_SCORE));
                } else if (cursor.getString(cursor.getColumnIndex(COLUMN_TYPE_TYPE)).equals("LABORATORY")) {
                    scoreData.lab = cursor.getString(cursor.getColumnIndex(COLUMN_SCORE_SCORE));
                }


            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return scoreData;
    }

    public void addGrades(JSONObject data, String term) throws JSONException {
        ContentValues dataHolder = new ContentValues();
        long rowid;
        Cursor cursor;
        JSONArray dataArray = data.getJSONArray("score");
        db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            cursor = db.query(TABLE_TERM, null, COLUMN_TERM_TERM + " = \"" + term + "\"", null, null, null, null);
            cursor.moveToFirst();
            long score_id;
            dataHolder.put(COLUMN_GPA_GPA, data.getJSONObject("credit").getString("GPA_CUM"));
            dataHolder.put(COLUMN_GPA_GPS, data.getJSONObject("credit").getString("GPA_CUR"));
            dataHolder.put(COLUMN_GPA_TERM, cursor.getString(cursor.getColumnIndex(COLUMN_ID)));
            db.insertWithOnConflict(TABLE_GPA, null, dataHolder, SQLiteDatabase.CONFLICT_IGNORE);
            for (int i = 0; i < dataArray.length(); i++) {
                dataHolder = courseExtractor(dataArray.getJSONObject(i), COLUMN_COURSE_COURSE, "course");
                dataHolder.put(COLUMN_COURSE_TERM, cursor.getString(cursor.getColumnIndex(COLUMN_ID)));
                rowid = db.insertWithOnConflict(TABLE_COURSE, null, dataHolder, SQLiteDatabase.CONFLICT_IGNORE);
                dataHolder = scoreMaker(rowid, COLUMN_SCORE_COURSE, TABLE_COURSE, dataHolder, COLUMN_COURSE_COURSE);
                score_id = db.insertWithOnConflict(TABLE_SCORE, null, dataHolder, SQLiteDatabase.CONFLICT_IGNORE);

                dataHolder = courseExtractor(dataArray.getJSONObject(i), COLUMN_TYPE_TYPE, "lam");
                rowid = db.insertWithOnConflict(TABLE_TYPE, null, dataHolder, SQLiteDatabase.CONFLICT_IGNORE);
                dataHolder = scoreMaker(rowid, COLUMN_SCORE_TYPE, TABLE_TYPE, dataHolder, COLUMN_TYPE_TYPE);
                db.update(TABLE_SCORE, dataHolder, "_id = " + score_id, null);

                dataHolder = scoreExtractor(dataArray.getJSONObject(i), cursor.getString(cursor.getColumnIndex(COLUMN_ID)));
                db.update(TABLE_SCORE, dataHolder, "_id = " + score_id, null);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        db.close();
    }

    private ContentValues scoreMaker(long i, String gradesColumn, String tableName, ContentValues dataHolder, String otherTableColumn) throws JSONException {
        ContentValues cv = new ContentValues();
        String[] searchColumn = new String[2];
        String[] searchAttribute = new String[2];
        Cursor cursor;
        if (i != -1) {
            cv.put(gradesColumn, i);
        } else {
            searchColumn[0] = "_id";
            searchAttribute[0] = dataHolder.getAsString(otherTableColumn);
            cursor = db.query(tableName, searchColumn, otherTableColumn + "=\"" + searchAttribute[0] + "\"", null, null, null, null);
            if (cursor.moveToFirst()) {
                i = cursor.getInt(cursor.getColumnIndex("_id"));
            }
            cv.put(gradesColumn, i);
            cursor.close();
        }
        return cv;
    }

    private ContentValues scoreExtractor(JSONObject data, String term) throws JSONException {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_SCORE_TERM, term);
        cv.put(COLUMN_SCORE_SCORE, data.getString("score"));
        cv.put(COLUMN_SCORE_TOTAL, data.getString("grade"));
        cv.put(COLUMN_SCORE_GRADE, data.getString("course_grade"));
        return cv;
    }

    private ContentValues courseExtractor(JSONObject rawData, String column, String stringName) throws JSONException {
        ContentValues cv = new ContentValues();
        String stringData;
        try {
            stringData = rawData.getString(stringName);
            cv.put(column, stringData);
        } catch (StringIndexOutOfBoundsException e) {
            cv.put(column, "n/a");
        }
        return cv;
    }
}
