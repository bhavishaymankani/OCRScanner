package com.bhavishaymankani.ocrscanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;

import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final String FRAGMENT_NAME = "imageFragment";

    private TextToSpeech mTextToSpeech;

    private Bitmap mImageBitmap;

    private FloatingActionButton  mSpeakTextButton, mClipBoardButton;
    private Button mDetectTextButton, mCaptureImageButton;
    private TextView mTextViewDisplayText;
    private ImageView mImageView;

    private String textView;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextViewDisplayText = findViewById(R.id.textview);
        mImageView = findViewById(R.id.imageview);
        mCaptureImageButton = findViewById(R.id.capture_image_btn);
        mDetectTextButton = findViewById(R.id.detect_image_btn);
        mSpeakTextButton = findViewById(R.id.speak_text_btn);
        mClipBoardButton = findViewById(R.id.clipboard_btn);

        speakTextInitialization();

        mCaptureImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTextViewDisplayText.setText(" ");
                dispatchTakePictureIntent();
            }
        });

        mDetectTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                detectTextFromImage();
            }
        });

        mSpeakTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speakText();
            }
        });

        mClipBoardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView = mTextViewDisplayText.getText().toString();
                copyToClipBoard(textView);
            }
        });

        mTextViewDisplayText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView = mTextViewDisplayText.getText().toString();
                viewFullText(textView);
            }
        });

        if (savedInstanceState != null) {
            textView = savedInstanceState.getString("text_view");
            mTextViewDisplayText.setText(textView);

            mImageBitmap = (Bitmap) savedInstanceState.getParcelable("image");
            mImageView.setImageBitmap(mImageBitmap);
        }

        //retainImage();

    }

    private void detectTextFromImage() {
        int rotationDegree = 0;
        InputImage image = InputImage.fromBitmap(mImageBitmap, rotationDegree);
        TextRecognizer recognizer = TextRecognition.getClient();

        Task<Text> result = recognizer.process(image).addOnSuccessListener(new OnSuccessListener<Text>() {
            @Override
            public void onSuccess(Text text) {

                List<Text.TextBlock> textBlocks = text.getTextBlocks();
                if (textBlocks.size() == 0) {
                    Toast.makeText(MainActivity.this,"No Text Found",Toast.LENGTH_LONG).show();
                } else {
                    for (Text.TextBlock block : textBlocks) {
                        String resultText = block.getText();
                        mTextViewDisplayText.append(resultText);

                        for (Text.Line line : block.getLines()) {
                            String lineText = line.getText();
                            for (Text.Element element : line.getElements()) {
                                String elementText = element.getText();

                            }
                        }
                    }
                }


            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void speakTextInitialization() {
        mTextToSpeech = new TextToSpeech(MainActivity.this, new OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                   int result = mTextToSpeech.setLanguage(Locale.ENGLISH);

                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(MainActivity.this, "Language not supported", Toast.LENGTH_SHORT).show();
                    } else {
                        mDetectTextButton.setEnabled(true);
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Initialization failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void speakText() {
        String text = mTextViewDisplayText.getText().toString();

        mTextToSpeech.setPitch(1f);
        mTextToSpeech.setSpeechRate(1f);

        mTextToSpeech.speak(text,TextToSpeech.QUEUE_FLUSH,null);


    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } catch (ActivityNotFoundException e) {
            // display error state to the user
            Toast.makeText(this, "Permission Required:" + e.getMessage() , Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            mImageBitmap = (Bitmap) extras.get("data");
            mImageView.setImageBitmap(mImageBitmap);
        }
    }

    private void copyToClipBoard(String text) {

        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("text", text);
        clipboardManager.setPrimaryClip(clipData);

        Toast.makeText(this, "Copied to Clipboard", Toast.LENGTH_SHORT).show();
    }

    private void viewFullText(String textView) {
        ViewFullTextDialogBox dialogBox = new ViewFullTextDialogBox();
        dialogBox.setText(textView);

        dialogBox.show(getSupportFragmentManager(), "view_full_text");
    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        textView = mTextViewDisplayText.getText().toString();
        outState.putString("text_view", textView);
        outState.putParcelable("image", mImageBitmap);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        //textView = savedInstanceState.getString("text_view");
    }

    private void retainImage() {
        ImageRetainingFragment imageRetainingFragment;
        // Find the retained fragment when activity restarts.
        FragmentManager fragmentManager = getSupportFragmentManager();
        imageRetainingFragment = (ImageRetainingFragment) fragmentManager.findFragmentByTag(FRAGMENT_NAME);

        // Create the fragment when initiated the first time.
        if (imageRetainingFragment == null) {
            imageRetainingFragment = new ImageRetainingFragment();

            fragmentManager.beginTransaction()
                    .add(imageRetainingFragment, FRAGMENT_NAME)
                    .commit();
        }

        imageRetainingFragment.setSelectedImageBitmap(mImageBitmap);

        Bitmap selectedImage = imageRetainingFragment.getSelectedImageBitmap();

        if (selectedImage == null) {
            return;
        }

        mImageView.setImageBitmap(selectedImage);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mTextToSpeech != null) {
            mTextToSpeech.stop();
            mTextToSpeech.shutdown();
        }
    }
}