package inc.osips.bleproject.Controller;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import inc.osips.bleproject.R;
import inc.osips.bleproject.Services.GattService;

public class ControllerActivity extends AppCompatActivity {

    private GattService gattService;
    private BluetoothDevice device;
    private ScanResult result;
    Handler commsHandler;
    private boolean mBound = false;
    private static final String TAG = ControllerActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        commsHandler = new Handler();
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.i(TAG, "binding services");
            GattService.BTLeServiceBinder binder = (GattService.BTLeServiceBinder) service;
            gattService = binder.getService();
            mBound = true;
            if (Build.VERSION.SDK_INT >= 21) {
                getDEviceAndConnect();
            }
            else {
                getDeviceAndConnectLowerAPI();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            //gattService = null;
            mBound = false;
            startActivity(new Intent(ControllerActivity.this, MainActivity.class));
        }
    };

    private boolean makeConnectionBLE(BluetoothDevice dBLE){
        if(!gattService.initialize()){
            Log.e(TAG, "Unable to initialize Bluetooth");
            finish();
        }
        return gattService.connect(dBLE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        Log.i(TAG, "starting service");
        Intent intent = new Intent(this, GattService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            gattService = null;
            mBound = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(commsUpdateReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(commsUpdateReceiver, commsUpdateIntentFilter());
        if (gattService != null) {
            final boolean result = gattService.connect(device);
            Log.i(TAG, "Connect request result=" + result);
        }
    }

    //API 21 and Above
    private void getDEviceAndConnect(){
        if (makeConnectionBLE(device)){
            return;
        }
        else {
            startActivity(new Intent (ControllerActivity.this, MainActivity.class));
        }
    }

    private void getDeviceAndConnectLowerAPI(){
        device = getIntent().getExtras().getParcelable("My Device");
        if (makeConnectionBLE(device)){
            return;
        }
        else {
            startActivity(new Intent (ControllerActivity.this, MainActivity.class));
        }
    }

    private final BroadcastReceiver commsUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case GattService.ACTION_CONNECTED:
                    // No need to do anything here. Service discovery is started by the service.
                    break;
                case GattService.ACTION_DISCONNECTED:
                    gattService.close();
                    break;
                case GattService.ACTION_DATA_AVAILABLE:
                    // This is called after a Notify completes
                    break;
            }
        }

        @Override
        public IBinder peekService(Context myContext, Intent service) {
            return super.peekService(myContext, service);
        }
    };

    /**
     * This sets up the filter for broadcasts that we want to be notified of.
     * This needs to match the broadcast receiver cases.
     *
     * @return intentFilter
     */
    private static IntentFilter commsUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(GattService.ACTION_CONNECTED);
        intentFilter.addAction(GattService.ACTION_DISCONNECTED);
        intentFilter.addAction(GattService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}
