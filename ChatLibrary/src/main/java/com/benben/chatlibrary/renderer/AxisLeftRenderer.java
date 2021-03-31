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

import com.benben.chatlibrary.components.YAxis;
import com.benben.chatlibrary.handler.ValueHandler;
import com.benben.chatlibrary.handler.ViewPortHandler;
import com.benben.chatlibrary.impl.IDataProvider;
import com.benben.chatlibrary.utils.Utils;

/**
 * @日期 : 2018/9/7
 * @描述 : 左标尺
 */
public class AxisLeftRenderer extends BaseRenderer {

    private final YAxis mYAxis;
    private final Paint mGridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mFrameLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final TextPaint mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);

    public int mEntriesCount = 0;
    public float[] mEntries = new float[]{};

    private final float mTextLeftLimit;
    private final float mTextBottomLimit;

    private final Path mRenderGridLinesPath = new Path();

    public AxisLeftRenderer(Context context, @NonNull ViewPortHandler viewPortHandler,
                            @NonNull IDataProvider dataProvider, @NonNull ValueHandler valueHandler, YAxis axisLeft) {
        super(context, viewPortHandler, dataProvider, valueHandler);
        mYAxis = axisLeft;
        mGridPaint.setStyle(Paint.Style.STROKE);
        mFrameLinePaint.setStyle(Paint.Style.STROKE);

        mTextLeftLimit = Utils.convertDpToPixel(5);
        mTextBottomLimit = Utils.convertDpToPixel(2);
    }

    private final RectF mGridClippingRect = new RectF();

    @Override
    public void initStyle() {
        mTextPaint.setColor(mYAxis.getTextColor());
        mTextPaint.setTextSize(mYAxis.getTextSize());
        mTextPaint.setTypeface(mYAxis.getTypeface());

        mGridPaint.setColor(mYAxis.getGridColor());
        mGridPaint.setStrokeWidth(mYAxis.getStrokeWidth());
        mGridPaint.setPathEffect(mYAxis.getPathEffect());

        mFrameLinePaint.setColor(mYAxis.getFrameGridColor());
        mFrameLinePaint.setStrokeWidth(mYAxis.getFrameStrokeWidth());
        mFrameLinePaint.setPathEffect(mYAxis.getFramePathEffect());
    }

    @Override
    public void calculateValue() {
        if (mDataProvider.getCount() == 0) {
            return;
        }

        if (!mYAxis.isEnabled()) {
            return;
        }

        mGridClippingRect.set(mViewPortHandler.getContentRect());

        float min = mValueHandler.getMinValue();
        float max = mValueHandler.getMaxValue();


        if (!mYAxis.isForceLabelsEnabled()) {
            int labelCount = mYAxis.getLabelCount();
            double range = Math.abs(max - min);
            int n = 0;
            double rawInterval = range / labelCount;
            double interval = Utils.roundToNextSignificant(rawInterval);

            double first = Math.ceil(min / interval) * interval;
            double last = Utils.nextUp(Math.floor(max / interval) * interval);

            double f;
            int i;
            for (f = first; f <= last; f += interval) {
                ++n;
            }
            if (mEntries.length < n) {
                mEntries = new float[n];
            }

            for (f = first, i = 0; i < n; f += interval, ++i) {
                mEntries[i] = (float) f;
            }
            mEntriesCount = n;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mDataProvider.getCount() == 0) {
            return;
        }

        if (!mYAxis.isEnabled()) {
            return;
        }

        int clipRestoreCount = canvas.save();
        canvas.clipRect(mGridClippingRect);

        Path gridLinePath = mRenderGridLinesPath;
        float left = mGridClippingRect.left;
        float right = mGridClippingRect.right;
        if (mYAxis.isDrawLabelsEnabled()) {
            if (mYAxis.isForceLabelsEnabled()) {
                float top = mGridClippingRect.top;
                int labelCount = mYAxis.getLabelCount();
                float minValue = mValueHandler.getMinValue();
                float maxValue = mValueHandler.getMaxValue();
                float range = (maxValue - minValue) / (labelCount +1);
                float interval = mGridClippingRect.height() / (labelCount + 1);
                for (int i = 1; i <= labelCount; i++) {
                    gridLinePath.reset();
                    float y = top + interval*i;
                    gridLinePath.moveTo(left,y);
                    gridLinePath.lineTo(right,y);
                    canvas.drawPath(gridLinePath, mGridPaint);

                    int save = canvas.save();
                    float value = (labelCount + 1 - i) * range + minValue;
                    String text = mYAxis.getAxisValueFormatter().getFormattedValue(value + "");
                    StaticLayout textLayout = new StaticLayout(text, mTextPaint, (int) mTextPaint.measureText(text),
                            Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                    canvas.translate(mTextLeftLimit, y - textLayout.getHeight() - mTextBottomLimit);
                    textLayout.draw(canvas);
                    canvas.restoreToCount(save);
                }

            } else {
                for (int i = 0; i < mEntriesCount; i++) {
                    gridLinePath.reset();
                    float y = mValueHandler.getCoordinateY((float) mEntries[i]);
                    gridLinePath.moveTo(left, y);
                    gridLinePath.lineTo(right, y);
                    canvas.drawPath(gridLinePath, mGridPaint);

                    int save = canvas.save();
                    String text = mYAxis.getAxisValueFormatter().getFormattedValue(mEntries[i] + "");
                    StaticLayout textLayout = new StaticLayout(text, mTextPaint, (int) mTextPaint.measureText(text),
                            Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                    canvas.translate(mTextLeftLimit, y - textLayout.getHeight() - mTextBottomLimit);
                    textLayout.draw(canvas);
                    canvas.restoreToCount(save);
                }
            }
        }

        float halfStrokeWidth = mFrameLinePaint.getStrokeWidth()/2;
        if (mYAxis.isDrawFrameBottomline()) {
            float bottom = mGridClippingRect.bottom;
            gridLinePath.reset();
            gridLinePath.moveTo(left, bottom - halfStrokeWidth);
            gridLinePath.lineTo(right, bottom - halfStrokeWidth);
            canvas.drawPath(gridLinePath, mFrameLinePaint);
        }

        if (mYAxis.isDrawFrameBottomValue()) {
            int save = canvas.save();
            float bottom = mGridClippingRect.bottom;
            String text =  mYAxis.getAxisValueFormatter().getFormattedValue(mValueHandler.getMinValue() + "");
            StaticLayout textLayout = new StaticLayout(text, mTextPaint, (int) mTextPaint.measureText(text),
                    Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            canvas.translate( mTextLeftLimit, bottom - textLayout.getHeight() - mTextBottomLimit - halfStrokeWidth);
            textLayout.draw(canvas);
            canvas.restoreToCount(save);
        }
        canvas.restoreToCount(clipRestoreCount);
    }
}
