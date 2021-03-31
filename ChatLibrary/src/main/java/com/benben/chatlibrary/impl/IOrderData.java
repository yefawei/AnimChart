package com.benben.chatlibrary.impl;

/**
 * @日期 : 2018/9/13
 * @描述 : 订单数据
 */
public interface IOrderData {

    boolean AnimIsEnd();

    void playerAnim();

    boolean animIsRunning();

    float getAnimPhase();

    long getTime();

    float getValue();

    /**
     * 购买的数量
     */
    String getAmount();

    /**
     * 是否是涨
     */
    boolean isUp();
}
