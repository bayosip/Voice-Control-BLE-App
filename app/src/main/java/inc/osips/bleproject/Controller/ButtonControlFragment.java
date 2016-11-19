package inc.osips.bleproject.Controller;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import java.util.zip.Inflater;
import inc.osips.bleproject.R;

public class ButtonControlFragment extends Fragment {

    private ImageButton buttonOnOff, buttonDark, buttonBright, buttonBack, buttonNext;
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.manual_control, container, false);
        initialiseWidget(view);
        return view;
    }

    private void initialiseWidget(View v){
        buttonOnOff = (ImageButton)v.findViewById(R.id.buttonOnoff);
        buttonDark = (ImageButton)v.findViewById(R.id.buttonDark);
        buttonBack = (ImageButton)v.findViewById(R.id.buttonBack);
        buttonBright = (ImageButton)v.findViewById(R.id.buttonBright);
        buttonNext = (ImageButton)v.findViewById(R.id.buttonNext);
    }
}
