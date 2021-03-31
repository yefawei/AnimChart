package com.benben.chatlibrary.handler;

import android.graphics.RectF;
import android.view.MotionEvent;

import com.benben.chatlibrary.impl.IDataProvider;
import com.benben.chatlibrary.impl.IOrderData;
import com.benben.chatlibrary.impl.ITimeData;

/**
 * @日期 : 2018/9/6
 * @描述 :
 */
public class ViewPortHandler {

    private float mDataLength;              // 视图总长度
    private float mTranslateX = 0;          // 画布X轴偏移方向

    protected int mStartIndex;              // 当前内容的开始坐标
    protected int mStopIndex;               // 当前内容的结束坐标
    private int mSelectedIndex;             // 当前选择的索引

    private float mTouchX;                  // 当前点击的X坐标
    private float mTouchY;                  // 当前点击的Y坐标

    private float mMaxValue = Float.MIN_VALUE;      // 当前视图最大值
    private float mMinValue = Float.MAX_VALUE;      // 当前视图最小值
    private float mPaddingValue = 0.1f;             // 绘图区间padding,防止最大最小值绘制触顶触底

    private float mScaleValueY = 0;                 // Y轴值的缩放比

    private IDataProvider mDataProvider;

    public ViewPortHandler(IDataProvider dataProvider) {
        mDataProvider = dataProvider;
    }

    /**
     * 根据ItemCount计算绘图数据总长度
     */
    public void calcDataLength() {
        int count = mDataProvider.getCount();
        if (count == 0) {
            mDataLength = 0;
            mTranslateX = 0;
            mScaleX = 1;
        } else {
            mDataLength = (count - 1) * mPointWidth;
        }
    }

    public int getStartIndex() {
        return mStartIndex;
    }

    public int getStopIndex() {
        return mStopIndex;
    }

    /**
     * 是否是选择状态
     */
    public boolean isSelected() {
        return mSelectedIndex != -1;
    }

    public int getSelectedIndex() {
        return mSelectedIndex;
    }

