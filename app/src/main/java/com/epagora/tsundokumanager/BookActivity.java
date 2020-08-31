package com.epagora.tsundokumanager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

//巻数一覧ページ用クラス
public class BookActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    private DatabaseAdapter dbAdapter = null;
    BookBaseAdapter adapter;
    List<BookTableItem> bookList;
    BookTableItem bookItem;
    ListView listView;
    EditText editText;
    Intent intent;
    int keyWorkId;
    String keyWorkTitle;
    String keyAuthorName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.itemListView);
        dbAdapter = new DatabaseAdapter(this);
        bookList = new ArrayList<>();
        keyWorkId = 0;

        //作品一覧ページ（WorkActivity）から作品コードと作品名、著者名を取得
        intent = getIntent();
        Bundle data = intent.getExtras();
        if (data != null) {
            keyWorkId = data.getInt("workId");
            keyWorkTitle = data.getString("title");
            keyAuthorName = data.getString("name");
        }

        setTitle(keyAuthorName + " > " + keyWorkTitle);

        //画面遷移時に巻数一覧を表示(アダプターのセットは遷移時のみ、更新時は行わない）
        loadBook();
        adapter = new BookBaseAdapter(this,bookList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.option_author:
                intent = new Intent(this,MainActivity.class);
                startActivity(intent);
                break;
            case R.id.option_work:
                intent = new Intent(this,WorkActivity.class);
                intent.putExtra("authorId", 0);
                startActivity(intent);
                break;
            case R.id.option_delete:
                dbAdapter.open();
                dbAdapter.allDelete();
                dbAdapter.close();
                intent = new Intent(this,MainActivity.class);
                startActivity(intent);
                break;
        }
        return true;
    }

    //ListViewの巻数クリック時にstatus（未購入=0、未読=1、既読=2）を変更
    //0 -> 1 -> 2 -> 0 とループする
    //変更後にbookList、ListView更新
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
        dbAdapter.changeStatus(bookItem.getBookId(), status);
        dbAdapter.close();

        loadBook();
        updateListView();
    }

    //ListViewの巻数ロングクリック時に削除
    //削除後にbookList、ListView更新
    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        bookItem = bookList.get(i);
        int bookId = bookItem.getBookId();

        dbAdapter.open();
        dbAdapter.selectDelete("book", bookId);
        dbAdapter.close();

        loadBook();
        updateListView();
        return true;
    }

    //追加ボタンクリック時にEditTextの入力内容を追加
    //追加後にbookList、ListView更新
    public void Insert(View v) {
        editText = findViewById(R.id.editText);

        dbAdapter.open();
        dbAdapter.save(keyWorkId, editText.getText().toString(), 0);
        dbAdapter.close();

        editText.getText().clear();

        loadBook();
        updateListView();
    }

    //bookList(BookTableItemのList)にデータベースから情報を取得
    protected void loadBook() {
        dbAdapter.open();
        bookList.clear();
        String [] columns = {"_id","work_id","book_number","status"};

        Cursor cs;
        if(keyWorkId == 0) {
            cs = dbAdapter.getTable("book", columns);
        }else {
            cs = dbAdapter.search("book", columns, "work_id", keyWorkId);
        }
        if(cs.moveToFirst()) {
            do {
                bookItem = new BookTableItem(cs.getInt(0),cs.getInt(1),cs.getString(2),cs.getInt(3));
                bookList.add(bookItem);
            }while (cs.moveToNext());
        }
        cs.close();

        dbAdapter.close();
    }

    //ListViewの更新
    //アダプターを取得し、そのアダプターにbookListをセットし直す
    public void updateListView() {
        adapter = (BookBaseAdapter)listView.getAdapter();
        adapter.setBookList(bookList);
        adapter.notifyDataSetChanged();
    }

    //巻数テーブルの各要素（作品コード、巻数名、状態[未購入、未読、既読]）をフィールドに持つクラス
    public class BookTableItem {
        protected int bookId;
        protected int workId;
        protected String bookNumber;
        protected int status;

        public BookTableItem(int bookId, int workId, String bookNumber, int status) {
            this.bookId = bookId;
            this.workId = workId;
            this.bookNumber = bookNumber;
            this.status = status;
        }

        public int getBookId() {
            return bookId;
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

    //bookListとListViewを繋ぐためのアダプタークラス（ListViewの各列をつくる）
    //bookListからBookTableItemを抜き出し、ListViewの各列に表示
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

        //bookListをセットし直す
        public void setBookList(List<BookTableItem> bookList) {
            this.bookList = bookList;
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
                case 0: //statusが0なら文字を灰色にして「未購入」表示
                    textViewStatus.setText(R.string.non_purchased);
                    textViewStatus.setTextColor(Color.GRAY);
                    textViewNumber.setTextColor(Color.GRAY);
                    break;
                case 1: //statusが1なら文字を赤色にして「未読」表示
                    textViewStatus.setText(R.string.unread);
                    textViewStatus.setTextColor(Color.RED);
                    textViewNumber.setTextColor(Color.RED);
                    break;
                case 2: //statusが2なら文字を黒色にして「既読」表示
                    textViewStatus.setText(R.string.read);
                    textViewStatus.setTextColor(Color.BLACK);
                    textViewNumber.setTextColor(Color.BLACK);
                    break;
            }
            return v;
        }
    }
}
