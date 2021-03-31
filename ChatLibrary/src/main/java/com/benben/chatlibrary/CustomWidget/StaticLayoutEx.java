package com.benben.chatlibrary.CustomWidget;

import android.graphics.text.LineBreaker;
import android.os.Build;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.StaticLayout;
import android.text.TextDirectionHeuristic;
import android.text.TextDirectionHeuristics;
import android.text.TextPaint;
import android.text.TextUtils;

import java.lang.reflect.Constructor;

/**
 * @日期 : 2018/9/11
 * @描述 :
 */
public class StaticLayoutEx {

    private static final String TEXT_DIR_CLASS = "android.text.TextDirectionHeuristic";
    private static final String TEXT_DIRS_CLASS = "android.text.TextDirectionHeuristics";
    private static final String TEXT_DIR_FIRSTSTRONG_LTR = "FIRSTSTRONG_LTR";
    private static boolean initialized;

    private static Constructor<StaticLayout> sConstructor;
    private static Object[] sConstructorArgs;
    private static Object sTextDirection;

    public static void init() {
        if (initialized) {
            return;
        }

        try {
            final Class<?> textDirClass;
            if (Build.VERSION.SDK_INT >= 18) {
                textDirClass = TextDirectionHeuristic.class;
                sTextDirection = TextDirectionHeuristics.FIRSTSTRONG_LTR;
            } else {
                ClassLoader loader = StaticLayoutEx.class.getClassLoader();
                textDirClass = loader.loadClass(TEXT_DIR_CLASS);
                Class<?> textDirsClass = loader.loadClass(TEXT_DIRS_CLASS);
                sTextDirection = textDirsClass.getField(TEXT_DIR_FIRSTSTRONG_LTR).get(textDirsClass);
            }

            final Class<?>[] signature = new Class[]{
                    CharSequence.class,
                    int.class,
                    int.class,
                    TextPaint.class,
                    int.class,
                    Layout.Alignment.class,
                    textDirClass,
                    float.class,
                    float.class,
                    boolean.class,
                    TextUtils.TruncateAt.class,
                    int.class,
                    int.class
            };

            sConstructor = StaticLayout.class.getDeclaredConstructor(signature);
            sConstructor.setAccessible(true);
            sConstructorArgs = new Object[signature.length];
            initialized = true;
        } catch (Throwable e) {

        }
    }

    public static StaticLayout createStaticLayout(CharSequence source, TextPaint paint, int width, Layout.Alignment align, float spacingmult, float spacingadd, boolean includepad, TextUtils.TruncateAt ellipsize, int ellipsisWidth, int maxLines) {
        return createStaticLayout(source, 0, source.length(), paint, width, align, spacingmult, spacingadd, includepad, ellipsize, ellipsisWidth, maxLines);
    }

    public static StaticLayout createStaticLayout(CharSequence source, int bufstart, int bufend, TextPaint paint, int outerWidth, Layout.Alignment align, float spacingMult, float spacingAdd, boolean includePad, TextUtils.TruncateAt ellipsize, int ellipsisWidth, int maxLines) {
        /*if (Build.VERSION.SDK_INT >= 14) {
            init();
            try {
                sConstructorArgs[0] = source;
                sConstructorArgs[1] = bufstart;
                sConstructorArgs[2] = bufend;
                sConstructorArgs[3] = paint;
                sConstructorArgs[4] = outerWidth;
                sConstructorArgs[5] = align;
                sConstructorArgs[6] = sTextDirection;
                sConstructorArgs[7] = spacingMult;
                sConstructorArgs[8] = spacingAdd;
                sConstructorArgs[9] = includePad;
                sConstructorArgs[10] = ellipsize;
                sConstructorArgs[11] = ellipsisWidth;
                sConstructorArgs[12] = maxLines;
                return sConstructor.newInstance(sConstructorArgs);
            } catch (Exception e) {
                FileLog.e(e);
            }
        }*/
        try {
            if (maxLines == 1) {
                CharSequence text = TextUtils.ellipsize(source, paint, ellipsisWidth, TextUtils.TruncateAt.END);
                return new StaticLayout(text, 0, text.length(), paint, outerWidth, align, spacingMult, spacingAdd, includePad);
            } else {
                StaticLayout layout;
                if (Build.VERSION.SDK_INT >= 23) {
                    StaticLayout.Builder builder = StaticLayout.Builder.obtain(source, 0, source.length(), paint, outerWidth)
                            .setAlignment(align)
                            .setLineSpacing(spacingAdd, spacingMult)
                            .setIncludePad(includePad)
                            .setEllipsize(null)
                            .setEllipsizedWidth(ellipsisWidth)
                            .setBreakStrategy(LineBreaker.BREAK_STRATEGY_HIGH_QUALITY)
                            .setHyphenationFrequency(Layout.HYPHENATION_FREQUENCY_NORMAL);
                    layout = builder.build();
                } else {
                    layout = new StaticLayout(source, paint, outerWidth, align, spacingMult, spacingAdd, includePad);
                }
                if (layout.getLineCount() <= maxLines) {
                    return layout;
                } else {
                    int off;
                    float left = layout.getLineLeft(maxLines - 1);
                    if (left != 0) {
                        off = layout.getOffsetForHorizontal(maxLines - 1, left);
                    } else {
                        off = layout.getOffsetForHorizontal(maxLines - 1, layout.getLineWidth(maxLines - 1));
                    }
                    SpannableStringBuilder stringBuilder = new SpannableStringBuilder(source.subSequence(0, Math.max(0, off - 1)));
                    stringBuilder.append("\u2026");
                    return new StaticLayout(stringBuilder, paint, outerWidth, align, spacingMult, spacingAdd, includePad);
                }
            }
        } catch (Exception e) {

        }
        return null;
    }
}