    public void calculateValue() {
        // 还原最大值最小值
        mMaxValue = Float.MIN_VALUE;
        mMinValue = Float.MAX_VALUE;

        if (mDataProvider.getCount() == 0) {
            mSelectedIndex = -1;
            mTranslateX = 0;
            mStartIndex = 0;
            mStopIndex = 0;
            return;
        }

        mStartIndex = indexOfTranslateX(xToTranslateX(contentLeft()));
        mStopIndex = indexOfTranslateX(xToTranslateX(contentRight()));

        if (mStartIndex != 0) {
            mStartIndex--;
        }
        if (getCoordinateX(mStopIndex) < contentRight() && (mStopIndex < mDataProvider.getCount() - 1)) {
            //填满视图右边数据
            mStopIndex++;
        }

        if (!mDataProvider.isRealEmpty()) {
            // 将当前价格纳入进来
            ITimeData currPoint = mDataProvider.getRealLastItem();
            mMinValue = Math.min(mMinValue, currPoint.getValue());
            mMaxValue = Math.max(mMaxValue, currPoint.getValue());

            if (mDataProvider.currGameHasOrder()) {
                // 将订单价格纳入进来
                IOrderData[] currGameOrder = mDataProvider.getCurrGameOrder();
                for (int i = 0; i < currGameOrder.length; i++) {
                    if (currGameOrder[i] != null) {
                        mMinValue = Math.min(mMinValue, currGameOrder[i].getValue());
                        mMaxValue = Math.max(mMaxValue, currGameOrder[i].getValue());
                    }
                }
            }
            int count = mDataProvider.getRealCount();
            int dataFirstIndex = mDataProvider.realItemFirstIndex();
            int dataLastIndex = mDataProvider.realItemLastIndex();
            int startIndex = 0;
            int endIndex = 0;
            if (count < 90) {
                startIndex = dataFirstIndex;
                endIndex = dataLastIndex;
            } else {
                if (dataLastIndex < mStartIndex) {
                    startIndex = dataLastIndex - 90;
                    endIndex = dataLastIndex;
                } else if (dataFirstIndex < mStartIndex && dataLastIndex >= mStartIndex && dataLastIndex <= mStopIndex) {
                    startIndex = dataLastIndex - mStartIndex > 90 ? mStartIndex : dataLastIndex - 90;
                    endIndex = dataLastIndex;
                } else if (dataFirstIndex <= mStartIndex && dataLastIndex >= mStopIndex) {
                    startIndex = mStopIndex - mStartIndex > 90 ? mStartIndex : mStopIndex - 90;
                    endIndex = mStopIndex;
                } else if (dataFirstIndex >= mStartIndex && dataLastIndex <= mStopIndex) {
                    startIndex = dataFirstIndex;
                    endIndex = dataLastIndex;
                } else if (dataFirstIndex >= mStartIndex && dataFirstIndex <= mStopIndex) {
                    startIndex = dataFirstIndex;
                    endIndex = mStopIndex - dataFirstIndex > 90 ? mStopIndex : dataFirstIndex + 90;
                } else {
                    startIndex = dataFirstIndex;
                    endIndex = dataFirstIndex + 90;
                }
            }

            for (int index = startIndex; index <= endIndex; index++) {
                ITimeData point = mDataProvider.getItem(index);
                if (!point.hasData()) continue;
                mMaxValue = Math.max(mMaxValue, point.getValue());
                mMinValue = Math.min(mMinValue, point.getValue());
            }
            float paddingValue = (mMaxValue - mMinValue) * mPaddingValue;
            mMinValue -= paddingValue;
            mMaxValue += paddingValue;

        } else {
            mMinValue = 0;
            mMaxValue = 100;
        }

        if (mMinValue < 0) {
            mMinValue = 0;
        }
        if (mMaxValue == mMinValue) {
            mMinValue = 0;
            mMaxValue = mMaxValue * 2;
        }
        mScaleValueY = contentHeight() * 1f / (mMaxValue - mMinValue);
    }

    public float getViewPortMaxValue() {
        return mMaxValue;
    }

    public float getViewPortMinValue() {
        return mMinValue;
    }

    /**
     * 根据当前值获取Y轴坐标
     */
    public float getCoordinateY(float value) {
        return (mMaxValue - value) * mScaleValueY + contentTop();
    }

    /**
     * 根据当前索引获取X轴坐标
     */
    public float getCoordinateX(float index) {
        return (index * mPointWidth + mTranslateX) * mScaleX + contentLeft();
    }

    public void cancelLongPress() {
        mTouchX = 0;
        mTouchY = 0;
        mSelectedIndex = -1;
    }

    public void onLongPress(MotionEvent motionEvent) {
        mTouchX = motionEvent.getX();
        mTouchY = motionEvent.getY();
        int lastIndex = mSelectedIndex;
        mSelectedIndex = calculateSelectedX(motionEvent.getX());
        if (lastIndex != mSelectedIndex) {
            // FIXME 这里回调点击的对象
        }
    }

    /**
     * scrollX 转换为画布的 TranslateX
     */
    public void setTranslateXFromScrollX(int scrollX) {
        if (isFullScreen()) {
            mTranslateX = scrollX + getMinTranslateX();
        } else {
            mTranslateX = mPointWidth / 2;
        }
    }

    /**
     * 判断数据是否充满屏幕
     */
    public boolean isFullScreen() {
        return mDataLength >= contentWidth() / mScaleX;
    }

    /**
     * 获取平移的最大边界值
     */
    public float getMaxTranslateX() {
        if (!isFullScreen()) {
            return getMinTranslateX();
        }
        return 0;
    }

    /**
     * 获取平移的最小边界值
     */
    public float getMinTranslateX() {
        if (mDataLength == 0) return 0;
        return -mDataLength + contentWidth() / mScaleX - mPointWidth / 2;
    }

