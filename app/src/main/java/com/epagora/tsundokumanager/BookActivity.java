package com.epagora.tsundokumanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

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

    //オプションメニューを作成
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.book_option_menu, menu);
        return true;
    }

    //オプションメニューが選択された時の動作を定義
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.book_option_author: //著者一覧ページに移動
                intent = new Intent(this,MainActivity.class);
                startActivity(intent);
                break;
            case R.id.book_option_work: //作品一覧ページに移動
                intent = new Intent(this,WorkActivity.class);
                intent.putExtra("authorId", 0);
                startActivity(intent);
                break;
            case R.id.book_option_delete: //データベースの中身をすべて削除
                new AlertDialog.Builder(this) //確認用のダイアログを表示
                        .setTitle(R.string.all_delete)
                        .setMessage(R.string.really_all_delete)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dbAdapter.open();
                                dbAdapter.allDelete();
                                dbAdapter.close();
                                intent = new Intent(BookActivity.this,MainActivity.class);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {}
                        })
                        .show();
                break;
        }
        return true;
    }

    //ListViewの巻数クリック時にstatus（未購入=0、未読=1、既読=2）を変更
    //0 -> 1 -> 2 でそれ以上は変更しない
    //変更後にbookList、ListView更新
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        bookItem = bookList.get(i);
        int status = bookItem.getStatus();

        if(status == 2) {
            Toast toast = Toast.makeText(this, R.string.already, Toast.LENGTH_SHORT);
            toast.show();
        }else {
            status++;
        }

        dbAdapter.open();
        dbAdapter.changeStatus(bookItem.getBookId(), status);
        dbAdapter.close();

        loadBook();
        updateListView();
    }

    //ListViewの巻数ロングクリック時にダイアログ表示（ステータスを戻す、項目名の変更、削除、キャンセル）
    //変更、削除後にbookList、ListView更新
    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        bookItem = bookList.get(i);
        final int bookId = bookItem.getBookId();
        final String bookNumber = bookItem.getBookNumber();

        final ConstraintLayout layout = (ConstraintLayout) LayoutInflater.from(this).inflate(R.layout.dialog_body, null);
        final EditText editNewText = layout.findViewById(R.id.editNewText);

        new AlertDialog.Builder(this)
                .setTitle(bookNumber)
                .setItems(R.array.book_dialog_list, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (getResources().obtainTypedArray(R.array.book_dialog_list).getResourceId(i, -1)) {
                            case R.string.return_status: //ステータスを[未購入]に戻す
                                dbAdapter.open();
                                dbAdapter.changeStatus(bookId, 0);
                                dbAdapter.close();
                                loadBook();
                                updateListView();
                                break;
                            case R.string.rename: //項目名を変更する
                                new AlertDialog.Builder(BookActivity.this)
                                        .setTitle(bookNumber)
                                        .setView(layout)
                                        .setPositiveButton(R.string.rename, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dbAdapter.open();
                                                dbAdapter.changeItemName("book", bookId, editNewText.getText().toString());
                                                dbAdapter.close();
                                                loadBook();
                                                updateListView();
                                            }
                                        })
                                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {}
                                        })
                                        .show();
                                break;
                            case R.string.delete: //項目を削除する
                                new AlertDialog.Builder(BookActivity.this)
                                        .setTitle(bookNumber)
                                        .setMessage(R.string.really_delete)
                                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dbAdapter.open();
                                                dbAdapter.selectDelete("book", bookId);
                                                dbAdapter.close();
                                                loadBook();
                                                updateListView();
                                            }
                                        })
                                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {}
                                        })
                                        .show();
                                break;
                            default:
                        }
                    }
                })
                .show();

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

    //巻数テーブルの各要素（巻数コード、作品コード、巻数名、状態[未購入、未読、既読]）をフィールドに持つクラス
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
