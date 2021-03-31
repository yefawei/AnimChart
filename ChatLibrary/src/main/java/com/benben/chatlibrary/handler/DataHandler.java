package com.benben.chatlibrary.handler;

import android.database.DataSetObserver;

import androidx.annotation.NonNull;

import com.benben.chatlibrary.impl.IAdapter;
import com.benben.chatlibrary.impl.IDataProvider;
import com.benben.chatlibrary.impl.IOrderData;
import com.benben.chatlibrary.impl.ITimeData;

import java.util.Arrays;

/**
 * @日期 : 2018/9/15
 * @描述 : 数据处理
 */
public class DataHandler<T extends ITimeData> implements IDataProvider {

    private static final long EMPTY_TIME = -1;

    private IAdapter mAdapter;

    private boolean mCanExtendStartTime = true; // 允许在扩展开始时间
    private long mStartTime = EMPTY_TIME;    // 开始时间也永远卡在分钟点
    private long mEndTime = EMPTY_TIME;      // 结束时间永远卡在分钟点
    private long mLastCheckGameTime = EMPTY_TIME;//上一局游戏检测的时间

    private long mDataStartTime = EMPTY_TIME;
    private long mDataEndTime = EMPTY_TIME;

    private boolean mHasOrder = false;
    private final IOrderData[] mOrderData = new IOrderData[60];

    private final BufferDataPool mPool;

    private GameListener mGameListener;

    private final DataChangeListener mDataChangeListener;

    public DataHandler(@NonNull DataChangeListener listener) {
        mDataChangeListener = listener;
        mPool = new BufferDataPool(10);
    }

    @Override
    public boolean currGameHasOrder() {
        return mHasOrder;
    }

    @Override
    public IOrderData[] getCurrGameOrder() {
        return mOrderData;
    }

    @Override
    public int getIndexForTime(long time) {
        if (mStartTime == EMPTY_TIME || time < mStartTime || time > mEndTime) return -1;
        return (int) (time - mStartTime);
    }

    @Override
    public long getRoundEndTime() {
        return mEndTime;
    }

    @Override
    public int getCount() {
        if (mStartTime == EMPTY_TIME) return 0;
        return (int) ((mEndTime - mStartTime) + 1);
    }

    @Override
    public T getItem(int index) {
        if (mDataStartTime == EMPTY_TIME) {
            BufferDataPool.BufferTimeData bufferTimeData = mPool.get();
            bufferTimeData.setTime(mStartTime + index);
            return (T) bufferTimeData;
        }

        int startLimit = (int) (mDataStartTime - mStartTime);
        if (index < startLimit) {
            BufferDataPool.BufferTimeData bufferTimeData = mPool.get();
            bufferTimeData.setTime(mStartTime + index);
            return (T) bufferTimeData;
        }

        int count = getRealCount();
        if (index >= (startLimit + count)) {
            BufferDataPool.BufferTimeData bufferTimeData = mPool.get();
            bufferTimeData.setTime(mStartTime + index);
            return (T) bufferTimeData;
        }

        int realIndex = index - startLimit;
        return getRealItem(realIndex);
    }

    public void setCanExtendStartTime(boolean enable) {
        mCanExtendStartTime = enable;
    }

    /**
     * 将起始时间向前扩展,最终结果为原来时间的两倍
     */
    public synchronized void extendStartTime() {
        if (mStartTime != EMPTY_TIME && mCanExtendStartTime) {
            mStartTime = mStartTime - (mEndTime - mStartTime);
            notifyChanged();
        }
    }

    @Override
    public boolean isRealEmpty() {
        return mAdapter == null || mAdapter.isEmpty();
    }

    @Override
    public int realItemFirstIndex() {
        if (mDataStartTime == EMPTY_TIME) {
            return -1;
        }

        return (int) (mDataStartTime - mStartTime);
    }

    @Override
    public int realItemLastIndex() {
        if (mDataEndTime == EMPTY_TIME) {
            return -1;
        }
        return (int) (mDataEndTime - mStartTime);
    }

    @Override
    public int getRealCount() {
        return mAdapter == null ? 0 : mAdapter.getCount();
    }

    @Override
    public T getRealItem(int index) {
        return (T) mAdapter.getItem(index);
    }

    @Override
    public T getRealFirstItem() {
        return (T) mAdapter.getItem(0);
    }

    @Override
    public T getRealLastItem() {
        return (T) mAdapter.getItem(mAdapter.getCount() - 1);
    }

    private boolean mIsWaiting = false;

    public void nextGame() {
        if (mIsWaiting) {
            mIsWaiting = false;
            notifyChanged();
            if (mGameListener != null) {
                mGameListener.nextGame();
            }
        }
    }

    public boolean isWaiting() {
        return mIsWaiting;
    }

