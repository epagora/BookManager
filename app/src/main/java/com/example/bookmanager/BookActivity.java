package com.example.bookmanager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class BookActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private DatabaseAdapter dbAdapter = null;
    List<BookTableItem> bookList;
    BookTableItem bookItem;
    ListView listView;
    EditText editText;
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

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        bookItem = bookList.get(i);
        int status = bookItem.getStatus();

        if(status == 2) {
            status = 0;
        }else {
            status++;
        }

        dbAdapter.open();
        dbAdapter.changeStatus(bookItem.getWorkId(), bookItem.getBookNumber(), status);
        dbAdapter.close();

        loadBook();
    }

    public void Insert(View v) {
        editText = findViewById(R.id.editText);
        dbAdapter.open();
        dbAdapter.save(keyWorkId, editText.getText().toString(), 0);
        dbAdapter.close();
        editText.getText().clear();
        loadBook();
    }



    protected void loadBook() {
        bookList.clear();
        dbAdapter.open();

        String [] columns = {"work_id","book_number","status"};
        Cursor cs;

        if(keyWorkId == 0) {
            cs = dbAdapter.getTable("book", columns);
        }else {
            cs = dbAdapter.search("book", columns, "work_id", keyWorkId);
        }
        if(cs.moveToFirst()) {
            do {
                bookItem = new BookTableItem(cs.getInt(0),cs.getString(1),cs.getInt(2));
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
        protected int status;

        public BookTableItem(int workId, String bookNumber, int status) {
            this.workId = workId;
            this.bookNumber = bookNumber;
            this.status = status;
        }

        public int getWorkId() {
            return workId;
        }

        public String getBookNumber() {
            return bookNumber;
        }

        public int getStatus() {
            return status;
        }

    }

    public class BookBaseAdapter extends BaseAdapter {
        private Context context;
        private List<BookTableItem> bookList;
        private TextView textViewNumber, textViewStatus;

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
        public View getView(final int i, View v, ViewGroup vg) {
            Activity activity = (Activity) context;
            final BookTableItem bookItem = (BookTableItem)getItem(i);
            if(v == null) {
                v = activity.getLayoutInflater().inflate(R.layout.rowbook,null);
            }
            textViewNumber = v.findViewById(R.id.textViewNumber);
            textViewStatus = v.findViewById(R.id.textViewStatus);

            textViewNumber.setText(bookItem.getBookNumber());
            switch (bookItem.getStatus()) {
                case 0:
                    textViewStatus.setText(R.string.non_purchased);
                    textViewStatus.setTextColor(Color.RED);
                    textViewNumber.setTextColor(Color.RED);
                    break;
                case 1:
                    textViewStatus.setText(R.string.unread);
                    textViewStatus.setTextColor(Color.YELLOW);
                    textViewNumber.setTextColor(Color.YELLOW);
                    break;
                case 2:
                    textViewStatus.setText(R.string.read);
                    textViewStatus.setTextColor(Color.BLACK);
                    textViewNumber.setTextColor(Color.BLACK);
                    break;
            }


//            CheckBox bought = v.findViewById(R.id.checkBoxBought);
//            CheckBox read = v.findViewById(R.id.checkBoxRead);
//            bought.setChecked(bookItem.getBought() != 0);
//            read.setChecked(bookItem.getRead() != 0);

//            bought.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                @Override
//                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                    boughtList.set(i,b);
//                }
//            });
//
//            read.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                @Override
//                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                    dbAdapter.open();
//                    if(b) {
//                        dbAdapter.changeRead(bookList.get(i).getWorkId(),bookList.get(i).getBookNumber(),1);
//                    }else {
//                        dbAdapter.changeRead(bookList.get(i).getWorkId(),bookList.get(i).getBookNumber(),0);
//                    }
//                    dbAdapter.close();
//                }
//            });

            return v;
        }
    }
}
