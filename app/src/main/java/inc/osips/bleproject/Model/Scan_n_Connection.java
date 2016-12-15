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
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import inc.osips.bleproject.Controller.ControllerActivity;
import inc.osips.bleproject.Controller.MainActivity;
import inc.osips.bleproject.Utilities.HW_Compatibility_Checker;
import inc.osips.bleproject.Utilities.ToastMessages;

public class Scan_n_Connection {

    private BluetoothAdapter bleAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private BluetoothDevice bleDevice;
    private Handler scanHandler;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private MainActivity ma;

    private boolean scanState;
    private String deviceName = "Bayo's BLE-LED Controller";
    private final String baseUUID = "6E400001-B5A3-F393-E0A9-E50E24DCCA9E";
    private Bundle extras;
    private ParcelUuid uuidParcel;
    //UUID uuid;

    public Scan_n_Connection(){
        ma = new MainActivity();
        scanHandler = new Handler();
        // dbAdapter = new DatabaseAdapter(ma.getApplicationContext());
        final BluetoothManager manager = (BluetoothManager) ma.getSystemService(
                Context.BLUETOOTH_SERVICE);

        bleAdapter = manager.getAdapter();
        extras = new Bundle();
        uuidParcel = new ParcelUuid(UUID.fromString(baseUUID));

        if (Build.VERSION.SDK_INT >= 21) {
            bluetoothLeScanner = bleAdapter.getBluetoothLeScanner();
            settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();
        }

    }

    public Handler getHandler() {
        return scanHandler;
    }

    public boolean isScanning() {
        return scanState;
    }

    public void onStart() {
        if (!HW_Compatibility_Checker.checkBluetooth(bleAdapter)) {
            HW_Compatibility_Checker.requestUserBluetooth(ma);
        }
        scanForBLEDevices(true);
    }

    public void onStop() {
        scanForBLEDevices(false);
    }

    private void scanForBLEDevices(Boolean yes) {
        if (yes && !scanState) {
            ScanFilter myDevice = new ScanFilter.Builder()
                    .setServiceUuid(uuidParcel).build();
            Log.d("Device UUID ", uuidParcel.toString());
            filters = new ArrayList<>();
            if (myDevice !=null){
            filters.add(myDevice);
            }
            else {
                myDevice = new ScanFilter.Builder()
                        .setDeviceName(deviceName).build();
                filters.add(myDevice);
            }
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
        ToastMessages.message(ma.getApplicationContext(), "Scanning Stopped!");
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
                       ma.startActivity(new Intent(ma, ControllerActivity.class)
                               .putExtra("Device Data", result));
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
                              //  ma.startActivity(new Intent(ma, CommunicationActivity.class)
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
