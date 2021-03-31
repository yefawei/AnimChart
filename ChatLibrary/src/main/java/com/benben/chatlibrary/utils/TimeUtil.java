package com.benben.chatlibrary.utils;

/**
 * @日期 : 2018/9/12
 * @描述 :
 */
public class TimeUtil {

    /**
     * 根据开始时间和结束时间计算出时间间隔
     * @return
     */
    public static long roundTimeInterval(long startTime, long stopTime) {
        long interval = stopTime - startTime;
        long i = interval / 130;
        int log = (int) (Math.log(i)/Math.log(2));
        long round = (long) (15 * Math.pow(2, log+1));
        if (round < 15) {
            round = 15;
        }
        return round;
    }

    /**
     * 根据开始时间和时间间隔得到interval的时间
     */
    public static long intervalStartTime(long startTime, long interval) {
        startTime -= startTime % interval;
        return startTime;
    }
}
