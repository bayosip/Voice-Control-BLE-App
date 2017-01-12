package inc.osips.bleproject.Controller;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import inc.osips.bleproject.Model.Scan_n_Connection;
import inc.osips.bleproject.R;
import inc.osips.bleproject.Utilities.UIEssentials;

public class MainActivity extends AppCompatActivity {

    private ImageButton connectButton;
    private static final int REQUEST_ENABLE_BLE = 1;
    private Scan_n_Connection scanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initiateWidets(this);
    }

    private void initiateWidets(final MainActivity ma){
        connectButton = (ImageButton)findViewById(R.id.buttonConnect);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchRingDialog( ma);
                //startActivity(new Intent(MainActivity.this, ControllerActivity.class));
            }
        });
    }

    public void launchRingDialog(final MainActivity ma) {
        final ProgressDialog ringProgressDialog = ProgressDialog.show(MainActivity.this,
                "Please wait ...", "Connecting ...", true);
        ringProgressDialog.setCancelable(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    scanner=null;
                    scanner = new Scan_n_Connection(ma);
                    scanner.onStart();
                    Thread.sleep(10000);
                } catch (Exception e) {
                    UIEssentials.message(getApplicationContext(),
                            "Cannot Scan for bluetooth le device");
                }
                ringProgressDialog.dismiss();
            }
        }).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == REQUEST_ENABLE_BLE) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                UIEssentials.message(getApplicationContext(),"Bluetooth On");
            }
            else if (resultCode == RESULT_CANCELED) {
                UIEssentials.message(getApplicationContext(), "Please turn on Bluetooth");
            }
        }
    }
}
