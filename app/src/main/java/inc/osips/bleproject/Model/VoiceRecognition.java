package inc.osips.bleproject.Model;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.speech.RecognizerIntent;

import java.util.Locale;

public class VoiceRecognition {

    private static Intent voiceIntent;

    public static Intent speechInputCall(){
        voiceIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        voiceIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        voiceIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        voiceIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Give Lighting Instruction:");

        return voiceIntent;
    }
}
