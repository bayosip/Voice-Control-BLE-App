package inc.osips.bleproject.Controller;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.zip.Inflater;
import inc.osips.bleproject.R;
import android.app.Activity;
import android.widget.ImageButton;
import android.widget.TextView;

public class VoiceControlFragment extends Fragment {

    private ImageButton buttonSpeak;
    private TextView interpretedText;
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
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
                interpretedText.setText("You said: ");
            }
        });
    }
}
