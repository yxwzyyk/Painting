package com.yxwzyyk.painting.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.github.clans.fab.FloatingActionButton;
import com.yxwzyyk.painting.R;
import com.yxwzyyk.painting.adapters.MainAdapter;
import com.yxwzyyk.painting.utils.L;
import com.yxwzyyk.painting.utils.Tools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.yxwzyyk.painting.app.ConstUtils.EXTENSION;
import static com.yxwzyyk.painting.app.ConstUtils.FILE_PATH;

public class MainActivity extends BaseActivity implements MainAdapter.OnRecyclerViewItemClickListener {
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.main_fab)
    FloatingActionButton mMainFab;
    @BindView(R.id.main_recyclerView)
    RecyclerView mMainRecyclerView;

    private List<File> mFileLise;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initView();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> list = new ArrayList<>();

            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                list.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            Tools.requestAlertWindowPermission(this, list, 0);
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        setRecyclerView();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int i = 0; i < permissions.length; i++) {
            if (permissions[i].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    toast(R.string.permission_storage);
                    finish();
                }
            }

        }
    }

    private void initView() {
        setSupportActionBar(mToolbar);

        mMainFab.setOnClickListener(v -> startActivity(new Intent(mContext, PaintingActivity.class)));
    }

    private void setRecyclerView() {
        getFiles(FILE_PATH, EXTENSION);

        MainAdapter mainAdapter = new MainAdapter(mContext, mFileLise);

        mMainRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        mainAdapter.setClickListener(this);
        mMainRecyclerView.setAdapter(mainAdapter);
    }

    private void getFiles(String Path, String Extension)  //搜索目录，扩展名，是否进入子文件夹
    {
        mFileLise = new ArrayList<>();
        try {

            File[] files = new File(Path).listFiles();
            if (files.length <= 0) return;

            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                if (f.isFile()) {
                    if (f.getPath().substring(f.getPath().length() - Extension.length()).equals(Extension))  //判断扩展名
                        mFileLise.add(f);

                } else if (f.isDirectory() && f.getPath().indexOf("/.") == -1)  //忽略点文件（隐藏文件/文件夹）
                    getFiles(f.getPath(), Extension);
            }
        } catch (Exception e) {
            L.e(TAG, e.getMessage());
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        Intent intent = new Intent(mContext, PaintingActivity.class);
        intent.putExtra("file", mFileLise.get(position).getName());
        startActivity(intent);
    }

    @Override
    public void onLongItemTouchHelper(View v, int position) {
        AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
        alertDialog.setTitle(R.string.main_delete);
        alertDialog.setMessage(mFileLise.get(position).getName());
        alertDialog.setCancelable(false);
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel), (dialog, which) -> {

        });
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok), (dialog, which) -> {
            File file = mFileLise.get(position);
            file.delete();
            setRecyclerView();
        });
        alertDialog.show();
    }
}
