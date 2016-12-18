package inc.osips.bleproject.Controller;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import inc.osips.bleproject.Model.Scan_n_Connection;
import inc.osips.bleproject.R;
import inc.osips.bleproject.Utilities.ToastMessages;

public class MainActivity extends AppCompatActivity {

    private ImageButton connectButton;
    private static final int REQUEST_ENABLE_BLE = 1;
    private Scan_n_Connection scanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initiateWidets();
    }

    private void initiateWidets(){
        connectButton = (ImageButton)findViewById(R.id.buttonConnect);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //scanner = new Scan_n_Connection();
                //scanner.onStart();
                startActivity(new Intent(MainActivity.this, ControllerActivity.class));
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == REQUEST_ENABLE_BLE) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                ToastMessages.message(getApplicationContext(),"Bluetooth On");
            }
            else if (resultCode == RESULT_CANCELED) {
                ToastMessages.message(getApplicationContext(), "Please turn on Bluetooth");
            }
        }
    }
}
