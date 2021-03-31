package com.benben.chatlibrary.impl;

/**
 * @日期 : 2018/9/13
 * @描述 :
 */
public abstract class OrderTimeData implements ITimeData {

    private AnimOrderData mOrderData;

    @Override
    public boolean hasOrder() {
        return mOrderData != null;
    }

    @Override
    public AnimOrderData getOrderData() {
        return mOrderData;
    }

    @Override
    public void setOrderData(AnimOrderData orderData) {
        mOrderData = orderData;
    }
}
