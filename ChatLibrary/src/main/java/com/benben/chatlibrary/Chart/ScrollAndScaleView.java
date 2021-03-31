package com.benben.chatlibrary.Chart;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ViewConfiguration;
import android.widget.OverScroller;
import android.widget.RelativeLayout;

import androidx.core.view.GestureDetectorCompat;

import com.benben.chatlibrary.animation.Easing;
import com.benben.chatlibrary.compat.GestureMoveActionCompat;
import com.benben.chatlibrary.handler.DataHandler;
import com.benben.chatlibrary.handler.ValueHandler;
import com.benben.chatlibrary.handler.ViewPortHandler;
import com.benben.chatlibrary.impl.IAdapter;
import com.benben.chatlibrary.impl.ITimeData;
import com.benben.chatlibrary.utils.Utils;

/**
 * @日期 : 2018/9/5
 * @描述 :
 */
public abstract class ScrollAndScaleView extends RelativeLayout implements RendererView, GestureDetector.OnGestureListener,
        ScaleGestureDetector.OnScaleGestureListener, DataHandler.DataChangeListener {


    /**
     * 管理图表的边界和绘制约束的对象
     */
    protected final ViewPortHandler mViewPortHandler;
    protected final DataHandler<ITimeData> mDataHandler;
    protected final ValueHandler mValueHandler;

    private boolean mTouch = false;             // 是否在触摸中
    private boolean mMultipleTouch = false;     // 是否是多点触控
    private boolean mOnVerticalMove = false;    // 是否是垂直移动
    private boolean mIsLongPress = false;       // 是否是长按

    private final GestureMoveActionCompat mGestureMoveActionCompat;
    protected GestureDetectorCompat mDetector;
    protected ScaleGestureDetector mScaleDetector;
    protected OverScroller mScroller;
    ValueAnimator mValueAnimator;

    public ScrollAndScaleView(Context context) {
        this(context, null);
    }

    public ScrollAndScaleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScrollAndScaleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);
        Utils.init(context);
        mDataHandler = new DataHandler<>(this);
        mViewPortHandler = new ViewPortHandler(mDataHandler);
        mValueHandler = new ValueHandler(mDataHandler, mViewPortHandler);
        int touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mGestureMoveActionCompat = new GestureMoveActionCompat(touchSlop);
        mDetector = new GestureDetectorCompat(context, this);
        mScaleDetector = new ScaleGestureDetector(context, this);
        mScroller = new OverScroller(context);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mViewPortHandler.setChartDimens(w, h);
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (mViewPortHandler.canScrollEnable()) {
            boolean onHorizontalMove = mGestureMoveActionCompat.onTouchEvent(event, event.getX(), event.getY());
            final int action = event.getActionMasked();
            mOnVerticalMove = false;
            if (action == MotionEvent.ACTION_MOVE) {
                if (!onHorizontalMove && !isLongPress() && !mMultipleTouch) {
                    mOnVerticalMove = true;
                    mTouch = false;
                }
            }
            getParent().requestDisallowInterceptTouchEvent(!mOnVerticalMove);
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mViewPortHandler.canScrollEnable()
                && !mViewPortHandler.canScaleEnable()
                && !mViewPortHandler.canLongEnable()) {
            return super.onTouchEvent(event);
        }
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mTouch = true;
                break;
            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() == 1) {
                    //长按之后移动
                    if (isLongPress()) {
                        onLongPress(event);
                    }
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mViewPortHandler.cancelLongPress();
                setLongPressState(false);
                mTouch = false;
                invalidate();
                break;
        }
        mMultipleTouch = event.getPointerCount() > 1;
        this.mDetector.onTouchEvent(event);
        this.mScaleDetector.onTouchEvent(event);
        return true;
    }

    @Override
    public void cancelLongPress() {
        mViewPortHandler.cancelLongPress();
        setLongPressState(false);
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            if (!mTouch) {
                scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            } else {
                mScroller.forceFinished(true);
            }
        }
    }

    /**
     * 设置ScrollX
     */
    @Override
    public void setScrollX(int scrollX) {
        scrollTo(scrollX, 0);
    }

    @Override
    public void scrollBy(int x, int y) {
        scrollTo(mViewPortHandler.getScrollX() - Math.round(x / mViewPortHandler.getScaleX()), 0);
    }

    @Override
    public void scrollTo(int x, int y) {
        if (!mViewPortHandler.canScrollEnable()) {
            mScroller.forceFinished(true);
            return;
        }
        int oldX = mViewPortHandler.getScrollX();
        int minScrollX = mViewPortHandler.getMinScrollX();
        int maxScrollX = mViewPortHandler.getMaxScrollX();
        if (x < minScrollX) {
            x = minScrollX;
            onRightSide();
            mScroller.forceFinished(true);
        } else if (x > maxScrollX) {
            x = maxScrollX;
            onLeftSide();
            mScroller.forceFinished(true);
        }
        mViewPortHandler.setScrollX(x);
        onScrollChanged(x, 0, oldX, 0);
        invalidate();
    }

    @Override
    public float getScaleX() {
        return mViewPortHandler.getScaleX();
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (!isLongPress() && !mMultipleTouch && !mOnVerticalMove) {
            scrollBy(Math.round(distanceX), 0);
            return true;
        }
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {
        if (mViewPortHandler.canLongEnable()) {
            setLongPressState(true);
        }
        mViewPortHandler.onLongPress(motionEvent);
        invalidate();
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (!mTouch && mViewPortHandler.canScrollEnable()) {
            mScroller.fling(mViewPortHandler.getScrollX(), 0
                    , Math.round(velocityX / mViewPortHandler.getScaleX()), 0,
                    Integer.MIN_VALUE, Integer.MAX_VALUE,
                    0, 0);
        }
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        if (!mViewPortHandler.canScaleEnable()) {
            return false;
        }
        float scaleX = mViewPortHandler.getScaleX();
        float oldScale = scaleX;
        scaleX *= detector.getScaleFactor();
        if (oldScale > scaleX && !mViewPortHandler.isFullScreen()) {
            return false;
        }
        if (scaleX < mViewPortHandler.getScaleXMin()) {
            scaleX = mViewPortHandler.getScaleXMin();
        } else if (scaleX > mViewPortHandler.getScaleXMax()) {
            scaleX = mViewPortHandler.getScaleXMax();
        }
        float focusX = detector.getFocusX();
        float focusY = detector.getFocusY();
        mViewPortHandler.onScaleChanged(scaleX, oldScale, focusX, focusY);
        invalidate();
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {

    }

    private void setLongPressState(boolean isLongpress) {
        mIsLongPress = isLongpress;
        //FIXME 这里可以回调长按状态
    }

    public boolean isLongPress() {
        return mIsLongPress;
    }

    /**
     * 设置滚动偏移
     *
     * @param limit 偏移值(dp)
     * @param init  是否将当前滚动值设为偏移值
     */
    public void setScrollLimitX(int limit, boolean init) {

        int limitX = (int) Utils.convertDpToPixel(limit);
        mViewPortHandler.setScrollLimitX(limitX);
        if (init) {
            mViewPortHandler.setScrollX(limitX);
        }
    }

    public void aminScrollZero() {
        if (mTouch) return;
        cancelAminScroll();
        mValueAnimator = ValueAnimator.ofInt(mViewPortHandler.getScrollX(), mViewPortHandler.getMinScrollX());
        mValueAnimator.setDuration(400);
        mValueAnimator.setInterpolator(Easing.EaseOutQuad);
        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (mTouch) {
                    cancelAminScroll();
                    return;
                }
                int value = (int) animation.getAnimatedValue();
                scrollTo(value, 0);
            }
        });
        mValueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mValueAnimator = null;
            }
        });
        mValueAnimator.start();
    }

    public void cancelAminScroll() {
        if (mValueAnimator != null) {
            if (mValueAnimator.isRunning()) {
                mValueAnimator.cancel();
            }
        }
    }

    /**
     * 滑到了最左边
     */
    abstract public void onLeftSide();

    /**
     * 滑到了最右边
     */
    abstract public void onRightSide();

    /**
     * 重新计算并刷新线条
     */
    @Override
    public void notifyDataChanged() {
        mViewPortHandler.calcDataLength();
        if (mViewPortHandler.needRevisedScrollX()) {
            mViewPortHandler.revisedScrollX();
            if (!mValueHandler.endViewIsOutSide()) {
                aminScrollZero();
            }
        }
        if (mDataHandler.getCount() != 0) {
            setScrollX(mViewPortHandler.getScrollX());
        } else {
            setScrollX(mViewPortHandler.getMinScrollX());
        }
    }

    /**
     * 设置数据适配器
     */
    public void setAdapter(IAdapter adapter) {
        mDataHandler.setAdapter(adapter);
    }

    public IAdapter getAdapter() {
        return mDataHandler.getAdapter();
    }

    public void setGameListener(DataHandler.GameListener listener) {
        mDataHandler.setGameListener(listener);
    }

    public void setNeedCheckMissing(boolean need) {
        mValueHandler.setNeedCheckMissing(need);
    }

    public void setMissingDataListener(ValueHandler.MissingDataListener listener) {
        mValueHandler.setMissingDataListener(listener);
    }

    public long getRoundEndTime() {
        return mDataHandler.getRoundEndTime();
    }

    public void nextGame() {
        mDataHandler.nextGame();
    }

    public boolean isWaiting() {
        return mDataHandler.isWaiting();
    }

    public void setEndRangeListener(ValueHandler.EndRangeListener listener) {
        mValueHandler.setEndRangeListener(listener);
    }
}
