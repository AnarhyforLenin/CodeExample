
package com.spicysauce.lessstress;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.lang.ref.WeakReference;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.stream.IntStream;

public class Tamagochi extends AppCompatActivity {
    private ImageView window;
    private ImageView karIm;
    private ImageView bed;
    private ImageView dark;
    private ImageButton kar;
    private ImageButton sleep_button;
    private AnimationDrawable sleepStaticAnim;
    private static final int[] time = { 100, 100, 100 };
    private static int numAnim;
    private boolean isTappedSleep = false;
    private SharedPreferences preferences;
    private CountDownTimer healthTimer, reversedHealthTimer;
    private TextView textHealth, textEat, textHappy;
    private ProgressBar progressHealth, progressEat, progressHappy;
    private final Handler HANDLER = new Handler();
    private CountDownTimer foodTimer, happinessTimer;
    private boolean isAnimationPlaying = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tamagochi);

        window = findViewById(R.id.window);
        karIm = findViewById(R.id.karambolaIm);
        kar = findViewById(R.id.karambola);
        sleep_button = findViewById(R.id.button_sleep);

        new BackgroundTask(this).execute();

        ImageButton lamp = findViewById(R.id.lamp_tm);
        ImageButton home = findViewById(R.id.tools_icon);
        dark = findViewById(R.id.dark);

        lamp.setOnClickListener(v -> {
            Intent homeTam = new Intent(Tamagochi.this, Thanks.class);
            homeTam.putExtra("activity",2);
            startActivity(homeTam);
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });

        preferences = getSharedPreferences("com.example.myapplication", Context.MODE_PRIVATE);
        updateEverything();

        textHealth = findViewById(R.id.health);
        progressHealth = findViewById(R.id.vertical_progressbar1);

        textEat = findViewById(R.id.eat);
        progressEat = findViewById(R.id.vertical_progressbar2);

        textHappy = findViewById(R.id.happyness);
        progressHappy = findViewById(R.id.vertical_progressbar3);

        home.setOnClickListener(v -> {
            Intent intent7 = new Intent(Tamagochi.this, MainScreen.class);
            startActivity(intent7);
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });

        foodTimer = setTimer(5000000, 5000, Timers.FOOD, -1);
        happinessTimer = setTimer(5000000, 5500,
                Timers.HAPPINESS, -1);
        healthTimer = setTimer(10000000, 10000, Timers.HEALTH, -1);
        reversedHealthTimer = Objects.requireNonNull(setTimer(1000000,
                10000, Timers.HEALTH, 1));

        reversedHealthTimer.cancel();
        ImageView dish = findViewById(R.id.yellow);
        dish.setOnLongClickListener(longClickListener);
        kar.setOnDragListener(dragListener);
        bed = findViewById(R.id.bed);
        sleep_button.setBackgroundResource(R.drawable.sleep_off);

        kar.setOnTouchListener(new View.OnTouchListener() {
            private long temp;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isAnimationPlaying = true;
                        kar.setBackgroundResource(R.drawable.neutral_to_happy);
                        AnimationDrawable happyAnim = (AnimationDrawable) kar.getBackground();
                        happyAnim.start();
                        temp = 0;
                        break;

                    case MotionEvent.ACTION_MOVE:
                        temp++;
                        if (temp % 8 == 0) {
                            time[0] = checkProgress(time[0] + 1);
                            textEat.setText(String.valueOf(time[0]));
                            progressEat.setProgress(checkProgress(time[0]));
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        kar.setBackgroundResource(R.drawable.happy_to_sad);
                        AnimationDrawable sadge = (AnimationDrawable) kar.getBackground();
                        sadge.start();
                        HANDLER.postDelayed((() -> isAnimationPlaying = false), 500);
                        break;
                }
                return true;
            }
        });

        kar.setOnLongClickListener(v -> {
            numAnim = 1;
            playAnimation(numAnim);
            return false;
        });

        sleep_button.setOnClickListener(v -> {
            numAnim = 2;
            playAnimation(numAnim);
        });
    }

    View.OnLongClickListener longClickListener = v -> {
        ClipData data = ClipData.newPlainText("", "");
        View.DragShadowBuilder myShadowBuilder = new View.DragShadowBuilder(v);
        v.startDrag(data, myShadowBuilder, v, 0);
        return false;
    };

    View.OnDragListener dragListener = new View.OnDragListener() {
        @Override
        public boolean onDrag(View v, DragEvent event) {
            isAnimationPlaying = true;
            int dragEvent = event.getAction();
            switch (dragEvent) {
                case DragEvent.ACTION_DRAG_ENTERED:
                    kar.setBackgroundResource(R.drawable.eat2min);
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    kar.setBackgroundResource(R.drawable.happy);
                    break;
                case DragEvent.ACTION_DROP:
                    final View view = (View) event.getLocalState();
                    if (view.getId() == R.id.yellow) {
                        numAnim = 3;
                        HANDLER.post(() -> playAnimation(numAnim));
                        return true;
                    }
                    break;
            }
            isAnimationPlaying = false;
            return true;
        }
    };

    public void checkMood(int[] time) {
        int total = IntStream.of(time).sum();
        if (total < 200) {
            HANDLER.postDelayed(() -> {
                if (total < 60)
                    kar.setBackgroundResource(R.drawable.sad31);
                else if (total < 140)
                    kar.setBackgroundResource(R.drawable.sad21);
                else
                    kar.setBackgroundResource(R.drawable.sad11);
            }, 3000);
        }
        else
            kar.setBackgroundResource(R.drawable.happy1min);
    }

    public int checkProgress(int progress) {
        return progress > 100 ? 100 : Math.max(progress, 0);
    }

    private void saveEverything() {
        SharedPreferences.Editor editor;
        editor = preferences.edit();
        editor.putInt("time1",time[1]);
        editor.putInt("time0",time[0]);
        editor.putInt("time2",time[2]);
        editor.apply();
    }

    private void updateEverything() {
        time[0] = preferences.getInt("time0",100);
        time[1] = preferences.getInt("time1",100);
        time[2] = preferences.getInt("time2",100);
    }

    private void playAnimation(int numAnim) {
        isAnimationPlaying = true;
        switch (numAnim) {

            case 2:
                if (!isTappedSleep) {
                    sleep_button.setEnabled(false);

                    dark.setVisibility(View.VISIBLE);
                    karIm.setVisibility(View.VISIBLE);
                    kar.setVisibility(View.GONE);
                    bed.setVisibility(View.GONE);

                    karIm.setBackgroundResource(R.drawable.sleep);
                    AnimationDrawable sleepAnim = (AnimationDrawable) karIm.getBackground();
                    sleepAnim.start();
                    sleep_button.setBackgroundResource(R.drawable.sleep_on);

                    HANDLER.postDelayed(() -> {
                        sleepAnim.stop();
                        karIm.setBackgroundResource(R.drawable.sleep_static);
                        sleepStaticAnim = (AnimationDrawable) karIm.getBackground();
                        sleepStaticAnim.start();
                        sleep_button.setEnabled(true);
                        healthTimer.cancel();
                        reversedHealthTimer.start();
                    }, 4500);
                    isTappedSleep = true;
                }

                else {
                    dark.setVisibility(View.GONE);
                    karIm.setVisibility(View.GONE);
                    kar.setVisibility(View.VISIBLE);
                    bed.setVisibility(View.VISIBLE);

                    sleep_button.setBackgroundResource(R.drawable.sleep_off);
                    sleepStaticAnim.stop();
                    reversedHealthTimer.cancel();
                    startTimers();
                    isTappedSleep = false;
                    isAnimationPlaying = false;
                }
                break;

            case 3:
                foodTimer.cancel();
                kar.setBackgroundResource(R.drawable.eat_short);
                AnimationDrawable eatAnim = (AnimationDrawable) kar.getBackground();
                eatAnim.start();
                HANDLER.postDelayed(() -> {
                    time[1] = checkProgress(time[1] + 20);
                    textHappy.setText(String.valueOf(time[1]));
                    progressHappy.setProgress(checkProgress(time[1]));
                    //checkMood(time);
                    foodTimer.start();
                }, 1000);
                HANDLER.postDelayed((() -> isAnimationPlaying = false), 1300);
                break;
            default:
                isAnimationPlaying = false;
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        startTimers();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopTimers();
        saveEverything();
    }

    private CountDownTimer setTimer(long millisInFuture, long countDownInterval, Timers timerType, int timerValue) {
        TextView currentTextView;
        ProgressBar currentProgressBar;
        int id = timerType.getId();
        switch (timerType) {
            case FOOD:
                currentTextView = textEat;
                currentProgressBar = progressEat;
                break;
            case HAPPINESS:
                currentTextView = textHappy;
                currentProgressBar = progressHappy;
                break;
            case HEALTH:
                currentTextView = textHealth;
                currentProgressBar = progressHealth;
                break;
            default:
                return null;
        }
        return new CountDownTimer(millisInFuture, countDownInterval) {
            @Override
            public void onTick(long millisUntilFinished) {
                time[id] = checkProgress(time[id]);
                currentTextView.setText(String.valueOf(time[id]));
                currentProgressBar.setProgress(checkProgress(time[id]));
                time[id] += timerValue;
                //if (!isAnimationPlaying) HANDLER.postDelayed(() -> checkMood(time), 3000);
            }
            @Override
            public void onFinish() { }
        };
    }

    private void startTimers()
    {
        foodTimer.start();
        happinessTimer.start();
        healthTimer.start();
    }

    private void stopTimers()
    {
        foodTimer.cancel();
        happinessTimer.cancel();
        healthTimer.cancel();
    }
    @Override
    public void onBackPressed() {
        Intent intentb = new Intent( Tamagochi.this, MainScreen.class);
        startActivity(intentb);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private static class BackgroundTask extends AsyncTask<Void, Void, Void> {
        private final WeakReference<Tamagochi> activityReference;
        BackgroundTask(Tamagochi thisActivity) {
            this.activityReference = new WeakReference<>(thisActivity);
        }
        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            TimeZone tz = TimeZone.getTimeZone("GMT+03");
            java.util.Calendar c = java.util.Calendar.getInstance(tz);
            String time2 = String.format(Locale.ENGLISH, "%02d" , c.get(java.util.Calendar.HOUR_OF_DAY));
            Tamagochi thisActivity = activityReference.get();
            if (Integer.parseInt(time2) > 20)
                thisActivity.window.setBackgroundResource(R.drawable.w1_night);
            else {
                thisActivity.dark.setVisibility(View.INVISIBLE);
                thisActivity.window.setBackgroundResource(R.drawable.w1_day);
            }
            thisActivity.karIm.setBackgroundResource(R.drawable.sleep);
            thisActivity.kar.setBackgroundResource(R.drawable.eat);
        }
        @Override
        protected Void doInBackground(Void... voids) {
            return null;
        }

    }
}
