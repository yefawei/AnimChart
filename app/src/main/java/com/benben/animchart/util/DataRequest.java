package com.benben.animchart.util;

import android.content.Context;

import com.benben.animchart.bean.LineTimeData;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @日期 : 2018/9/7
 * @描述 :
 */
public class DataRequest {

    private static final List<LineTimeData> sFirstDatas = new ArrayList<>();
    public static List<LineTimeData> mTimeDatas = new ArrayList<>();

    public static void initData(Context context) {
        sFirstDatas.clear();
        mTimeDatas.clear();
        List<LineTimeData> datas = new Gson().fromJson(getStringFromAssert(context, "ibm.json"),
                new TypeToken<List<LineTimeData>>() {}.getType());

        long startTime = 1536292800;
        for (int i = 0; i < datas.size(); i++) {
            LineTimeData lineData = datas.get(i);
            lineData.timestamp = startTime;
            startTime++;
            if (i < 90) {
                sFirstDatas.add(lineData);
            } else {
                mTimeDatas.add(lineData);
            }
        }

    }

    public static String getStringFromAssert(Context context, String fileName) {
        try {
            InputStream in = context.getResources().getAssets().open(fileName);
            int length = in.available();
            byte[] buffer = new byte[length];
            in.read(buffer);
            return new String(buffer, 0, buffer.length, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static List<LineTimeData> getFirstDatas() {
        return sFirstDatas;
    }

    public static LineTimeData getData(int index) {
        return mTimeDatas.get(index);
    }
}
