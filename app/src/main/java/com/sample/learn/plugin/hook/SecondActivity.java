package com.sample.learn.plugin.hook;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.sample.learn.binder.R;

/**
 * Project Name：LearnBinder
 * Created by hejunqiu on 2019/12/12 11:38
 * Description:
 */
public class SecondActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
    }
}
