package com.sample.learn.binder.aidl;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import com.sample.aidl.ISampleAidlInterface;
import com.sample.learn.binder.R;

/**
 * Project Name：LearnBinder
 * Created by hejunqiu on 2019/12/9 10:55
 * Description:
 */
public class RemoteActivity extends Activity {

    static final String TAG = "remote";

    private ISampleAidlInterface mService = null;
    private boolean mIsBounded = false;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = ISampleAidlInterface.Stub.asInterface(service);
            mIsBounded = true;

            try{
                Log.i(TAG, mService.getName());

            }catch (RemoteException e){
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mIsBounded = false;
            mService = null;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote);

        findViewById(R.id.doSomethingBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIsBounded){
                    try{
                        mService.doSomeThing();
                    }catch (RemoteException e){
                        e.printStackTrace();
                    }
                }
            }
        });

        findViewById(R.id.bind).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bindService();
            }
        });

        findViewById(R.id.unbind).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unBindService();
            }
        });
    }

    private void bindService(){
        if(mIsBounded){
            return;
        }
        Intent intent = new Intent();
        intent.setAction("com.sample.aidl.service");
        intent.setPackage("com.sample.learn.binder");

        bindService(intent, connection, Service.BIND_AUTO_CREATE);
    }

    private void unBindService(){
        if (mIsBounded) {
            unbindService(connection);
            mIsBounded = false;
            mService = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unBindService();
    }
}
