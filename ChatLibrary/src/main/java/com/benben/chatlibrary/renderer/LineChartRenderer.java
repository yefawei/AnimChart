package com.benben.chatlibrary.renderer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;

import androidx.annotation.NonNull;

import com.benben.chatlibrary.handler.ValueHandler;
import com.benben.chatlibrary.handler.ViewPortHandler;
import com.benben.chatlibrary.impl.IDataProvider;
import com.benben.chatlibrary.utils.Utils;

/**
 * @日期 : 2018/9/6
 * @描述 : 范围线
 */
public class LineChartRenderer extends BaseRenderer {


    private final Paint mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);        // 画笔
    private final Paint mShaderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);  // 渐变阴影

    private final int[] mShadeColors;

    private final RectF mLimitLineClippingRect = new RectF();
    private final float[] mBuffRect = new float[4];

    public LineChartRenderer(Context context, @NonNull ViewPortHandler viewPortHandler,
                             @NonNull IDataProvider dataProvider, @NonNull ValueHandler valueHandler) {
        super(context, viewPortHandler, dataProvider, valueHandler);
        mLinePaint.setColor(Color.parseColor("#159FFF"));
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(Utils.convertDpToPixel(2));
        mLinePaint.setStrokeJoin(Paint.Join.ROUND);
        mLinePaint.setStrokeCap(Paint.Cap.ROUND);

        mShadeColors = new int[]{
                Color.argb(123, 21, 159, 255),
                Color.argb(0, 21, 159, 255)};
    }


    @Override
    public void calculateValue() {
        if (!mValueHandler.viewPortHasLine()) {
            return;
        }

        mLimitLineClippingRect.set(mViewPortHandler.getContentRect());

        if (mLimitLineClippingRect.left != mBuffRect[0]
                || mLimitLineClippingRect.top != mBuffRect[1]
                || mLimitLineClippingRect.right != mBuffRect[2]
                || mLimitLineClippingRect.bottom != mBuffRect[3]) {

            mBuffRect[0] = mLimitLineClippingRect.left;
            mBuffRect[1] = mLimitLineClippingRect.top;
            mBuffRect[2] = mLimitLineClippingRect.right;
            mBuffRect[3] = mLimitLineClippingRect.bottom;
            LinearGradient mShader = new LinearGradient(mLimitLineClippingRect.left, mLimitLineClippingRect.top, mLimitLineClippingRect.left, mLimitLineClippingRect.bottom, mShadeColors, null, Shader.TileMode.CLAMP);
            mShaderPaint.setShader(mShader);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!mValueHandler.viewPortHasLine()) {
            return;
        }

        int clipRestoreCount = canvas.save();
        canvas.clipRect(mLimitLineClippingRect);

        //  渐变阴影
        canvas.drawPath(mValueHandler.getShaderPath(), mShaderPaint);

        canvas.drawPath(mValueHandler.getLinePath(), mLinePaint);

        canvas.restoreToCount(clipRestoreCount);
    }
}
