package com.benben.chatlibrary.components;

import android.graphics.Color;
import android.graphics.DashPathEffect;

import com.benben.chatlibrary.utils.Utils;

/**
 * @日期 : 2018/9/6
 * @描述 :
 */
public class YAxis extends AxisBase {

    private AxisLabelForceMode mLabelForceMode = AxisLabelForceMode.SIDE;

    private boolean mForceLabels = false;

    private float mForceLabelsPadding = 0;

    private boolean mDrawFrameBottomline = true;
    private boolean mDrawFrameBottomValue = true;

    private int mFrameGridColor = Color.BLACK;

    private float mFrameStrokeWidth = Utils.convertDpToPixel(1);

    private DashPathEffect mFramePathEffect;


    public AxisLabelForceMode getLabelForceMode() {
        return mLabelForceMode;
    }

    /**
     * true,意味着标签会固定并均匀的分布在轴上
     * fakse,动态计算标签位置,标签数尽可能的接近设置值
     */
    public void setForceLabels(boolean force) {
        mForceLabels = force;
    }

    public boolean isForceLabelsEnabled() {
        return mForceLabels;
    }

    public float getForceLabelsPadding() {
        return mForceLabelsPadding;
    }

    /**
     * mForceLabels为true时生效
     * AxisLabelForceMode.START:从起点偏移padding剩下均分
     * AxisLabelForceMode.SIDE: 两端偏移padding剩下均分
     * AxisLabelForceMode.END:  从底部偏移padding剩下均分
     * @param padding dp
     */
    public void setForceLabelsPadding(float padding) {
        mForceLabelsPadding = Utils.convertDpToPixel(padding);
    }

    public void setLabelForceMode(AxisLabelForceMode mode) {
        mLabelForceMode = mode;
    }

    public boolean isDrawFrameBottomline() {
        return mDrawFrameBottomline;
    }

    public boolean isDrawFrameBottomValue() {
        return mDrawFrameBottomValue;
    }

    public void setFrameGridColor(int color) {
        mFrameGridColor = color;
    }

    public int getFrameGridColor() {
        return mFrameGridColor;
    }

    public void setFrameStrokeWidth(float width) {
        mFrameStrokeWidth = Utils.convertDpToPixel(width);
    }

    public float getFrameStrokeWidth() {
        return mFrameStrokeWidth;
    }

    public DashPathEffect getFramePathEffect() {
        return mFramePathEffect;
    }

    public void setFramePathEffect(DashPathEffect framePathEffect) {
        mFramePathEffect = framePathEffect;
    }

    /**
     * Y轴是否需要偏移量
     */
    public boolean needsOffset() {
        return isEnabled() && isDrawLabelsEnabled() && getLabelPosition() == AxisLabelPosition.OUTSIDE_CHART;
    }

    public enum AxisLabelForceMode {
        START, SIDE, END
    }
}
