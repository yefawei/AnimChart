package com.benben.chatlibrary.renderer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import androidx.annotation.NonNull;

import com.benben.chatlibrary.components.YAxis;
import com.benben.chatlibrary.handler.ValueHandler;
import com.benben.chatlibrary.handler.ViewPortHandler;
import com.benben.chatlibrary.impl.IDataProvider;
import com.benben.chatlibrary.utils.Utils;

/**
 * @日期 : 2018/9/13
 * @描述 : 当前值渲染
 */
public class CurrValueRenderer extends BaseRenderer {

    private final Interpolator mInterpolator = new AccelerateDecelerateInterpolator();

    private final YAxis mYAxis;

    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final TextPaint mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);

    private final RectF mRoundRect = new RectF();

    private final float mWhiteCircleRadius;
    private final float mBlueCircleRadius;
    private final float mAnimCircleRadius;
    private final float mRound;
    private final float mRoundHalfHeight;

    private final int mBlueColor;
    private final int mAlphaBlueColor;

    private final float mTextLimit;

    public CurrValueRenderer(Context context, @NonNull ViewPortHandler viewPortHandler,
                             @NonNull IDataProvider dataProvider, @NonNull ValueHandler valueHandler, YAxis axisLeft) {
        super(context, viewPortHandler, dataProvider, valueHandler);
        mYAxis = axisLeft;
        mBlueColor = Color.argb(255, 21, 159, 255);
        mAlphaBlueColor = Color.argb(50, 21, 159, 255);

        mPaint.setStrokeWidth(Utils.convertDpToPixel(1));
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextSize(Utils.convertDpToPixel(12));

        mWhiteCircleRadius = Utils.convertDpToPixel(1.5f);
        mBlueCircleRadius = Utils.convertDpToPixel(3.5f);
        mAnimCircleRadius = Utils.convertDpToPixel(14f);
        mRound = Utils.convertDpToPixel(4);
        mRoundHalfHeight = Utils.convertDpToPixel(8);

        mTextLimit = Utils.convertDpToPixel(9);
    }

    private final RectF mClippingRect = new RectF();

    @Override
    public void calculateValue() {
        if (mDataProvider.isRealEmpty()) {
            return;
        }
        mClippingRect.set(mViewPortHandler.getContentRect());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mDataProvider.isRealEmpty()) {
            return;
        }

        float[] point = mValueHandler.getCurrValuePoint();

        int clipRestoreCount = canvas.save();
        canvas.clipRect((int) mClippingRect.left, (int) (point[1] - mAnimCircleRadius), (int) mClippingRect.right, (int) (point[1] + mAnimCircleRadius));

        String text = mYAxis.getAxisValueFormatter().getFormattedValue(mDataProvider.getRealLastItem().getValue() + "");
        StaticLayout textLayout = new StaticLayout(text, mTextPaint, (int) mTextPaint.measureText(text),
                Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);

        float halfStrokeWidth = mPaint.getStrokeWidth()/2;

        mRoundRect.set(mClippingRect.left + halfStrokeWidth, point[1] - mRoundHalfHeight,
                mClippingRect.left + textLayout.getWidth() + mTextLimit*2 + halfStrokeWidth, point[1] + mRoundHalfHeight);

        //绘制圆角矩形
        mPaint.setColor(mAlphaBlueColor);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(mRoundRect, mRound, mRound, mPaint);
        mPaint.setColor(mBlueColor);
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawRoundRect(mRoundRect, mRound, mRound, mPaint);

        //绘制横线
        canvas.drawLine(mRoundRect.right, point[1], mClippingRect.right, point[1], mPaint);

        if (!mValueHandler.currPointIsOutSide()) {
            //绘制动画点
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(Color.WHITE);
            float precent = mInterpolator.getInterpolation(System.currentTimeMillis() % 2000 / 2.0f / 1000.0f);
            if (precent > 0.5f) {
                precent = (1 - precent) * 2;
            } else {
                precent = precent * 2;
            }
            mPaint.setAlpha((int) (51 * precent));
            canvas.drawCircle(point[0], point[1], mAnimCircleRadius * precent, mPaint);
            mPaint.setAlpha(255);
            mPaint.setColor(mBlueColor);
            canvas.drawCircle(point[0], point[1], mBlueCircleRadius, mPaint);
            mPaint.setColor(Color.WHITE);
            canvas.drawCircle(point[0], point[1], mWhiteCircleRadius, mPaint);
        }

        //绘制当前金额
        int save = canvas.save();
        float limit = (mRoundRect.height() - textLayout.getHeight()) / 2 + halfStrokeWidth;
        canvas.translate(mRoundRect.left + mTextLimit, mRoundRect.top + limit);
        textLayout.draw(canvas);
        canvas.restoreToCount(save);

        canvas.restoreToCount(clipRestoreCount);
        if (!mValueHandler.currPointIsOutSide()) {
            invalidate();
        }

    }
}
