package com.benben.chatlibrary.Chart;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.benben.chatlibrary.components.XAxis;
import com.benben.chatlibrary.components.YAxis;
import com.benben.chatlibrary.renderer.BaseRenderer;

import java.util.ArrayList;
import java.util.List;

/**
 * @日期 : 2018/9/5
 * @描述 : 交互式View
 */
public abstract class RendererChartView extends ScrollAndScaleView {

    protected YAxis mAxisLeft;
    protected XAxis mAxisBottom;

    private final List<BaseRenderer> mRenderers = new ArrayList<>();

    public RendererChartView(Context context) {
        this(context, null);
    }

    public RendererChartView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RendererChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mAxisLeft = new YAxis();
        mAxisBottom = new XAxis();
    }

    @Override
    @Deprecated
    public void addView(View child) {

    }

    @Override
    @Deprecated
    public void addView(View child, int width, int height) {

    }

    @Override
    @Deprecated
    public void addView(View child, int index) {

    }

    @Override
    @Deprecated
    public void addView(View child, ViewGroup.LayoutParams params) {

    }

    @Override
    @Deprecated
    public void addView(View child, int index, ViewGroup.LayoutParams params) {

    }

    @Override
    public int getRendererCount() {
        return mRenderers.size();
    }

    @Override
    public BaseRenderer getRenderer(int index) {
        return mRenderers.get(index);
    }

    @Override
    public void addRenderer(BaseRenderer renderer) {
        mRenderers.add(renderer);
        mValueHandler.initRenderer(mRenderers);
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        super.addView((View) renderer, -1, layoutParams);
    }

    @Override
    public void addRenderer(BaseRenderer renderer, int index) {
        mRenderers.add(renderer);
        mValueHandler.initRenderer(mRenderers);
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        super.addView((View) renderer, index, layoutParams);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!mViewPortHandler.hasChartDimens()) {
            return;
        }

        calculateOffsets();
        mViewPortHandler.calculateValue();
        mValueHandler.checkMissingData(mViewPortHandler.getStartIndex());
        mValueHandler.calculateValue();
        for (BaseRenderer renderer : mRenderers) {
            renderer.initStyle();
            renderer.calculateValue();
            renderer.invalidate();
        }
    }

    protected void calculateOffsets() {
        float offsetLeft = 0f, offsetBottom = 0f;
        if (mAxisLeft.needsOffset()) {
            offsetLeft += mAxisLeft.getXOffset();
        }

        if (mAxisBottom.needsOffset()) {
            offsetBottom += mAxisBottom.getYOffset();
        }
        mViewPortHandler.restrainViewPort(offsetLeft, 0, 0, offsetBottom);
    }

    @Override
    public void onLeftSide() {
        //滑动到最左边了,对时间进行扩展
        mDataHandler.extendStartTime();
    }

    @Override
    public void onRightSide() {

    }

    public YAxis getAxisLeft() {
        return mAxisLeft;
    }

    public XAxis getAxisBottom() {
        return mAxisBottom;
    }

}
