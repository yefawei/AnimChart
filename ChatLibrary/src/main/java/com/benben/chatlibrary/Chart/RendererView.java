package com.benben.chatlibrary.Chart;

import com.benben.chatlibrary.renderer.BaseRenderer;

/**
 * @日期 : 2018/9/17
 * @描述 :
 */
public interface RendererView {

    int getRendererCount();

    BaseRenderer getRenderer(int index);

    void addRenderer(BaseRenderer renderer);

    void addRenderer(BaseRenderer renderer, int index);
}
