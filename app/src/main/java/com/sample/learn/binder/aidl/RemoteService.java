package com.sample.learn.binder.aidl;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;

import com.sample.aidl.ISampleAidlInterface;

/**
 * Project Name：LearnBinder
 * Created by hejunqiu on 2019/12/9 10:28
 * Description:
 */
public class RemoteService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new SampleAidlService();
    }

    public class SampleAidlService extends ISampleAidlInterface.Stub {

        @Override
        public String getName() throws RemoteException {
            return "sample aidl";
        }

        @Override
        public void doSomeThing() throws RemoteException {
            Log.i("remote", "doSomething");
        }
    }
}