package com.epagora.tsundokumanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

//データベース操作用アダプタークラス
//SQLiteOpenHelperを継承したDatabaseHelperクラスを内部クラスに持つ
public class DatabaseAdapter {
    private static final String DBNAME = "tsundokuManager";
    private static final int VERSION = 1;

    public final static String TABLE_A = "author";
    public final static String _ID_A = "_id";
    public final static String NAME_A = "author_name";
    public final static String TABLE_W = "work";
    public final static String _ID_W = "_id";
    public final static String TITLE_W = "work_title";
    public final static String A_ID_W = "author_id";
    public final static String TABLE_B ="book";
    public final static String _ID_B = "_id";
    public final static String W_ID_B = "work_id";
    public final static String NUMBER_B = "book_number";
    public final static String STATUS_B = "status";

    protected final Context context;
    protected DatabaseHelper dbhelper;
    protected SQLiteDatabase db;

    public DatabaseAdapter(Context context) {
        this.context = context;
        dbhelper = new DatabaseHelper(this.context);
    }

    //読み書きモードで開く
    public DatabaseAdapter open() {
        db = dbhelper.getWritableDatabase();
        return this;
    }

    //読み込みモードで開く
    public DatabaseAdapter read() {
        db = dbhelper.getReadableDatabase();
        return this;
    }

    //閉じる
    public void close() {
        db.close();
    }

    //saveメソッドは引数の違いでテーブルを見分ける
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
    public void save(int work_id, String book_number, int status) {
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(W_ID_B, work_id);
            values.put(NUMBER_B, book_number);
            values.put(STATUS_B, status);
            db.insert(TABLE_B, null, values);
            db.setTransactionSuccessful();
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            db.endTransaction();
        }
    }

    //巻数テーブルのstatus（未購入=0、未読=1、既読=2）を変更
    public void changeStatus(int work_id, String book_number, int status) {
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(STATUS_B, status);
            db.update(TABLE_B, values, W_ID_B + "=" + work_id + " and " + NUMBER_B + "= ?", new String[]{book_number});
            db.setTransactionSuccessful();
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            db.endTransaction();
        }
    }

    //任意のテーブルの全ての項目を取得
    public Cursor getTable(String dbTable, String[] columns) {
        return db.query(dbTable, columns, null, null, null, null, null);
    }

    //任意のテーブルの任意の項目を取得
    public Cursor search(String dbTable, String[] columns, String column, int id) {
        return db.query(dbTable, columns, column + "=" + id, null, null, null, null);
    }

    //著者コードから著者テーブルの著者名を取得
    public String getAuthorName(int id) {
        String[] columns = {"_id","author_name"};
        Cursor cs = db.query("author", columns, "_id=" + id, null, null, null, null);
        cs.moveToFirst();
        String authorName = cs.getString(1);
        cs.close();
        return authorName;
    }

    //全て削除
    //ON DELETE CASCADEを設定済みのため、authorテーブルを削除すれば全て削除出来る
    public void allDelete() {
        db.beginTransaction();
        try {
            db.delete(TABLE_A, null, null);
            db.setTransactionSuccessful();
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            db.endTransaction();
        }
    }

    //任意の項目を削除(authorテーブル、workテーブル用）
    //テーブルは引数で指定
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

    //任意の項目を削除（bookテーブル用）
    //テーブルの指定は必要なし
    public void selectDelete(int id, String book_number) {
        db.beginTransaction();
        try {
            db.delete(TABLE_B, W_ID_B + "=" + id + " and " + NUMBER_B + "= ?", new String[]{book_number});
            db.setTransactionSuccessful();
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            db.endTransaction();
        }
    }

    //SQLiteOpenHelperを継承したクラス、データベースを管理する
    private static class DatabaseHelper extends SQLiteOpenHelper {
        public DatabaseHelper(Context context) {
            super(context, DBNAME, null, VERSION);
        }

        @Override
        public void onOpen(SQLiteDatabase db) {
            super.onOpen(db);
            db.execSQL("PRAGMA foreign_keys = ON;");
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
            //巻数テーブル（巻数コード[主キー]、作品コード[外部キー]、巻数（限定版やファンブックもあるためTEXT）、状態（未購入=0、未読=1、既読=2）
            //巻数コードはデータベース内部でしか使用していない
            db.execSQL("CREATE TABLE " + TABLE_B + "("
                    + _ID_B + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + W_ID_B + " INTEGER,"
                    + NUMBER_B + " TEXT,"
                    + STATUS_B + " INTEGER,"
//                    + "PRIMARY KEY(" + W_ID_B + "," + NUMBER_B + "), "
                    + "FOREIGN KEY(" + W_ID_B + ") REFERENCES " + TABLE_W + "(" + _ID_W + ") ON DELETE CASCADE);");
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
