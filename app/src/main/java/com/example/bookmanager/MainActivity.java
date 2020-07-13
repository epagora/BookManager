package com.example.bookmanager;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private DatabaseHelper helper = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ArrayList<String> author = new ArrayList<>();

        helper = new DatabaseHelper(this);

        SQLiteDatabase db = helper.getReadableDatabase();
        String [] column = {"author_name"};

        Cursor cs = db.query("author",column,null,null,null,null,null,null);
        if(cs.moveToFirst()) {
            for(int i=0;i<=cs.getColumnCount();i++) {
                author.add(cs.getString(0));
                cs.moveToNext();
            }
        }
        cs.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,author);
        ListView list = findViewById(R.id.itemListView);
        list.setAdapter(adapter);


    }
}