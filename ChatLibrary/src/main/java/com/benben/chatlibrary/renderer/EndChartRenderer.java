package com.benben.chatlibrary.renderer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.benben.chatlibrary.R;
import com.benben.chatlibrary.handler.ValueHandler;
import com.benben.chatlibrary.handler.ViewPortHandler;
import com.benben.chatlibrary.impl.IDataProvider;
import com.benben.chatlibrary.utils.Utils;

/**
 * @日期 : 2018/9/11
 * @描述 : 结束时间渲染
 */
public class EndChartRenderer extends BaseRenderer {

    private final Paint mShaderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);  // 渐变阴影

    private final Paint mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);    // 渐变阴影

    private final int mYellowColor;
    private final int mGrayColor;
    private final DashPathEffect mDashPathEffect;
    private final int[] mShadeColors;
    private final Path mLinePath = new Path();
    private final Path mShaderPath = new Path();

    float[] mCoordinateBuffer = new float[2];

    private final Drawable mTerminus;
    private final Drawable mCountDown;
    private final Drawable mRedCountDown;

    private int mInterval = 0;

    protected RectF mClippingRect = new RectF();

    private long mWarnLimitTime = 0;

    public EndChartRenderer(Context context, @NonNull ViewPortHandler viewPortHandler,
                            @NonNull IDataProvider dataProvider, @NonNull ValueHandler valueHandler) {
        super(context, viewPortHandler, dataProvider, valueHandler);
        mShaderPaint.setStrokeWidth(1);
        mShaderPaint.setStyle(Paint.Style.FILL);
        mYellowColor = Color.parseColor("#FFAD56");
        mGrayColor = Color.parseColor("#7B87A4");
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(Utils.convertDpToPixel(1));
        mDashPathEffect = new DashPathEffect(new float[]{10f,8f},0);

        mShadeColors = new int[]{
                Color.argb(76, 255, 173, 86),
                Color.argb(0, 255, 173, 86)};

        mTerminus = ResourcesCompat.getDrawable(context.getResources(), R.mipmap.img_terminus, null);
        mCountDown = ResourcesCompat.getDrawable(context.getResources(), R.mipmap.img_count_down, null);
        mRedCountDown = ResourcesCompat.getDrawable(context.getResources(), R.mipmap.img_red_count_down, null);
    }

    @Override
    public void calculateValue() {
        if (mDataProvider.isRealEmpty()) {
            return;
        }
        if (mValueHandler.endViewIsOutSide()) return;
        mClippingRect.set(mViewPortHandler.getContentRect());
        //FIXME 现发现华为手机在绘制渐变线触及到View的边界时出现莫名的黄线,顾做此偏移
        mClippingRect.top = mClippingRect.top - mShaderPaint.getStrokeWidth();
        float[] endViewPosition = mValueHandler.getEndViewXPosition();
        float coordinateStartX = endViewPosition[0];
        float coordinateEndX = endViewPosition[1];
        if (mCoordinateBuffer[0] != coordinateStartX || mCoordinateBuffer[1] != coordinateEndX) {
            //终点位置有所变化
            mCoordinateBuffer[0] = coordinateStartX;
            mCoordinateBuffer[1] = coordinateEndX;
            LinearGradient mShader = new LinearGradient(coordinateStartX, mClippingRect.top, coordinateStartX, mClippingRect.bottom, mShadeColors, null, Shader.TileMode.CLAMP);
            mShaderPaint.setShader(mShader);
            mShaderPath.reset();
            mShaderPath.moveTo(coordinateStartX, mClippingRect.top);
            mShaderPath.lineTo(coordinateEndX, mClippingRect.top);
            mShaderPath.lineTo(coordinateEndX, mClippingRect.bottom);
            mShaderPath.lineTo(coordinateStartX, mClippingRect.bottom);
            mShaderPath.close();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mDataProvider.isRealEmpty()) {
            return;
        }
        if (mValueHandler.endViewIsOutSide()) return;

        int clipRestoreCount = canvas.save();
        canvas.clipRect(mClippingRect);

        // 渐变阴影
        canvas.drawPath(mShaderPath, mShaderPaint);

        float y = (int) (mClippingRect.height() / 2 + mClippingRect.top);
        // 绘制起点线
        if (mValueHandler.needWarn() && mInterval == 0) {
            mInterval = 1;
            // 绘制起点图标
            Utils.drawImage(canvas,mRedCountDown, mCoordinateBuffer[0],y,mCountDown.getIntrinsicWidth(),mCountDown.getIntrinsicHeight());
            mLinePaint.setColor(Color.RED);
        } else {
            mInterval = 0;
            // 绘制起点图标
            Utils.drawImage(canvas,mCountDown, mCoordinateBuffer[0],y,mCountDown.getIntrinsicWidth(),mCountDown.getIntrinsicHeight());
            mLinePaint.setColor(mGrayColor);
        }
        mLinePaint.setPathEffect(mDashPathEffect);
        mLinePath.reset();
        mLinePath.moveTo(mCoordinateBuffer[0],mClippingRect.top);
        mLinePath.lineTo(mCoordinateBuffer[0],y - mCountDown.getIntrinsicHeight()/2.0f);
        canvas.drawPath(mLinePath,mLinePaint);
        mLinePath.reset();
        mLinePath.moveTo(mCoordinateBuffer[0],y + mCountDown.getIntrinsicHeight()/2.0f);
        mLinePath.lineTo(mCoordinateBuffer[0],mClippingRect.bottom);
        canvas.drawPath(mLinePath,mLinePaint);


        // 绘制终点线
        mLinePaint.setColor(mYellowColor);
        mLinePaint.setPathEffect(null);
        mLinePath.reset();
        mLinePath.moveTo(mCoordinateBuffer[1],mClippingRect.top);
        mLinePath.lineTo(mCoordinateBuffer[1],y - mCountDown.getIntrinsicHeight()/2.0f);
        canvas.drawPath(mLinePath,mLinePaint);
        mLinePath.reset();
        mLinePath.moveTo(mCoordinateBuffer[1],y + mCountDown.getIntrinsicHeight()/2.0f);
        mLinePath.lineTo(mCoordinateBuffer[1],mClippingRect.bottom);
        canvas.drawPath(mLinePath,mLinePaint);
        // 绘制终点图标
        Utils.drawImage(canvas,mTerminus, mCoordinateBuffer[1],y,mCountDown.getIntrinsicWidth(),mCountDown.getIntrinsicHeight());

        canvas.restoreToCount(clipRestoreCount);
        if (mValueHandler.needWarn()) {
            long curr = System.currentTimeMillis();
            if (curr - mWarnLimitTime < 50) {
                return;
            }
            mWarnLimitTime = curr;
            postInvalidateDelayed(50);
        }
    }
}
