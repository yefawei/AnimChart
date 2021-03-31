
package com.benben.chatlibrary.animation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;

import androidx.annotation.Keep;
import androidx.annotation.RequiresApi;

import com.benben.chatlibrary.animation.Easing.EasingFunction;

/**
 * @日期 : 2018/9/18
 * @描述 :
 */
public class ChartAnimator {

    private AnimatorUpdateListener mListener;
    private ObjectAnimator mObjectAnimator;

    @SuppressWarnings("WeakerAccess")
    protected float mPhase = 1f;

    public ChartAnimator() {
    }

    @RequiresApi(11)
    public ChartAnimator(AnimatorUpdateListener listener) {
        mListener = listener;
    }

    @RequiresApi(11)
    private ObjectAnimator animator(int duration, EasingFunction easing) {

        ObjectAnimator animator = ObjectAnimator.ofFloat(this, "phase", 0f, 1f);
        animator.setInterpolator(easing);
        animator.setDuration(duration);

        return animator;
    }

    /**
     * 是否在运行
     * @return
     */
    public boolean animateIsRunning() {
        return mObjectAnimator != null && mObjectAnimator.isRunning();
    }

    /**
     * 取消动画
     */
    public void animateCancel() {
        if (mObjectAnimator != null) {
            mObjectAnimator.cancel();
        }
    }

    /**
     * 启动动画
     * @param durationMillis 动画时间(毫秒)
     */
    @RequiresApi(11)
    public void animate(int durationMillis) {
        animate(durationMillis, Easing.Linear);
    }

    /**
     * 开始动画
     *
     * @param durationMillis 动画时间(毫秒)
     * @param easing         插值器
     */
    @RequiresApi(11)
    public void animate(int durationMillis, EasingFunction easing) {
        mObjectAnimator = animator(durationMillis, easing);
        setPhase(0);
        mObjectAnimator.addUpdateListener(mListener);
        mObjectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (mObjectAnimator != null) {
                    mListener.onAnimationUpdate(mObjectAnimator);
                    mObjectAnimator = null;
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mObjectAnimator = null;
            }
        });
        mObjectAnimator.start();
    }

    /**
     * 获取动画过程值
     *
     * @return {@link #mPhase}
     */
    public float getPhase() {
        return mPhase;
    }

    /**
     * 动画过程值
     *
     * @param phase 0-1之间
     */
    @Keep
    public void setPhase(float phase) {
        if (phase > 1f) {
            phase = 1f;
        } else if (phase < 0f) {
            phase = 0f;
        }
        mPhase = phase;
    }
}
