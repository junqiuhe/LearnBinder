#### Android 插件化相关知识.

##### 一、了解 Android Binder 机制

##### 二、Java 反射的使用

##### 三、了解什么是代理模式.

##### 四、Hook startActivity方法

startActivity 的两种方式:

    1、activity.startActivity()
    
    2、context.startActivity()
    
但是它们都是在App进程中通知AMS要启动哪个Activity.

在Activity1 中执行 startActivity 启动 activity2， 要Hook此中的过程，我们可以分为两步:

###### 1、Activity1 通知 AMS，要启动 Activity2;
   
Hook点:
    
    1)、Activity的startActivityForResult方法 (重写Activity的startActivityForResult方法)
    
    2)、Activity的 mInstrumentation 字段 (对Activity的 mInstrumentation 字段进行Hook)
    
    3)、AMN 的 getDefault方法获取到的对象
    
###### 2、AMS 通知 app 进程，要启动 Activity2.

Hook点:

    1)、ActivtyThread 中的 mCallback字段
    
    2)、ActivityThread 中的 mInstrumentation 对象 中 callActivityOnCreate 方法.
    

#### 启动没有在 AndroidManifest 中声明的 Activity.

对于插件化项目而言，开发人员在插件包中新增一个Activity. 放在服务器上，由用户下载到手机，HostApp并不能启动这个Activity。
因为这个Activity事先并没有在HostApp的 AndroidManifest.xml 文件中声明.

注意: App开发人员无法对 AMS 系统进程进行Hook，只能对App所在的进程进行Hook.

解决方案:
    
    1、发送要启动的 Activity 信息给AMS之前，把这个Activity 替换为一个在 AndroidManifest 中声明的StubActivity.
    这样就能绕过AMS的检查。在替换的过程中，把原来的Activity信息存放在 bundle 中.
    
    2、AMS通知 APP启动 StubActivity 时，我们自然不会启动StubActivity, 而是在即将启动的时候，把StubActivity替换为原先的Activity。
    原先的Activity信息存放在Bundle中，取出来就好了。
    
    
[Android9.0 Activity启动原理差异解析](https://www.jianshu.com/p/aa03c4458b9a)

[手把手讲解 Android Hook-实现无清单启动Activity](https://www.jianshu.com/p/eb772e50c690)


#### Android 资源

    Android中资源的分类:
        
    1、res 目录下存放的可编译的资源文件。编译时，系统会自动在R.java中生成资源文件的十六进制.
    （可通过Resources对象获取资源.）
    
    2、assets目录下存放的原始资源文件。（可利用AssetsManager类的open方法来获取assets目录下的文件资源）
    
    
#### [最简单的插件化方案.](https://www.zhihu.com/pub/reader/119567993/chapter/1016383525167067136)

    1、合并所有插件的dex,来解决插件的类的加载问题. 
    (通过DexClassLoader加载插件中的类信息，添加到宿主中的classLoader dexElements中。)
    
    2、预先在宿主的AndroidManifest.xml文件中声明插件中的四大组件。
    
    3、把插件中的所有资源一次性地合并到宿主的资源中。
    （AssetManager加载插件中的资源.）
    
问题? 

1）、因为插件和宿主的资源都合并到了一起，资源Id可能会存在冲突

2）、不能事先预料插件中有哪些组件。


#### [Activity的插件化解决方案](https://www.zhihu.com/pub/reader/119567993/chapter/1016383525217570816)

Activity 的插件化需要解决3方面的技术问题：
    
    1、宿主App可以加载插件App中的类
    
    2、宿主App可以加载插件App中的资源
    
    3、宿主App可以加载插件中的Activity.
    
如何启动没有在 AndroidManifest.xml 文件中声明的插件 Activity? 参考 ActivityStarter.java