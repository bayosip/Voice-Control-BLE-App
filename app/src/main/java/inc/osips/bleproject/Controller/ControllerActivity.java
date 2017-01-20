package inc.osips.bleproject.Controller;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import inc.osips.bleproject.Interfaces.FragmentListner;
import inc.osips.bleproject.R;
import inc.osips.bleproject.Services.BleGattService;
import inc.osips.bleproject.Utilities.UIEssentials;

public class ControllerActivity extends AppCompatActivity implements FragmentListner {

    private BleGattService gattService;
    private BluetoothDevice device;
    private ScanResult result;
    private Fragment voiceFrag, manualFrag;
    private Switch controlSwitch;
    private TextView myDeviceName;
    private Toolbar ctrlToolBar;
    private Button disconnectButton;
    FragmentTransaction fragmentTransaction;

    private boolean mBound = false;
    private static final String TAG = ControllerActivity.class.getSimpleName();
    private String deviceName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);
        voiceFrag = new VoiceControlFragment();
        manualFrag = new ButtonControlFragment();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        result = getIntent().getExtras()
               .getParcelable("Device Data");
            Log.i(TAG+"result", result.toString());
            device =  result.getDevice();
            deviceName = device.getName();
            UIEssentials.message(getApplicationContext(), deviceName);
        }
        else {
            device = getIntent().getExtras()
                    .getParcelable("BLE Device");
            deviceName = device.getName();
        }

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.contentFragment) != null) {
            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.contentFragment, manualFrag).commit();
            initiateWidgets();
        }
        UIEssentials.getHandler().post(new Runnable() {
            @Override
            public void run() {
                myDeviceName.setText(deviceName);
            }
        });
    }

    private void changeFragments(Fragment fragment){
        fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.contentFragment, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void initiateWidgets(){
        ctrlToolBar = (Toolbar) findViewById(R.id.controlAppbar);
        setSupportActionBar(ctrlToolBar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        disconnectButton = (Button) findViewById(R.id.buttonDisconnect);
        disconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIEssentials.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        if (mBound) {
                            unbindService(mConnection);
                            gattService = null;
                            mBound = false;
                            finish();
                        }
                    }
                });
            }
        });
        myDeviceName = (TextView)findViewById(R.id.textViewDeviceName);
        controlSwitch = (Switch)findViewById(R.id.switchControl);
        controlSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    changeFragments(voiceFrag);
                }else {
                    changeFragments(manualFrag);
                }
            }
        });
    }

    /*
    * Defines callbacks for service binding, passed to bindService()
    */
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
                UIEssentials.message(getApplicationContext(), "API too low for app!");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
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
        Intent intent = new Intent(this, BleGattService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
        unregisterReceiver(ctrlUpdateReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(ctrlUpdateReceiver, commsUpdateIntentFilter());
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
            UIEssentials.message(getApplicationContext(), "Cannot Connect to Device");
            startActivity(new Intent (ControllerActivity.this, MainActivity.class));
        }
    }

    private final BroadcastReceiver ctrlUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case BleGattService.ACTION_CONNECTED:
                    // No need to do anything here. Service discovery is started by the service.
                    break;
                case BleGattService.ACTION_DISCONNECTED:
                    gattService.close();
                    UIEssentials.getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            if (mBound) {
                                unbindService(mConnection);
                                mBound = false;
                                finish();
                            }
                        }
                    });
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

    @Override
    public Activity getCurrentActivity() {
        return this;
    }
}
