package com.benben.chatlibrary.components;

import android.graphics.Color;
import android.graphics.DashPathEffect;

import com.benben.chatlibrary.formatter.DefaultAxisValueFormatter;
import com.benben.chatlibrary.formatter.IAxisValueFormatter;
import com.benben.chatlibrary.utils.Utils;

/**
 * @日期 : 2018/9/6
 * @描述 :
 */
public abstract class AxisBase extends ComponentBase {

    protected IAxisValueFormatter mAxisValueFormatter;

    protected boolean mDrawLabels = true;

    private AxisLabelPosition mLabelPosition = AxisLabelPosition.INSIDE_CHART;

    private int mLabelCount = 7;

    private int mGridColor = Color.BLACK;

    private float mStrokeWidth = Utils.convertDpToPixel(1);

    private DashPathEffect mPathEffect;


    public void setAxisValueFormatter(IAxisValueFormatter formatter) {
        mAxisValueFormatter = formatter;
    }

    public IAxisValueFormatter getAxisValueFormatter() {
        if (mAxisValueFormatter == null) {
            mAxisValueFormatter = new DefaultAxisValueFormatter();
        }
        return mAxisValueFormatter;
    }

    /**
     * 设置是否绘制标签
     */
    public void setDrawLabels(boolean enabled) {
        mDrawLabels = enabled;
    }

    public boolean isDrawLabelsEnabled() {
        return mDrawLabels;
    }

    public AxisLabelPosition getLabelPosition() {
        return mLabelPosition;
    }

    public void setLabelPosition(AxisLabelPosition position) {
        mLabelPosition = position;
    }

    /**
     * 设置标签数
     */
    public void setLabelCount(int count) {
        mLabelCount = count;
    }

    public int getLabelCount() {
        return mLabelCount;
    }

    public int getGridColor() {
        return mGridColor;
    }

    public void setGridColor(int color) {
        mGridColor = color;
    }

    public float getStrokeWidth() {
        return mStrokeWidth;
    }

    public void setStrokeWidth(float strokeWidth) {
        mStrokeWidth = Utils.convertDpToPixel(strokeWidth);
    }

    public void setPathEffect(DashPathEffect pathEffect) {
        mPathEffect = pathEffect;
    }

    public DashPathEffect getPathEffect() {
        return mPathEffect;
    }

    public enum AxisLabelPosition {
        OUTSIDE_CHART, INSIDE_CHART
    }
}
