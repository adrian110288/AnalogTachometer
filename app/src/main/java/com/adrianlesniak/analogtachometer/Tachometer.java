package com.adrianlesniak.analogtachometer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;import java.lang.InterruptedException;import java.lang.Math;import java.lang.Override;import java.lang.String;import java.lang.Thread;

public class Tachometer extends View {

    private static final String LOG = "DRAW";
    private static final int COLOR_RED = Color.parseColor("#FF0000");

    private int mOuterBorderRadius;
    private Paint mBasePaint;
    private TextPaint mNumbersTextPaint;
    private Path mMainIndicatorPath;
    private Path mNeedlePath;

    private float speed = 0.0f;
    private float needleRotation;

    private Handler mHandler;

    public Tachometer(Context context) {
        super(context);
        setup();
    }

    public Tachometer(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    public Tachometer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup();
    }

    public void setSpeed(float speedIn) {
        speed = speedIn;
    }

    private void setup(){
        createBasePaint();
        startSpeeding();
    }

    private class SpeedingThread extends Thread {

        private static final float INITIAL_ROTATION = -128.0f;

        private Handler handler;
        private float currentRotation;

        public SpeedingThread(Handler h) {
            handler = h;
            currentRotation = INITIAL_ROTATION;
        }

        @Override
        public void run() {

            Message m;
            Bundle b = new Bundle();

            while(true) {

                float maxRotation = INITIAL_ROTATION + (Math.abs(INITIAL_ROTATION)*2*speed);

                m  = Message.obtain();
                b.putFloat("rotation", currentRotation);
                m.setData(b);
                handler.sendMessage(m);

                try {
                    if(currentRotation <= maxRotation){
                        currentRotation+=speed;
                        sleep((long)Math.ceil((double)(10-(10*speed))));
                    }

                    else {
                        currentRotation-=0.8;
                        sleep(10);
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void startSpeeding() {

        createHandler();

        SpeedingThread speedingThread = new SpeedingThread(mHandler);
        speedingThread.start();
    }

    private void createHandler() {

        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {

                needleRotation = msg.getData().getFloat("rotation");
                return true;
            }
        });
    }

    private void createBasePaint() {
        mBasePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBasePaint.setColor(getResources().getColor(android.R.color.white));
        mBasePaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawOuterBorder(canvas);
        drawInnerCircle(canvas);
        drawMainIndicators(canvas);
        drawHalfIndicators(canvas);
        drawQuarterIndicators(canvas);
        drawIndicatorNumbers(canvas);
        drawRpmText(canvas);
        drawNeedle(canvas);
    }

    private void drawNeedle(Canvas c) {

//        mBasePaint.setColor(getResources().getColor(android.R.color.white));
//        mBasePaint.setStyle(Paint.Style.FILL);

        if(mNeedlePath == null) createNeedlePath(c);

        c.save();

        c.rotate(needleRotation, c.getWidth() / 2, c.getHeight() / 2);
//        c.drawPath(mNeedlePath, mBasePaint);

        mBasePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mBasePaint.setStrokeWidth(5);
        mBasePaint.setColor(COLOR_RED);
        c.drawPath(mNeedlePath, mBasePaint);

        c.restore();
        resetBasePaint();

        invalidate();
    }

    private void createNeedlePath(Canvas c) {

        mNeedlePath = new Path();
        mNeedlePath.moveTo(c.getWidth() / 2, (float) ((c.getHeight() / 2) - mOuterBorderRadius + (mOuterBorderRadius * 0.125)));
        mNeedlePath.lineTo((float) ((c.getWidth() / 2) - (mOuterBorderRadius * 0.020)), c.getHeight() / 2);
        mNeedlePath.lineTo((float) (c.getWidth() / 2 - (mOuterBorderRadius * 0.001)), (float) ((c.getHeight() / 2) + (mOuterBorderRadius * 0.2)));
        mNeedlePath.lineTo((float) (c.getWidth() / 2 - (mOuterBorderRadius * 0.001)), (float) ((c.getHeight() / 2) + (mOuterBorderRadius * 0.2)));
        mNeedlePath.lineTo((float) ((c.getWidth() / 2) + (mOuterBorderRadius * 0.020)), c.getHeight() / 2);
        mNeedlePath.lineTo(c.getWidth() / 2, (float) ((c.getHeight() / 2) - mOuterBorderRadius + (mOuterBorderRadius * 0.125)));
//        mNeedlePath.lineTo((float)(c.getWidth() / 2 - (mOuterBorderRadius*0.0000)), (float) ((c.getHeight() / 2) - mOuterBorderRadius + (mOuterBorderRadius * 0.125)));
        mNeedlePath.close();
    }

    private void drawRpmText(Canvas c) {

        if(mNumbersTextPaint == null) {
            createNumbersTextPaint();
        }

        mNumbersTextPaint.setTextSize((float) (mOuterBorderRadius * 0.10));

        c.drawText("x1000rpm", c.getWidth()/2 - (mNumbersTextPaint.measureText("x1000rpm")/2), (float)(c.getHeight()/2 - mOuterBorderRadius + (mOuterBorderRadius * 0.6) + ((mNumbersTextPaint.ascent() + mNumbersTextPaint.descent())/2)), mNumbersTextPaint);
    }

    private void drawIndicatorNumbers(Canvas c) {

        if(mNumbersTextPaint == null) {
            createNumbersTextPaint();
        }

        mNumbersTextPaint.setTextSize((float) (mOuterBorderRadius * 0.16));

        int startRadius = 180-128+90;
        int endRadius = 32*9 + startRadius;

        for(int radius=startRadius, count=0; radius<endRadius; radius+=32, count++) {

            int x = (int)((mOuterBorderRadius-(mOuterBorderRadius*0.25)) * Math.cos(Math.toRadians(radius)) + c.getWidth()/2);
            int y = (int)((mOuterBorderRadius-(mOuterBorderRadius*0.25)) * Math.sin(Math.toRadians(radius)) + c.getHeight()/2);

            float adjustedX = x - (mNumbersTextPaint.measureText(String.valueOf(count))/2);
            float adjustedY = y - ((mNumbersTextPaint.ascent() + mNumbersTextPaint.descent())/2);

            c.drawText(String.valueOf(count), adjustedX, adjustedY, mNumbersTextPaint);
        }
    }

    private void createNumbersTextPaint() {
        mNumbersTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mNumbersTextPaint.setColor(getResources().getColor(android.R.color.white));
        mNumbersTextPaint.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "Exo-SemiBold.otf"));
    }

