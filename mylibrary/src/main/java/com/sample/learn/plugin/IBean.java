package com.sample.learn.plugin;

/**
 * Project Name：LearnBinder
 * Created by hejunqiu on 2019/12/13 10:35
 * Description:
 */
public interface IBean {

    String getName();

    void setName(String param);

    void register(ICallback callback);
}
