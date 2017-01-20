package inc.osips.bleproject.Utilities;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

/**
 * Created by BABY v2.0 on 10/11/2016.
 */

public class UIEssentials {
    private Context context;
    private Toast toast;

    private static Handler uiHandler;

    static {
        uiHandler = new Handler(Looper.getMainLooper());
    }


    public static void  message (Context context,String message){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static Handler getHandler(){
        return uiHandler;
    }



}
