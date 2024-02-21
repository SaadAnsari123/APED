package com.example.aped;
import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class home extends AppCompatActivity {
    protected static final int RESULT_SPEECH = 1;
    protected static final int REQUEST_AUDIO_PERMISSION_CODE = 101;
    MediaRecorder mediaRecorder;
    MediaPlayer mediaPlayer;
    boolean isRecording = false;
    boolean isPlaying = false;
    String path = null;
    int seconds = 0;
    int dummySeconds = 0;
    int playableSeconds = 0;
    Handler handler;
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    private ImageButton btnSpeak;
    private ImageButton btnListenGoogle;
    private ImageButton btnListenYou;
    private EditText tvText;
    private Button lngbtn;
    private SeekBar mSeekBarPitch;
    private SeekBar mSeekBarSpeed;
    TextToSpeech t1;
    DBhelper DB;
    private BottomNavigationView bottomNavigation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        tvText = findViewById(R.id.tvText);
        btnSpeak = findViewById(R.id.btnSpeak);
        btnListenGoogle = findViewById(R.id.btnListenGoogle);
        btnListenYou = findViewById(R.id.btnListenYou);
        mSeekBarPitch = findViewById(R.id.seek_bar_pitch);
        mSeekBarSpeed = findViewById(R.id.seek_bar_speed);
        lngbtn = findViewById(R.id.lngbtn);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        mediaPlayer = new MediaPlayer();
        DB = new DBhelper(this);

        LoadLocale();

        t1 = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS){
                    int result = t1.setLanguage(Locale.ENGLISH);

                }
            }
        });

        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.history) {
                    String txt = tvText.getText().toString();
                   new PrefManager(home.this).saveHist(txt);
                  //  DB.insertData(txt);
                    Intent intent = new Intent(home.this, history.class);
                    intent.putExtra("KEY_SENDER",txt);
                    startActivity(intent);
                    return false;
                }
            return true;
            }
        });

                btnListenGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String txt = tvText.getText().toString();

                float pitch = (float) mSeekBarPitch.getProgress() / 50;
                if (pitch < 0.1) pitch = 0.1f;
                float speed = (float) mSeekBarSpeed.getProgress() / 50;
                if (speed < 0.1) speed = 0.1f;
                t1.setPitch(pitch);
                t1.setSpeechRate(speed);

                    Locale l1 = new Locale("ur", "IN");
                    t1.setLanguage(l1);
                    t1.speak(txt, TextToSpeech.QUEUE_FLUSH, null);

            }
        });

                btnListenYou.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if(!isPlaying){

                            if(path!=null){
                                try {
                                    mediaPlayer.setDataSource(getFilePath());
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            else {
                                Toast.makeText(getApplicationContext(),"No Recording Present",Toast.LENGTH_SHORT).show();
                                return;
                            }
                            try {
                                mediaPlayer.prepare();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            mediaPlayer.start();
                            isPlaying=true;

                        }
                        else {
                         mediaPlayer.stop();
                         mediaPlayer.release();
                         mediaPlayer = null;
                         mediaPlayer = new MediaPlayer();
                         isPlaying = false;
                         seconds = 0;
                         handler.removeCallbacksAndMessages(null);

                        }
                    }
                });

        btnSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(checkRecordingPermission()){
                    if(!isRecording){
                        isRecording=true;
                        executorService.execute(new Runnable() {
                            @Override
                            public void run() {
                                mediaRecorder = new MediaRecorder();
                                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                                mediaRecorder.setOutputFile(getFilePath());
                                path = getFilePath();
                                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                                try {
                                    mediaRecorder.prepare();
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                                mediaRecorder.start();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        playableSeconds=0;
                                        seconds=0;
                                        dummySeconds=0;

                                    }
                                });
                            }
                        });
                    }
                    else {
                        executorService.execute(new Runnable() {
                            @Override
                            public void run() {
                                mediaRecorder.stop();
                                mediaRecorder.release();
                                mediaRecorder = null;
                                playableSeconds = seconds;
                                dummySeconds = seconds;
                                seconds = 0;
                                isRecording = false;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        handler.removeCallbacksAndMessages(null);
                                    }
                                });
                            }
                        });
                    }
                }
                else {
                    requestRecordingPermission();
                }

                String lang[] = {"English","Gujarati","Marathi","Hindi","Urdu"};
                AlertDialog.Builder sbuilder = new AlertDialog.Builder(home.this);
                sbuilder.setTitle("Language");
                sbuilder.setSingleChoiceItems(lang, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0){

                            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
                            try {
                                startActivityForResult(intent, RESULT_SPEECH);
                                tvText.setText("");
                            } catch (ActivityNotFoundException e) {
                                Toast.makeText(getApplicationContext(), "Your device doesn't support Speech to Text", Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }

                            recreate();
                        }
                        else if (i == 1){

                            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "gu");
                            try {
                                startActivityForResult(intent, RESULT_SPEECH);
                                tvText.setText("");
                            } catch (ActivityNotFoundException e) {
                                Toast.makeText(getApplicationContext(), "Your device doesn't support Speech to Text", Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }

                            recreate();
                        }
                        else if (i == 2){

                            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "mr");
                            try {
                                startActivityForResult(intent, RESULT_SPEECH);
                                tvText.setText("");
                            } catch (ActivityNotFoundException e) {
                                Toast.makeText(getApplicationContext(), "Your device doesn't support Speech to Text", Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }

                            recreate();
                        }
                        else if (i == 3){

                            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi");
                            try {
                                startActivityForResult(intent, RESULT_SPEECH);
                                tvText.setText("");
                            } catch (ActivityNotFoundException e) {
                                Toast.makeText(getApplicationContext(), "Your device doesn't support Speech to Text", Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }

                            recreate();
                        }
                        else if (i == 4){

                            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ur");
                            try {
                                startActivityForResult(intent, RESULT_SPEECH);
                                tvText.setText("");
                            } catch (ActivityNotFoundException e) {
                                Toast.makeText(getApplicationContext(), "Your device doesn't support Speech to Text", Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }

                            recreate();
                        }
                    }
                });
                sbuilder.create();
                sbuilder.show();

            }
        });


        lngbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String languages[] = {"English","Gujarati","Marathi","Hindi","Urdu"};
                AlertDialog.Builder mbuilder = new AlertDialog.Builder(home.this);
                mbuilder.setTitle("Language");
                mbuilder.setSingleChoiceItems(languages, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0){
                            setLocale("");
                            recreate();
                        }
                        else if (i == 1){
                            setLocale("gu");
                            recreate();
                        }
                        else if (i == 2){
                            setLocale("mr");
                            recreate();
                        }
                        else if (i == 3){
                            setLocale("hi");
                            recreate();
                        }
                        else if (i == 4){
                            setLocale("ur");
                            recreate();
                        }
                    }
                });
                mbuilder.create();
                mbuilder.show();

            }
        });
    }



    private void setLocale(String language) {

        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Configuration configuration = new Configuration();
        configuration.locale = locale;
        getBaseContext().getResources().updateConfiguration(configuration,getBaseContext().getResources().getDisplayMetrics());

        SharedPreferences.Editor editor = getSharedPreferences("Settings",MODE_PRIVATE).edit();
        editor.putString("app_lang",language);
        editor.apply();
    }

    private void LoadLocale(){
        SharedPreferences preferences = getSharedPreferences("Settings",MODE_PRIVATE);
        String language = preferences.getString("app_lang","");
        setLocale(language);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case RESULT_SPEECH:
                if(resultCode == RESULT_OK && data != null){
                    ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    tvText.setText(text.get(0));
                }
                break;
        }
    }


    private void requestRecordingPermission(){

        ActivityCompat.requestPermissions(home.this,new String[]{Manifest.permission.RECORD_AUDIO},REQUEST_AUDIO_PERMISSION_CODE);
    }

    private boolean checkRecordingPermission(){

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO)== PackageManager.PERMISSION_DENIED){
            requestRecordingPermission();
            return false;

        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==REQUEST_AUDIO_PERMISSION_CODE) {

            if(grantResults.length>0){
                boolean permissionToRecord=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                if(permissionToRecord){
                    Toast.makeText(getApplicationContext(),"Permission Granted to Record",Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getApplicationContext(),"Permission Denied to Record",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private String getFilePath(){
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        File music = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        File file = new File(music, "recordingFile"+".mp3");
        return file.getPath();
    }


}