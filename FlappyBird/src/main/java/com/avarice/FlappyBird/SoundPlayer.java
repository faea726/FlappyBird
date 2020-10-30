package com.avarice.FlappyBird;

import android.content.Context;
import android.media.SoundPool;

import com.avarice.app.R;

public class SoundPlayer {
    private static SoundPool soundPool;
    private static int hitSound;
    private static int pointSound;
    private static int wingSound;
    private static int dieSound;

    public SoundPlayer(Context context) {
        soundPool = new SoundPool.Builder().setMaxStreams(4).build();

        hitSound = soundPool.load(context, R.raw.hit, 1);
        pointSound = soundPool.load(context, R.raw.point, 1);
        wingSound = soundPool.load(context, R.raw.wing, 1);
        dieSound = soundPool.load(context, R.raw.die, 1);
    }

    public void playHitSound() {
        soundPool.play(hitSound, 1.0f, 1.0f, 0, 0, 1.0f);
    }

    public void playFlySound() {
        soundPool.play(wingSound, 1.0f, 1.0f, 0, 0, 1.0f);
    }

    public void playDieSound() {
        soundPool.play(dieSound, 1.0f, 1.0f, 0, 0, 1.0f);
    }

    public void playAddPointSound() {
        soundPool.play(pointSound, 1.0f, 1.0f, 0, 0, 1.0f);
    }
}
