package com.benben.animchart.adapter;

import com.benben.animchart.bean.LineTimeData;
import com.benben.chatlibrary.adapter.BaseAdapter;
import com.benben.chatlibrary.impl.AnimOrderData;

import java.util.List;

/**
 * @日期 : 2018/9/7
 * @描述 :
 */
public class LineAdapter extends BaseAdapter<LineTimeData> {

    List<LineTimeData> mLineData;

    public LineAdapter(List<LineTimeData> data) {
        mLineData = data;
    }

    public void addData(LineTimeData lineData) {
        mLineData.add(lineData);
        notifyDataSetChanged();
    }

    public void addMissingData(List<LineTimeData> lineData) {
        mLineData.addAll(0,lineData);
        notifyDataSetChanged();
    }

    public void clearData() {
        mLineData.clear();
        notifyDataSetChanged();
    }

    public void addOrder(AnimOrderData orderData) {
        LineTimeData LineTimeData = mLineData.get(0);
        int index = (int) (orderData.getTime() - LineTimeData.timestamp);
        if (index < 0 || index >= mLineData.size()) return;
        mLineData.get(index).setOrderData(orderData);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mLineData == null ? 0: mLineData.size();
    }

    @Override
    public LineTimeData getItem(int position) {
        return mLineData.get(position);
    }

    @Override
    public boolean isEmpty() {
        return mLineData.isEmpty();
    }
}
