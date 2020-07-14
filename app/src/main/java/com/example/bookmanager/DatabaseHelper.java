package com.example.bookmanager;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {
    static final private String DBNAME = "bookManager";
    static final private int VERSION = 1;
    public DatabaseHelper(@Nullable Context context) {
        super(context, DBNAME, null, VERSION);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE author(_id INTEGER PRIMARY KEY AUTOINCREMENT, author_name TEXT)");
        db.execSQL("CREATE TABLE work(_id INTEGER PRIMARY KEY AUTOINCREMENT, work_title TEXT, author_id INTEGER, FOREIGN KEY(author_id) REFERENCES author(_id) ON DELETE CASCADE)");
        db.execSQL("CREATE TABLE book(work_id INTEGER, book_number TEXT, bought INTEGER, read INTEGER, PRIMARY KEY(work_id, book_number), FOREIGN KEY(work_id) REFERENCES work(_id) ON DELETE CASCADE)");
        db.execSQL("INSERT INTO author(author_name) VALUES('神林長平')");
        db.execSQL("INSERT INTO author(author_name) VALUES('森博嗣')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int old_v, int new_v) {
        db.execSQL("DROP TABLE IF EXISTS author");
        db.execSQL("DROP TABLE IF EXISTS work");
        db.execSQL("DROP TABLE IF EXISTS book");
        onCreate(db);
    }
}
