package com.sample.learn.hostapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.sample.learn.plugin.IBean;
import com.sample.learn.plugin.ICallback;
import com.sample.learn.plugin.IDynamic;
import com.sample.learn.plugin.utils.RefInvoke;

public class MainActivity extends BaseActivity {

    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv = findViewById(R.id.tv);

        //普通调用，反射的方式
        findViewById(R.id.btn_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try {
                    Object instance =
                            RefInvoke.newInstance(classLoader, "com.sample.learn.plugin1.Bean");

                    String name = (String) RefInvoke.invokeInstanceMethod(
                            instance.getClass(), instance, "getName");

                    tv.setText(name);
                    Toast.makeText(getApplicationContext(), name, Toast.LENGTH_LONG).show();

                } catch (Exception e) {
                    Log.e("DEMO", "msg:" + e.getMessage());
                }
            }
        });

        //带参数调用
        findViewById(R.id.btn_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try {
                    Object instance =
                            RefInvoke.newInstance(classLoader, "com.sample.learn.plugin1.Bean");

                    IBean bean = (IBean) instance;
                    bean.setName("Hello");
                    tv.setText(bean.getName());
                } catch (Exception e) {
                    Log.e("DEMO", "msg:" + e.getMessage());
                }

            }
        });

        //带回调函数的调用
        findViewById(R.id.btn_3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try {
                    Object instance =
                            RefInvoke.newInstance(classLoader, "com.sample.learn.plugin1.Bean");

                    IBean bean = (IBean) instance;
                    ICallback callback = new ICallback() {
                        @Override
                        public void sendResult(String result) {
                            tv.setText(result);
                        }
                    };
                    bean.register(callback);
                } catch (Exception e) {
                    Log.e("DEMO", "msg:" + e.getMessage());
                }
            }
        });

        findViewById(R.id.btn_43).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    loadResources();

                    IDynamic instance = (IDynamic) RefInvoke.newInstance(
                            classLoader, "com.sample.learn.plugin1.Dynamic");

                    String pluginText = instance.getStringForResId(MainActivity.this);

                    tv.setText(pluginText);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
