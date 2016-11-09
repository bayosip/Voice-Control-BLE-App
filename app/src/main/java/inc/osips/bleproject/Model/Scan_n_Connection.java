package inc.osips.bleproject.Model;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import inc.osips.bleproject.Controller.ControllerActivity;
import inc.osips.bleproject.Utilities.HW_Compatibility_Checker;
import inc.osips.bleproject.Utilities.ToastMessages;

public class Scan_n_Connection {

    private BluetoothAdapter bleAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private BluetoothDevice bleDevice;
    private Handler scanHandler;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private ControllerActivity ca;

    private boolean scanState;
    private String deviceName;
    private Bundle extras;

    public Scan_n_Connection(){
        ca = new ControllerActivity();
        scanHandler = new Handler();
        // dbAdapter = new DatabaseAdapter(ma.getApplicationContext());
        final BluetoothManager manager = (BluetoothManager) ca.getSystemService(
                Context.BLUETOOTH_SERVICE);

        bleAdapter = manager.getAdapter();

        if (Build.VERSION.SDK_INT >= 21) {
            bluetoothLeScanner = bleAdapter.getBluetoothLeScanner();
            settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();
        }
        extras = new Bundle();
    }

    public Handler getHandler() {
        return scanHandler;
    }

    public boolean isScanning() {
        return scanState;
    }

    public void onStart() {
        if (!HW_Compatibility_Checker.checkBluetooth(bleAdapter)) {
            HW_Compatibility_Checker.requestUserBluetooth(ca);
        } else {
            ToastMessages.message(ca.getApplicationContext(), "Tap to Connect");
        }
    }

    public void setDeviceName(String dName) {
        this.deviceName = dName;
    }

    public void onStop() {
        scanForBLEDevices(false);
    }

    public void scanForBLEDevices(Boolean yes) {
        if (yes && !scanState) {
            ScanFilter savedDevices = new ScanFilter.Builder()
                    .setDeviceName(deviceName).build();
            Log.d("FFS: ", deviceName);
            filters = new ArrayList<ScanFilter>();
            filters.add(savedDevices);
            //start scan
            scanHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanState = false;
                    if (Build.VERSION.SDK_INT < 21){
                        bleAdapter.stopLeScan(scanCallBackLe);
                    } else {
                        scanStop();
                    }
                }
            }, 5000);
            scanState = true;
            if (Build.VERSION.SDK_INT < 21) {
                bleAdapter.startLeScan(scanCallBackLe);
            } else {
                bluetoothLeScanner.startScan(filters, settings, mScanCallback);
            }
        }
        else{
        ToastMessages.message(ca.getApplicationContext(), "Scanning Stopped!");
        }
    }

    private void scanStop() {
        bluetoothLeScanner.stopScan(mScanCallback);
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, final ScanResult result) {
            Log.i("callbackType", String.valueOf(callbackType));
            Log.i("result", result.toString());
            final int RSSI = result.getRssi();
            if (RSSI>=-85) {
                scanHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        bleDevice = result.getDevice();
                       // ca.startActivity(new Intent(ma, CommunicationActivity.class)
                          //      .putExtra("Device Data", result));
                    }
                });
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
                Log.i("ScanResult - Results", sr.toString());
                //ToastMakers.message(scannerActivity.getApplicationContext(), sr.toString());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e("Scan Failed", "Error Code: " + errorCode);
        }
    };

    private BluetoothAdapter.LeScanCallback scanCallBackLe =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, final byte[] scanRecord) {
                    final int RSSI = rssi;
                    if (RSSI >= -85){
                        scanHandler.post(new Runnable() {
                            @Override
                            public void run() {
                              //  ca.startActivity(new Intent(ma, CommunicationActivity.class)
                               //         .putExtra("Device Name", device));
                            }
                        });
                    }
                }
            };

    public BluetoothDevice getBLEDevice (){
        return this.bleDevice;
    }
}
