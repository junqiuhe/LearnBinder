package com.sample.learn.hostapp.ams_hook;

import android.content.ComponentName;
import android.content.Intent;
import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import static com.sample.learn.hostapp.ams_hook.ActivityStarter.EXTRA_TARGET_INTENT;
import static com.sample.learn.hostapp.ams_hook.ActivityStarter.TAG;

/**
 * Project Name：LearnBinder
 * Created by hejunqiu on 2019/12/16 14:35
 * Description:
 */
public class MockClass1 implements InvocationHandler {

    private Object mTarget;

    private String mStubClassName;

    MockClass1(Object target, String stubClassName) {
        mTarget = target;
        mStubClassName = stubClassName;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Log.d(TAG, "method: " + method.getName());

        /**
         * 基本思路就是: 拦截startActivity方法，从参数中取出原有的 Intent,
         * 替换为启动 StubActivity 的 newIntent,
         * 同时把原有的 Intent 保存在 newIntent 中.
         */
        if ("startActivity".equalsIgnoreCase(method.getName())) {
            Log.d(TAG, "I hooked startActivity method.");
            // 只拦截这个方法
            // 替换参数，任何所为；甚至替换原始Activity启动别的Activity，偷梁换柱
            // 找到参数里面的第一个 Intent 对象

            Intent oldIntent;
            int index = 0;

            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof Intent) {
                    index = i;
                    break;
                }
            }

            oldIntent = (Intent) args[index];

            //替身 Activity 的包名，也就是我们自己的包名
//            String stubPackage = oldIntent.getComponent().getPackageName();
            String stubPackage = mStubClassName.substring(0, mStubClassName.lastIndexOf('.'));

            Log.d(TAG, "stubPackage: " + stubPackage + ", stubClassName:" +
                    "" + mStubClassName);

            Intent newIntent = new Intent();
            newIntent.setComponent(new ComponentName(stubPackage, mStubClassName));
            newIntent.putExtra(EXTRA_TARGET_INTENT, oldIntent); //把我们原始要启动的 TargetActivity先保存起来.

            args[index] = newIntent;

            Log.d(TAG, "Hook success");
            return method.invoke(mTarget, args);
        }
        return method.invoke(mTarget, args);
    }
}
