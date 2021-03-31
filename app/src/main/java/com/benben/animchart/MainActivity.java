package com.benben.animchart;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.benben.animchart.adapter.LineAdapter;
import com.benben.animchart.bean.LineTimeData;
import com.benben.animchart.bean.OrderData;
import com.benben.animchart.util.BigDemicalUtil;
import com.benben.animchart.util.DataRequest;
import com.benben.chatlibrary.Chart.ChartView;
import com.benben.chatlibrary.components.AxisBase;
import com.benben.chatlibrary.formatter.IAxisValueFormatter;
import com.benben.chatlibrary.handler.DataHandler;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private int index = 0;

    Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                LineTimeData lineData = (LineTimeData) msg.obj;
                mLineAdapter.addData(lineData);
            }
            if (mChartView.getAdapter() == null) return;
            startTime();
        }
    };
    private ChartView mChartView;
    private LineAdapter mLineAdapter;
    private ImageView mImgScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mChartView = findViewById(R.id.chart_view);
        mImgScrollView = findViewById(R.id.img_scroll_view);

        mChartView.getAxisLeft().setTypeface(ResourcesCompat.getFont(this, R.font.dinoffcpro_medium));
        mChartView.getAxisLeft().setTextColor(Color.parseColor("#464E63"));
        mChartView.getAxisLeft().setTextSize(9);
        mChartView.getAxisLeft().setForceLabels(true);
        mChartView.getAxisLeft().setAxisValueFormatter(value -> BigDemicalUtil.mul2(value, "1", 2, BigDecimal.ROUND_DOWN));
        mChartView.getAxisLeft().setGridColor(Color.parseColor("#3C4151"));
        mChartView.getAxisLeft().setStrokeWidth(1);
        mChartView.getAxisLeft().setPathEffect(new DashPathEffect(new float[]{12f, 2f}, 0));
        mChartView.getAxisLeft().setFrameGridColor(Color.parseColor("#3C4151"));
        mChartView.getAxisLeft().setFrameStrokeWidth(1);

        mChartView.getAxisBottom().setLabelPosition(AxisBase.AxisLabelPosition.OUTSIDE_CHART);
        mChartView.getAxisBottom().setYOffset(21);
        mChartView.getAxisBottom().setTypeface(ResourcesCompat.getFont(this, R.font.dinoffcpro_medium));
        mChartView.getAxisBottom().setTextColor(Color.parseColor("#464E63"));
        mChartView.getAxisBottom().setTextSize(9);
        mChartView.getAxisBottom().setAxisValueFormatter(new IAxisValueFormatter() {
            final SimpleDateFormat mDaytimeformat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

            @Override
            public String getFormattedValue(String value) {
                return mDaytimeformat.format(new Date(Long.parseLong(value)));
            }
        });
        mChartView.getAxisBottom().setGridColor(Color.parseColor("#3C4151"));
        mChartView.getAxisBottom().setStrokeWidth(1);
        mChartView.getAxisBottom().setPathEffect(new DashPathEffect(new float[]{12f, 2f}, 0));

        mChartView.setScrollLimitX(-32, true);
        mChartView.setNeedCheckMissing(true);
        mChartView.setGameListener(new DataHandler.GameListener() {
            @Override
            public void waiting() {
            }

            @Override
            public void nextGame() {
            }
        });
        mChartView.setMissingDataListener((startTime, endTime) -> {

        });
        mChartView.setEndRangeListener(isOutSide -> mImgScrollView.animate().alpha(isOutSide ? 1 : 0).setDuration(200).start());
        DataRequest.initData(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage("快速查看：setAdapter -> 定时加载\n\n下单效果：下看涨看跌单")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .create();
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
        mChartView.setAdapter(null);
    }

    public void scrollView(View view) {
        mChartView.aminScrollZero();
    }


    public void setAdapter(View view) {
        List<LineTimeData> datas = new ArrayList<>(DataRequest.getFirstDatas());
        mLineAdapter = new LineAdapter(datas);
        mChartView.setAdapter(mLineAdapter);
    }

    public void startTimer(View view) {
        if (mChartView.getAdapter() == null) {
            Toast.makeText(this, "adapter null", Toast.LENGTH_SHORT).show();
            return;
        }
        startTime();
    }

    public void stopTimer(View view) {
        mHandler.removeCallbacksAndMessages(null);
    }

    public void addData(View view) {
        if (mChartView.getAdapter() == null) {
            Toast.makeText(this, "adapter null", Toast.LENGTH_SHORT).show();
            return;
        }
        LineTimeData data = DataRequest.getData(index);
        index++;
        mLineAdapter.addData(data);
    }

    public void clear(View view) {
        mLineAdapter.clearData();
        index = 0;
    }

    public void reset(View view) {
        index = 0;
        DataRequest.initData(this);
        mHandler.removeCallbacksAndMessages(null);
        mChartView.setAdapter(null);
    }

    public void addUpOrder(View view) {
        if (mChartView.getAdapter() == null) {
            Toast.makeText(this, "adapter null", Toast.LENGTH_SHORT).show();
            return;
        }

        LineTimeData realLastItem = mLineAdapter.getItem(mLineAdapter.getCount() - 1);
        final long time = realLastItem.getTime();
        OrderData orderData = new OrderData();
        orderData.timestamp = time;
        orderData.amount = "1";
        orderData.type = 1;
        orderData.close_price = realLastItem.close_price;
        mLineAdapter.addOrder(orderData);
    }

    public void addTimerUpOrder(View view) {
        if (mChartView.getAdapter() == null) {
            Toast.makeText(this, "adapter null", Toast.LENGTH_SHORT).show();
            return;
        }
        final LineTimeData realLastItem = mLineAdapter.getItem(mLineAdapter.getCount() - 1);
        final long time = realLastItem.getTime();
        final String close_price = realLastItem.close_price;
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mChartView.getAdapter() == null) return;

                OrderData orderData = new OrderData();
                orderData.timestamp = time;
                orderData.amount = "1000";
                orderData.type = 1;
                orderData.close_price = close_price;
                mLineAdapter.addOrder(orderData);
            }
        }, 3000);
    }

    public void addDownOrder(View view) {
        if (mChartView.getAdapter() == null) {
            Toast.makeText(this, "adapter null", Toast.LENGTH_SHORT).show();
            return;
        }

        LineTimeData realLastItem = mLineAdapter.getItem(mLineAdapter.getCount() - 1);
        final long time = realLastItem.getTime();
        OrderData orderData = new OrderData();
        orderData.timestamp = time;
        orderData.amount = "1";
        orderData.type = 2;
        orderData.close_price = realLastItem.close_price;
        mLineAdapter.addOrder(orderData);
    }

    public void addTimerDownOrder(View view) {
        if (mChartView.getAdapter() == null) {
            Toast.makeText(this, "adapter null", Toast.LENGTH_SHORT).show();
            return;
        }

        final LineTimeData realLastItem = mLineAdapter.getItem(mLineAdapter.getCount() - 1);
        final long time = realLastItem.getTime();
        final String close_price = realLastItem.close_price;
        mHandler.postDelayed(() -> {
            if (mChartView.getAdapter() == null) return;

            OrderData orderData = new OrderData();
            orderData.timestamp = time;
            orderData.amount = "1000";
            orderData.type = 2;
            orderData.close_price = close_price;
            mLineAdapter.addOrder(orderData);
        }, 3000);
    }

    public void startTime() {
        if (mChartView.getAdapter() == null) {
            Toast.makeText(this, "adapter null", Toast.LENGTH_SHORT).show();
            return;
        }
        mHandler.removeMessages(1);
        mHandler.postDelayed(() -> {
            Message obtain = Message.obtain();
            obtain.what = 1;
            obtain.obj = DataRequest.getData(index);
            mHandler.sendMessage(obtain);
            index++;
        }, 1000);
    }
}