    /**
     * 检验数据
     */
    private void verifyData() {
        ITimeData firstData = mAdapter.getItem(0);
        long firstDataTime = firstData.getTime();
        this.mDataStartTime = firstDataTime;
        long startTime = firstDataTime - firstDataTime % 60;
        if (mStartTime == EMPTY_TIME || this.mStartTime > startTime) {
            this.mStartTime = startTime;
        }

        ITimeData lastData = mAdapter.getItem(mAdapter.getCount() - 1);
        long lastDatatime = lastData.getTime();
        this.mDataEndTime = lastDatatime;

        //FIXME 在特定的时间比如delivery==30时已经判断过了,此时有一笔订单,delivery==30又会重新判断一次,会出现这一刻没有订单和有订单的两重判断
        long delivery = lastDatatime % 60;
        if (delivery == 0 && mIsWaiting) {
            // 到达出结果的那一刻,开始下一局
            mIsWaiting = false;
            this.mEndTime = lastDatatime + 60;
            if (mGameListener != null) {
                mGameListener.nextGame();
            }
        } else if (delivery >= 30) {
            long lastCheckGameTime = lastDatatime - delivery - 30;
            if (mLastCheckGameTime == lastCheckGameTime) {
                if (mIsWaiting) {
                    this.mEndTime = lastDatatime - delivery + 60;
                } else {
                    this.mEndTime = lastDatatime - delivery + 120;
                }
                return;
            }

            mLastCheckGameTime = lastCheckGameTime;
            // 需要判断当前局是否有下单
            long currGameEndTime = lastDatatime - delivery + 60;
            int count = mAdapter.getCount();
            int reverseTime = (int) (currGameEndTime - 90);
            int startIndex;
            int interval = (int) (reverseTime - mDataStartTime);
            if (interval >= 0) {
                startIndex = (int) (reverseTime - mDataStartTime) + 1;
            } else {
                startIndex = 0;
            }
            int endIndex;
            interval = (int) (currGameEndTime - mDataEndTime);
            if (interval < 30) {
                endIndex = count - (30 - interval) - 1;
            } else {
                endIndex = count - 1;
            }

            boolean has = false;
            for (int index = startIndex; index <= endIndex; index++) {
                ITimeData item = mAdapter.getItem(index);
                if (item.hasOrder()) {
                    has = true;
                    break;
                }
            }

            if (has) {
                mIsWaiting = true;
                this.mEndTime = lastDatatime - delivery + 60;
                if (mGameListener != null) {
                    mGameListener.waiting();
                }
            } else {
                // 当局没有下单,直接下一局
                this.mEndTime = lastDatatime - delivery + 120;
                if (mGameListener != null) {
                    mGameListener.nextGame();
                }
            }
        } else {
            this.mEndTime = lastDatatime - delivery + 60;
        }
    }

    private void addCurrGameOrder() {
        int count = mAdapter.getCount();
        int reverseTime = (int) (mEndTime - 90);
        int startIndex;
        int interval = (int) (reverseTime - mDataStartTime);
        if (interval >= 0) {
            startIndex = (int) (reverseTime - mDataStartTime) + 1;
        } else {
            startIndex = 0;
        }
        int endIndex;
        interval = (int) (mEndTime - mDataEndTime);
        if (interval < 30) {
            endIndex = count - (30 - interval) - 1;
        } else {
            endIndex = count - 1;
        }
        int index = startIndex;
        mHasOrder = false;
        for (int i = 0; i < mOrderData.length; i++) {
            if (index > endIndex) {
                mOrderData[i] = null;
            } else {
                ITimeData item = mAdapter.getItem(index);
                if (item.hasOrder()) {
                    mHasOrder = true;
                    mOrderData[i] = item.getOrderData();
                } else {
                    mOrderData[i] = null;
                }
            }
            index++;
        }
    }

    private void clearOrderBuffer() {
        Arrays.fill(mOrderData, null);
        mHasOrder = false;
    }

    private void notifyChanged() {
        if (mAdapter != null) {
            if (mAdapter.isEmpty()) {
                mDataStartTime = EMPTY_TIME;
                mDataEndTime = EMPTY_TIME;
                mLastCheckGameTime = EMPTY_TIME;
                if (mStartTime == EMPTY_TIME) {
                    long currTime = System.currentTimeMillis() / 1000;
                    currTime -= currTime % 60;
                    this.mStartTime = currTime;
                    this.mEndTime = currTime + 180;
                }
                clearOrderBuffer();
            } else {
                verifyData();
                addCurrGameOrder();
            }
        } else {
            mStartTime = EMPTY_TIME;
            mEndTime = EMPTY_TIME;
            mLastCheckGameTime = EMPTY_TIME;
            mDataStartTime = EMPTY_TIME;
            mDataEndTime = EMPTY_TIME;
            mIsWaiting = false;
            clearOrderBuffer();
        }
        mDataChangeListener.notifyDataChanged();
    }

    public IAdapter getAdapter() {
        return mAdapter;
    }

    public void setAdapter(IAdapter adapter) {
        if (mAdapter != null) {
            mAdapter.unregisterDataSetObserver(mDataSetObserver);
        }
        mAdapter = adapter;
        if (mAdapter != null) {
            mAdapter.registerDataSetObserver(mDataSetObserver);
        }
        notifyChanged();
    }

    private final DataSetObserver mDataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            notifyChanged();
        }

        @Override
        public void onInvalidated() {
            notifyChanged();
        }
    };

    public void setGameListener(GameListener listener) {
        this.mGameListener = listener;
    }

    public interface DataChangeListener {
        void notifyDataChanged();
    }

    public interface GameListener {
        /**
         * 处于等待结果的状态
         */
        void waiting();

        /**
         * 下一局
         */
        void nextGame();
    }

}
