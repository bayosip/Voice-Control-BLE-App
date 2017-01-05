package inc.osips.bleproject.Controller;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.Locale;

import inc.osips.bleproject.Interfaces.FragmentListner;
import inc.osips.bleproject.Model.VoiceRecognition;
import inc.osips.bleproject.R;
import inc.osips.bleproject.Utilities.UIEssentials;
import android.widget.ImageButton;
import android.widget.TextView;
import static android.app.Activity.RESULT_OK;

public class VoiceControlFragment extends Fragment {

    private ImageButton buttonSpeak;
    private TextView interpretedText;
    FragmentListner fragListner;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            fragListner = (FragmentListner) context;
        }catch (ClassCastException e){
            throw new ClassCastException(context.toString());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.voice_control, container, false);
        initialiseWidgets(view);
        return view;
    }

    private void initialiseWidgets(View v){
        interpretedText =(TextView) v.findViewById(R.id.textViewInterpret);
        buttonSpeak = (ImageButton) v.findViewById(R.id.buttonSpeak);
        buttonSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivityForResult(VoiceRecognition.speechInputCall(), 100);

                }catch (ActivityNotFoundException e){
                    UIEssentials.message(getContext(),e.getMessage());
                }
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode){
            case 100:
                if ((resultCode == RESULT_OK)&& (intent!=null)){
                    ArrayList<String> instructions = intent.getStringArrayListExtra(
                            RecognizerIntent.EXTRA_RESULTS);
                    final String toText = instructions.get(0).toLowerCase(Locale.getDefault());
                    if (toText.toLowerCase().contains("on"))
                        fragListner.sendInstructions("on");
                    else if (toText.toLowerCase().contains("off"))
                        fragListner.sendInstructions("off");
                    else fragListner.sendInstructions(toText);
                    UIEssentials.getHandeler().post(new Runnable() {
                        @Override
                        public void run() {
                            interpretedText.setText("You said: " + toText);
                        }
                    });

                }
                break;
        }super.onActivityResult(requestCode, resultCode, intent);
    }
}
