package com.sample.hook;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.sample.learn.binder.R;
import com.sample.reflect.utils.ActivityStarter;

/**
 * Project Name：LearnBinder
 * Created by hejunqiu on 2019/12/12 11:38
 * Description:
 */
public class FirstActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);

        ActivityStarter.hookAMN("com.sample.StubActivity");
//        ActivityStarter.hookActivityThreadBymH();

        ActivityStarter.hookActivityThreadInstrumentation();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_first);

        findViewById(R.id.launchSecondBtn).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(FirstActivity.this, SecondActivity.class);
                        startActivity(intent);
                    }
                });
    }
}
