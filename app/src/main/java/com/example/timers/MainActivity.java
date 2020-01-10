package com.example.timers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
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

import com.bumptech.glide.Glide;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    ImageView fireGif;
    SeekBar seekbar;
    TextView time_tv;
    TextView button;
    static boolean counterIsActive = false ;
    CountDownTimer countDownTimer;
    MediaPlayer mPlayer;
    boolean mPlayerIsActive = false;
    int currentTimeLeft = 0;

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
        if(seconds < 10){
            time_tv.setText(minutes + ":0" + seconds);
        }
        else{
            time_tv.setText(minutes + ":" + seconds);
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
        if(seconds < 10){
            time_tv.setText(minutes + ":0" + seconds);
            seekbar.setProgress(seconds + minutes * 60);
        }
        else{
            time_tv.setText(minutes + ":" + seconds);
            seekbar.setProgress(seconds + minutes * 60);
        }
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
                timeLeft = seekbar.getProgress();
            }
            fireGif.animate().alpha(1f).translationYBy(10).setDuration(1000);
            counterIsActive = true;
            seekbar.setEnabled(false);
            button.setText("STOP");
            countDownTimer = new CountDownTimer(timeLeft * 1000, 1000) {
                public void onTick(long milUntilDone) {
                    updateTime((int) milUntilDone / 1000);
                }

                public void onFinish() {
                    if (counterIsActive){
                        //TODO: Fix sound keep playing even if stopped
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        time_tv = findViewById(R.id.textView);
        fireGif = findViewById(R.id.fire);
        seekbar = findViewById(R.id.seekBar);
        button = findViewById(R.id.start_button);
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
}
