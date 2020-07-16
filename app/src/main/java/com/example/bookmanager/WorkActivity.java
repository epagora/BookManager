package com.example.bookmanager;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class WorkActivity extends AppCompatActivity {
    private DatabaseAdapter dbAdapter = null;
    List<WorkTableItem> workList;
    WorkTableItem workItem;
    ListView listView;
    Intent intent;
    int keyAuthorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.itemListView);
        dbAdapter = new DatabaseAdapter(this);
        workList = new ArrayList<>();

        intent = getIntent();
        Bundle data = intent.getExtras();
        if(data != null) {
            keyAuthorId = data.getInt("authorId");
        }

        loadWork();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> av, View v, int i, long l) {
                workItem = workList.get(i);
                int authorId = workItem.getWorkId();

                //intent = new Intent(this,WorkActivity.class);
                intent.putExtra("authorId",authorId);
                startActivity(intent);
            }
        });
    }

    protected void loadWork() {
        workList.clear();
        dbAdapter.open();

        String [] column = {"_id","work_title","author_id"};
        Cursor cs;

        if(keyAuthorId == 0) {
            cs = dbAdapter.getTable("work", column);
        }else {
            cs = dbAdapter.search("work", column, "author_id = ?", keyAuthorId);
        }
        if(cs.moveToFirst()) {
            do {
                workItem = new WorkTableItem(cs.getInt(0),cs.getString(1),cs.getInt(2));
                workList.add(workItem);
            }while (cs.moveToNext());
        }
        ArrayAdapter<WorkTableItem> adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,workList);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        cs.close();
        dbAdapter.close();
    }

    public class WorkTableItem {
        protected int workId;
        protected String title;
        protected int authorId;

        public WorkTableItem(int workId, String title, int authorId) {
            this.workId = workId;
            this.title = title;
            this.authorId = authorId;
        }

        public int getWorkId() {
            return workId;
        }

        public String getTitle() {
            return title;
        }

        public int getAuthorId() {
            return authorId;
        }
    }
}
