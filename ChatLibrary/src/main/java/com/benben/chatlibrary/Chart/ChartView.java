package com.benben.chatlibrary.Chart;

import android.content.Context;
import android.util.AttributeSet;

import com.benben.chatlibrary.renderer.AxisBottomRenderer;
import com.benben.chatlibrary.renderer.AxisLeftRenderer;
import com.benben.chatlibrary.renderer.CurrValueRenderer;
import com.benben.chatlibrary.renderer.EndChartRenderer;
import com.benben.chatlibrary.renderer.LineChartRenderer;
import com.benben.chatlibrary.renderer.OrderRenderer;

/**
 * @日期 : 2018/9/6
 * @描述 :
 */
public class ChartView extends RendererChartView {

    public ChartView(Context context) {
        this(context, null);
    }

    public ChartView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    private void initialize(Context context) {
        AxisLeftRenderer axisLeftRenderer = new AxisLeftRenderer(context, mViewPortHandler, mDataHandler, mValueHandler, mAxisLeft);
        AxisBottomRenderer axisBottomRenderer = new AxisBottomRenderer(context, mViewPortHandler, mDataHandler, mValueHandler, mAxisBottom);
        LineChartRenderer lineChartRenderer = new LineChartRenderer(context, mViewPortHandler, mDataHandler, mValueHandler);
        EndChartRenderer endChartRenderer = new EndChartRenderer(context, mViewPortHandler, mDataHandler, mValueHandler);
        CurrValueRenderer currValueRenderer = new CurrValueRenderer(context, mViewPortHandler, mDataHandler, mValueHandler, mAxisLeft);
        OrderRenderer orderRenderer = new OrderRenderer(context, mViewPortHandler, mDataHandler, mValueHandler);

        addRenderer(axisLeftRenderer);
        addRenderer(axisBottomRenderer);
        addRenderer(lineChartRenderer);
        addRenderer(endChartRenderer);
        addRenderer(currValueRenderer);
        addRenderer(orderRenderer);
    }

}
