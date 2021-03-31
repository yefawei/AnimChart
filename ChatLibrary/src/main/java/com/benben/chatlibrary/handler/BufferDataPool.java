package com.benben.chatlibrary.handler;

import com.benben.chatlibrary.impl.AnimOrderData;
import com.benben.chatlibrary.impl.ITimeData;

/**
 * @日期 : 2018/9/13
 * @描述 :
 */
public class BufferDataPool {

    private final int size;
    private int id;
    private final BufferTimeData[] pools;

    public BufferDataPool(int size) {
        this.size = size;
        pools = new BufferTimeData[size];
        for (int i = 0; i < size; i++) {
            pools[i] = new BufferTimeData(i);
        }
    }

    public BufferTimeData get() {
        if (id == size) {
            id = 0;
        }
        return pools[id++];
    }

    public static synchronized BufferDataPool create(int size){
        return new BufferDataPool(size);
    }


    public static class BufferTimeData implements ITimeData {
        public int id;
        private long time;

        public BufferTimeData(int id) {
            this.id = id;
        }

        public void setTime(long time) {
            this.time = time;
        }

        @Override
        public boolean hasOrder() {
            return false;
        }

        @Override
        public AnimOrderData getOrderData() {
            return null;
        }

        @Override
        public void setOrderData(AnimOrderData orderData) {

        }

        @Override
        public boolean hasData() {
            return false;
        }

        @Override
        public float getValue() {
            return 0;
        }

        @Override
        public long getTime() {
            return time;
        }
    }
}
