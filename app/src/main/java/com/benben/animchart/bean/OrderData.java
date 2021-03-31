package com.benben.animchart.bean;

import com.benben.chatlibrary.impl.AnimOrderData;

/**
 * @日期 : 2018/9/13
 * @描述 :
 */
public class OrderData extends AnimOrderData {

    public String amount;
    public long timestamp;
    public int type;
    public String close_price;

    @Override
    public long getTime() {
        return timestamp;
    }

    @Override
    public float getValue() {
        return Float.parseFloat(close_price);
    }

    @Override
    public String getAmount() {
        return amount;
    }

    @Override
    public boolean isUp() {
        return type == 1;
    }
}
