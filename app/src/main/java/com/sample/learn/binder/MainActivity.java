package com.sample.learn.binder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.sample.learn.binder.aidl.RemoteActivity;
import com.sample.learn.binder.local.LocalServiceActivities;
import com.sample.learn.binder.messager.MessengerServiceActivities;
import com.sample.proxy.HookHelper;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        try{
//            HookHelper.hookActivityManager();

            HookHelper.hookPackageManager();

        }catch (Exception e){
            e.printStackTrace();
        }
        super.attachBaseContext(newBase);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.bindService).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LocalServiceActivities.Binding.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.startService).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LocalServiceActivities.Controller.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.startMessenger).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MessengerServiceActivities.Binding.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.useAidl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RemoteActivity.class);
                startActivity(intent);
            }
        });
    }
}
