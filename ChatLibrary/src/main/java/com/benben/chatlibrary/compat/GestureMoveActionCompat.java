package com.benben.chatlibrary.compat;

import android.view.MotionEvent;

/**
 * @版权 : 深圳区块链科技有限公司
 * @作者 : 叶发伟
 * @日期 : 2017/9/26
 * @描述 :
 * @网址 : @see <a href=""></a>
 */

public class GestureMoveActionCompat {

    private OnGestureMoveListener mGestureMoveListener;

    private float mLastMotionX; // ACTION_DOWN 事件的坐标 X

    private float mLastMotionY; // ACTION_DOWN 事件的坐标 Y

    private int mInterceptStatus = 0; // 当前滑动的方向。0 无滑动（视为点击）；1 垂直滑动；2 横向滑动

    /**
     *
     * 因为有手指抖动的影响,有时候会产生少量的 ACTION_MOVE 事件,造成程序识别错误。
     */
    private boolean mEnableClick = true;    // 是否响应点击事件,因为有手指抖动的影响,有时候会产生少量的 ACTION_MOVE 事件,造成程序识别错误。

    private int mTouchSlop = 20; //避免程序识别错误的一个阀值。只有触摸移动的距离大于这个阀值时,才认为是一个有效的移动。

    private boolean mDragging = false;

    public GestureMoveActionCompat() {
    }

    public GestureMoveActionCompat(int touchSlop){
        mTouchSlop = touchSlop;
    }

    public void enableClick(boolean enableClick) {
        mEnableClick = enableClick;
    }

    public void setTouchSlop(int touchSlop) {
        this.mTouchSlop = touchSlop;
    }

    public boolean isDragging() {
        return mDragging;
    }

    /**
     * @param e 事件 e
     * @param x 本次事件的坐标 x。可以是 e.getRawX() 或是 e.getX(),具体看情况
     * @param y 本次事件的坐标 y。可以是 e.getRawY() 或是 e.getY(),具体看情况
     *
     * @return 事件是否是横向滑动
     */
    public boolean onTouchEvent(MotionEvent e, float x, float y) {
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastMotionY = y;
                mLastMotionX = x;
                mInterceptStatus = 0;
                mDragging = false;
                break;

            case MotionEvent.ACTION_MOVE:
                float deltaY = Math.abs(y - mLastMotionY);
                float deltaX = Math.abs(x - mLastMotionX);

                /**
                 * 如果之前是垂直滑动,即使现在是横向滑动,仍然认为它是垂直滑动的
                 * 如果之前是横向滑动,即使现在是垂直滑动,仍然认为它是横向滑动的
                 * 防止在一个方向上来回滑动时,发生垂直滑动和横向滑动的频繁切换,造成识别错误
                 */
                if (mInterceptStatus != 1 &&
                        (mDragging || deltaX > deltaY && deltaX > mTouchSlop)) {
                    mInterceptStatus = 2;
                    mDragging = true;

                    if (mGestureMoveListener != null) {
                        mGestureMoveListener.onHorizontalMove(e, x, y);
                    }
                } else if (mInterceptStatus != 2 &&
                        (mDragging || deltaX < deltaY && deltaY > mTouchSlop)) {
                    mInterceptStatus = 1;
                    mDragging = true;

                    if (mGestureMoveListener != null) {
                        mGestureMoveListener.onVerticalMove(e, x, y);
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
                if (mInterceptStatus == 0) {
                    if (mEnableClick && mGestureMoveListener != null) {
                        mGestureMoveListener.onClick(e, x, y);
                    }
                }
                mInterceptStatus = 0;
                mDragging = false;
                break;
        }
        return mInterceptStatus == 2;
    }

    public void setGestureMoveListener(OnGestureMoveListener listener){
        mGestureMoveListener = listener;
    }

    public interface OnGestureMoveListener {

        /**
         * 横向移动
         */
        void onHorizontalMove(MotionEvent e, float x, float y);

        /**
         * 垂直移动
         */
        void onVerticalMove(MotionEvent e, float x, float y);

        /**
         * 点击事件
         */
        void onClick(MotionEvent e, float x, float y);
    }
}
