package com.example.timers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    ImageView fireGif;
    SeekBar seekbar;
    TextView minutes_tv;
    TextView seconds_tv;
    TextView button;
    ImageView heart;
    int volume_minimum = 60;
    static boolean counterIsActive = false ;
    CountDownTimer countDownTimer;
    MediaPlayer mPlayer;
    boolean mPlayerIsActive = false;
    int currentTimeLeft = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        minutes_tv = findViewById(R.id.Minutes);
        seconds_tv = findViewById(R.id.Seconds);
        fireGif = findViewById(R.id.fire);
        seekbar = findViewById(R.id.seekBar);
        button = findViewById(R.id.start_button);
        heart = findViewById(R.id.heart);
        seekbar.setMax(1200);
        seekbar.setProgress(30);
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                updateTimeBar(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        Glide.with(this)
                .load(R.drawable.fire)
                .into(fireGif);
        fireGif.animate().alpha(0f).translationYBy(-10);

        VolumeCheck();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
    @Override
    protected void onResume() {
        stopService();
        controlTimerIfRestored();
        super.onResume();
    }
    @Override
    protected void onPause() {
        if (counterIsActive){
            counterIsActive = false;
            countDownTimer.cancel();
            startService();
        }
        if(mPlayerIsActive) {
            mPlayer.stop();
            mPlayerIsActive = false;
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onPause();
    }
    @Override
    protected void onStop() {
        super.onStop();
    }
    @Override
    protected void onDestroy() {
        stopService();
        super.onDestroy();
    }

    public void VolumeCheck(){
        //Media volume levels check
        AudioManager am = null;
        try {
            am = (AudioManager) getSystemService(AUDIO_SERVICE);
        }
        catch (NullPointerException e){
            Toast.makeText(getApplicationContext(),"ERROR trying the get device volume", Toast.LENGTH_LONG).show();
            Log.i("VolumeCheckError", e.toString());
        }
        if (am == null){
            return;
        }
        int music_volume_level = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        int max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int volume_percentage =  (int)((((float)music_volume_level/(float)max)) * 100.0);
        Log.i("VolumeCheck", "volume_percentage: " + volume_percentage);
        if (volume_percentage < volume_minimum){
            Toast.makeText(getApplicationContext(),"Sound volume is too low (pump it up)", Toast.LENGTH_LONG).show();
        }
    }

    public void startService(){
        Intent serviceIntent = new Intent(this, TimerService.class);
        serviceIntent.putExtra("timeLeft", Integer.toString(currentTimeLeft));
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    public void stopService(){
        Intent serviceIntent = new Intent(this, TimerService.class);
        stopService(serviceIntent);
    }

    public void updateTime(int secondsLeft){
        currentTimeLeft = secondsLeft;
        int minutes = secondsLeft / 60;
        int seconds = secondsLeft - minutes * 60;
        if(seconds < 10) {
            seconds_tv.setText("0" + Integer.toString(seconds));
        }
        else{
            seconds_tv.setText(Integer.toString(seconds));
        }
        if(minutes < 10){
            minutes_tv.setText("0" + Integer.toString(minutes));
        }
        else{
            minutes_tv.setText(Integer.toString(minutes));
        }
    }

    public void updateTimeBar(int secondsLeft){
        int minutes = secondsLeft / 60;
        int seconds = secondsLeft - minutes * 60;
        switch (seconds / 15){
            case 0:
                seconds = 0;
                break;
            case 1:
                seconds = 15;
                break;
            case 2:
                seconds = 30;
                break;
            case 3:
                seconds = 45;
                break;
            default:
                seconds = 0;
        }
        if(seconds < 10) {
            seconds_tv.setText("0" + Integer.toString(seconds));
        }
        else{
            seconds_tv.setText(Integer.toString(seconds));
        }
        if(minutes < 10){
            minutes_tv.setText("0" + Integer.toString(minutes));
        }
        else{
            minutes_tv.setText(Integer.toString(minutes));
        }
        seekbar.setProgress(seconds + minutes * 60);
    }
    public void controlTimerWrapper(View view){
        controlTimer(view, 1);
    }
    public void controlTimer(View view, int timeLeft){
        if(!counterIsActive && timeLeft > 0) {
            Log.i("controlTimerCheck", "Entered !counterIsActive");
            Log.i("controlTimerCheck", "timeLeft =  " + timeLeft);
            //fireGif.setAlpha(1f);
            if(view != null){
                int minutes = Integer.parseInt(minutes_tv.getText().toString());
                int seconds = Integer.parseInt(seconds_tv.getText().toString());
                timeLeft = seconds + (minutes * 60);
                //timeLeft = seekbar.getProgress();
            }
            fireGif.animate().alpha(1f).translationYBy(10).setDuration(1000);
            counterIsActive = true;
            seekbar.setEnabled(false);
            minutes_tv.setEnabled(false);
            seconds_tv.setEnabled(false);
            button.setText("STOP");
            countDownTimer = new CountDownTimer(timeLeft * 1000, 1000) {
                public void onTick(long milUntilDone) {
                    updateTime((int) milUntilDone / 1000);
                }

                public void onFinish() {
                    if (counterIsActive){
                        Log.i("Done", "Main FINISHED!!, counterIsActive =  " + counterIsActive);
                        mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.boiling_sound);
                        mPlayer.start();
                        mPlayerIsActive = true;
                    }
                }
            }.start();
        } else if (counterIsActive){
            Log.i("controlTimerCheck", "Entered counterIsActive");
            counterIsActive = false;
            Log.i("controlTimerCheck", "counterIsActive = " + counterIsActive);
            countDownTimer.cancel();
            seekbar.setProgress(30);
            button.setText("START");
            seekbar.setEnabled(true);
            minutes_tv.setEnabled(true);
            seconds_tv.setEnabled(true);
            updateTime(30);
            fireGif.animate().alpha(0f).translationYBy(-10).setDuration(1000);
            if(mPlayerIsActive) {
                mPlayer.stop();
                mPlayerIsActive = false;
            }
        }
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String timeLeft = intent.getStringExtra("timeLeft");
            Log.i("controlTimerCheck", "Calling controlTimer with time = " + timeLeft);
            controlTimer(null, Integer.parseInt(timeLeft));
        }
    };

    public void controlTimerIfRestored(){
        //---------------------------------------------------------
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("intentName"));
        //---------------------------------------------------------
    }

    public void showHeart(View view){
        new Handler().post(new Runnable() {

            @Override
            public void run() {
                heart.setScaleX(0f);
                heart.setScaleY(0f);
                heart.setVisibility(View.VISIBLE);
                heart.setAlpha(1f);
                heart.animate().alpha(1f).scaleXBy(1f).scaleYBy(1f).alpha(0f).setDuration(1500);
            }
        } );
    }

}
