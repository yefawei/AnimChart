package com.benben.chatlibrary.components;

/**
 * @日期 : 2018/9/6
 * @描述 :
 */
public class XAxis extends AxisBase {

    /**
     * X轴是否需要偏移量
     */
    public boolean needsOffset() {
        return isEnabled() && isDrawLabelsEnabled() && getLabelPosition() == AxisLabelPosition.OUTSIDE_CHART;
    }

}
