package inc.osips.bleproject.Controller;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import inc.osips.bleproject.Interfaces.AppActivity;
import inc.osips.bleproject.Interfaces.Scanner;
import inc.osips.bleproject.Model.Scan_n_Connection;
import inc.osips.bleproject.R;
import inc.osips.bleproject.Utilities.UIEssentials;

public class MainActivity extends AppCompatActivity implements AppActivity {

    private ImageButton connectButton;
    private static final int REQUEST_ENABLE_BLE = 1;
    private Scanner scanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initiateWidgets();
    }

    private Scanner getScanner(){
        scanner = new Scan_n_Connection(getCurrentActivity());
        return scanner;
    }
    public void initiateWidgets(){
        connectButton = (ImageButton)findViewById(R.id.buttonConnect);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!getScanner().isScanning())
                launchRingDialog();
            }
        });
    }

    public void launchRingDialog() {
        final ProgressDialog ringProgressDialog = ProgressDialog.show(MainActivity.this,
                "Please wait ...", "Connecting ...", true);
        ringProgressDialog.setCancelable(true);
        //Thread thread = new Thread();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    scanner.onStart();
                    Thread.sleep(10000);
                } catch (Exception e) {
                    UIEssentials.getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            UIEssentials.message(getApplicationContext(),
                                    "Cannot Scan for bluetooth le device");
                        }
                    });
                }
                ringProgressDialog.dismiss();
            }
        }).start();
    }

    @Override
    protected void onStop() {
        if(scanner.isScanning())
        scanner.onStop();
        super.onStop();
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

    @Override
    public Activity getCurrentActivity() {
        return this;
    }
}
