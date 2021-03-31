package com.benben.chatlibrary.renderer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import androidx.annotation.NonNull;

import com.benben.chatlibrary.components.XAxis;
import com.benben.chatlibrary.handler.ValueHandler;
import com.benben.chatlibrary.handler.ViewPortHandler;
import com.benben.chatlibrary.impl.IDataProvider;
import com.benben.chatlibrary.utils.TimeUtil;

/**
 * @日期 : 2018/9/7
 * @描述 : 底部标尺
 */
public class AxisBottomRenderer extends BaseRenderer {

    private final XAxis mXAxis;

    private final Paint mGridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final TextPaint mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);

    public int mEntriesCount = 0;
    public long[] mEntries = new long[]{};

    private final Path mRenderGridLinesPath = new Path();

    public AxisBottomRenderer(Context context, @NonNull ViewPortHandler viewPortHandler,
                              @NonNull IDataProvider dataProvider, @NonNull ValueHandler valueHandler, XAxis axisBottom) {
        super(context, viewPortHandler, dataProvider, valueHandler);
        mXAxis = axisBottom;
        mGridPaint.setStyle(Paint.Style.STROKE);
    }

    private float mYOffset;
    private final RectF mGridClippingRect = new RectF();

    @Override
    public void initStyle() {
        mTextPaint.setColor(mXAxis.getTextColor());
        mTextPaint.setTextSize(mXAxis.getTextSize());
        mTextPaint.setTypeface(mXAxis.getTypeface());

        mGridPaint.setColor(mXAxis.getGridColor());
        mGridPaint.setStrokeWidth(mXAxis.getStrokeWidth());
        mGridPaint.setPathEffect(mXAxis.getPathEffect());
    }

    @Override
    public void calculateValue() {
        if (mDataProvider.getCount() == 0) {
            return;
        }

        if (!mXAxis.isEnabled()) {
            return;
        }
        mGridClippingRect.set(mViewPortHandler.getContentRect());
        if (mXAxis.needsOffset()) {
            mYOffset = mXAxis.getYOffset();
            mGridClippingRect.bottom = mGridClippingRect.bottom + mYOffset;
        }

        int startIndex = mViewPortHandler.getStartIndex();
        int stopIndex = mViewPortHandler.getStopIndex();

        long startTime = mDataProvider.getItem(startIndex).getTime();
        long endTime = mDataProvider.getItem(stopIndex).getTime();

        long timeInterval = TimeUtil.roundTimeInterval(startTime, endTime);
        long intervalStartTime = TimeUtil.intervalStartTime(startTime, timeInterval);
        long intervalEndTime = TimeUtil.intervalStartTime(endTime, timeInterval) + timeInterval;

        int n = (int) ((intervalEndTime - intervalStartTime) / timeInterval + 1);

        if (mEntries.length < n) {
            mEntries = new long[n];
        }
        for (int i = 0; i < n; i++) {
            mEntries[i] = intervalStartTime;
            intervalStartTime += timeInterval;
        }
        mEntriesCount = n;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mDataProvider.getCount() == 0) {
            return;
        }
        if (!mXAxis.isEnabled()) {
            return;
        }

        if (mXAxis.isDrawLabelsEnabled()) {
            int clipRestoreCount = canvas.save();
            canvas.clipRect(mGridClippingRect);
            for (int i = 0; i < mEntriesCount; i++) {
                long time = mEntries[i];
                int index = mDataProvider.getIndexForTime(time);
                float coordinateX = mViewPortHandler.getCoordinateX(index);
                mRenderGridLinesPath.reset();
                mRenderGridLinesPath.moveTo(coordinateX, mGridClippingRect.top - mYOffset);
                mRenderGridLinesPath.lineTo(coordinateX, mGridClippingRect.bottom - mYOffset);
                canvas.drawPath(mRenderGridLinesPath, mGridPaint);

                int save = canvas.save();
                String text = mXAxis.getAxisValueFormatter().getFormattedValue(time * 1000 + "");
                int textWidth = (int) mTextPaint.measureText(text);
                StaticLayout textLayout = new StaticLayout(text, mTextPaint, textWidth,
                        Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);

                int height = textLayout.getHeight();
                float y = mGridClippingRect.bottom - mYOffset + (mYOffset - height)/2;
                canvas.translate(coordinateX - textWidth / 2.0f, y);
                textLayout.draw(canvas);
                canvas.restoreToCount(save);
            }
            canvas.restoreToCount(clipRestoreCount);
        }
    }
}
