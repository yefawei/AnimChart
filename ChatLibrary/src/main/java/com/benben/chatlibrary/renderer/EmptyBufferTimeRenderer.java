package com.benben.chatlibrary.renderer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import androidx.annotation.NonNull;

import com.benben.chatlibrary.handler.ValueHandler;
import com.benben.chatlibrary.handler.ViewPortHandler;
import com.benben.chatlibrary.impl.IDataProvider;
import com.benben.chatlibrary.impl.ITimeData;

/**
 * @日期 : 2018/9/13
 * @描述 : 往左滑动会出现空时间buffer数据的状态
 */
public class EmptyBufferTimeRenderer extends BaseRenderer {

    private final Paint mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);    // 绘制空时间buffer数据
    private final Path mLinePath = new Path();

    public EmptyBufferTimeRenderer(Context context, @NonNull ViewPortHandler viewPortHandler,
                                   @NonNull IDataProvider dataProvider, @NonNull ValueHandler valueHandler) {
        super(context, viewPortHandler, dataProvider, valueHandler);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setColor(Color.argb(51, 123, 135, 164));
        mLinePaint.setPathEffect(new DashPathEffect(new float[]{10f, 10f}, 0));
    }

    private final RectF mClippingRect = new RectF();

    @Override
    public void calculateValue() {
        if (mDataProvider.getCount() == 0) {
            return;
        }

        if (mDataProvider.isRealEmpty()) {
            return;
        }

        mClippingRect.setEmpty();


        int startIndex = mViewPortHandler.getStartIndex();
        int stopIndex = mViewPortHandler.getStopIndex();
        ITimeData dataFromIndex = mDataProvider.getItem(stopIndex);

        ITimeData realFirstItem = mDataProvider.getRealFirstItem();
        int realFirstItemIndex = (int) (stopIndex + (realFirstItem.getTime() - dataFromIndex.getTime()));

        if (realFirstItemIndex >= stopIndex) {
            mClippingRect.set(mViewPortHandler.getContentRect());
        } else if (realFirstItemIndex < stopIndex && realFirstItemIndex > startIndex) {
            mClippingRect.set(mViewPortHandler.getContentRect());
            mClippingRect.right = mViewPortHandler.getCoordinateX(realFirstItemIndex);
        }
        mLinePath.reset();
        if (mClippingRect.width() > mClippingRect.height()) {
            mLinePath.moveTo(mClippingRect.left, mClippingRect.top);
            mLinePath.lineTo(mClippingRect.right, mClippingRect.top + mClippingRect.width());
        } else {
            mLinePath.moveTo(mClippingRect.left, mClippingRect.top);
            mLinePath.lineTo(mClippingRect.left + mClippingRect.height(), mClippingRect.bottom);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mDataProvider.getCount() == 0) {
            //FIXME 绘制空视图
            return;
        }
        if (mClippingRect.isEmpty()) {
            return;
        }
        int clipRestoreCount = canvas.save();
        canvas.clipRect(mClippingRect);
        canvas.drawPath(mLinePath, mLinePaint);
        canvas.restoreToCount(clipRestoreCount);
    }
}
