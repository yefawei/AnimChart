package com.benben.chatlibrary.impl;

/**
 * @日期 : 2018/9/7
 * @描述 :
 */
public interface ITimeData {

    /**
     * 是否有订单数据
     */
    boolean hasOrder();

    AnimOrderData getOrderData();

    void setOrderData(AnimOrderData orderData);

    boolean hasData();

    float getValue();

    long getTime();

}
