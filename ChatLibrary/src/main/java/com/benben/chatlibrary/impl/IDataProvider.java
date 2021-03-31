package com.benben.chatlibrary.impl;

/**
 * @日期 : 2018/9/7
 * @描述 :
 */
public interface IDataProvider {

    boolean currGameHasOrder();

    IOrderData[] getCurrGameOrder();

    int getIndexForTime(long time);

    long getRoundEndTime();

    int getCount();

    ITimeData getItem(int index);

    boolean isRealEmpty();

    int realItemFirstIndex();

    int realItemLastIndex();

    int getRealCount();

    ITimeData getRealItem(int index);

    ITimeData getRealFirstItem();

    ITimeData getRealLastItem();
}
