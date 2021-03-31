package com.benben.chatlibrary.renderer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

import com.benben.chatlibrary.R;
import com.benben.chatlibrary.handler.ValueHandler;
import com.benben.chatlibrary.handler.ViewPortHandler;
import com.benben.chatlibrary.impl.IDataProvider;
import com.benben.chatlibrary.impl.IOrderData;
import com.benben.chatlibrary.utils.Utils;

/**
 * @日期 : 2018/9/13
 * @描述 :
 */
public class OrderRenderer extends BaseRenderer {

    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Paint mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final TextPaint mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);

    private final Matrix mMatrix = new Matrix();

    private final OvershootInterpolator mInterpolator = new OvershootInterpolator();

    int[] mLocatiOnScreen = new int[2];

    private final int mRedColor;
    private final int mGreenColor;
    private final Drawable mUpBg;
    private final Drawable mDownBg;

    private final Drawable mUp;
    private final Drawable mDown;
    private final float mRedCircleRadius;      // 红色圆形透明背景最大半径
    private final float mWhiteBGCircleRadius;    // 圆形背景最大半径
    private final float mRedBGCircleRadius;      // 圆形背景最大半径
    private final float mAmountLimit;       // 数量文本偏移
    private final float mRoundRectLimit;       // 背景偏移

    private final RectF mClippingRect = new RectF();

    private final RectF mWhiteRoundRect = new RectF();
    private final RectF mRedRoundRect = new RectF();

    public OrderRenderer(Context context, @NonNull ViewPortHandler viewPortHandler,
                         @NonNull IDataProvider dataProvider, @NonNull ValueHandler valueHandler) {
        super(context, viewPortHandler, dataProvider, valueHandler);

        mUpBg = ResourcesCompat.getDrawable(context.getResources(),R.drawable.up_bg,null);
        mDownBg = ResourcesCompat.getDrawable(context.getResources(),R.drawable.down_bg,null);
        mUp = ResourcesCompat.getDrawable(context.getResources(),R.mipmap.img_up,null);
        mDown = ResourcesCompat.getDrawable(context.getResources(),R.mipmap.img_down,null);
        mRedColor = Color.parseColor("#DD354F");
        mGreenColor = Color.parseColor("#27B56F");

        mRedCircleRadius = Utils.convertDpToPixel(32);
        mRedBGCircleRadius = Utils.convertDpToPixel(8.5f);
        mWhiteBGCircleRadius = Utils.convertDpToPixel(10.5f);

        mAmountLimit = Utils.convertDpToPixel(8);
        mRoundRectLimit = Utils.convertDpToPixel(4);

        mLinePaint.setStrokeWidth(Utils.convertDpToPixel(1));

        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextSize(Utils.convertDpToPixel(12));
    }

    @Override
    public void calculateValue() {
        if (!mDataProvider.currGameHasOrder()) {
            return;
        }
        mClippingRect.set(mViewPortHandler.getContentRect());
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        getLocationOnScreen(mLocatiOnScreen);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!mDataProvider.currGameHasOrder()) {
            return;
        }
        int clipRestoreCount = canvas.save();
        canvas.clipRect(mClippingRect);
        IOrderData[] currGameOrder = mDataProvider.getCurrGameOrder();
        int startIndex = mViewPortHandler.getStartIndex();
        int stopIndex = mViewPortHandler.getStopIndex();
        boolean hasAnim = false;
        for (int i = 0; i < currGameOrder.length; i++) {
            if (currGameOrder[i] != null) {
                IOrderData orderData = currGameOrder[i];
                int index = mDataProvider.getIndexForTime(orderData.getTime());

                if (!orderData.AnimIsEnd()) {
                    // 开始执行动画
                    if (!orderData.animIsRunning()) orderData.playerAnim();
                    hasAnim = true;
                    if (index < startIndex) continue;
                    drawAnimOrder(canvas, orderData, index, index > stopIndex + 3);// +3是因为数据订单点比较大,增加偏移
                } else {
                    if (index < startIndex) continue;
                    drawFinishOrder(canvas, orderData, index, index > stopIndex + 3);// +3是因为数据订单点比较大,增加偏移
                }
            }
        }
        canvas.restoreToCount(clipRestoreCount);

        if (hasAnim) invalidate();
    }

    /**
     * @param index      坐标
     * @param onTheRight 是否是屏幕右边
     */
    private void drawAnimOrder(Canvas canvas, IOrderData data, int index, boolean onTheRight) {
        float x = mValueHandler.getCoordinateX(index);
        float y = mValueHandler.getCoordinateY(data.getValue());
        float animPhase = data.getAnimPhase();
        //背景圈
        if (animPhase <= 0.2f && !onTheRight) {
            float percent = animPhase * 5f;
            if (data.isUp()) {
                mPaint.setColor(mGreenColor);
            } else {
                mPaint.setColor(mRedColor);
            }
            mPaint.setAlpha((int) ((1 - percent) * 123));
            canvas.drawCircle(x, y, mRedCircleRadius * percent, mPaint);
        }

        //绘制线
        if (animPhase >= 0.1f && animPhase < 0.26f) {
            if (data.isUp()) {
                mLinePaint.setColor(mGreenColor);
            } else {
                mLinePaint.setColor(mRedColor);
            }
            float percent = (animPhase - 0.1f) * 6.25f;
            mLinePaint.setAlpha((int) (255 * percent));
            if (onTheRight) {
                canvas.drawLine(mClippingRect.left, y, mClippingRect.right, y, mLinePaint);
            } else {
                canvas.drawLine(mClippingRect.left, y, x, y, mLinePaint);
            }
        }
        if (animPhase >= 0.26f) {
            if (data.isUp()) {
                mLinePaint.setColor(mGreenColor);
            } else {
                mLinePaint.setColor(mRedColor);
            }
            mLinePaint.setAlpha(255);
            if (onTheRight) {
                canvas.drawLine(mClippingRect.left, y, mClippingRect.right, y, mLinePaint);
            } else {
                canvas.drawLine(mClippingRect.left, y, x, y, mLinePaint);
            }
        }


        String amount = data.getAmount() + " USDT";
        float textWidth = mTextPaint.measureText(amount);

        //绘制背景
        if (animPhase >= 0.1f && animPhase < 0.26f && !onTheRight) {
            // 小 -> 大
            float percent = mInterpolator.getInterpolation((animPhase - 0.1f) * 6.25f);

            mPaint.setAlpha(255);
            mPaint.setColor(Color.WHITE);
            canvas.drawCircle(x, y, mWhiteBGCircleRadius * percent, mPaint);
            if (data.isUp()) {
                mPaint.setColor(mGreenColor);
                canvas.drawCircle(x, y, mRedBGCircleRadius * percent, mPaint);
                int save = canvas.save();
                mMatrix.setScale(percent, percent);
                if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                    // android 6.0及以下需要位移屏幕位置信息才行
                    mMatrix.postTranslate(x * (1 - percent), y * (1 - percent) + mLocatiOnScreen[1]);
                } else {
                    mMatrix.postTranslate(x * (1 - percent), y * (1 - percent));
                }
                canvas.setMatrix(mMatrix);
                Utils.drawImage(canvas, mUp, x, y, mUp.getIntrinsicWidth(), mUp.getIntrinsicHeight());
                canvas.restoreToCount(save);
            } else {
                mPaint.setColor(mRedColor);
                canvas.drawCircle(x, y, mRedBGCircleRadius * percent, mPaint);
                int save = canvas.save();
                mMatrix.setScale(percent, percent);
                if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                    // android 6.0及以下需要位移屏幕位置信息才行
                    mMatrix.postTranslate(x * (1 - percent), y * (1 - percent) + +mLocatiOnScreen[1]);
                } else {
                    mMatrix.postTranslate(x * (1 - percent), y * (1 - percent));
                }
                canvas.setMatrix(mMatrix);
                Utils.drawImage(canvas, mDown, x, y, mDown.getIntrinsicWidth(), mDown.getIntrinsicHeight());
                canvas.restoreToCount(save);
            }

        }
        if (animPhase >= 0.26f && animPhase < 0.30f && !onTheRight) {
            // 固定
            mPaint.setColor(Color.WHITE);
            canvas.drawCircle(x, y, mWhiteBGCircleRadius, mPaint);
            if (data.isUp()) {
                mPaint.setColor(mGreenColor);
                canvas.drawCircle(x, y, mRedBGCircleRadius, mPaint);
                Utils.drawImage(canvas, mUp, x, y, mUp.getIntrinsicWidth(), mUp.getIntrinsicHeight());
            } else {
                mPaint.setColor(mRedColor);
                canvas.drawCircle(x, y, mRedBGCircleRadius, mPaint);
                Utils.drawImage(canvas, mDown, x, y, mDown.getIntrinsicWidth(), mDown.getIntrinsicHeight());
            }
        }
        if (animPhase >= 0.3f && animPhase < 0.46f && !onTheRight) {
            // 展开
            float percent = (animPhase - 0.3f) * 6.25f;
            float half = mWhiteBGCircleRadius;
            mWhiteRoundRect.left = x - half;
            mWhiteRoundRect.top = y - half;
            mWhiteRoundRect.right = x + half + (textWidth + mRoundRectLimit) * percent;
            mWhiteRoundRect.bottom = y + half;
            mPaint.setColor(Color.WHITE);
            canvas.drawRoundRect(mWhiteRoundRect, half, half, mPaint);

            half = mRedBGCircleRadius;
            mRedRoundRect.left = x - half;
            mRedRoundRect.top = y - half;
            mRedRoundRect.right = x + half + (textWidth + mRoundRectLimit) * percent;
            mRedRoundRect.bottom = y + half;
            if (data.isUp()) {
                mPaint.setColor(mGreenColor);
                canvas.drawRoundRect(mRedRoundRect, half, half, mPaint);
                Utils.drawImage(canvas, mUp, x, y, mUp.getIntrinsicWidth(), mUp.getIntrinsicHeight());
            } else {
                mPaint.setColor(mRedColor);
                canvas.drawRoundRect(mRedRoundRect, half, half, mPaint);
                Utils.drawImage(canvas, mDown, x, y, mDown.getIntrinsicWidth(), mDown.getIntrinsicHeight());
            }
        }
        if (animPhase >= 0.46f && animPhase <= 0.82f && !onTheRight) {
            // 固定展开
            float half = mWhiteBGCircleRadius;
            mWhiteRoundRect.left = x - half;
            mWhiteRoundRect.top = y - half;
            mWhiteRoundRect.right = x + half + (textWidth + mRoundRectLimit);
            mWhiteRoundRect.bottom = y + half;
            mPaint.setColor(Color.WHITE);
            canvas.drawRoundRect(mWhiteRoundRect, half, half, mPaint);

            half = mRedBGCircleRadius;
            mRedRoundRect.left = x - half;
            mRedRoundRect.top = y - half;
            mRedRoundRect.right = x + half + (textWidth + mRoundRectLimit);
            mRedRoundRect.bottom = y + half;
            if (data.isUp()) {
                mPaint.setColor(mGreenColor);
                canvas.drawRoundRect(mRedRoundRect, half, half, mPaint);
                Utils.drawImage(canvas, mUp, x, y, mUp.getIntrinsicWidth(), mUp.getIntrinsicHeight());
            } else {
                mPaint.setColor(mRedColor);
                canvas.drawRoundRect(mRedRoundRect, half, half, mPaint);
                Utils.drawImage(canvas, mDown, x, y, mDown.getIntrinsicWidth(), mDown.getIntrinsicHeight());
            }
        }
        if (animPhase >= 0.82f && animPhase < 0.98f && !onTheRight) {
            // 收起展开
            float percent = 1.0f - (animPhase - 0.82f) * 6.25f;
            float half = mWhiteBGCircleRadius;
            mWhiteRoundRect.left = x - half;
            mWhiteRoundRect.top = y - half;
            mWhiteRoundRect.right = x + half + (textWidth + mRoundRectLimit) * percent;
            mWhiteRoundRect.bottom = y + half;
            mPaint.setColor(Color.WHITE);
            canvas.drawRoundRect(mWhiteRoundRect, half, half, mPaint);

            half = mRedBGCircleRadius;
            mRedRoundRect.left = x - half;
            mRedRoundRect.top = y - half;
            mRedRoundRect.right = x + half + (textWidth + mRoundRectLimit) * percent;
            mRedRoundRect.bottom = y + half;
            if (data.isUp()) {
                mPaint.setColor(mGreenColor);
                canvas.drawRoundRect(mRedRoundRect, half, half, mPaint);
                Utils.drawImage(canvas, mUp, x, y, mUp.getIntrinsicWidth(), mUp.getIntrinsicHeight());
            } else {
                mPaint.setColor(mRedColor);
                canvas.drawRoundRect(mRedRoundRect, half, half, mPaint);
                Utils.drawImage(canvas, mDown, x, y, mDown.getIntrinsicWidth(), mDown.getIntrinsicHeight());
            }
        }

        if (animPhase >= 0.98f && !onTheRight) {
            // 固定
            mPaint.setColor(Color.WHITE);
            canvas.drawCircle(x, y, mWhiteBGCircleRadius, mPaint);
            if (data.isUp()) {
                mPaint.setColor(mGreenColor);
                canvas.drawCircle(x, y, mRedBGCircleRadius, mPaint);
                Utils.drawImage(canvas, mUp, x, y, mUp.getIntrinsicWidth(), mUp.getIntrinsicHeight());
            } else {
                mPaint.setColor(mRedColor);
                canvas.drawCircle(x, y, mRedBGCircleRadius, mPaint);
                Utils.drawImage(canvas, mDown, x, y, mDown.getIntrinsicWidth(), mDown.getIntrinsicHeight());
            }
        }

        //绘制当前价格
        if (animPhase >= 0.4f && animPhase < 0.5f && !onTheRight) {
            float percent = (animPhase - 0.4f) * 10f;
            mTextPaint.setAlpha((int) (255 * percent));

            int save = canvas.save();
            StaticLayout textLayout = new StaticLayout(amount, mTextPaint, (int) mTextPaint.measureText(amount),
                    Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            float limitX = x + mAmountLimit;
            float limitY = y - textLayout.getHeight() / 2.0f;
            canvas.translate(limitX, limitY);
            textLayout.draw(canvas);
            canvas.restoreToCount(save);
        }
        if (animPhase >= 0.5f && animPhase < 0.8f && !onTheRight) {
            mTextPaint.setAlpha(255);
            int save = canvas.save();
            StaticLayout textLayout = new StaticLayout(amount, mTextPaint, (int) mTextPaint.measureText(amount),
                    Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            float limitX = x + mAmountLimit;
            float limitY = y - textLayout.getHeight() / 2.0f;
            canvas.translate(limitX, limitY);
            textLayout.draw(canvas);
            canvas.restoreToCount(save);
        }
        if (animPhase >= 0.76f && animPhase < 0.86f && !onTheRight) {
            float percent = (animPhase - 0.76f) * 10f;
            mTextPaint.setAlpha((int) (255 * (1 - percent)));

            int save = canvas.save();
            StaticLayout textLayout = new StaticLayout(amount, mTextPaint, (int) mTextPaint.measureText(amount),
                    Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            float limitX = x + mAmountLimit;
            float limitY = y - textLayout.getHeight() / 2.0f;
            canvas.translate(limitX, limitY);
            textLayout.draw(canvas);
            canvas.restoreToCount(save);
        }
    }

    /**
     * @param index      坐标
     * @param onTheRight 是否是屏幕右边
     */
    private void drawFinishOrder(Canvas canvas, IOrderData data, int index, boolean onTheRight) {
        if (onTheRight) {
            mLinePaint.setAlpha(255);
            if (data.isUp()) {
                mLinePaint.setColor(mGreenColor);
            } else {
                mLinePaint.setColor(mRedColor);
            }
            float y = mValueHandler.getCoordinateY(data.getValue());
            canvas.drawLine(mClippingRect.left, y, mClippingRect.right, y, mLinePaint);
        } else {
            float x = mValueHandler.getCoordinateX(index);
            float y = mValueHandler.getCoordinateY(data.getValue());
            if (data.isUp()) {
                mLinePaint.setColor(mGreenColor);
                canvas.drawLine(mClippingRect.left, y, x, y, mLinePaint);
                Utils.drawImage(canvas, mUpBg, x, y, mUpBg.getIntrinsicWidth(), mUpBg.getIntrinsicHeight());
            } else {
                mLinePaint.setColor(mRedColor);
                canvas.drawLine(mClippingRect.left, y, x, y, mLinePaint);
                Utils.drawImage(canvas, mDownBg, x, y, mDownBg.getIntrinsicWidth(), mDownBg.getIntrinsicHeight());
            }
        }

    }
}
