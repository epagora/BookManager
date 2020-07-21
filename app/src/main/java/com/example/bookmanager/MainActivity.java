package com.example.bookmanager;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    private DatabaseAdapter dbAdapter = null;
    AuthorBaseAdapter adapter;
    List<AuthorTableItem> authorList;
    AuthorTableItem authorItem;
    ListView listView;
    EditText editText;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.itemListView);
        dbAdapter = new DatabaseAdapter(this);
        authorList = new ArrayList<>();

        setTitle(R.string.authorList);

        loadAuthor();
        adapter = new AuthorBaseAdapter(this,authorList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> av, View v, int i, long l) {
        authorItem = authorList.get(i);
        int authorId = authorItem.getAuthorId();
        String name = authorItem.getName();

        intent = new Intent(this,WorkActivity.class);
        intent.putExtra("authorId",authorId);
        intent.putExtra("name", name);
        startActivity(intent);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        authorItem = authorList.get(i);
        int authorId = authorItem.getAuthorId();
        dbAdapter.open();
        dbAdapter.selectDelete("author", authorId);
        dbAdapter.close();
        loadAuthor();
        updateListView();
        return true;
    }

    public void Insert(View v) {
        editText = findViewById(R.id.editText);
        dbAdapter.open();
        dbAdapter.save(editText.getText().toString());
        dbAdapter.close();
        editText.getText().clear();
        loadAuthor();
        updateListView();
    }

    protected void loadAuthor() {
        authorList.clear();
        dbAdapter.open();

        String [] column = {"_id","author_name"};

        Cursor cs = dbAdapter.getTable("author",column);
        if(cs.moveToFirst()) {
            do {
                authorItem = new AuthorTableItem(cs.getInt(0),cs.getString(1));
                authorList.add(authorItem);
            }while (cs.moveToNext());
        }

        cs.close();
        dbAdapter.close();
    }

    public void updateListView() {
        adapter = (AuthorBaseAdapter)listView.getAdapter();
        adapter.setAuthorList(authorList);
        adapter.notifyDataSetChanged();
    }

    public class AuthorTableItem {
        protected int authorId;
        protected String name;

        public AuthorTableItem(int authorId, String name) {
            this.authorId = authorId;
            this.name = name;
        }

        public int getAuthorId() {
            return authorId;
        }

        public String getName() {
            return name;
        }
    }

    public class AuthorBaseAdapter extends BaseAdapter {
        private Context context;
        private List<AuthorTableItem> authorList;

        public AuthorBaseAdapter(Context context, List<AuthorTableItem> authorList) {
            this.context = context;
            this.authorList = authorList;
        }

        @Override
        public int getCount() {
            return authorList.size();
        }

        @Override
        public Object getItem(int i) {
            return authorList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return authorList.get(i).getAuthorId();
        }

        public void setAuthorList(List<AuthorTableItem> authorList) {
            this.authorList = authorList;
        }

        @Override
        public View getView(int i, View v, ViewGroup vg) {
            Activity activity = (Activity) context;
            AuthorTableItem authorItem = (AuthorTableItem)getItem(i);
            if(v == null) {
                v = activity.getLayoutInflater().inflate(android.R.layout.simple_list_item_1,null);
            }
            ((TextView)v.findViewById(android.R.id.text1)).setText(authorItem.getName());
            return v;
        }
    }
}