package com.benben.chatlibrary.renderer;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;

import com.benben.chatlibrary.handler.ValueHandler;
import com.benben.chatlibrary.handler.ViewPortHandler;
import com.benben.chatlibrary.impl.IDataProvider;

/**
 * @日期 : 2018/9/7
 * @描述 :
 */
public abstract class BaseRenderer extends View implements Renderer {

    protected ViewPortHandler mViewPortHandler;

    protected IDataProvider mDataProvider;

    protected ValueHandler mValueHandler;
    public BaseRenderer(Context context, @NonNull ViewPortHandler viewPortHandler,
                        @NonNull IDataProvider dataProvider, @NonNull ValueHandler valueHandler) {
        super(context);
        mValueHandler = valueHandler;
        mViewPortHandler = viewPortHandler;
        mDataProvider = dataProvider;
    }

    @Override
    public void initStyle() {

    }
}
