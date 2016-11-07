package inc.osips.bleproject.Utilities;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;

/**
 * Created by BABY v2.0 on 10/24/2016.
 */

public class BT_Compatibility_Checker {

    private Context context;
    public static final int REQUEST_ENABLE_BT =1;

    /*public BluetoothCheck(Context context) {
        this.context = context;
    }*/
    public static boolean checkBluetooth(BluetoothAdapter bleAdapter) {

        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (bleAdapter == null || !bleAdapter.isEnabled()) {
            return false;
        }
        else {
            return true;
        }
    }

    public static void requestUserBluetooth(Activity activity) {

        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT );
    }
}
