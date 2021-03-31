package com.benben.chatlibrary.impl;

/**
 * @日期 : 2018/9/20
 * @描述 :
 */
public abstract class AnimOrderData implements IOrderData {

    private static final float ANIM_TIME = 2000.0f;

    private boolean playedAnim = false;
    private boolean runAnim = false;
    private long animStartTime = -1;

    @Override
    public boolean AnimIsEnd() {
        return playedAnim;
    }

    @Override
    public void playerAnim() {
        runAnim = true;
        animStartTime = System.currentTimeMillis();
    }

    @Override
    public boolean animIsRunning() {
        return runAnim;
    }

    @Override
    public float getAnimPhase() {
        if (playedAnim) return 1.0f;
        long currTime = System.currentTimeMillis();
        long limit = currTime - animStartTime;
        float percent = limit/ ANIM_TIME;
        if (percent > 1.0f) {
            runAnim = false;
            playedAnim = true;
            percent = 1.0f;
        }
        return percent;
    }
}
