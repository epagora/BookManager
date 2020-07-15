package com.example.bookmanager;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private DatabaseAdapter dbAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ArrayList<String> author = new ArrayList<>();

        dbAdapter = new DatabaseAdapter(this);
        dbAdapter.open();

        String [] column = {"author_name"};

        Cursor cs = dbAdapter.getTable("author",column);
        if(cs.moveToFirst()) {
            do {
                author.add(cs.getString(0));
            }while (cs.moveToNext());
        }
        cs.close();
        dbAdapter.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,author);
        ListView list = findViewById(R.id.itemListView);
        list.setAdapter(adapter);


    }
}