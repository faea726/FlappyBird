package com.avarice.FlappyBird;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Handler;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;

import com.avarice.app.R;

import java.util.Random;

import static android.app.Activity.RESULT_OK;

public class GameView extends View {

    final int UPDATE_MILLIS = 25;
    Handler handler;//Handler is required to schedule a runnable after some delay
    Runnable runnable;
    Bitmap background;
    Display display;
    Point point;
    int dWidth, dHeight;//Device width and height
    Rect rect;

    boolean startGame;

    //Bird
    Bitmap[] birds;
    int birdFrame = 0;
    //Bird position
    int birdX, birdY;
    int birdFallVelocity = 0, gravity = 3; //'velocity' fall down

    //Tubes
    Bitmap topTube, bottomTube;
    int gap = 300;//Gap between top and bottom tube
    int minTubeOffset, maxTubeOffset;
    int numberOfTubes = 4;
    int distanceBetweenTubes;
    int[] tubeX = new int[numberOfTubes];
    int[] topTubeY = new int[numberOfTubes];
    Random random;
    int tubeVelocity = 8;

    //hit box
    Rect birdBox, topBox, botBox;

    //Score
    Paint scorePaint;
    int score;

    //Sounds
    SoundPlayer soundPlayer;

    public GameView(Context context) {
        super(context);
        handler = new Handler();
        //Call onDraw()
        runnable = this::invalidate;

        //Get screen size
        display = ((Activity) getContext()).getWindowManager().getDefaultDisplay();
        point = new Point();
        display.getSize(point);
        dWidth = point.x;
        dHeight = point.y;
        rect = new Rect(0, 0, dWidth, dHeight);

        background = BitmapFactory.decodeResource(getResources(), R.drawable.background);

        topTube = BitmapFactory.decodeResource(getResources(), R.drawable.toptube);
        bottomTube = BitmapFactory.decodeResource(getResources(), R.drawable.bottomtube);
        birds = new Bitmap[2];
        birds[0] = BitmapFactory.decodeResource(getResources(), R.drawable.bird);
        birds[1] = BitmapFactory.decodeResource(getResources(), R.drawable.bird2);

        birdX = (dWidth - birds[0].getWidth()) / 2;
        birdY = (dHeight - birds[0].getHeight()) / 2;

        distanceBetweenTubes = tubeVelocity * UPDATE_MILLIS * 2;
        minTubeOffset = dHeight / 4;
        maxTubeOffset = dHeight - minTubeOffset - gap;
        random = new Random();

        //Initial tubes
        for (int i = 0; i < tubeX.length; i++) {
            tubeX[i] = dWidth + i * distanceBetweenTubes;
            topTubeY[i] = minTubeOffset + random.nextInt(maxTubeOffset - minTubeOffset);
        }

        //Score
        scorePaint = new Paint();
        scorePaint.setColor(Color.WHITE);
        scorePaint.setTextSize(80);
        scorePaint.setTypeface(Typeface.DEFAULT_BOLD);
        scorePaint.setAntiAlias(true);
        score = 0;

        //Sound
        soundPlayer = new SoundPlayer(getContext());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //Background
        canvas.drawBitmap(background, null, rect, null);

        //Bird animation
        if (birdFrame == 0) {
            birdFrame = 1;
        } else {
            birdFrame = 0;
        }

        if (startGame) {
            //Bird position
            if (birdY < dHeight - birds[0].getHeight() && birdY > 0) {
                birdFallVelocity += gravity;
                birdY += birdFallVelocity;
            } else {
                startGame = false;
                soundPlayer.playHitSound();
                backToMain();
            }

            if (startGame) {
                //Score
                scoreAndHit(birdY, tubeX, topTubeY);
                //Tube
                for (int i = 0; i < tubeX.length; i++) {
                    tubeX[i] -= tubeVelocity;
                }
                if (tubeX[0] < -topTube.getWidth()) {
                    for (int i = 0; i < tubeX.length - 1; i++) {
                        tubeX[i] = tubeX[i + 1];
                        topTubeY[i] = topTubeY[i + 1];
                    }
                    tubeX[tubeX.length - 1] += distanceBetweenTubes;
                    topTubeY[tubeX.length - 1] = minTubeOffset + random.nextInt(maxTubeOffset - minTubeOffset + 1);
                }
            }
        }

        //Display tubes
        for (int i = 0; i < tubeX.length; i++) {
            canvas.drawBitmap(topTube, tubeX[i], topTubeY[i] - topTube.getHeight(), null);
            canvas.drawBitmap(bottomTube, tubeX[i], topTubeY[i] + gap, null);
        }
        //Display bird and score at center of screen
        canvas.drawText(String.valueOf(score), (float) dWidth / 2, 80, scorePaint);
        canvas.drawBitmap(birds[birdFrame], birdX, birdY, null);
        handler.postDelayed(runnable, UPDATE_MILLIS);
    }

    public void scoreAndHit(int birdY, int[] x, int[] y) {
        int w = birds[0].getWidth();
        int h = birds[0].getHeight();
        for (int i = 0; i < x.length; i++) {
            //Check intersect
            birdBox = new Rect(birdX, birdY, birdX + w, birdY + h);
            topBox = new Rect(x[i], 0, x[i] + topTube.getWidth(), y[i]);
            botBox = new Rect(x[i], y[i] + gap, x[i] + bottomTube.getWidth(), dHeight);
            boolean hitTop = birdBox.intersect(topBox);
            boolean hitBot = birdBox.intersect(botBox);
            if (hitTop || hitBot) {
                startGame = false;
                soundPlayer.playHitSound();
                backToMain();
                break;
            }

            //Update score
            if ((birdX == x[i]) && (birdY > y[i]) && ((birdY + h) < (y[i] + gap))) {
                score++;
                soundPlayer.playAddPointSound();
                break;
            }
        }
    }

    public void backToMain() {
        soundPlayer.playDieSound();

        new AlertDialog.Builder(getContext())
                .setTitle("GAME OVER")
                .setMessage("No hope!\nJust " + score + " point!\nPress OK to become winner!")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    Intent intent = new Intent();
                    intent.putExtra("newRecord", score);
                    ((Activity) getContext()).setResult(RESULT_OK, intent);
                    ((Activity) getContext()).finish();
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setCancelable(false)
                .show();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {//Tap detector
            //Bird move upward some unit
            birdFallVelocity = -30;
            soundPlayer.playFlySound();
            startGame = true;
        }

        return true;//By return TRUE, mean that done with touch event and have no further action is required by Android
    }
}
