package com.sample.learn.hostapp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.sample.learn.hostapp.ams_hook.ActivityStarter;
import com.sample.learn.hostapp.classloader_hook.LoadedApkClassLoaderHookHelper;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        try {
            Utils.extractAssets(newBase, "plugin1.apk");

            LoadedApkClassLoaderHookHelper.hookLoadedApkInActivityThread(
                    getFileStreamPath("plugin1.apk"));

            ActivityStarter.hookAMN("com.sample.learn.hostapp.StubActivity");
            ActivityStarter.hookActivityThreadBymH();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent t = new Intent();
                    t.setComponent(new ComponentName("com.sample.learn.plugin1",
                            "com.sample.learn.plugin1.MainActivity"));

                    startActivity(t);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
