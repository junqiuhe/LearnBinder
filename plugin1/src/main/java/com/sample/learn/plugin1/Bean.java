package com.sample.learn.plugin1;

import com.sample.learn.plugin.IBean;
import com.sample.learn.plugin.ICallback;

/**
 * Project Name：LearnBinder
 * Created by hejunqiu on 2019/12/13 10:39
 * Description:
 */
public class Bean implements IBean {

    private String name = "Jackson";

    private ICallback callback;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String param) {
        this.name = param;
    }

    @Override
    public void register(ICallback callback) {
        this.callback = callback;

//        clickButton();
    }

    public void clickButton(){
        callback.sendResult("hello: " + this.name);
    }
}
