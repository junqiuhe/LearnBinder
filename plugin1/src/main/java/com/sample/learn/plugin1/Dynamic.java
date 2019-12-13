package com.sample.learn.plugin1;

import android.content.Context;

import com.sample.learn.plugin.IDynamic;

/**
 * Project Name：LearnBinder
 * Created by hejunqiu on 2019/12/13 14:10
 * Description:
 */
public class Dynamic implements IDynamic {
    @Override
    public String getStringForResId(Context context) {
        return context.getString(R.string.myplugin_hello_world);
    }
}