    private void drawQuarterIndicators(Canvas c) {

        mBasePaint.setStrokeWidth(5);

        for(int rotation=-120, count=15; rotation<=120; rotation+=16, count--) {

            c.save();
            c.rotate(rotation, c.getWidth() / 2, c.getHeight() / 2);

            if(count<=1) {
                mBasePaint.setColor(COLOR_RED);
            }

            c.drawLine(
                    c.getWidth() / 2,
                    (float) (c.getHeight() / 2 - mOuterBorderRadius + (mOuterBorderRadius * 0.06)),
                    c.getWidth() / 2,
                    (float) (c.getHeight() / 2 - mOuterBorderRadius + (mOuterBorderRadius * 0.09)),
                    mBasePaint);

            c.restore();
        }

        mBasePaint.setColor(getResources().getColor(android.R.color.white));
    }

    private void drawHalfIndicators(Canvas c) {

        mBasePaint.setStrokeWidth(12);

        for(int rotation=-112, count=7; rotation<=112; rotation+=32, count--) {

            c.save();
            c.rotate(rotation, c.getWidth() / 2, c.getHeight() / 2);

            if(count == 0) {
                mBasePaint.setColor(COLOR_RED);
            }

            c.drawLine(
                    c.getWidth() / 2,
                    (float) (c.getHeight() / 2 - mOuterBorderRadius + (mOuterBorderRadius * 0.06)),
                    c.getWidth() / 2,
                    (float) (c.getHeight() / 2 - mOuterBorderRadius + (mOuterBorderRadius * 0.12)),
                    mBasePaint);

            c.restore();
        }

        mBasePaint.setColor(getResources().getColor(android.R.color.white));
    }

    private void drawMainIndicators(Canvas c) {

        mBasePaint.setStrokeWidth(15);
        mBasePaint.setStyle(Paint.Style.FILL);
        mBasePaint.setPathEffect(new CornerPathEffect(4));

        if(mMainIndicatorPath == null) {
            createMainIndicatorPath(c);
        }

        for(int rotation=-128, count=8; rotation <=128; rotation+=32, count--) {

            c.save();
            c.rotate(rotation, c.getWidth() / 2, c.getHeight() / 2);

            if(count<=1) {
                mBasePaint.setColor(COLOR_RED);
            }

            c.drawPath(mMainIndicatorPath, mBasePaint);

            c.restore();
        }

        resetBasePaint();
    }

    private void createMainIndicatorPath(Canvas c) {

        float upperY = (float)(c.getHeight()/2 - mOuterBorderRadius + (mOuterBorderRadius* 0.06));
        float lowerY = (float)(c.getHeight()/2 - mOuterBorderRadius + (mOuterBorderRadius* 0.15));

        mMainIndicatorPath = new Path();

        mMainIndicatorPath.moveTo((c.getWidth() / 2) - 12, upperY);
        mMainIndicatorPath.lineTo((c.getWidth() / 2) + 12, upperY);
        mMainIndicatorPath.lineTo((c.getWidth()/2) + 6, lowerY);
        mMainIndicatorPath.lineTo((c.getWidth() / 2) - 6, lowerY);
        mMainIndicatorPath.lineTo((c.getWidth() / 2) - 12, upperY);

        mMainIndicatorPath.close();
    }

    private void drawOuterBorder(Canvas c) {

        mBasePaint.setStrokeWidth(20);

        mOuterBorderRadius = (int)((Math.min(c.getWidth() / 2, c.getHeight() / 2)) - (mBasePaint.getStrokeWidth()/2));
        c.drawCircle(c.getWidth() / 2, c.getHeight() / 2, mOuterBorderRadius, mBasePaint);
    }

    private void drawInnerCircle(Canvas c) {

        mBasePaint.setStrokeWidth(6);
        int innerRadius = (int)(mOuterBorderRadius * 0.06);

        c.drawCircle(c.getWidth() / 2, c.getHeight() / 2, innerRadius, mBasePaint);
    }

    private void resetBasePaint() {
        mBasePaint.setColor(getResources().getColor(android.R.color.white));
        mBasePaint.setStyle(Paint.Style.STROKE);
        mBasePaint.setPathEffect(null);
    }

}
