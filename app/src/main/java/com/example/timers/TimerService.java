package com.example.timers;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import static com.example.timers.App.CHANNEL_ID;


public class TimerService extends Service {

    NotificationCompat.Builder notification;
    NotificationManagerCompat notificationManager;
    NotificationCompat.Builder builder;
    MediaPlayer mPlayer;
    static boolean mPlayerIsActive = false;
    PendingIntent pendingIntent;
    CountDownTimer countDownTimer;
    static boolean timerIsActive = false;
    int gTime = 0;

    public void updateNotification(String text){
        notificationManager = NotificationManagerCompat.from(this);
        builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        builder.setContentTitle("Egg Timer")
                .setContentText(text)
                .setOnlyAlertOnce(true)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW);
        notificationManager.notify(1, builder.build());
    }
    public void updateTime(int time){
        gTime = time;
        int minutes = time / 60;
        int seconds = time - minutes * 60;
        if(seconds < 10){
            updateNotification(minutes + ":0" + seconds);
        }
        else{
            updateNotification(minutes + ":" + seconds);
        }
    }

    public void runTimer(int timeLeft){
        if (timeLeft > 0){
            timerIsActive = true;
            Log.i("controlTimerCheck", "Timer in service starts, timeLeft = " + timeLeft);
            countDownTimer = new CountDownTimer(timeLeft * 1000, 1000) {
                public void onTick(long milUntilDone) {
                    updateTime((int) milUntilDone / 1000);
                }
                public void onFinish() {
                    Log.i("Done", "Service FINISHED!!");
                    timerIsActive = false;
                    mPlayerIsActive = true;
                    mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.boiling_sound);
                    mPlayer.start();
                }
            }.start();
        } else {
            updateTime(timeLeft);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle extras = intent.getExtras();
        String text = extras.getString("timeLeft", "0");

        Intent notificationIntent = new Intent(this, MainActivity.class);
        pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Egg Timer")
                .setContentText(text)
                .setSmallIcon(R.mipmap.ic_launcher_round);
        startForeground(1, notification.build());
        runTimer(Integer.parseInt(text));
        return START_NOT_STICKY;
    }


    @Override
    public void onDestroy() {
        if (timerIsActive){
            timerIsActive = false;
            countDownTimer.cancel();
        }
        if (mPlayerIsActive){
            mPlayerIsActive = false;
            mPlayer.stop();
        }
        Log.i("controlTimerCheck", "countDownTimer is canceled in service");
        Intent i = new Intent("intentName").putExtra("timeLeft", Integer.toString(gTime));
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
        stopSelf();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}