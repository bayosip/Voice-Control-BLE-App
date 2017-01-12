package inc.osips.bleproject.Model;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.app.Fragment;
import android.util.Log;

import java.util.Locale;
import inc.osips.bleproject.Interfaces.VoiceRecognition;
import inc.osips.bleproject.Utilities.UIEssentials;

public abstract class VoiceRecognitionImpl extends Fragment implements VoiceRecognition{

    String TAG = VoiceRecognitionImpl.class.getSimpleName();
    private static Intent voiceIntent;
    protected SpeechRecognizer sr;


    @Override
    public void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void speechInputCall(){
        try {
            voiceIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            voiceIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            voiceIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            if(voiceIntent!=null)
            sr.startListening(voiceIntent);
            else UIEssentials.message(getContext(), "cannot start recognizer");
        } catch (Exception e){
            Log.e(TAG, e.toString());
            UIEssentials.message(getContext(), "Speech recognition not available");
        }
    }


    @Override
    public void stopListening() {
        if (sr != null) {
            sr.stopListening();
            sr.cancel();
            sr.destroy();
        }
        sr = null;
    }

    @Override
    public abstract void processInstructions(String Commands);

    @Override
    public void restartListeningService() {
        stopListening();
        speechInputCall();
    }
}
