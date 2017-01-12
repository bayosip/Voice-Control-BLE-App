package inc.osips.bleproject.Controller;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import inc.osips.bleproject.Interfaces.FragmentListner;
import inc.osips.bleproject.Model.VoiceRecognitionImpl;
import inc.osips.bleproject.R;
import inc.osips.bleproject.Utilities.UIEssentials;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

//public class VoiceControlFragment extends Fragment implements VCPopUpWindow {
public class VoiceControlFragment extends VoiceRecognitionImpl implements RecognitionListener {
    private ImageButton buttonSpeak;
    private TextView interpretedText;
    private FragmentListner fragListner;
    private PopupWindow vcPopUp;
    private LayoutInflater layoutInflater;
    private ImageView micImage;
    private  ArrayList<String> instructions;
    private static View containerView;
    private ViewGroup vcContainer;

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
        containerView = view;
        return view;
    }

    private void initialiseWidgets(View v){
        interpretedText =(TextView) v.findViewById(R.id.textViewInterpret);
        buttonSpeak = (ImageButton) v.findViewById(R.id.buttonSpeak);

        //Pop up intialization;
        layoutInflater = (LayoutInflater) getContext().getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
        vcContainer = (ViewGroup) layoutInflater.inflate
                (R.layout.voice_input_pop_up,null);
        vcPopUp = new PopupWindow(vcContainer,  ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,  true);
        micImage=(ImageView) vcContainer.findViewById(R.id.imageViewMic);
        
        buttonSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    initSpeech();
                    vcPopUp.showAtLocation(containerView, Gravity.CENTER,0,0 );
                    // Set an elevation value for popup window
                    // Call requires API level 21
                    if(Build.VERSION.SDK_INT>=21){
                        vcPopUp.setElevation(10.0f);
                    }

                    micImage.setImageResource(R.drawable.mic_on);
                    speechInputCall();
                    vcContainer.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            vcPopUp.dismiss();
                            stopListening();
                            return true;
                        }
                    });
                    // startActivityForResult(SPT.speechInputCall(), 100);
                }catch (ActivityNotFoundException e){
                    UIEssentials.message(getContext(),e.getMessage());
                }
            }
        });
    }


    private void initSpeech() {
        try {
            sr = SpeechRecognizer.createSpeechRecognizer(getContext());
            if (!SpeechRecognizer.isRecognitionAvailable(getContext())) {
                UIEssentials.message(getContext(), "Cannot reach Recognizer");
            }
            sr.setRecognitionListener(this);
        }catch (NullPointerException e){
            UIEssentials.message(getContext(), "Recognition Listener null");
        }
    }

    public void processInstructions (final String commands){
        if (commands.contains("on"))
            fragListner.sendInstructions("on");
        else if (commands.toLowerCase().contains("off"))
            fragListner.sendInstructions("off");
        else fragListner.sendInstructions(commands);
        UIEssentials.getHandeler().post(new Runnable() {
            @Override
            public void run() {
                interpretedText.setText("You said: " + commands);
            }
        });
        micImage.setImageResource(R.drawable.mic_off);
        stopListening();
        vcPopUp.dismiss();
    }

    //The user has started to speak.
    @Override
    public void onBeginningOfSpeech() {
        System.out.println("Starting to listen");
    }

    @Override
    public void onError(int error) {
        restartListeningService();
    }

    // This method will be executed when voice commands were found
    @Override
    public void onResults(Bundle results) {
        micImage.setImageResource(R.drawable.mic_off);
        instructions = results.getStringArrayList(
                SpeechRecognizer.RESULTS_RECOGNITION);
        String command = instructions.get(0).toLowerCase(Locale.getDefault());
        System.out.println(command);
        processInstructions(command);
    }

    @Override
    public void onReadyForSpeech(Bundle params) {}
    @Override
    public void onRmsChanged(float rmsdB) {}
    @Override
    public void onBufferReceived(byte[] buffer) {}
    //Called after the user stops speaking.
    @Override
    public void onEndOfSpeech() {
    }
    @Override
    public void onPartialResults(Bundle partialResults) {}
    @Override
    public void onEvent(int eventType, Bundle params) {}

}
