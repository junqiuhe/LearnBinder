package com.sample.proxy;

import android.os.Build;
import android.util.Log;

import com.sample.reflect.utils.RefInvoke;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

/**
 * Project Name：LearnBinder
 * Created by hejunqiu on 2019/12/11 15:34
 * Description:
 */
public class HookHelper {

    /**
     * 对ActivityManagerService进行Hook.
     */
    public static void hookActivityManager() {
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

            Object originObj = RefInvoke.getFieldObject(
                    "android.util.Singleton",
                    gDefault,
                    "mInstance");

            Object proxy = Proxy.newProxyInstance(
                    originObj.getClass().getClassLoader(),
                    originObj.getClass().getInterfaces(), new HookHandler(originObj));

            RefInvoke.setFieldObject(
                    "android.util.Singleton",
                    gDefault,
                    "mInstance",
                    proxy);

        } catch (Exception e) {
            throw new RuntimeException("Hook Failed", e);
        }
    }

    public static void hookPackageManager() {
        try {

            // 获取全局的ActivityThread对象
            Object activityThreadInstance = RefInvoke.getStaticFieldObject(
                    "android.app.ActivityThread",
                    "sCurrentActivityThread");

            //获取ActivityThread里面原始的 sPackageManager
            Object originPackageManager = RefInvoke.getFieldObject(
                    "android.app.ActivityThread",
                    activityThreadInstance,
                    "sPackageManager");

            // 准备好代理对象, 用来替换原始的对象
            Object proxyInstance = Proxy.newProxyInstance(
                    originPackageManager.getClass().getClassLoader(),
                    originPackageManager.getClass().getInterfaces(),
                    new HookHandler(originPackageManager));

            // 1. 替换掉ActivityThread里面的 sPackageManager 字段
            RefInvoke.setFieldObject(
                    "android.app.ActivityThread",
                    activityThreadInstance,
                    "sPackageManager",
                    proxyInstance);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static class HookHandler implements InvocationHandler {

        private static final String TAG = "HookHandler";

        private Object mTargetObj;

        public HookHandler(Object mTargetObj) {
            this.mTargetObj = mTargetObj;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Log.d(TAG, "hey, baby; you are hooked!!");
            Log.d(TAG, "method: " + method.getName() + " called with args: " + Arrays.toString(args));

            return method.invoke(mTargetObj, args);
        }
    }
}
