package com.sample.learn.hostapp.ams_hook;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.sample.learn.plugin.utils.RefInvoke;

import static com.sample.learn.hostapp.ams_hook.ActivityStarter.EXTRA_TARGET_INTENT;
import static com.sample.learn.hostapp.ams_hook.ActivityStarter.TAG;

/**
 * Project Name：LearnBinder
 * Created by hejunqiu on 2019/12/16 14:38
 * Description:
 */
public class MockClass2 implements Handler.Callback {

    private static final int LAUNCH_ACTIVITY = 100; // targetApi < 28

    private static final int EXECUTE_TRANSACTION = 159;  // targetApi >= 28

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case LAUNCH_ACTIVITY:
                try {

                    /**
                     * 可在此欺骗系统，替换成你最终想要启动的目标 Activity.
                     */
                    Object obj = msg.obj;
                    Intent intent = (Intent) RefInvoke.getFieldObject(obj.getClass(), obj, "intent");

                    Intent targetIntent = intent.getParcelableExtra(EXTRA_TARGET_INTENT);

                    intent.setComponent(targetIntent.getComponent());

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case EXECUTE_TRANSACTION:
                Log.d(TAG, "hook --------------");
                break;
        }
        return false;
    }

}
