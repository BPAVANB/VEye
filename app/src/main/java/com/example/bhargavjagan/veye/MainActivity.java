package com.example.bhargavjagan.veye;

import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    TextView textView;
    ImageButton button;
    TextToSpeech tts;
    String text;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.textView);
        button = findViewById(R.id.imageButton);
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
            if(status != TextToSpeech.ERROR )
            {
                tts.setLanguage(Locale.ENGLISH);
            }
            }
        });
    }

    public void speechToText(View view) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, Locale.getDefault());

        if(intent.resolveActivity(getPackageManager())!= null)
        {
            startActivityForResult(intent,10);
        }else {
            Toast.makeText(this, "Your device doesn't support.", Toast.LENGTH_SHORT).show();
        }
    }

    private static final int CAMERA_CAPTURE=0;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch( requestCode)
        {
            case 10:
                if(resultCode == RESULT_OK && data != null)
                {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    textView.setText(result.get(0));
                    text = textView.getText().toString();
                    if(text.startsWith("describe"))
                    {
                        Toast.makeText(this, "describe", Toast.LENGTH_SHORT).show();
                        Intent camera = new Intent();
                        camera.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(camera,CAMERA_CAPTURE);

                    }else if(text.startsWith("analyze"))
                    {
                        Toast.makeText(this, "analyze", Toast.LENGTH_SHORT).show();
                        //Intent ana_intent = new Intent(this,AnalyzeActivity.class);
                    }else if(text.startsWith("read"))
                    {
                        Toast.makeText(this, "read", Toast.LENGTH_SHORT).show();
                        //Intent read_intent = new Intent(this,HandwritingRecognizeActivity.class );
                    }
                    //Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
                    tts.speak(text,TextToSpeech.QUEUE_FLUSH,null);
                }
                break;
            case 0:
                Toast.makeText(this, "describe is invoked", Toast.LENGTH_SHORT).show();
                Bundle extras = data.getExtras();
                Bitmap capturedPhoto = (Bitmap) extras.get("data");
                button.setImageBitmap(capturedPhoto);
                break;
            case 1:
                Toast.makeText(this, "analyze is invoked", Toast.LENGTH_SHORT).show();
                break;
            case 3:
                Toast.makeText(this, "text recognition is invoked", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