    /**
     * 根据触控点X计算选择的索引
     */
    private int calculateSelectedX(float x) {
        int index = indexOfTranslateX(xToTranslateX(x));
        if (index < mStartIndex) {
            index = mStartIndex;
        }
        if (index > mStopIndex) {
            index = mStopIndex;
        }
        return index;
    }

    /**
     * view中的x转化为TranslateX
     */
    private float xToTranslateX(float viewX) {
        float x = viewX - contentLeft();
        return -mTranslateX + x / mScaleX;
    }

    /**
     * translateX转化为view中的x
     */
    private float translateXtoX(float translateX) {
        return (translateX + mTranslateX) * mScaleX;
    }

    private int indexOfTranslateX(float translateX) {
        if (mDataProvider.getCount() == 0) {
            return 0;
        }
        return indexOfTranslateX(translateX, 0, mDataProvider.getCount() - 1);
    }

    /**
     * 二分查找当前值的index
     *
     * @return
     */
    private int indexOfTranslateX(float translateX, int start, int end) {
        if (end == start) {
            return start;
        }
        if (end - start == 1) {
            float startValue = getTranslateX(start);
            float endValue = getTranslateX(end);
            return Math.abs(translateX - startValue) < Math.abs(translateX - endValue) ? start : end;
        }
        int mid = start + (end - start) / 2;
        float midValue = getTranslateX(mid);
        if (translateX < midValue) {
            return indexOfTranslateX(translateX, start, mid);
        } else if (translateX > midValue) {
            return indexOfTranslateX(translateX, mid, end);
        } else {
            return mid;
        }
    }

    /**
     * 根据索引索取translateX坐标
     *
     * @param position 索引值
     */
    private float getTranslateX(int position) {
        return position * mPointWidth;
    }

    /*--------------------------------------------------------------------------------------------*/

    private boolean mScrollEnable = true;       // 是否可滚动
    private boolean mScaleEnable = true;        // 是否可缩放
    private boolean mLongEnable = true;         // 是否可长按

    private float mScaleXMax = 2.2f;            // 放大最大值
    private float mScaleXMin = 0.1f;            // 缩放最小值
    private float mScaleX = 1;                  // 当前缩放值

    private int mScrollLimitX = 0;            // 滚动偏移值
    private int mScrollX = 0;                 // 当前滚动值

    private boolean mNeedFixViewPointPosition = true;// 如果为False,视图将永远显示把点往左移位
    private long mRecordRoundEndTime = -1;           // 记录结束时间

    /**
     * 是否需要修正滚动位置
     * @return
     */
    public boolean needRevisedScrollX(){
        if (!mNeedFixViewPointPosition) return false;
        if (mDataProvider.getCount() == 0) {
            mRecordRoundEndTime = -1;
            return false;
        }
        long roundEndTime = mDataProvider.getRoundEndTime();
        if (mRecordRoundEndTime == -1) {
            mRecordRoundEndTime = roundEndTime;
        }
        return mRecordRoundEndTime != roundEndTime;
    }

    /**
     * 修正滚动位置
     */
    public void revisedScrollX() {
        long roundEndTime = mDataProvider.getRoundEndTime();
        long size = roundEndTime - mRecordRoundEndTime;
        int scrollX = getScrollX();
        scrollX += getPointWidth() * size;
        mScrollX = scrollX;
        mRecordRoundEndTime = roundEndTime;
        if (mScrollX < getMinScrollX()) {
            mScrollX = getMinScrollX();
        } else if (mScrollX > getMaxScrollX()) {
            mScrollX = getMaxScrollX();
        }
        setTranslateXFromScrollX(mScrollX);
    }

    public int getMinScrollX() {
        return (int) ((mPointWidth / 2 + mScrollLimitX) / mScaleX);
    }

    public int getMaxScrollX() {
        float maxTranslateX = getMaxTranslateX();
        float minTranslateX = getMinTranslateX();
        return Math.round(maxTranslateX - minTranslateX);
    }

    public float getScaleXMax() {
        return mScaleXMax;
    }

    public void setScaleXMax(float scaleXMax) {
        mScaleXMax = scaleXMax;
    }

