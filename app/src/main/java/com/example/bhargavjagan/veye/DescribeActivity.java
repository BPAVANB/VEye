
package com.example.bhargavjagan.veye;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.bhargavjagan.veye.R;
import com.google.gson.Gson;
import com.microsoft.projectoxford.vision.VisionServiceClient;
import com.microsoft.projectoxford.vision.VisionServiceRestClient;
import com.microsoft.projectoxford.vision.contract.AnalysisResult;
import com.microsoft.projectoxford.vision.contract.Caption;
import com.microsoft.projectoxford.vision.rest.VisionServiceException;
import com.example.bhargavjagan.veye.helper.ImageHelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

public class DescribeActivity extends AppCompatActivity {

    // Flag to indicate which task is to be performed.
    private static final int REQUEST_SELECT_IMAGE = 0;

    // The button to select an image
    private Button mButtonSelectImage;

    // The URI of the image selected to detect.
    private Uri mImageUri;

    // The image selected to detect.
    private Bitmap mBitmap;

    // The edit to show status and result.
    private EditText mEditText;

    //Client Service method
    private VisionServiceClient client;

    //Text To Speech  integration

    TextToSpeech tts;
    int result;
    String text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_describe);

        if (client==null){
            client = new VisionServiceRestClient(getString(R.string.subscription_key), getString(R.string.subscription_apiroot));
        }

        mButtonSelectImage = (Button)findViewById(R.id.buttonSelectImage);
        mEditText = (EditText)findViewById(R.id.editTextResult);
            }
    public void TTS()
    {

                if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
                {
                    Toast.makeText(getApplicationContext(),"Feature not supported", Toast.LENGTH_LONG);
                }
                else
                {
                    text =mEditText.getText().toString();
                    mEditText.append(text);
                    tts.speak(text, TextToSpeech.QUEUE_FLUSH,null);
                    Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
                }
    }

    @Override
    protected void onStart() {
        super.onStart();
        tts = new TextToSpeech(DescribeActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i == TextToSpeech.SUCCESS)
                {
                    result = tts.setLanguage(Locale.UK);
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"feature not supported", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(tts != null)
        {
            tts.stop();
            tts.shutdown();
        }
    }



    public void doDescribe() {
        mButtonSelectImage.setEnabled(false);
        mEditText.setText("Describing...");

        try {
            new doRequest().execute();
        } catch (Exception e)
        {
            mEditText.setText("Error encountered. Exception is: " + e.toString());
        }
    }

    // Called when the "Select Image" button is clicked.
    public void selectImage(View view) {
        mEditText.setText("");

        Intent intent;
        intent = new Intent(DescribeActivity.this, com.example.bhargavjagan.veye.helper.SelectImageActivity.class);
        startActivityForResult(intent, REQUEST_SELECT_IMAGE);
        this.TTS();
    }

    // Called when image selection is done.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("DescribeActivity", "onActivityResult");
        switch (requestCode) {
            case REQUEST_SELECT_IMAGE:
                if(resultCode == RESULT_OK) {
                    // If image is selected successfully, set the image URI and bitmap.
                    mImageUri = data.getData();

                    mBitmap = com.example.bhargavjagan.veye.helper.ImageHelper.loadSizeLimitedBitmapFromUri(
                            mImageUri, getContentResolver());
                    if (mBitmap != null) {
                        // Show the image on screen.
                        ImageView imageView = (ImageView) findViewById(R.id.selectedImage);
                        imageView.setImageBitmap(mBitmap);

                        // Add detection log.
                        Log.d("DescribeActivity", "Image: " + mImageUri + " resized to " + mBitmap.getWidth()
                                + "x" + mBitmap.getHeight());

                        doDescribe();
                    }
                }
                break;
            default:
                break;
        }
    }


    private String process() throws VisionServiceException, IOException {
        Gson gson = new Gson();

        // Put the image into an input stream for detection.
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());

        AnalysisResult v = this.client.describe(inputStream, 1);

        String result = gson.toJson(v);
        Log.d("result", result);

        return result;
    }

    private class doRequest extends AsyncTask<String, String, String> {
        // Store error message
        private Exception e = null;

        public doRequest() {
        }

        @Override
        protected String doInBackground(String... args) {
            try {
                return process();
            } catch (Exception e) {
                this.e = e;    // Store error
            }

            return null;
        }

        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);
            // Display based on error existence

            mEditText.setText("");
            if (e != null) {
                mEditText.setText("Error: " + e.getMessage());
                this.e = null;
            } else {
                Gson gson = new Gson();
                AnalysisResult result = gson.fromJson(data, AnalysisResult.class);

               // mEditText.append("Image format: " + result.metadata.format + "\n");
               // mEditText.append("Image width: " + result.metadata.width + ", height:" + result.metadata.height + "\n");
               // mEditText.append("\n");

                for (Caption caption: result.description.captions) {
                    mEditText.append(/*"Caption: " +*/ caption.text );//+ ", confidence: " + caption.confidence + "\n");
                }
                mEditText.append("\n");


               // for (String tag: result.description.tags) {
               //     mEditText.append("Tag: " + tag + "\n");
               // }
              //  mEditText.append("\n");

              //  mEditText.append("\n--- Raw Data ---\n\n");
              //  mEditText.append(data);
              //  mEditText.setSelection(0);
            }




            mButtonSelectImage.setEnabled(true);
        }

    }


}
