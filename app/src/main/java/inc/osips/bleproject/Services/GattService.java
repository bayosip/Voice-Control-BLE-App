package inc.osips.bleproject.Services;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

import inc.osips.bleproject.Utilities.BLE_Properties;
import inc.osips.bleproject.Utilities.ToastMessages;

/**
 * Created by BABY v2.0 on 10/11/2016.
 */

public class GattService extends Service {
    private final static String TAG = GattService.class.getSimpleName();
    private BluetoothGatt bleGatt;
    private boolean hasService;
    private BluetoothManager bManager;
    private BluetoothAdapter bAdapter;

    //  Queue for BLE events
    //  This is needed so that rapid BLE events don't get dropped
    private static final Queue<Object> BleQueue = new LinkedList<>();
    private int mConnectionState = STATE_DISCONNECTED;
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_CONNECTED =
            "inc.osips.bleproject.Services.GattService.ACTION_CONNECTED";
    public final static String ACTION_DISCONNECTED =
            "inc.osips.bleproject.Services.GattService.ACTION_DISCONNECTED";
    public final static String ACTION_BLE_SERVICES_DISCOVERED =
            "inc.osips.bleproject.Services.GattService.ACTION_BLE_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "inc.osips.bleproject.Services.GattService.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA = "inc.osips.bleproject.Services.GattService.EXTRA_DATA";
    public  String EXTRA_UUID;
    int mydata;
    String MyLogData;

    private BluetoothGattCharacteristic myWriteCharx;
    private BluetoothGattCharacteristic myReadCharx;
    public class BTLeServiceBinder extends Binder {
        public GattService getService(){
            return GattService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        //throw new UnsupportedOperationException("");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        disconnect();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new BTLeServiceBinder();
    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     *public BLE_GATT_Service(Context context){
    this.context = context;
    }
     */

    /**
     * Implements callback methods for GATT events.
     */
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        /**
         * This is called on a connection state change (either connection or disconnection)
         * @param gatt The GATT database object
         * @param status Status of the event
         * @param newState New state (connected or disconnected)
         */
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                broadcastUpdate(ACTION_CONNECTED);
                MyLogData += "Connected to GATT server.\n";
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                MyLogData +="Attempting to start service discovery:" +
                        bleGatt.discoverServices() +"\n";
                Log.i(TAG, "Attempting to start service discovery:" +
                        bleGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "Disconnected from GATT server.");
                MyLogData += "Disconnected from GATT server.\n";
                broadcastUpdate(ACTION_DISCONNECTED);
            }
        }

