package com.sample.learn.hostapp;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.sample.learn.plugin.utils.RefInvoke;

import java.io.File;

import dalvik.system.DexClassLoader;

/**
 * Project Name：LearnBinder
 * Created by hejunqiu on 2019/12/13 14:24
 * Description:
 */
public class BaseActivity extends AppCompatActivity {

    private AssetManager mAssetManager;
    private Resources mResources;
    private Resources.Theme mTheme;

    private String dexPath = null;    //apk文件地址

    private File fileRelease = null;  //释放目录

    protected DexClassLoader classLoader = null;

    private String apkName = "plugin1.apk";    //apk名称

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        try {
            Utils.extractAssets(newBase, apkName);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        File extractFile = this.getFileStreamPath(apkName);
        dexPath = extractFile.getPath();

        fileRelease = getDir("dex", 0); //0 表示Context.MODE_PRIVATE

        classLoader = new DexClassLoader(dexPath,
                fileRelease.getAbsolutePath(), null, getClassLoader());
    }

    /**
     * loadResource方法中，通过反射调用addAssetPath方法，把插件的路径添加到AssetManager对象中.
     * AssetManager就可以加载Plugin1中的资源了.
     */
    protected void loadResources() {
        if (mAssetManager != null) {
            return;
        }

        try {
            AssetManager assetManager = getAssets();

            Class[] parameterTypes = new Class[]{String.class};
            Object[] parameterValues = new Object[]{dexPath};
            RefInvoke.invokeInstanceMethod(assetManager.getClass(), assetManager,
                    "addAssetPath", parameterTypes, parameterValues);

            mAssetManager = assetManager;

            mResources = new Resources(mAssetManager,
                    super.getResources().getDisplayMetrics(),
                    super.getResources().getConfiguration());

            mTheme = mResources.newTheme();
            mTheme.setTo(super.getTheme());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public AssetManager getAssets() {
        if (mAssetManager == null) {
            return super.getAssets();
        }
        return mAssetManager;
    }

    @Override
    public Resources getResources() {
        if (mResources == null) {
            return super.getResources();
        }
        return mResources;
    }

    @Override
    public Resources.Theme getTheme() {
        if (mTheme == null) {
            return super.getTheme();
        }
        return mTheme;
    }
}
