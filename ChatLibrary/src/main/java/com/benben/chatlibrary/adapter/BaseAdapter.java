package com.benben.chatlibrary.adapter;

import android.database.DataSetObservable;
import android.database.DataSetObserver;

import com.benben.chatlibrary.impl.IAdapter;
import com.benben.chatlibrary.impl.ITimeData;

/**
 * @日期 : 2018/9/5
 * @描述 :
 */
public abstract class BaseAdapter<T extends ITimeData> implements IAdapter<T> {

    private final DataSetObservable mDataSetObservable = new DataSetObservable();

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        mDataSetObservable.registerObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        mDataSetObservable.unregisterObserver(observer);
    }

    @Override
    public void notifyDataSetChanged() {
        if (isEmpty()) {
            mDataSetObservable.notifyInvalidated();
        } else {
            mDataSetObservable.notifyChanged();
        }
    }

}
