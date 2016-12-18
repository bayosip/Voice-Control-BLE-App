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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import inc.osips.bleproject.Interfaces.FragmentListner;
import inc.osips.bleproject.R;
import inc.osips.bleproject.Services.BleGattService;
import inc.osips.bleproject.Utilities.ToastMessages;

public class ControllerActivity extends FragmentActivity implements FragmentListner {

    private BleGattService gattService;
    private BluetoothDevice device;
    private ScanResult result;
    private Fragment voiceFrag, manualFrag;
    private Switch controlSwitch;
    private TextView myDeviceName;
    private volatile int flag =0;
    FragmentTransaction fragmentTransaction;

    Handler commsHandler;
    private boolean mBound = false;
    private static final String TAG = ControllerActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);
        commsHandler = new Handler();
        voiceFrag = new VoiceControlFragment();
        manualFrag = new ButtonControlFragment();

        if (findViewById(R.id.contentFragment) != null) {
            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }
            //result = getIntent().getExtras()
            //        .getParcelable("Device Data");
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.contentFragment, manualFrag).commit();
            initiatewidgets();
        }
    }

    private void changeFragments(Fragment fragment){
        fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.contentFragment, fragment);
        fragmentTransaction.commit();
    }

    private void initiatewidgets (){

                //(ButtonControlFragment)getSupportFragmentManager()
                //.findFragmentById(R.id.fragmentButtonControl);
        myDeviceName = (TextView)findViewById(R.id.textViewDeviceName);
        controlSwitch = (Switch)findViewById(R.id.switchControl);
        controlSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    changeFragments(voiceFrag);
                    //fragmentTransaction.detach(manualFrag);
                    //fragmentTransaction.attach(voiceFrag);
                }else {
                    changeFragments(manualFrag);
                    //fragmentTransaction.detach(voiceFrag);
                    //fragmentTransaction.attach(manualFrag);
                }
            }
        });
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.i(TAG, "binding services");
            BleGattService.BTLeServiceBinder binder = (BleGattService.BTLeServiceBinder) service;
            gattService = binder.getService();
            mBound = true;
            if (Build.VERSION.SDK_INT >= 21) {
                getDEviceAndConnect();
            }
            else {
                ToastMessages.message(getApplicationContext(), "API too low for app!");
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
        /*Intent intent = new Intent(this, BleGattService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);*/
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

    private final BroadcastReceiver commsUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case BleGattService.ACTION_CONNECTED:
                    // No need to do anything here. Service discovery is started by the service.
                    break;
                case BleGattService.ACTION_DISCONNECTED:
                    gattService.close();
                    break;
                case BleGattService.ACTION_DATA_AVAILABLE:
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
        intentFilter.addAction(BleGattService.ACTION_CONNECTED);
        intentFilter.addAction(BleGattService.ACTION_DISCONNECTED);
        intentFilter.addAction(BleGattService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    /*
    * This gets called when any of the buttons on any fragment is pressed
    */
    @Override
    public void sendInstructions(String instruct) {

        gattService.writeLEDInstructions(instruct);
    }
}
