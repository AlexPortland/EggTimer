package com.example.timers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

public class MainActivity extends AppCompatActivity {
    ImageView fireGif;
    SeekBar seekbar;
    TextView time_tv;
    TextView button;
    boolean counterIsActive = false ;
    CountDownTimer countDownTimer;
    MediaPlayer mPlayer;
    PowerManager pm;
    PowerManager.WakeLock wl = null;

    private static final String TAG = MainActivity.class.getSimpleName();

//    public void initChannels(Context context) {
//        if (Build.VERSION.SDK_INT < 26) {
//            return;
//        }
//        NotificationManager notificationManager =
//                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//        NotificationChannel channel = new NotificationChannel("default",
//                "Channel name",
//                NotificationManager.IMPORTANCE_DEFAULT);
//        channel.setDescription("Channel description");
//        notificationManager.createNotificationChannel(channel);
//    }

    public void updateTime(int secondsLeft){
        int minutes = secondsLeft / 60;
        int seconds = secondsLeft - minutes * 60;
        if(seconds < 10){
            time_tv.setText((Integer.toString(minutes)) + ":0" + (Integer.toString(seconds)));
        }
        else{
            time_tv.setText((Integer.toString(minutes)) + ":" + (Integer.toString(seconds)));
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
            time_tv.setText((Integer.toString(minutes)) + ":0" + (Integer.toString(seconds)));
            seekbar.setProgress(seconds + minutes * 60);
        }
        else{
            time_tv.setText((Integer.toString(minutes)) + ":" + (Integer.toString(seconds)));
            seekbar.setProgress(seconds + minutes * 60);
        }
    }
    public void controlTimer(View view){
        if(!counterIsActive) {
            //fireGif.setAlpha(1f);
            wl.acquire();
            fireGif.animate().alpha(1f).translationYBy(10).setDuration(1000);
            counterIsActive = true;
            seekbar.setEnabled(false);
            button.setText("STOP");
            countDownTimer = new CountDownTimer(seekbar.getProgress() * 1000 + 100, 1000) {
                public void onTick(long milUntilDone) {
                    updateTime((int) milUntilDone / 1000);
                }

                public void onFinish() {
                    Log.i("Done", "FINISHED!!");
                    mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.boiling_sound);
                    mPlayer.start();
                }
            }.start();
        }
        else{
            wl.release();
            updateTime(30);
            counterIsActive = false;
            seekbar.setProgress(30);
            countDownTimer.cancel();
            button.setText("START");
            seekbar.setEnabled(true);
            //fireGif.setAlpha(0f);
            fireGif.animate().alpha(0f).translationYBy(-10).setDuration(1000);
            if(mPlayer != null){
                mPlayer.stop();
            }
        }
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
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,TAG);//"EggTimer:WAKE_LOCK");
        wl.acquire();
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
            //initChannels(MainActivity.this);
//---------------------------

//---------------------------

        });

        Glide.with(this)
                .load(R.drawable.fire)
                .into(fireGif);
        fireGif.animate().alpha(0f).translationYBy(-10);

//        final TextView text = findViewById(R.id.text);
//        final Handler handler = new Handler();
//        Runnable run = new Runnable() {
//            @Override
//            public void run() {
//                text.setText(Integer.toString(time));
//                handler.postDelayed(this, 1000);
//                time++;
//                Log.i("run",Integer.toString(time));
//            }
//        };
//        handler.post(run);
        //-----------------------------------------------------


    }

    @Override
    protected void onPause() {
        if (wl.isHeld())
            wl.release();
        super.onPause();
    }
}