        /**
         * This is called when service discovery has completed.
         *
         * It broadcasts an update to the main activity.
         *
         * @param gatt The GATT database object
         * @param status Status of whether the discovery was successful.
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_BLE_SERVICES_DISCOVERED);

            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
                MyLogData += "onServicesDiscovered received: " + status+ "\n";
            }
        }

        private void handleBleQueue() {
            if(BleQueue.size() > 0) {
                // Determine which type of event is next and fire it off
                if (BleQueue.element() instanceof BluetoothGattDescriptor) {
                    bleGatt.writeDescriptor((BluetoothGattDescriptor) BleQueue.element());
                } else if (BleQueue.element() instanceof BluetoothGattCharacteristic) {
                    bleGatt.writeCharacteristic((BluetoothGattCharacteristic) BleQueue.element());
                }
            }
        }

        /**
         * This is called when a characteristic write has completed. Is uses a queue to determine if
         * additional BLE actions are still pending and launches the next one if there are.
         *
         * @param gatt The GATT database object
         * @param characteristic The characteristic that was written.
         * @param status Status of whether the write was successful.
         */
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic,
                                          int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE);
                // Pop the item that was written from the queue
                BleQueue.remove();
                // See if there are more items in the BLE queues
                handleBleQueue();
            }
        }


        /**
         * This is called when a characteristic with notify set changes.
         * It broadcasts an update to the main activity with the changed data.
         *
         * @param gatt The GATT database object
         * @param characteristic The characteristic that was changed
         */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            // Get the UUID of the characteristic that changed
            String uuid = characteristic.getUuid().toString();

            mydata= characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT32,0);

            // Tell the activity that new car data is available
            broadcastUpdate(ACTION_DATA_AVAILABLE);
        }
    };

    /**
     * Sends a broadcast to the listener in the main activity.
     * @param action The type of action that occurred.
     */
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    /**
     * Initialize a reference to the local Bluetooth adapter.
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (bManager == null) {
            bManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (bManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }
        bAdapter = bManager.getAdapter();
        if (bAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     * @param device The BLEdestination device.
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public boolean connect(final BluetoothDevice device) {
        if (bAdapter == null || device == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
        if(bleGatt !=null) {
            //try to reconnect to previously connected device
            Log.i(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            MyLogData += "Trying to use an existing mBluetoothGatt for connection.\n";
            return bleGatt.connect();
        }
        bleGatt = device.connectGatt(this, false, mGattCallback);
        Log.i(TAG, "Trying to create a new connection.");
        MyLogData +="Trying to create a new connection.\n";
        return true;
    }

    public boolean deviceIsConnected(BluetoothGatt gatt){
        if (gatt !=null)
            return true;
        else return false;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (bleGatt== null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            ToastMessages.message(getApplicationContext(), "No Device Connect!");
        }
        else if (bleGatt != null){
            bleGatt.disconnect();
            close();
            bleGatt = null;
            ToastMessages.message(getApplicationContext(), "Disconnecting...");
        }
    }
    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (bleGatt == null) {
            return;
        }
        bleGatt.close();
    }


    public void getACharXandWriteToIt(int value, ScanResult result) {
        Log.i(TAG, "This is result:: " + result.toString());
        List<ParcelUuid> serviceUUIDs = result.getScanRecord().getServiceUuids();
        ArrayList<BluetoothGattService> services = new ArrayList<>();
        List<BluetoothGattCharacteristic> characteristics;

        ArrayList<UUID> uuidServiceList = new ArrayList<>();
        for (ParcelUuid pUUIDx : serviceUUIDs){
            uuidServiceList.add(pUUIDx.getUuid());
            Log.i(TAG, uuidServiceList.toString());
        }
        for (UUID serviceUUIDx: uuidServiceList){
            Log.i(TAG, "services are: "+ serviceUUIDx.toString());
            MyLogData +="services are: "+ serviceUUIDx.toString()+"\n";
            BluetoothGattService service = bleGatt.getService(serviceUUIDx);
            if (service!=null) {
                characteristics = service.getCharacteristics();
                services.add(service);
                for (BluetoothGattCharacteristic characteristic : characteristics) {
                    if(characteristic!=null){
                        Log.i(TAG, "CharX:: " + characteristic.toString());
                        MyLogData += "CharX:: " + characteristic.toString()+"\n";
                        Log.i(TAG, "Charx UUID:: " + characteristic.getUuid().toString());
                        MyLogData += "Charx UUID:: " + characteristic.getUuid().toString()+"\n";
                        Log.i(TAG, "Charx write type:: " + characteristic.getProperties());


                      /*  Log.i(TAG, "Write no response:: " +
                                BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE);
                        Log.i(TAG, "Write:: " +
                                BluetoothGattCharacteristic.PROPERTY_WRITE);
                        Log.i(TAG, "Notify:: " +
                                BluetoothGattCharacteristic.PROPERTY_NOTIFY);
                        Log.i(TAG, "Read:: " +
                                BluetoothGattCharacteristic.PROPERTY_READ);*/
                        if (characteristic.getWriteType() == 1){
                            MyLogData += "Write data: " + value + "\n";
                            characteristic.setValue(value,BluetoothGattCharacteristic.FORMAT_SINT8, 0);
                            writeCharacteristic(characteristic);
                        }
                    }
                }
            }
        }
    }

    /**
     * Request a write on a given {@code BluetoothGattCharacteristic}.
     *
     * @param characteristic The characteristic to write.
     */
    private void writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (bAdapter == null || bleGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        BleQueue.add(characteristic);
        if (BleQueue.size() == 1) {
            bleGatt.writeCharacteristic(characteristic);
            Log.i(TAG, "Writing Characteristic");
        }
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled        If true, enable notification.  False otherwise.
     */
    private void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                               boolean enabled) {
    }

    public String getMyLogData (){
        return MyLogData;
    }

}
