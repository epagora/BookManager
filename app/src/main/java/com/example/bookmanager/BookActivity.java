package com.example.bookmanager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class BookActivity extends AppCompatActivity {
    private DatabaseAdapter dbAdapter = null;
    List<BookTableItem> bookList;
    BookTableItem bookItem;
    ListView listView;
    Intent intent;
    int keyWorkId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.itemListView);
        dbAdapter = new DatabaseAdapter(this);
        bookList = new ArrayList<>();
        keyWorkId = 0;

        intent = getIntent();
        Bundle data = intent.getExtras();
        if (data != null) {
            keyWorkId = data.getInt("workId");
        }

        loadBook();
    }

    protected void loadBook() {
        bookList.clear();
        dbAdapter.open();

        String [] columns = {"work_id","book_number","bought","read"};
        Cursor cs;

        if(keyWorkId == 0) {
            cs = dbAdapter.getTable("book", columns);
        }else {
            cs = dbAdapter.search("book", columns, "work_id", keyWorkId);
        }
        if(cs.moveToFirst()) {
            do {
                bookItem = new BookTableItem(cs.getInt(0),cs.getString(1),cs.getInt(2),cs.getInt(3));
                bookList.add(bookItem);
            }while (cs.moveToNext());
        }
        BookBaseAdapter adapter = new BookBaseAdapter(this,bookList);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        cs.close();
        dbAdapter.close();
    }

    public class BookTableItem {
        protected int workId;
        protected String bookNumber;
        protected int bought;
        protected int read;

        public BookTableItem(int workId, String bookNumber, int bought, int read) {
            this.workId = workId;
            this.bookNumber = bookNumber;
            this.bought = bought;
            this.read = read;
        }

        public int getWorkId() {
            return workId;
        }

        public String getBookNumber() {
            return bookNumber;
        }

        public int getBought() {
            return bought;
        }

        public int getRead() {
            return read;
        }
    }

    public class BookBaseAdapter extends BaseAdapter {
        private Context context;
        private List<BookTableItem> bookList;

        public BookBaseAdapter(Context context, List<BookTableItem> bookList) {
            this.context = context;
            this.bookList = bookList;
        }

        @Override
        public int getCount() {
            return bookList.size();
        }

        @Override
        public Object getItem(int i) {
            return bookList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return bookList.get(i).getWorkId();
        }

        @Override
        public View getView(int i, View v, ViewGroup vg) {
            Activity activity = (Activity) context;
            BookTableItem bookItem = (BookTableItem)getItem(i);
            if(v == null) {
                v = activity.getLayoutInflater().inflate(R.layout.rowbook,null);
            }
            ((TextView)v.findViewById(R.id.TextView)).setText(bookItem.getBookNumber());
            ((CheckBox)v.findViewById(R.id.checkBoxBought)).setChecked(bookItem.getBought() != 0);
            ((CheckBox)v.findViewById(R.id.checkBoxRead)).setChecked(bookItem.getRead() != 0);
            return v;
        }
    }
}
