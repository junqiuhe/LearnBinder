package com.sample.learn.plugin.helper;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.sample.RefInvoke;

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

    private static final String TAG = "ActivityStarter";

    private static final String EXTRA_TARGET_INTENT = "target_intent";

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
                        new HookAMSProxyHandler(originObj, stubActivityClassName));

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

    private static class HookAMSProxyHandler implements InvocationHandler {

        private Object mTarget;

        private String mStubClassName;

        private HookAMSProxyHandler(Object target, String stubClassName) {
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

                //替身 Activity 的报名，也就是我们自己的包名
                String stubPackage = oldIntent.getComponent().getPackageName();

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

            RefInvoke.setFieldObject(handler.getClass(), handler, "mCallback", new ProxyHookHandler());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class ProxyHookHandler implements Handler.Callback {

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
