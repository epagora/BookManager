package com.epagora.tsundokumanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
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

//作品一覧ページ用クラス
public class WorkActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    private DatabaseAdapter dbAdapter = null;
    WorkBaseAdapter adapter;
    List<WorkTableItem> workList;
    WorkTableItem workItem;
    ListView listView;
    EditText editText;
    Intent intent;
    int keyAuthorId;
    String keyAuthorName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbAdapter = new DatabaseAdapter(this);
        workList = new ArrayList<>();
        keyAuthorId = 0;

        //著者一覧ページ（MainActivity）から著者コードと著者名を取得
        //オプションメニューの作品一覧から来た場合は著者コードは0扱い、タイトルは作品一覧になる
        intent = getIntent();
        Bundle data = intent.getExtras();
        if (data != null) {
            keyAuthorId = data.getInt("authorId");
            if (keyAuthorId == 0) {
                setContentView(R.layout.activity_work_all);
                setTitle(R.string.work_list);
            }else {
                setContentView(R.layout.activity_main);
                keyAuthorName = data.getString("name");
                setTitle(keyAuthorName);
            }
        }
        listView = findViewById(R.id.itemListView);

        //画面遷移時に作品一覧を表示(アダプターのセットは遷移時のみ、更新時は行わない）
        loadWork();
        adapter = new WorkBaseAdapter(this,workList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);
    }

    //オプションメニューを作成
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option_menu, menu);
        return true;
    }

    //オプションメニューが選択された時の動作を定義
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.option_author: //著者一覧ページに移動
                intent = new Intent(this,MainActivity.class);
                startActivity(intent);
                break;
            case R.id.option_work: //作品一覧ページに移動、すでに作品一覧ページならトースト表示
                if(keyAuthorId == 0) {
                    Toast toast = Toast.makeText(this, R.string.currently_displayed, Toast.LENGTH_SHORT);
                    toast.show();
                }else {
                    intent = new Intent(this,WorkActivity.class);
                    intent.putExtra("authorId", 0);
                    startActivity(intent);
                }
                break;
            case R.id.option_delete: //データベースの中身をすべて削除
                new AlertDialog.Builder(this) //確認用のダイアログを表示
                        .setTitle(R.string.all_delete)
                        .setMessage(R.string.really_all_delete)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dbAdapter.open();
                                dbAdapter.allDelete();
                                dbAdapter.close();
                                intent = new Intent(WorkActivity.this,MainActivity.class);
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

    //ListViewの作品名クリック時に巻数一覧ページに移動
    //作品コードと作品名、著者名を渡す
    @Override
    public void onItemClick(AdapterView<?> av, View v, int i, long l) {
        workItem = workList.get(i);
        int workId = workItem.getWorkId();
        String title = workItem.getTitle();
        dbAdapter.open();
        keyAuthorName = dbAdapter.getAuthorName(workItem.getAuthorId());
        dbAdapter.close();

        intent = new Intent(this,BookActivity.class);
        intent.putExtra("workId", workId);
        intent.putExtra("title", title);
        intent.putExtra("name", keyAuthorName);

        startActivity(intent);
    }

    //ListViewの作品名ロングクリック時にダイアログ表示（項目名の変更、削除、キャンセル）
    //変更、削除後にworkList、ListView更新
    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        workItem = workList.get(i);
        final int workId = workItem.getWorkId();
        final String title = workItem.getTitle();

        final ConstraintLayout layout = (ConstraintLayout) LayoutInflater.from(this).inflate(R.layout.dialog_body, null);
        final EditText editNewText = layout.findViewById(R.id.editNewText);

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setItems(R.array.main_dialog_list, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (getResources().obtainTypedArray(R.array.main_dialog_list).getResourceId(i, -1)) {
                            case R.string.rename: //項目名を変更する
                                new AlertDialog.Builder(WorkActivity.this)
                                        .setTitle(title)
                                        .setView(layout)
                                        .setPositiveButton(R.string.rename, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dbAdapter.open();
                                                dbAdapter.changeItemName("work", workId, editNewText.getText().toString());
                                                dbAdapter.close();
                                                loadWork();
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
                                new AlertDialog.Builder(WorkActivity.this)
                                        .setTitle(title)
                                        .setMessage(R.string.really_delete)
                                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dbAdapter.open();
                                                dbAdapter.selectDelete("work", workId);
                                                dbAdapter.close();
                                                loadWork();
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
    //追加後にworkList、ListView更新
    public void Insert(View v) {
        editText = findViewById(R.id.editText);

        dbAdapter.open();
        dbAdapter.save(editText.getText().toString(), keyAuthorId);
        dbAdapter.close();

        editText.getText().clear();

        loadWork();
        updateListView();
    }

    //workList(WorkTableItemのList）にデータベースから情報を取得
    protected void loadWork() {
        dbAdapter.open();
        workList.clear();
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
        cs.close();

        dbAdapter.close();
    }

    //ListViewの更新
    //アダプターを取得し、そのアダプターにworkListをセットし直す
    public void updateListView() {
        adapter = (WorkBaseAdapter)listView.getAdapter();
        adapter.setWorkList(workList);
        adapter.notifyDataSetChanged();
    }

    //作品テーブルの各要素（作品コード、作品名、著者コード）をフィールドに持つクラス
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

    //workListとListViewを繋ぐためのアダプタークラス（ListViewの各列をつくる）
    //workListからWorkTableItemを抜き出し、ListViewの各列に表示
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

        //workListをセットし直す
        public void setWorkList(List<WorkTableItem> workList) {
            this.workList = workList;
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
