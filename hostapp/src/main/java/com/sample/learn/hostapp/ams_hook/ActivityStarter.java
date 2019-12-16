package com.sample.learn.hostapp.ams_hook;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.sample.learn.plugin.utils.RefInvoke;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Project Name：LearnBinder
 * Created by hejunqiu on 2019/12/12 16:31
 * Description:
 *
 * 启动一个没有在 AndroidManifest.xml文件中 声明的 Activity.
 */
public class ActivityStarter {

    static final String TAG = "ActivityStarter";

    static final String EXTRA_TARGET_INTENT = "target_intent";

    //----------------------Hook 上半场-------------------

    /**
     * 对 ActivityManagerService 进行 hook.
     */
    public static void hookAMN(String stubActivityClassName) {
        try {
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
                        new MockClass1(originObj, stubActivityClassName));

                RefInvoke.setFieldObject(
                        "android.util.Singleton",
                        gDefault,
                        "mInstance",
                        proxy);

            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //----------------------Hook 下半场-------------------

    /**
     * 对 ActivityThread 类 中的 mH 的 mCallBack 进行 Hook.
     */
    public static void hookActivityThreadBymH() {
        try {

            Object sCurrentActivityThread = RefInvoke.getStaticFieldObject(
                    "android.app.ActivityThread", "sCurrentActivityThread");

            Handler handler = (Handler) RefInvoke.getFieldObject(
                    "android.app.ActivityThread", sCurrentActivityThread, "mH");

            RefInvoke.setFieldObject(handler.getClass(), handler, "mCallback", new MockClass2());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 对 ActivityThread 类 中的 mInstrumentation 进行 Hook.
     */
    public static void hookActivityThreadInstrumentation(){
        try{
            Object sCurrentActivityThread = RefInvoke.getStaticFieldObject(
                    "android.app.ActivityThread", "sCurrentActivityThread");

            Instrumentation instrumentation = (Instrumentation) RefInvoke.getFieldObject(
                    "android.app.ActivityThread", sCurrentActivityThread, "mInstrumentation");

            RefInvoke.setFieldObject(
                    "android.app.ActivityThread", sCurrentActivityThread,
                    "mInstrumentation", new EvilInstrumentation(instrumentation));

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static class EvilInstrumentation extends Instrumentation {

        private Instrumentation mTarget;

        EvilInstrumentation(Instrumentation target) {
            super();
            mTarget = target;
        }

        public Activity newActivity(ClassLoader cl, String className,
                                    Intent intent)
                throws InstantiationException, IllegalAccessException,
                ClassNotFoundException {

            Intent targetIntent = intent.getParcelableExtra(EXTRA_TARGET_INTENT);
            if(targetIntent == null){
                return mTarget.newActivity(cl, className, intent);
            }

            String targetClassName = targetIntent.getComponent().getClassName();
            return mTarget.newActivity(cl, targetClassName, targetIntent);
        }
    }
}
