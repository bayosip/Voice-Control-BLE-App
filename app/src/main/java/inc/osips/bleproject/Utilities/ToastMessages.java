package inc.osips.bleproject.Utilities;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by BABY v2.0 on 10/11/2016.
 */

public class ToastMessages {
    private Context context;
    private Toast toast;

    public static void  message (Context context,String message){

        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
