package com.sample.learn.binder.local;


// Need the following import to get access to the app resources, since this
// class is in a sub-package.

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/**
 * This is an example of implementing an application service that runs locally
 * in the same process as the application.  The {@link LocalServiceActivities.Controller}
 * and {@link LocalServiceActivities.Binding} classes show how to interact with the
 * service.
 *
 * <p>Notice the use of the {@link NotificationManager} when interesting things
 * happen in the service.  This is generally how background services should
 * interact with the user, rather than doing something more disruptive such as
 * calling startActivity().
 */
//BEGIN_INCLUDE(service)
public class LocalService extends Service {

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {

        LocalService getService() {
            return LocalService.this;
        }

    }

    /**
     * Service的声明周期管理. https://developer.android.google.cn/guide/components/services#Lifecycle
     * <p>
     * 1、startService的方式启动, onCreate() -> onStartCommand() -> onDestroy()
     * <p>
     * 2、bindService的方式启动, onCreate() -> onBind() -> onUnbind() -> onDestroy()
     */

    @Override
    public void onCreate() {
        Log.i("LocalService", "local service onCreate");
    }

    @Override
    public void onDestroy() {
        // Tell the user we stopped.
        Log.i("LocalService", "local service onDestroy");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);
        return START_NOT_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent) {
        Log.i("LocalService", "local service onBind method");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i("LocalService", "local service onUnbind method");
        return super.onUnbind(intent);
    }

    public void add(int a, int b) {
        int result = a + b;
        Log.i("LocalService", "add method result: " + result);
    }

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();
}
//END_INCLUDE(service)
