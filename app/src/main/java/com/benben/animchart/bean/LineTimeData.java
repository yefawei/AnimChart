package com.benben.animchart.bean;

import com.benben.chatlibrary.impl.OrderTimeData;

/**
 * @日期 : 2018/9/7
 * @描述 :
 */
public class LineTimeData extends OrderTimeData {

    public String close_price;
    public long timestamp;

    @Override
    public boolean hasData() {
        return true;
    }

    @Override
    public float getValue() {
        return Float.parseFloat(close_price);
    }

    @Override
    public long getTime() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "LineTimeData{" +
                "close_price='" + close_price + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
