package com.sample.learn.hostapp.classloader_hook;

import android.content.pm.ApplicationInfo;

import com.sample.learn.hostapp.Utils;
import com.sample.learn.plugin.utils.RefInvoke;

import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Project Name：LearnBinder
 * Created by hejunqiu on 2019/12/16 14:41
 * Description:
 */
public class LoadedApkClassLoaderHookHelper {

    public static Map<String, Object> sLoadedApk = new HashMap<>();

    public static void hookLoadedApkInActivityThread(File apkFile)
            throws ClassNotFoundException, NoSuchMethodException, NoSuchFieldException,
            InstantiationException, IllegalAccessException, InvocationTargetException {

        //获取当前的 ActivityThread 对象
        Object currentActivityThread = RefInvoke.invokeStaticMethod(
                "android.app.ActivityThread", "currentActivityThread");

        //
        Object defaultCompatibilityInfo = RefInvoke.getStaticFieldObject(
                "android.content.res.CompatibilityInfo", "DEFAULT_COMPATIBILITY_INFO");

        //从插件Apk获取ApplicationInfo信息.
        ApplicationInfo applicationInfo = generateApplicationInfo(apkFile);


        //调用ActivityThread的getPackageInfoNoCheck方法loadedApk，得到，上面两个数据都是用来做参数的
        Class[] p1 = {ApplicationInfo.class, Class.forName("android.content.res.CompatibilityInfo")};
        Object[] v1 = {applicationInfo, defaultCompatibilityInfo};
        Object loadedApk = RefInvoke.invokeInstanceMethod(
                "android.app.ActivityThread", currentActivityThread, "getPackageInfoNoCheck", p1, v1);


        String odexPath = Utils.getPluginOptDexDir(applicationInfo.packageName).getPath();
        String libDir = Utils.getPluginLibDir(applicationInfo.packageName).getPath();

        //为插件造一个新的ClassLoader
        ClassLoader classLoader = new CustomClassLoader(apkFile.getPath(),
                odexPath, libDir, ClassLoader.getSystemClassLoader());

        RefInvoke.setFieldObject(loadedApk.getClass(), loadedApk, "mClassLoader", classLoader);

        //把插件LoadedApk对象放入缓存
        WeakReference weakReference = new WeakReference(loadedApk);

        //获取 mPackages 变量，缓存了Dex包的信息
        Map mPackages = (Map) RefInvoke.getFieldObject(
                "android.app.ActivityThread", currentActivityThread, "mPackages");
        mPackages.put(applicationInfo.packageName, weakReference);

        // 由于是弱引用, 因此我们必须在某个地方存一份, 不然容易被GC; 那么就前功尽弃了.
        sLoadedApk.put(applicationInfo.packageName, loadedApk);
    }

    /**
     * 这个方法的最终目的是调用
     * android.content.pm.PackageParser#generateActivityInfo(android.content.pm.PackageParser.Activity, int, android.content.pm.PackageUserState, int)
     */
    public static ApplicationInfo generateApplicationInfo(File apkFile)
            throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException,
            InstantiationException, InvocationTargetException, NoSuchFieldException {

        // 找出需要反射的核心类: android.content.pm.PackageParser
        Class<?> packageParserClass = Class.forName("android.content.pm.PackageParser");
        Class<?> packageParser$PackageClass = Class.forName("android.content.pm.PackageParser$Package");
        Class<?> packageUserStateClass = Class.forName("android.content.pm.PackageUserState");


        // 我们的终极目标: android.content.pm.PackageParser#generateApplicationInfo(android.content.pm.PackageParser.Package,
        // int, android.content.pm.PackageUserState)
        // 要调用这个方法, 需要做很多准备工作; 考验反射技术的时候到了 - -!
        // 下面, 我们开始这场Hack之旅吧!

        // 首先拿到我们得终极目标: generateApplicationInfo方法
        // API 23 !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        // public static ApplicationInfo generateApplicationInfo(Package p, int flags,
        //    PackageUserState state) {
        // 其他Android版本不保证也是如此.


        // 首先, 我们得创建出一个Package对象出来供这个方法调用
        // 而这个需要得对象可以通过 android.content.pm.PackageParser#parsePackage 这个方法返回得 Package对象得字段获取得到
        // 创建出一个PackageParser对象供使用
        Object packageParser = packageParserClass.newInstance();

        // 调用 PackageParser.parsePackage 解析apk的信息
        // 实际上是一个 android.content.pm.PackageParser.Package 对象
        Class[] p1 = {File.class, int.class};
        Object[] v1 = {apkFile, 0};
        Object packageObj = RefInvoke.invokeInstanceMethod(
                packageParserClass, packageParser, "parsePackage", p1, v1);


        // 第三个参数 mDefaultPackageUserState 我们直接使用默认构造函数构造一个出来即可
        Object defaultPackageUserState = packageUserStateClass.newInstance();

        // 万事具备!!!!!!!!!!!!!!
        Class[] p2 = {packageParser$PackageClass, int.class, packageUserStateClass};
        Object[] v2 = {packageObj, 0, defaultPackageUserState};
        ApplicationInfo applicationInfo = (ApplicationInfo) RefInvoke.invokeInstanceMethod(
                packageParserClass, packageParser, "generateApplicationInfo", p2, v2);

        String apkPath = apkFile.getPath();
        applicationInfo.sourceDir = apkPath;
        applicationInfo.publicSourceDir = apkPath;

        return applicationInfo;
    }
}