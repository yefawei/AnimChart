package com.benben.chatlibrary.handler;

import android.animation.ValueAnimator;
import android.graphics.Path;
import android.graphics.PathMeasure;

import com.benben.chatlibrary.animation.ChartAnimator;
import com.benben.chatlibrary.impl.IDataProvider;
import com.benben.chatlibrary.impl.ITimeData;
import com.benben.chatlibrary.renderer.AxisLeftRenderer;
import com.benben.chatlibrary.renderer.BaseRenderer;
import com.benben.chatlibrary.renderer.CurrValueRenderer;
import com.benben.chatlibrary.renderer.LineChartRenderer;
import com.benben.chatlibrary.renderer.OrderRenderer;

import java.util.ArrayList;
import java.util.List;

/**
 * @日期 : 2018/9/18
 * @描述 :
 */
public class ValueHandler {

    private final int mAnimatorTime = 300;                    // 动画时长
    private boolean mMinMaxAnimatorEnable = true;        // 是否对最大最小值进行动画
    private boolean mStepPathAnimatorEnable = true;      // 是否对线进行过渡动画

    private final IDataProvider mDataProvider;
    private final ViewPortHandler mViewPortHandler;

    private final List<BaseRenderer> mMinMaxRenderer = new ArrayList<>();
    private final List<BaseRenderer> mStepLineRenderer = new ArrayList<>();

    public ValueHandler(IDataProvider dataProvider, ViewPortHandler viewPortHandler) {
        mDataProvider = dataProvider;
        mViewPortHandler = viewPortHandler;
        initMinMaxAnimator();
        initStepAnimator();
    }

    public void initRenderer(List<BaseRenderer> renderers) {
        mMinMaxRenderer.clear();
        mStepLineRenderer.clear();
        for (BaseRenderer renderer : renderers) {
            if (renderer instanceof AxisLeftRenderer
                    || renderer instanceof LineChartRenderer
                    || renderer instanceof OrderRenderer
                    || renderer instanceof CurrValueRenderer) {
                mMinMaxRenderer.add(renderer);
            }
            if (renderer instanceof LineChartRenderer
                    || renderer instanceof CurrValueRenderer) {
                mStepLineRenderer.add(renderer);
            }
        }
    }

    public void calculateValue() {
        calculateMinMaxAnim();
        calculateStepLineAnim();

        calculateEndRange();
    }

    /*--------------------------------------------------------------------------------------------*/
    private boolean mNeedCheckMissing = false;
    private MissingDataListener mMissingDataListener;
    private final long[] mMissingDataBuffer = new long[2];

    /**
     * 检测缺失的数据
     */
    public void checkMissingData(int viewLeftIndex) {
        int firstIndex = mDataProvider.realItemFirstIndex();
        if (firstIndex < viewLeftIndex) {
            return;
        }
        if (mNeedCheckMissing && !mDataProvider.isRealEmpty() && mMissingDataListener != null) {
            ITimeData viewLeftData = mDataProvider.getItem(viewLeftIndex);
            if (!viewLeftData.hasData()) {
                long time = viewLeftData.getTime();
                ITimeData realFirstItem = mDataProvider.getRealFirstItem();
                long missingStartTime = time - time % 60;
                long missingEndTime = realFirstItem.getTime();
                if (mMissingDataBuffer[0] != missingStartTime || mMissingDataBuffer[1] != missingEndTime) {
                    mMissingDataBuffer[0] = missingStartTime;
                    mMissingDataBuffer[1] = missingEndTime;
                    mMissingDataListener.missingDataLimit(missingStartTime, missingEndTime);
                }
            } else {
                mMissingDataBuffer[0] = 0;
                mMissingDataBuffer[1] = 0;
            }
        } else {
            mMissingDataBuffer[0] = 0;
            mMissingDataBuffer[1] = 0;
        }
    }

    /**
     * 是否需要检查缺失数据
     */
    public void setNeedCheckMissing(boolean need) {
        mNeedCheckMissing = need;
    }

    public void setMissingDataListener(MissingDataListener listener) {
        mMissingDataListener = listener;
    }

    public interface MissingDataListener {
        /**
         * 缺失数据回调
         *
         * @param startTime 缺失的起始时间
         * @param endTime   缺失的结束时间,注意:endTime这一时刻是有数据的
         */
        void missingDataLimit(long startTime, long endTime);
    }

