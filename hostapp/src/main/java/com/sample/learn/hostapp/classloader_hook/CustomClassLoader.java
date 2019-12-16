package com.sample.learn.hostapp.classloader_hook;

import android.util.Log;

import dalvik.system.DexClassLoader;

/**
 * Project Name：LearnBinder
 * Created by hejunqiu on 2019/12/16 14:40
 * Description:
 */
public class CustomClassLoader extends DexClassLoader {

    public CustomClassLoader(String dexPath,
                             String optimizedDirectory,
                             String librarySearchPath,
                             ClassLoader parent) {
        super(dexPath, optimizedDirectory, librarySearchPath, parent);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        Log.d("CustomClassLoader", "className: " + name);
        return super.findClass(name);
    }
}