package inc.osips.bleproject.Controller;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import inc.osips.bleproject.Interfaces.FragmentListner;
import inc.osips.bleproject.R;

public class ButtonControlFragment extends Fragment {

    private ImageButton buttonOnOff, buttonDark, buttonBright, buttonBack, buttonNext;
    private FragmentListner fragListner;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.manual_control, container, false);
        initialiseWidget(view);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            fragListner = (FragmentListner) context;
        }catch (ClassCastException e){
            throw new ClassCastException(context.toString());
        }

    }

    private void initialiseWidget(View v){
        buttonOnOff = (ImageButton)v.findViewById(R.id.buttonOnoff);
        buttonOnOff.setOnClickListener(OnClick);
        buttonDark = (ImageButton)v.findViewById(R.id.buttonDark);
        buttonDark.setOnClickListener(OnClick);
        buttonBack = (ImageButton)v.findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(OnClick);
        buttonBright = (ImageButton)v.findViewById(R.id.buttonBright);
        buttonBright.setOnClickListener(OnClick);
        buttonNext = (ImageButton)v.findViewById(R.id.buttonNext);
        buttonNext.setOnClickListener(OnClick);
    }

    Button.OnClickListener OnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String instruct = "";
            switch (v.getId()){
                case R.id.buttonOnoff:
                    instruct = "On/Off";
                    break;
                case R.id.buttonDark:
                    instruct = "Dim";
                    break;
                case R.id.buttonBright:
                    instruct = "Bright";
                    break;
                case R.id.buttonBack:
                    instruct = "Back";
                    break;
                case R.id.buttonNext:
                    instruct = "Next";
                    break;
            }
            fragListner.sendInstructions(instruct.toLowerCase());
        }
    };
}
