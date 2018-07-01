package com.bs.function.diary;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.bs.easy_chat.R;
import com.bs.util.BaseActivity;
import com.bumptech.glide.Glide;

import java.io.File;

public class ImageActivity extends BaseActivity{

    int position;
    private Toolbar.OnMenuItemClickListener onMenuItemClick = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()){
                case R.id.action_delete:
                    if(new File(CreateDiaryActivity.picturePath.get(position)).delete()){
                        CreateDiaryActivity.picturePath.remove(position);
                        CreateDiaryActivity.adapter.notifyDataSetChanged();
                        finish();
                    }
                    else finish();
                    break;
            }
            return true;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_layout);
        position = getIntent().getExtras().getInt("position");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("");
        toolbar.setOnMenuItemClickListener(onMenuItemClick);
        if(getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Glide.with(this).
                load(new File(CreateDiaryActivity.picturePath.get(position))).
                into((ImageView) findViewById(R.id.image));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.image_deletion_menu, menu);
        return true;
    }
}
