package com.sample.learn.plugin.helper;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.sample.RefInvoke;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Project Name：LearnBinder
 * Created by hejunqiu on 2019/12/12 11:44
 * Description:
 */
public class HookAMSHelper {

    private static final String TAG = "HookAMSHelper";

    /**
     * 对Activity中 mInstrumentation 字段进行Hook.
     * 这种Hook方式的缺点 -- 只针对当前Activity有效.
     */
    public static void hookActivityByInstrumentation(Activity activityInstance) {
        try {
            Instrumentation instrumentation = (Instrumentation) RefInvoke.getFieldObject(
                    activityInstance.getClass(), activityInstance, "mInstrumentation");

            Instrumentation proxyInstrumentation = new EvilInstrumentation(instrumentation);

            RefInvoke.setFieldObject(activityInstance.getClass(),
                    activityInstance, "mInstrumentation", proxyInstrumentation);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class EvilInstrumentation extends Instrumentation {

        private Instrumentation mTarget;

        EvilInstrumentation(Instrumentation target) {
            super();
            mTarget = target;
        }

        public ActivityResult execStartActivity(
                Context who, IBinder contextThread,
                IBinder token, Activity target,
                Intent intent, int requestCode, Bundle options) {

            Log.d(TAG, "俺老孙到此一游");

            Class[] parameterTypes = {Context.class, IBinder.class,
                    IBinder.class, Activity.class,
                    Intent.class, int.class, Bundle.class};

            Object[] parameters = {who, contextThread, token, target, intent, requestCode, options};

            return (ActivityResult) RefInvoke.invokeInstanceMethod(
                    mTarget.getClass(),
                    mTarget,
                    "execStartActivity", parameterTypes, parameters);
        }

        public Activity newActivity(ClassLoader cl, String className,
                                    Intent intent)
                throws InstantiationException, IllegalAccessException,
                ClassNotFoundException {

            Log.d(TAG, "Jackson 到此一游 newActivity");

            return mTarget.newActivity(cl, className, intent);
        }

        @Override
        public void callActivityOnCreate(Activity activity, Bundle icicle) {

            Log.d(TAG, "Jackson 到此一游 callActivityOnCreate ");

            mTarget.callActivityOnCreate(activity, icicle);
        }
    }


    /**
     * 对 ActivityManagerService 在 App进程的 proxy 进行Hook.
     * 全局有效.
     */
    public static void hookAMSProxy() {
        try {
            Object gDefault;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                gDefault = RefInvoke.getStaticFieldObject(
                        "android.app.ActivityManager",
                        "IActivityManagerSingleton");

            } else {
                //获取 android.app.ActivityManagerNative 静态属性的值.
                gDefault = RefInvoke.getStaticFieldObject(
                        "android.app.ActivityManagerNative",
                        "gDefault");
            }

            // 执行实例 Singleton get 方法 初始化 mInstance属性
            RefInvoke.invokeInstanceMethod("android.util.Singleton",
                    gDefault,
                    "get");

            final Object originObj = RefInvoke.getFieldObject(
                    "android.util.Singleton",
                    gDefault,
                    "mInstance");

            Object proxy = Proxy.newProxyInstance(
                    originObj.getClass().getClassLoader(),
                    originObj.getClass().getInterfaces(),
                    new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//                            Log.d(TAG, "hey, baby; you are hooked!!");
//                            Log.d(TAG, "method: " + method.getName() + " called with args: " + Arrays.toString(args));
                            if ("startActivity".equalsIgnoreCase(method.getName())) {
                                Log.d(TAG, "I hooked startActivity method.");
                                return method.invoke(originObj, args);
                            }
                            return method.invoke(originObj, args);
                        }
                    });

            RefInvoke.setFieldObject(
                    "android.util.Singleton",
                    gDefault,
                    "mInstance",
                    proxy);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //-------------以上代码都是在调用AMS前进行Hook-----------------

    //-------------以下代码都是在 AMS 通知 App进程 后进行Hook-----------------

    /**
     * 对 ActivityThread 类中的 mH 字段进行 hook.
     */
    public static void hookActivityThreadByH() {
        try {
            Object sCurrentActivityThread = RefInvoke.getStaticFieldObject(
                    "android.app.ActivityThread", "sCurrentActivityThread");

            Handler handler = (Handler) RefInvoke.getFieldObject(
                    "android.app.ActivityThread", sCurrentActivityThread, "mH");

            RefInvoke.setFieldObject(Handler.class, handler, "mCallback", new ProxyHookHandler());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class ProxyHookHandler implements Handler.Callback {

        private static final int LAUNCH_ACTIVITY = 100;

        private static final int EXECUTE_TRANSACTION = 159;

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case LAUNCH_ACTIVITY:
                    /**
                     * 可在此欺骗系统，替换成你最终想要启动的目标 Activity.
                     */
                    Log.d(TAG, msg.obj.toString());
                    break;

                case EXECUTE_TRANSACTION:
                    Log.d(TAG, "hook --------------");
                    break;
            }
            return false;
        }
    }

    /**
     * 对 ActivityThread 类中的 mInstrumentation 字段进行 hook.
     */
    public static void hookActivityThreadInstrumentation() {
        try {
            Object sCurrentActivityThread = RefInvoke.getStaticFieldObject(
                    "android.app.ActivityThread", "sCurrentActivityThread");

            Instrumentation instrumentation = (Instrumentation) RefInvoke.getFieldObject("android.app.ActivityThread",
                    sCurrentActivityThread, "mInstrumentation");


            RefInvoke.setFieldObject("android.app.ActivityThread",
                    sCurrentActivityThread, "mInstrumentation",
                    new EvilInstrumentation(instrumentation));

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
