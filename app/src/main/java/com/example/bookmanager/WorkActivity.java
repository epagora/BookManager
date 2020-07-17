package com.example.bookmanager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class WorkActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
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
        keyAuthorId = 0;

        intent = getIntent();
        Bundle data = intent.getExtras();
        if (data != null) {
            keyAuthorId = data.getInt("authorId");
        }

        loadWork();

        listView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> av, View v, int i, long l) {
        workItem = workList.get(i);
        int workId = workItem.getWorkId();

        intent = new Intent(this,BookActivity.class);
        intent.putExtra("workId",workId);
        startActivity(intent);
    }

    protected void loadWork() {
        workList.clear();
        dbAdapter.open();

        String [] columns = {"_id","work_title","author_id"};
        Cursor cs;

        if(keyAuthorId == 0) {
            cs = dbAdapter.getTable("work", columns);
        }else {
            cs = dbAdapter.search("work", columns, "author_id", keyAuthorId);
        }
        if(cs.moveToFirst()) {
            do {
                workItem = new WorkTableItem(cs.getInt(0),cs.getString(1),cs.getInt(2));
                workList.add(workItem);
            }while (cs.moveToNext());
        }
        WorkBaseAdapter adapter = new WorkBaseAdapter(this,workList);
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

    public class WorkBaseAdapter extends BaseAdapter {
        private Context context;
        private List<WorkTableItem> workList;

        public WorkBaseAdapter(Context context, List<WorkTableItem> workList) {
            this.context = context;
            this.workList = workList;
        }

        @Override
        public int getCount() {
            return workList.size();
        }

        @Override
        public Object getItem(int i) {
            return workList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return workList.get(i).getAuthorId();
        }

        @Override
        public View getView(int i, View v, ViewGroup vg) {
            Activity activity = (Activity) context;
            WorkTableItem workItem = (WorkTableItem)getItem(i);
            if(v == null) {
                v = activity.getLayoutInflater().inflate(android.R.layout.simple_list_item_1,null);
            }
            ((TextView)v.findViewById(android.R.id.text1)).setText(workItem.getTitle());
            return v;
        }
    }
}
