package com.yxwzyyk.painting.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * Created by yyk on 29/09/2016.
 */

public class BaseActivity extends AppCompatActivity {
    protected final String TAG = this.getClass().getSimpleName();
    protected FirebaseAnalytics mFirebaseAnalytics;
    protected Context mContext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        init();
    }

    private void init() {
        mContext = this;
    }

    protected void toast(String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }

    protected void toast(int message) {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }
}
