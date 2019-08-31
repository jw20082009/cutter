package com.wilbert.library.basic.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.wilbert.library.R;

public class SimpleVideoActivity extends AppCompatActivity {

    public static Intent createIntent(Context context, String filePath) {
        Intent intent = new Intent(context, SimpleVideoActivity.class);
        intent.putExtras(SimpleVideoFragment.createArgs(filePath));
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample_activity_simplevideo);
        initActionBar();
        SimpleVideoFragment fragment = new SimpleVideoFragment();
        fragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction().replace(R.id.layoutview, fragment).commit();
    }

    private void initActionBar() {
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
