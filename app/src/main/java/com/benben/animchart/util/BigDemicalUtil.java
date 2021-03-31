package com.benben.animchart.util;

import android.text.TextUtils;

import java.math.BigDecimal;

/**
 * @版权 : 深圳区块链科技有限公司
 * @作者 : 叶发伟
 * @日期 : 2017/9/29
 * @描述 : 高精度加减乘除
 */

public class BigDemicalUtil {

    /**
     * 提供精确的小数位处理。
     */
    public static String round(String v, int scale, int roundingMode) {
        if (scale < 0) {
            throw new IllegalArgumentException("精确度不能小于0");
        }
        BigDecimal b = new BigDecimal(v);
        BigDecimal one = new BigDecimal("1");
        return b.divide(one, scale, roundingMode).toPlainString();
    }

    /**
     * 提供精确减法
     */
    public static String sub(String value1, String value2){
        BigDecimal b1 = new BigDecimal(value1);
        BigDecimal b2 = new BigDecimal(value2);
        return b1.subtract(b2).toPlainString();
    }

    /**
     * 提供精确的除法
     * @param value1
     * @param value2
     * @param scale  保留小数位
     * @return 除得结果后,按小数位精度处理
     */
    public static String div(String value1, String value2, int scale, int roundingMode){
        if (TextUtils.isEmpty(value1)) {
            return "0.0000";
        }
        BigDecimal b1 = new BigDecimal(value1);
        BigDecimal b2 = new BigDecimal(value2);
        return b1.divide(b2, scale, roundingMode).toPlainString();
    }

    /**
     * 提供精确的乘法计算
     * @param value1
     * @param value2
     * @param scale  保留小数位
     * @return 乘得结果之后,按小数位精度处理(小数位是0仍然按位数保留)
     */
    public static String mul(String value1, String value2, int scale, int roundingMode){
        if (TextUtils.isEmpty(value1) || TextUtils.isEmpty(value2)){
            return "0.0000";
        }
        BigDecimal b1 = new BigDecimal(value1);
        BigDecimal b2 = new BigDecimal(value2);
        BigDecimal multiply = b1.multiply(b2);
        return multiply.setScale(scale,roundingMode).toPlainString();
    }

    /**
     * 提供精确的乘法计算
     * @param value1
     * @param value2
     * @param scale  保留小数位
     * @return 乘得结果之后,按小数位精度处理(小数位是0则直接去掉)
     */
    public static String mul2(String value1, String value2, int scale, int roundingMode) {
        if (TextUtils.isEmpty(value1) || TextUtils.isEmpty(value2)){
            return "0";
        }
        BigDecimal b1 = new BigDecimal(value1);
        BigDecimal b2 = new BigDecimal(value2);
        BigDecimal multiply = b1.multiply(b2);
        return multiply.setScale(scale,roundingMode).stripTrailingZeros().toPlainString();
    }
}
