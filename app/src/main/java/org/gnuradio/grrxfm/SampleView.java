package org.gnuradio.grrxfm;

import android.util.Log;

import android.content.Context;
import android.util.AttributeSet;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Canvas;
import android.graphics.Point;

import android.view.View;
import android.view.Display;
import android.view.WindowManager;

import android.os.SystemClock;

import org.gnuradio.grcontrolport.RPCConnection;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class SampleView extends View
{
    private Paint mPaint;
    private float [] mData;
    private RunGraph mGraph;
    private int mScreenWidth;

    public SampleView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        //mPaint.setColor(getResources().getColor(R.color.gr_orange));
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeWidth(2);

        Paint mFramePaint = new Paint();
        mFramePaint.setAntiAlias(true);
        mFramePaint.setStyle(Paint.Style.STROKE);
        mFramePaint.setStrokeWidth(0);

        WindowManager winmgr = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        Display display = winmgr.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mScreenWidth = size.x;
    }

    public void SetData(float [] data)
    {
        mData = data;
    }

    public void SetData(RunGraph graph)
    {
        mGraph = graph;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        //canvas.drawColor(getResources().getColor(R.color.gr_grey));

        /*
        final String probeName = "probe2_f0::rxdata";
        ArrayList<String> a = new ArrayList<>(1);
        a.add(probeName);
        mGraph.postGetKnob(a);
        RunGraph.CPHandlerGet h = mGraph.mControlPortThread.getHandlerGet();
        HashMap<String, RPCConnection.KnobInfo> k = h.getKnobs();

        if(k != null) {
            RPCConnection.KnobInfo v = k.get(probeName);
            Log.d("===>> SampleView", "V: " + v);
            ArrayList<Float> f = (ArrayList<Float>)(v.value);
            Log.d("===>> SampleView", "F: " + f);
            for(int i = 0; i < f.size(); i++) {
                Log.d("                       ", " -> " + f.get(i).getClass());
                mData[i] = f.get(i);
            }
        }

        for(int i = 0; i < mScreenWidth; i++) {
            float mStartPoint = mData[i] + 200;
            float mEndPoint = mData[i + 1] + 200;

            float mStartTime = 1 * i;
            float mEndTime = 1 * (i + 1);

            canvas.drawLine(mStartTime, mStartPoint, mEndTime, mEndPoint, mPaint);
        }

        SystemClock.sleep(100);
        invalidate();
        */
    }
}
