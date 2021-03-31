package com.benben.chatlibrary.components;

import android.graphics.Color;
import android.graphics.Typeface;

import com.benben.chatlibrary.utils.Utils;

/**
 * @日期 : 2018/9/6
 * @描述 :
 */
public abstract class ComponentBase {

    protected boolean mEnabled = true;

    protected float mXOffset = 0f;

    protected float mYOffset = 0f;

    protected Typeface mTypeface = null;

    protected float mTextSize = Utils.convertDpToPixel(10f);

    protected int mTextColor = Color.BLACK;

    public float getXOffset() {
        return mXOffset;
    }

    public void setXOffset(float xOffset) {
        mXOffset = Utils.convertDpToPixel(xOffset);
    }

    public float getYOffset() {
        return mYOffset;
    }

    public void setYOffset(float yOffset) {
        mYOffset = Utils.convertDpToPixel(yOffset);
    }

    public Typeface getTypeface() {
        return mTypeface;
    }

    public void setTypeface(Typeface tf) {
        mTypeface = tf;
    }

    public void setTextSize(float size) {
        mTextSize = Utils.convertDpToPixel(size);
    }

    public float getTextSize() {
        return mTextSize;
    }

    public int getTextColor() {
        return mTextColor;
    }

    public void setTextColor(int color) {
        mTextColor = color;
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
    }
}