    public float getScaleXMin() {
        return mScaleXMin;
    }

    public void setScaleXMin(float scaleXMin) {
        mScaleXMin = scaleXMin;
    }

    public float getScaleX() {
        return mScaleX;
    }

    public void onScaleChanged(float scale, float oldScale, float focusX, float focusY) {
        mScaleX = scale;
        // 计算手指触控焦点,使得缩放以焦点中心放大缩小
        //FIXME 放得很大时并不居中
        mScrollX += (int) ((focusX / getChartWidth()) * (getChartWidth() / scale) * (scale - oldScale));
        if (mScrollX < getMinScrollX()) {
            mScrollX = getMinScrollX();
        } else if (mScrollX > getMaxScrollX()) {
            mScrollX = getMaxScrollX();
        }
        setTranslateXFromScrollX(mScrollX);
    }

    public void setScrollLimitX(int scrollLimitX) {
        mScrollLimitX = scrollLimitX;
    }

    public int getScrollX() {
        return mScrollX;
    }

    public void setScrollX(int scrollX) {
        mScrollX = scrollX;
        setTranslateXFromScrollX(scrollX);
    }

    public boolean canScrollEnable() {
        return mScrollEnable;
    }

    public void setScrollEnable(boolean enable) {
        mScrollEnable = enable;
    }

    public boolean canScaleEnable() {
        return mScaleEnable;
    }

    public void setScaleEnable(boolean enable) {
        mScaleEnable = enable;
    }

    public boolean canLongEnable() {
        return mLongEnable;
    }

    public void setLongEnable(boolean enable) {
        mLongEnable = enable;
    }
    /*--------------------------------------------------------------------------------------------*/
    /**
     * 这个矩形定义了可以绘制图形值的区域
     */
    private final RectF mContentRect = new RectF();

    private float mChartWidth;  // 视图总宽度
    private float mChartHeight; // 视图总高度

    private int mViewPortSize = 120;        // 未缩放时屏幕显示的item数
    private float mPointWidth = 0;          // 点绘制区间 单位 px

    public int getViewPortSize() {
        return mViewPortSize;
    }

    public float getPointWidth() {
        return mPointWidth;
    }

    public void setChartDimens(float width, float height) {

        float offsetLeft = this.offsetLeft();
        float offsetTop = this.offsetTop();
        float offsetRight = this.offsetRight();
        float offsetBottom = this.offsetBottom();

        mChartHeight = height;
        mChartWidth = width;

        restrainViewPort(offsetLeft, offsetTop, offsetRight, offsetBottom);
        mPointWidth = contentWidth() / mViewPortSize;

        setTranslateXFromScrollX(mScrollX);
    }

    /**
     * 可绘制图形区域
     */
    public void restrainViewPort(float offsetLeft, float offsetTop, float offsetRight, float offsetBottom) {
        mContentRect.set(offsetLeft, offsetTop, mChartWidth - offsetRight, mChartHeight
                - offsetBottom);
    }

    /**
     * 是否有可绘制图形区域
     */
    public boolean hasChartDimens() {
        return mChartHeight > 0 && mChartWidth > 0;
    }


    public float offsetLeft() {
        return mContentRect.left;
    }

    public float offsetRight() {
        return mChartWidth - mContentRect.right;
    }

    public float offsetTop() {
        return mContentRect.top;
    }

    public float offsetBottom() {
        return mChartHeight - mContentRect.bottom;
    }

    public float contentTop() {
        return mContentRect.top;
    }

    public float contentLeft() {
        return mContentRect.left;
    }

    public float contentRight() {
        return mContentRect.right;
    }

    public float contentBottom() {
        return mContentRect.bottom;
    }

    public float contentWidth() {
        return mContentRect.width();
    }

    public float contentHeight() {
        return mContentRect.height();
    }

    public RectF getContentRect() {
        return mContentRect;
    }

    public float getChartHeight() {
        return mChartHeight;
    }

    public float getChartWidth() {
        return mChartWidth;
    }
}
