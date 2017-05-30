package eu.geekapp.automaticsoundlevel;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.IOException;

public class Noise extends AppCompatActivity {

    private static final String LOG_TAG = "AudioRecordTest";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String mFileName = null;

    private boolean permissionToRecordAccepted = false;
    private MediaRecorder mRecorder = null;

    private String [] permissions = {Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};


    TextView output_text;
    ToggleButton onoff;
    ProgressBar progressBar;
    AudioManager audioManager;
    SeekBar seekBar;
    ProgressBar result;
    int max;
    int offset = 5;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main1);

        audioManager = (AudioManager) getSystemService(this.AUDIO_SERVICE);
        max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        seekBar = (SeekBar) findViewById(R.id.seekBar2);
        seekBar.setMax(max);

        output_text = (TextView)findViewById(R.id.status);
        output_text.setText("----");

        onoff = (ToggleButton) findViewById(R.id.toggleButton2);
        onoff.setChecked(false);

        progressBar = (ProgressBar) findViewById(R.id.progressBar3);
        progressBar.setMax(30000);

        result = (ProgressBar) findViewById(R.id.progressBarRes);
        result.setMax(max);

        result.setMax(max);

        onoff.setOnCheckedChangeListener(new ToggleButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    Log.d("isCh", ""+isChecked);
                    if(permissionToRecordAccepted){
                        startRecording();
                    }else{
                        request_permision();
                    }

                }else{
                    Log.d("stop","");
                    stopRecording();
                }
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                offset = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });



        periodicCheck.start();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    Handler handler = new Handler();
    Thread periodicCheck = new Thread(new Runnable() {
        @Override
        public void run() {
            double amp = getAmplitude();
            //Log.d("amplitude", ""+amp);
            output_text.setText(""+amp);
            progressBar.setProgress((int) amp);
            handler.postDelayed(periodicCheck, 100);

            //audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, );
            double prec = amp/30000.0;
            int now = (int) Math.min(max, offset-3 + 6 * prec);
            //Log.d("vol", "y "+now);

            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, now, 0);

            int crt_vol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            result.setProgress(crt_vol);
        }

    });



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                startRecording();
                break;
        }
        if (!permissionToRecordAccepted ) finish();

        //startRecording();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //startRecording();

        int cvol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        progressBar.setProgress(cvol);
        result.setProgress(cvol);


    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        //startRecording();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //onoff.setChecked(false);
        //stopRecording();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRecording();
    }

    void request_permision(){
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
    }

    private void stopRecording() {
        if(mRecorder != null){
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }
    }

    private void startRecording() {
        if(permissionToRecordAccepted && mRecorder == null) {
            Log.d("recorder", "started");
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mFileName = getExternalCacheDir().getAbsolutePath();
            mFileName += "/audiorecordtest.3gp";
            mRecorder.setOutputFile(mFileName);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            try {
                mRecorder.prepare();

                Log.d(LOG_TAG, "prepared");
            } catch (IOException e) {
                Log.e(LOG_TAG, "prepare() failed");
            }

            mRecorder.start();
            Log.d(LOG_TAG, "recording");
        }
    }

    public double getAmplitude() {
        if (mRecorder != null)
            return  (mRecorder.getMaxAmplitude());
        else
            return 0;

    }


}