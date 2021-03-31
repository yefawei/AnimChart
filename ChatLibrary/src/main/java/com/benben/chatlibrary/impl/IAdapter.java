package com.benben.chatlibrary.impl;

import android.database.DataSetObserver;

/**
 * @日期 : 2018/9/5
 * @描述 :
 */
public interface IAdapter<T extends ITimeData> {

    int getCount();

    T getItem(int position);

    boolean isEmpty();

    /*
     * 注册一个数据观察者
     * @param observer
     */
    void registerDataSetObserver(DataSetObserver observer);

    /**
     * 移除一个数据观察者
     * @param observer 数据观察者
     */
    void unregisterDataSetObserver(DataSetObserver observer);

    /**
     * 当数据发生变化时调用
     */
    void notifyDataSetChanged();
}
