package com.example.bookmanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseAdapter {
    private static final String DBNAME = "bookManager";
    private static final int VERSION = 1;

    public final static String TABLE_A = "author";
    public final static String _ID_A = "_id";
    public final static String NAME_A = "author_name";
    public final static String TABLE_W = "work";
    public final static String _ID_W = "_id";
    public final static String TITLE_W = "work_title";
    public final static String A_ID_W = "author_id";
    public final static String TABLE_B ="book";
    public final static String W_ID_B = "work_id";
    public final static String NUMBER_B = "book_number";
    public final static String BOUGHT_B = "bought";
    public final static String READ_B = "read";

    protected final Context context;
    protected DatabaseHelper dbhelper;
    protected SQLiteDatabase db;

    public DatabaseAdapter(Context context) {
        this.context = context;
        dbhelper = new DatabaseHelper(this.context);
    }

    public DatabaseAdapter open() {
        db = dbhelper.getWritableDatabase();
        return this;
    }

    public DatabaseAdapter read() {
        db = dbhelper.getReadableDatabase();
        return this;
    }

    public void close() {
        db.close();
    }

    //著者テーブルに登録
    public void save(String author_name) {
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(NAME_A, author_name);
            db.insert(TABLE_A, null, values);
            db.setTransactionSuccessful();
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            db.endTransaction();
        }
    }

    //作品テーブルに登録
    public void save(String work_title, int author_id) {
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(TITLE_W, work_title);
            values.put(A_ID_W, author_id);
            db.insert(TABLE_W, null, values);
            db.setTransactionSuccessful();
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            db.endTransaction();
        }
    }

    //巻数テーブルに登録
    public void save(int work_id, String book_number, int bought, int read) {
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(W_ID_B, work_id);
            values.put(NUMBER_B, book_number);
            values.put(BOUGHT_B, bought);
            values.put(READ_B, read);
            db.insert(TABLE_B, null, values);
            db.setTransactionSuccessful();
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            db.endTransaction();
        }
    }

    public void changeBought(int work_id, String book_number, int bought) {
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(BOUGHT_B, bought);
            db.update(TABLE_B, values, W_ID_B + "=" + work_id + " and " + NUMBER_B + "= ?", new String[]{book_number});
            db.setTransactionSuccessful();
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            db.endTransaction();
        }
    }

    public void changeRead(int work_id, String book_number, int read) {
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(READ_B, read);
            db.update(TABLE_B, values, W_ID_B + "=" + work_id + " and " + NUMBER_B + "= ?", new String[]{book_number});
            db.setTransactionSuccessful();
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            db.endTransaction();
        }
    }

    public Cursor getTable(String dbTable, String[] columns) {
        return db.query(dbTable, columns, null, null, null, null, null);
    }

    public Cursor search(String dbTable, String[] columns, String column, int id) {
        return db.query(dbTable, columns, column + "=" + id, null, null, null, null);
    }

    public void allDelete() {
        db.beginTransaction();
        try {
            db.delete(TABLE_A, null, null);
            db.delete(TABLE_W, null, null);
            db.delete(TABLE_B, null, null);
            db.setTransactionSuccessful();
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            db.endTransaction();
        }
    }

    public void selectDelete(String dbTable, int id) {
        db.beginTransaction();
        try {
            if (dbTable.equals(TABLE_A)) {
                db.delete(TABLE_A, _ID_A + "=" + id, null);
            }else if(dbTable.equals(TABLE_W)) {
                db.delete(TABLE_W, _ID_W + "=" + id, null);
            }
            db.setTransactionSuccessful();
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            db.endTransaction();
        }
    }

    public void selectDelete(int id, String book_number) {
        db.beginTransaction();
        try {
            db.delete(TABLE_B, W_ID_B + "=" + id + " and " + NUMBER_B + "=" + book_number, null);
            db.setTransactionSuccessful();
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            db.endTransaction();
        }
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        public DatabaseHelper(Context context) {
            super(context, DBNAME, null, VERSION);
        }

        @Override
        public void onOpen(SQLiteDatabase db) {
            super.onOpen(db);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            //著者テーブル（著者コード[主キー]、著者名）
            db.execSQL("CREATE TABLE " + TABLE_A + "("
                    + _ID_A + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + NAME_A + " TEXT);");
            //作品テーブル（作品コード[主キー]、作品名、著者コード[外部キー]）
            db.execSQL("CREATE TABLE " + TABLE_W + "("
                    + _ID_W + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + TITLE_W + " TEXT,"
                    + A_ID_W + " INTEGER, "
                    + "FOREIGN KEY(" + A_ID_W + ") REFERENCES " + TABLE_A + "(" + _ID_A + ") ON DELETE CASCADE);");
            //巻数テーブル（作品コード[主キー、外部キー]、巻数[主キー]、所有、既読
            db.execSQL("CREATE TABLE " + TABLE_B + "("
                    + W_ID_B + " INTEGER,"
                    + NUMBER_B + " TEXT,"
                    + BOUGHT_B + " INTEGER,"
                    + READ_B + " INTEGER, "
                    + "PRIMARY KEY(" + W_ID_B + "," + NUMBER_B + "), "
                    + "FOREIGN KEY(" + W_ID_B + ") REFERENCES " + TABLE_W + "(" + _ID_W + ") ON DELETE CASCADE);");

//            db.execSQL("INSERT INTO " + TABLE_A + "(" + NAME_A + ") VALUES('神林長平')");
//            db.execSQL("INSERT INTO " + TABLE_A + "(" + NAME_A + ") VALUES('森博嗣')");
//            db.execSQL("INSERT INTO " + TABLE_A + "(" + NAME_A + ") VALUES('宮部みゆき')");
//            db.execSQL("INSERT INTO " + TABLE_A + "(" + NAME_A + ") VALUES('伊坂幸太郎')");
//            db.execSQL("INSERT INTO " + TABLE_W + "(" + TITLE_W + "," + A_ID_W + ") VALUES('膚の下','1')");
//            db.execSQL("INSERT INTO " + TABLE_W + "(" + TITLE_W + "," + A_ID_W + ") VALUES('帝王の殻','1')");
//            db.execSQL("INSERT INTO " + TABLE_W + "(" + TITLE_W + "," + A_ID_W + ") VALUES('戦闘妖精雪風','1')");

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int old_v, int new_v) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_A);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_W);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_B);
            onCreate(db);
        }
    }
}
