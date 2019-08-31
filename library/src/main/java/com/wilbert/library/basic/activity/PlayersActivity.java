package com.wilbert.library.basic.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.wilbert.library.R;

import java.util.ArrayList;

public class PlayersActivity extends FragmentActivity {

    protected static final String PARAMS_NAME = "videoList";

    public static Intent createIntent(Context context, ArrayList<String> videoList) {
        Intent intent = new Intent(context, PlayersActivity.class);
        intent.putStringArrayListExtra(PARAMS_NAME, videoList);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample_activity_players);
        PlayersFragment fragment = new PlayersFragment();
        fragment.setArguments(PlayersFragment.createArgs(getIntent().getStringArrayListExtra(PARAMS_NAME), 1080, 1080));
        getSupportFragmentManager().beginTransaction().replace(R.id.videoLayout, fragment).commit();
    }
}
