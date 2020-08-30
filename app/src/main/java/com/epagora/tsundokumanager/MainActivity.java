package com.epagora.tsundokumanager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

//名前変えてないけど著者一覧ページ用クラス
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

        setTitle(R.string.author_list);

        //起動、画面遷移時に著者一覧を表示(アダプターのセットは起動、画面遷移時のみ、更新時は行わない）
        loadAuthor();
        adapter = new AuthorBaseAdapter(this,authorList);
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
                Toast toast = Toast.makeText(this, "現在表示されています", Toast.LENGTH_SHORT);
                toast.show();
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
                loadAuthor();
                updateListView();
                break;
        }
        return true;
    }

    //ListViewの著者名クリック時に作品一覧ページに移動
    //著者コードと著者名を渡す
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

    //ListViewの著者名ロングクリック時に削除
    //削除後にauthorList、ListView更新
    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        authorItem = authorList.get(i);
        int authorId = authorItem.getAuthorId();
        String itemName = authorItem.getName();

        DialogFragment dialog = new MainDialogFragment();
        Bundle args = new Bundle();
        args.putInt("id", authorId);
        args.putString("itemName", itemName);
        args.putString("table", "author");
        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager(), "dialog_main");

//        dbAdapter.open();
//        dbAdapter.selectDelete("author", authorId);
//        dbAdapter.close();

        loadAuthor();
        updateListView();
        return true;
    }

    //追加ボタンクリック時にEditTextの入力内容を追加
    //追加後にauthorList、ListView更新
    public void Insert(View v) {
        editText = findViewById(R.id.editText);

        dbAdapter.open();
        dbAdapter.save(editText.getText().toString());
        dbAdapter.close();

        editText.getText().clear();

        loadAuthor();
        updateListView();
    }

    //authorList（AuthorTableItemのList）にデータベースから情報を取得
    protected void loadAuthor() {
        dbAdapter.open();
        authorList.clear();
        String[] columns = {"_id","author_name"};

        Cursor cs = dbAdapter.getTable("author",columns);
        if(cs.moveToFirst()) {
            do {
                authorItem = new AuthorTableItem(cs.getInt(0),cs.getString(1));
                authorList.add(authorItem);
            }while (cs.moveToNext());
        }
        cs.close();

        dbAdapter.close();
    }

    //ListViewの更新
    //アダプターを取得し、そのアダプターにauthorListをセットし直す
    public void updateListView() {
        adapter = (AuthorBaseAdapter)listView.getAdapter();
        adapter.setAuthorList(authorList);
        adapter.notifyDataSetChanged();
    }

    //著者テーブルの各要素（著者コード、著者名）をフィールドに持つクラス
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

    //authorListとListViewを繋ぐためのアダプタークラス（ListViewの各列をつくる）
    //authorListからAuthorTableItemを抜き出し、ListViewの各列に表示
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

        //authorListをセットし直す
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