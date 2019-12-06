package com.sample.learn.binder.local;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.sample.learn.binder.R;

public class LocalServiceActivities {
    /**
     * <p>Example of explicitly starting and stopping the local service.
     * This demonstrates the implementation of a service that runs in the same
     * process as the rest of the application, which is explicitly started and stopped
     * as desired.</p>
     *
     * <p>Note that this is implemented as an inner class only keep the sample
     * all together; typically this code would appear in some separate class.
     */
    public static class Controller extends Activity {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.local_service_controller);
            // Watch for button clicks.
            findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Make sure the service is started.  It will continue running
                    // until someone calls stopService().  The Intent we use to find
                    // the service explicitly specifies our service component, because
                    // we want it running in our own process and don't want other
                    // applications to replace it.
                    startService(new Intent(Controller.this,
                            LocalService.class));
                }
            });
            findViewById(R.id.stop).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Cancel a previous call to startService().  Note that the
                    // service will not actually stop at this point if there are
                    // still bound clients.
                    stopService(new Intent(Controller.this,
                            LocalService.class));
                }
            });
        }
    }

    // ----------------------------------------------------------------------
// BEGIN_INCLUDE(bind)

    /**
     * Example of binding and unbinding to the local service.
     * bind to, receiving an object through which it can communicate with the service.
     * <p>
     * Note that this is implemented as an inner class only keep the sample
     * all together; typically this code would appear in some separate class.
     */
    public static class Binding extends Activity {
        // BEGIN_INCLUDE(bind)
        // Don't attempt to unbind from the service unless the client has received some
        // information about the service's state.
        private boolean mShouldUnbind;
        // To invoke the bound service, first make sure that this value
        // is not null.
        private LocalService mBoundService;

        private ServiceConnection mConnection = new ServiceConnection() {

            public void onServiceConnected(ComponentName className, IBinder service) {
                // This is called when the connection with the service has been
                // established, giving us the service object we can use to
                // interact with the service.  Because we have bound to a explicit
                // service that we know is running in our own process, we can
                // cast its IBinder to a concrete class and directly access it.
                mBoundService = ((LocalService.LocalBinder) service).getService();
                // Tell the user about this for our demo.
                Toast.makeText(Binding.this, R.string.local_service_connected,
                        Toast.LENGTH_SHORT).show();
            }

            public void onServiceDisconnected(ComponentName className) {
                // This is called when the connection with the service has been
                // unexpectedly disconnected -- that is, its process crashed.
                // Because it is running in our same process, we should never
                // see this happen.
                mBoundService = null;
                Toast.makeText(Binding.this, R.string.local_service_disconnected,
                        Toast.LENGTH_SHORT).show();
            }
        };

        void doBindService() {
            // Attempts to establish a connection with the service.  We use an
            // explicit class name because we want a specific service
            // implementation that we know will be running in our own process
            // (and thus won't be supporting component replacement by other
            // applications).
            if (bindService(new Intent(Binding.this, LocalService.class),
                    mConnection, Context.BIND_AUTO_CREATE)) {
                mShouldUnbind = true;
            } else {
                Log.e("MY_APP_TAG", "Error: The requested service doesn't " +
                        "exist, or this client isn't allowed access to it.");
            }
        }

        void doUnbindService() {
            if (mShouldUnbind) {
                // Release information about the service's state.
                unbindService(mConnection);
                mShouldUnbind = false;
            }
        }

        @Override
        protected void onDestroy() {
            super.onDestroy();
            doUnbindService();
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.local_service_binding);

            // Watch for button clicks.
            findViewById(R.id.bind).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    doBindService();
                }
            });

            findViewById(R.id.unbind).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    doUnbindService();
                }
            });

            findViewById(R.id.callAddMethod).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mBoundService != null) {
                        mBoundService.add(1, 2);
                    }
                }
            });
        }
    }
}