    /*--------------------------------------------------------------------------------------------*/
    /*--------------------------------------------------------------------------------------------*/
    private ChartAnimator mMinMaxAnimator;

    private void initMinMaxAnimator() {
        mMinMaxAnimator = new ChartAnimator(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                float phase = mMinMaxAnimator.getPhase();
                if (phase == 1) {
                    mMinMaxPhaseBuffer[0] = mTargetMinMax[0];
                    mMinMaxPhaseBuffer[1] = mTargetMinMax[1];
                } else {
                    mMinMaxPhaseBuffer[0] = (mTargetMinMax[0] - mMinMaxBuffer[0]) * phase + mMinMaxBuffer[0];
                    mMinMaxPhaseBuffer[1] = (mTargetMinMax[1] - mMinMaxBuffer[1]) * phase + mMinMaxBuffer[1];
                }
                mBufferScaleValueY = mViewPortHandler.contentHeight() / (mMinMaxPhaseBuffer[1] - mMinMaxPhaseBuffer[0]);
                calculateStepLineAnim();
                for (BaseRenderer baseRenderer : mMinMaxRenderer) {
                    baseRenderer.initStyle();
                    baseRenderer.calculateValue();
                    baseRenderer.postInvalidate();
                }
            }
        });
    }

    private float mBufferScaleValueY = 0;
    private final float[] mMinMaxPhaseBuffer = new float[]{Float.MIN_VALUE, Float.MAX_VALUE};

    private final float[] mMinMaxBuffer = new float[]{Float.MIN_VALUE, Float.MAX_VALUE};
    private final float[] mTargetMinMax = new float[]{Float.MIN_VALUE, Float.MAX_VALUE};

    /**
     * 计算最大值最小值变化
     */
    public void calculateMinMaxAnim() {
        float viewPortMinValue = mViewPortHandler.getViewPortMinValue();
        float viewPortMaxValue = mViewPortHandler.getViewPortMaxValue();
        if (!verifyValue(viewPortMinValue) || !verifyValue(viewPortMaxValue)) {
            if (mMinMaxAnimator.animateIsRunning()) {
                mMinMaxAnimator.animateCancel();
            }
            mMinMaxAnimator.setPhase(1.0f);
            mMinMaxBuffer[0] = Float.MIN_VALUE;
            mMinMaxBuffer[1] = Float.MAX_VALUE;
            mTargetMinMax[0] = Float.MIN_VALUE;
            mTargetMinMax[1] = Float.MAX_VALUE;
            return;
        }

        if (mTargetMinMax[0] != viewPortMinValue || mTargetMinMax[1] != viewPortMaxValue) {
            float tempMin = mTargetMinMax[0];
            float tempMax = mTargetMinMax[1];
            // 保存目标值
            mTargetMinMax[0] = viewPortMinValue;
            mTargetMinMax[1] = viewPortMaxValue;
            if (mMinMaxAnimatorEnable && verifyValue(tempMin) && verifyValue(tempMax)) {
                if (mMinMaxAnimator.animateIsRunning()) {
                    mMinMaxAnimator.animateCancel();
                    // 纠正起始数据
                    float phase = mMinMaxAnimator.getPhase();
                    mMinMaxBuffer[0] = (mTargetMinMax[0] - mMinMaxBuffer[0]) * phase + mMinMaxBuffer[0];
                    mMinMaxBuffer[1] = (mTargetMinMax[1] - mMinMaxBuffer[1]) * phase + mMinMaxBuffer[1];
                } else {
                    // 记录上一个值
                    mMinMaxBuffer[0] = tempMin;
                    mMinMaxBuffer[1] = tempMax;
                }
                mMinMaxAnimator.animate(mAnimatorTime);
            } else {
                mMinMaxPhaseBuffer[0] = mTargetMinMax[0];
                mMinMaxPhaseBuffer[1] = mTargetMinMax[1];
                mBufferScaleValueY = mViewPortHandler.contentHeight() / (mMinMaxPhaseBuffer[1] - mMinMaxPhaseBuffer[0]);
            }
        }
    }

    private boolean verifyValue(float buffer) {
        if (buffer != Float.MIN_VALUE && buffer != Float.MAX_VALUE) {
            return true;
        }
        return false;
    }

    public float getMaxValue() {
        return mMinMaxPhaseBuffer[1];
    }

    public float getMinValue() {
        return mMinMaxPhaseBuffer[0];
    }

    /**
     * 根据索引获取获取X坐标
     */
    public float getCoordinateX(float index) {
        return mViewPortHandler.getCoordinateX(index);
    }

    /**
     * 根据值获取Y坐标
     */
    public float getCoordinateY(float value) {
        return (mMinMaxPhaseBuffer[1] - value) * mBufferScaleValueY + mViewPortHandler.contentTop();
    }

    /*--------------------------------------------------------------------------------------------*/
    /*--------------------------------------------------------------------------------------------*/
    private ChartAnimator mStepAnimator;

    private void initStepAnimator() {
        mStepAnimator = new ChartAnimator(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                calculateStepLineAnim();
                for (BaseRenderer baseRenderer : mStepLineRenderer) {
                    baseRenderer.initStyle();
                    baseRenderer.calculateValue();
                    baseRenderer.postInvalidate();
                }
            }
        });
    }

    private long mRealLastTimeBuffer = 0;

    private final PathMeasure mAllPathMeasure = new PathMeasure();
    private final PathMeasure mEndPathMeasure = new PathMeasure();
    private final Path mAnimatorPath = new Path();
    private final Path mAnimatorEndPath = new Path();

    private final float[] mEndPoint = new float[2];
    private final Path mLinePath = new Path();
    private final Path mShaderPath = new Path();

    private boolean mCurrPointIsOutSide = false;    // 当前点是否在视图外
    private boolean mViewPortHasLine;       // 视图内是否有线

    public void calculateStepLineAnim() {
        mLinePath.reset();
        mShaderPath.reset();
        mAnimatorPath.reset();
        mAnimatorEndPath.reset();
        mViewPortHasLine = false;

        if (mDataProvider.isRealEmpty()) {
            mRealLastTimeBuffer = 0;
            return;
        }

        int startIndex = mViewPortHandler.getStartIndex();
        int stopIndex = mViewPortHandler.getStopIndex();

        ITimeData realLastItem = mDataProvider.getRealLastItem();
        long time = realLastItem.getTime();
        int dataLastIndex = mDataProvider.getIndexForTime(time);
        boolean needAnimator = false;
        if (mRealLastTimeBuffer != time) {
            if (mRealLastTimeBuffer != 0) {
                needAnimator = true;
            }
            mRealLastTimeBuffer = time;
        }

        if (dataLastIndex < startIndex || dataLastIndex > stopIndex) {
            mCurrPointIsOutSide = true;
        } else {
            mCurrPointIsOutSide = false;
            stopIndex = dataLastIndex;
        }

        if (mCurrPointIsOutSide) {
            if (mStepAnimator.animateIsRunning()) {
                mStepAnimator.animateCancel();
                mStepAnimator.setPhase(1.0f);
            }
        } else if (mStepPathAnimatorEnable && needAnimator) {
            if (mStepAnimator.animateIsRunning()) {
                mStepAnimator.animateCancel();
                mStepAnimator.setPhase(0f);
            }
            mStepAnimator.animate(mAnimatorTime);
        }

        // 判断区间是否有值
        int dataFirstIndex = mDataProvider.realItemFirstIndex();
        if (stopIndex <= dataFirstIndex || startIndex >= dataLastIndex || startIndex == stopIndex) {
            mEndPoint[0] = getCoordinateX(dataLastIndex);
            mEndPoint[1] = getCoordinateY(realLastItem.getValue());
            return;
        }
        if (mDataProvider.getRealCount() == 1) {
            mEndPoint[0] = getCoordinateX(dataLastIndex);
            mEndPoint[1] = getCoordinateY(realLastItem.getValue());
            return;
        }

        mViewPortHasLine = true;
        float startPointX = 0;
        boolean moveStartPoint = false;
        for (int index = startIndex; index <= stopIndex; index++) {
            ITimeData data = mDataProvider.getItem(index);
            if (!data.hasData()) {
                continue;
            }
            float currentPointX = getCoordinateX(index);
            float currentPointY = getCoordinateY(data.getValue());
            if (!moveStartPoint) {
                moveStartPoint = true;
                startPointX = currentPointX;
                mAnimatorPath.moveTo(currentPointX, currentPointY);
            }
            mAnimatorPath.lineTo(currentPointX, currentPointY);

            if (index == (stopIndex - 1)) {
                mAnimatorEndPath.moveTo(currentPointX, currentPointY);
            } else if (index == stopIndex) {
                float phase = mStepAnimator.getPhase();
                mAllPathMeasure.setPath(mAnimatorPath, false);
                float allLength = mAllPathMeasure.getLength();

                mAnimatorEndPath.lineTo(currentPointX, currentPointY);
                mEndPathMeasure.setPath(mAnimatorEndPath, false);
                float endLength = mEndPathMeasure.getLength();

                float length = allLength - (1 - phase) * endLength;
                // 硬件加速的BUG,需要lineTo(0,0)避免
                // mLinePath.lineTo(0, 0);
                mAllPathMeasure.getSegment(0, length, mLinePath, true);

                // 硬件加速的BUG,需要lineTo(0,0)避免
                // mShaderPath.lineTo(0, 0);
                mAllPathMeasure.getSegment(0, length, mShaderPath, true);
                if (mCurrPointIsOutSide) {
                    currentPointX = getCoordinateX(dataLastIndex);
                    currentPointY = getCoordinateY(realLastItem.getValue());
                    mEndPoint[0] = currentPointX;
                    mEndPoint[1] = currentPointY;
                } else {
                    mAllPathMeasure.getPosTan(length, mEndPoint, null);
                }
            }
        }
        if (mCurrPointIsOutSide) {
            mShaderPath.lineTo(mViewPortHandler.contentRight(), mViewPortHandler.contentBottom());
        } else {
            mShaderPath.lineTo(mEndPoint[0], mViewPortHandler.contentBottom());
        }
        mShaderPath.lineTo(startPointX, mViewPortHandler.contentBottom());
        mShaderPath.close();
    }

    public Path getLinePath() {
        return mLinePath;
    }

    public Path getShaderPath() {
        return mShaderPath;
    }

    public float[] getCurrValuePoint() {
        return mEndPoint;
    }

    public boolean currPointIsOutSide() {
        return mCurrPointIsOutSide;
    }

    public boolean viewPortHasLine() {
        return mViewPortHasLine;
    }
    /*--------------------------------------------------------------------------------------------*/
    /*--------------------------------------------------------------------------------------------*/
    private final float[] mCoordinateBuffer = new float[2];   // mCoordinateBuffer[0]终点开始位置 mCoordinateBuffer[1]终点结束位置
    private boolean mEndViewIsOutSide = true;
    private boolean mNeedWarn = false;
    private EndRangeListener mEndRangeListener;

    private void calculateEndRange() {
        //TODO 这里需要处理是否自动滑动到最右边
        if (mDataProvider.isRealEmpty()) {
            mCoordinateBuffer[0] = 0;
            mCoordinateBuffer[1] = 0;
            mEndViewIsOutSide = true;
            mNeedWarn = false;
            return;
        }
        long endTime = mDataProvider.getRoundEndTime();
        int startTimeIndex = mDataProvider.getIndexForTime(endTime - 30);
        int viewStopIndex = mViewPortHandler.getStopIndex();

        if (startTimeIndex > viewStopIndex) {
            if (!mEndViewIsOutSide && mEndRangeListener != null) {
                mEndRangeListener.onOutSideChange(true);
            }
            mEndViewIsOutSide = true;
            mCoordinateBuffer[0] = 0;
            mCoordinateBuffer[1] = 0;
            mNeedWarn = false;
            return;
        }
        if (mEndViewIsOutSide && mEndRangeListener != null) {
            mEndRangeListener.onOutSideChange(false);
        }
        mEndViewIsOutSide = false;

        mCoordinateBuffer[0] = mViewPortHandler.getCoordinateX(startTimeIndex);
        mCoordinateBuffer[1] = mViewPortHandler.getCoordinateX(startTimeIndex + 30);

        long time = mDataProvider.getRealLastItem().getTime();
        long warnTime = endTime - 30;
        mNeedWarn = time <= warnTime && time > (warnTime - 2);
    }

    public boolean endViewIsOutSide() {
        return mEndViewIsOutSide;
    }

    public boolean needWarn() {
        return mNeedWarn;
    }

    public float[] getEndViewXPosition() {
        return mCoordinateBuffer;
    }

    public void setEndRangeListener(EndRangeListener listener) {
        mEndRangeListener = listener;
    }

    public interface EndRangeListener {
        void onOutSideChange(boolean isOutSide);
    }
    /*--------------------------------------------------------------------------------------------*/
}
