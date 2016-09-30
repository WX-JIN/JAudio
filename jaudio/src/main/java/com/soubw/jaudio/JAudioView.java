/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.soubw.jaudio;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.soubw.other.CheapSoundFile;


public class JAudioView extends View {

    /**
     * 波形上下边距距离
     */
    private int topAndBottomMargin = 42;
    /**
     * 进度圆和进度线的颜色画笔
     */
    private Paint progressPaint;

    /**
     * 已播放画笔
     */
    private Paint selectedLinePaint;
    /**
     * 未播放画笔
     */
    private Paint unSelectedLinePaint;

    /**
     * 当前音频播放状态（关联画图状态）
     */
    private JAudioStatus jAudioStatus;


    //private JAudioFile jAudioFile;
    private CheapSoundFile cheapSoundFile;

    
    private int[] mLenByZoomLevel;
    private double[][] mValuesByZoomLevel;
    private double[] mZoomFactorByZoomLevel;
    private int[] mHeightsAtThisZoomLevel;
    private int mZoomLevel;
    private int mSampleRate;
    private int mSamplesPerFrame;
    private int mOffset;
    
    private int mSelectionStart;
    private int mSelectionEnd;
    /**
     * 音频当前播放位置
     */
    private int currentLocation  = -1;
    /**
     * 加载文件是否已经完成
     */
    private boolean isFinishLoadFile = false;

    public JAudioStatus getJAudioStatus() {
        return jAudioStatus;
    }

    public void setJAudioStatus(JAudioStatus jAudioStatus) {
        this.jAudioStatus = jAudioStatus;
    }
    
	public int getTopAndBottomMargin() {
		return topAndBottomMargin;
	}

	public void setTopAndBottomMargin(int topAndBottomMargin) {
		this.topAndBottomMargin = topAndBottomMargin;
	}
    
	public JAudioView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // We don't want keys, the markers get these
        setFocusable(false);

        progressPaint = new Paint();
        progressPaint.setColor(Color.rgb(246, 131, 126));
        progressPaint.setAntiAlias(true);
        
        selectedLinePaint = new Paint();
        selectedLinePaint.setAntiAlias(false);
        selectedLinePaint.setColor(getResources().getColor(R.color.waveform_selected));

        unSelectedLinePaint = new Paint();
        unSelectedLinePaint.setAntiAlias(false);
        unSelectedLinePaint.setColor(getResources().getColor(R.color.waveform_unselected));

        mLenByZoomLevel = null;
        mValuesByZoomLevel = null;
        mHeightsAtThisZoomLevel = null;
        mOffset = 0;
        currentLocation = -1;
        mSelectionStart = 0;
        mSelectionEnd = 0;
    }
    
/*    public void setAudioFile(JAudioFile file) {
        jAudioFile = file;
        mSampleRate = jAudioFile.getSampleRate();
        mSamplesPerFrame = jAudioFile.getSamplesPerFrame();
        computeDoublesForAllZoomLevels();
        mHeightsAtThisZoomLevel = null;
    }*/

    public void setAudioFile(CheapSoundFile c) {
        cheapSoundFile = c;
        mSampleRate = cheapSoundFile.getSampleRate();
        mSamplesPerFrame = cheapSoundFile.getSamplesPerFrame();
        computeDoublesForAllZoomLevels();
        mHeightsAtThisZoomLevel = null;
    }


    /**
     * @return 波形的总条数
     */
    public int getWaveFormCount() {
        return mLenByZoomLevel[mZoomLevel];
    }


/*    public double pixelsToSeconds(int pixels) {
        double z = mZoomFactorByZoomLevel[0];
        return (jAudioFile.getFrameCountFloat()*2 * (double)mSamplesPerFrame / (mSampleRate * z));
    }

    public int millisecsToPixels(int msecs) {
        double z = mZoomFactorByZoomLevel[mZoomLevel];
        return (int)((msecs * 1.0 * mSampleRate * z) / (1000.0 * mSamplesPerFrame) + 0.5);
    }

    public int pixelsToMillisecs(int pixels) {
        double z = mZoomFactorByZoomLevel[mZoomLevel];
        return (int)(pixels * (1000.0 * mSamplesPerFrame) / (mSampleRate * z) + 0.5);
    }
    
    public int pixelsToMillisecsTotal() {
        double z = mZoomFactorByZoomLevel[mZoomLevel];
        return (int)(jAudioFile.getFrameCountFloat() * (1000.0 * mSamplesPerFrame) / (mSampleRate * 1) + 0.5);
    }*/

    public int secondsToPixels(double seconds) {
        double z = mZoomFactorByZoomLevel[mZoomLevel];
        return (int) (z * seconds * mSampleRate / mSamplesPerFrame + 0.5);
    }

    public double pixelsToSeconds(int pixels) {
        double z = mZoomFactorByZoomLevel[mZoomLevel];
        return (pixels * (double) mSamplesPerFrame / (mSampleRate * z));
    }

    public int millisecsToPixels(int msecs) {
        double z = mZoomFactorByZoomLevel[mZoomLevel];
        return (int) ((msecs * 1.0 * mSampleRate * z) / (1000.0 * mSamplesPerFrame) + 0.5);
    }

    public int pixelsToMillisecs(int pixels) {
        double z = mZoomFactorByZoomLevel[mZoomLevel];
        return (int) (pixels * (1000.0 * mSamplesPerFrame) / (mSampleRate * z) + 0.5);
    }

    public int pixelsToMillisecsTotal() {
        double z = mZoomFactorByZoomLevel[mZoomLevel];
        return (int) (cheapSoundFile.getFrameCount()/cheapSoundFile.getChannels() * (1000.0 * mSamplesPerFrame) / (mSampleRate * z) + 0.5);
    }
    public int getStart() {
        return mSelectionStart;
    }

    public int getEnd() {
        return mSelectionEnd;
    }

    public int getOffset() {
        return mOffset;
    }

    public void setCurrentLocation(int pos) {
        currentLocation = pos;
    }


    protected void drawWaveformLine(Canvas canvas, int x, int y0, int y1, Paint paint) {
    	int pos = getWaveFormCount();
    	float rat =((float)getMeasuredWidth()/pos);
        canvas.drawLine((int)(x*rat), y0, (int)(x*rat), y1, paint);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        Log.d("wxj0", "(i): ");
        if (!isFinishLoadFile)
            return;

        if (mHeightsAtThisZoomLevel == null)
            computeIntsForThisZoomLevel();

        // Draw waveform
        int start = mOffset;
        int width = mHeightsAtThisZoomLevel.length - start;
        int ctr = measuredHeight / 2;
        if (width > measuredWidth)
            width = measuredWidth;

        // Draw grid
        double onePixelInSecs = pixelsToSeconds(1);
        double fractionalSecs = mOffset * onePixelInSecs;
        int integerSecs = (int) fractionalSecs;
        int i = 0;
        while (i < width) {
            i++;
            fractionalSecs += onePixelInSecs;
            int integerSecsNew = (int) fractionalSecs;
            if (integerSecsNew != integerSecs) {
                integerSecs = integerSecsNew;
            }
        }
      
        // Draw waveform
        for (i = 0; i < getWaveFormCount(); i++) {
        	Paint paint;
            if (i + start <= currentLocation) {
                Log.d("wxj1", "(i): "+(i));
                Log.d("wxj1", "( getWaveFormCount()): "+ getWaveFormCount());
                Log.d("wxj3", "(i + start): "+(i + start));
                Log.d("wxj3", "i*getMeasuredWidth()/getWaveFormCount(): "+i*getMeasuredWidth()/ getWaveFormCount());
                Log.d("wxj2", "(i + start) < i*getMeasuredWidth()/getWaveFormCount(): "+((i + start) < i*getMeasuredWidth()/ getWaveFormCount()));
                paint = selectedLinePaint;
            } else {
                paint = unSelectedLinePaint;
            }
            paint.setStrokeWidth(2);//每条的宽度
           
            drawWaveformLine(canvas, i, (ctr - mHeightsAtThisZoomLevel[start + i]), (ctr + 1 + mHeightsAtThisZoomLevel[start + i]), paint);

            if (i + start == currentLocation) {
     	        canvas.drawCircle(i*getMeasuredWidth()/ getWaveFormCount(), topAndBottomMargin /4, topAndBottomMargin /4, progressPaint);// 上圆
     	        canvas.drawCircle(i*getMeasuredWidth()/ getWaveFormCount(), getMeasuredHeight()- topAndBottomMargin /4, topAndBottomMargin /4, progressPaint);// 下圆
     	        canvas.drawLine(i*getMeasuredWidth()/ getWaveFormCount(), 0, i*getMeasuredWidth()/ getWaveFormCount(), getMeasuredHeight(), progressPaint);//垂直的线
            }

        }

    }


    /**
     * 添加一个新的音频文件时调用一次
     * 计算并添加所有音频波形折叠的长度
     */
    private void computeDoublesForAllZoomLevels() {
/*        int numFrames = jAudioFile.getFrameCount();
        int[] frameGains = jAudioFile.getFrameGains();*/
        int numFrames = cheapSoundFile.getFrameCount();
        int[] frameGains = cheapSoundFile.getFrameGains();
        double[] smoothedGains = new double[numFrames];
        if (numFrames == 1) {
            smoothedGains[0] = frameGains[0];
        } else if (numFrames == 2) {
            smoothedGains[0] = frameGains[0];
            smoothedGains[1] = frameGains[1];
        } else if (numFrames > 2) {
            smoothedGains[0] = (
                (frameGains[0] / 2.0) +
                (frameGains[1] / 2.0));
            for (int i = 1; i < numFrames - 1; i++) {
                smoothedGains[i] = (
                    (frameGains[i - 1] / 3.0) +
                    (frameGains[i    ] / 3.0) +
                    (frameGains[i + 1] / 3.0));
            }
            smoothedGains[numFrames - 1] = (
                (frameGains[numFrames - 2] / 2.0) +
                (frameGains[numFrames - 1] / 2.0));
        }

        // Make sure the range is no more than 0 - 255
        double maxGain = 1.0;
        for (int i = 0; i < numFrames; i++) {
            if (smoothedGains[i] > maxGain) {
                maxGain = smoothedGains[i];
            }
        }
        double scaleFactor = 1.0;
        if (maxGain > 255.0) {
            scaleFactor = 255 / maxGain;
        }

        // Build histogram of 256 bins and figure out the new scaled max
        maxGain = 0;
        int gainHist[] = new int[256];
        for (int i = 0; i < numFrames; i++) {
            int smoothedGain = (int)(smoothedGains[i] * scaleFactor);
            if (smoothedGain < 0)
                smoothedGain = 0;
            if (smoothedGain > 255)
                smoothedGain = 255;
            if (smoothedGain > maxGain)
                maxGain = smoothedGain;

            gainHist[smoothedGain]++;
        }

        // Re-calibrate the min to be 5%
        double minGain = 0;
        int sum = 0;
        while (minGain < 255 && sum < numFrames / 20) {
            sum += gainHist[(int)minGain];
            minGain++;
        }

        // Re-calibrate the max to be 99%
        sum = 0;
        while (maxGain > 2 && sum < numFrames / 100) {
            sum += gainHist[(int)maxGain];
            maxGain--;
        }
        if(maxGain <=50){
        	maxGain = 80;
        }else if(maxGain>50 && maxGain < 120){
        maxGain = 142;
        }else{
        maxGain+=10;
        }

        // Compute the heights
        double[] heights = new double[numFrames];
        double range = maxGain - minGain;
        for (int i = 0; i < numFrames; i++) {
            double value = (smoothedGains[i] * scaleFactor - minGain) / range;
            if (value < 0.0)
                value = 0.0;
            if (value > 1.0)
                value = 1.0;
            heights[i] = value * value;
        }

        mLenByZoomLevel = new int[5];
        mZoomFactorByZoomLevel = new double[5];
        mValuesByZoomLevel = new double[5][];

        // Level 0 is doubled, with interpolated values
        mLenByZoomLevel[0] = numFrames * 2;
        System.out.println("ssnum"+numFrames);
        mZoomFactorByZoomLevel[0] = 2.0;
        mValuesByZoomLevel[0] = new double[mLenByZoomLevel[0]];
        if (numFrames > 0) {
            mValuesByZoomLevel[0][0] = 0.5 * heights[0];
            mValuesByZoomLevel[0][1] = heights[0];
        }
        for (int i = 1; i < numFrames; i++) {
            mValuesByZoomLevel[0][2 * i] = 0.5 * (heights[i - 1] + heights[i]);
            mValuesByZoomLevel[0][2 * i + 1] = heights[i];
        }

        // Level 1 is normal
        mLenByZoomLevel[1] = numFrames;
        mValuesByZoomLevel[1] = new double[mLenByZoomLevel[1]];
        mZoomFactorByZoomLevel[1] = 1.0;
        for (int i = 0; i < mLenByZoomLevel[1]; i++) {
            mValuesByZoomLevel[1][i] = heights[i];
        }

        // 3 more levels are each halved
        for (int j = 2; j < 5; j++) {
            mLenByZoomLevel[j] = mLenByZoomLevel[j - 1] / 2;
            mValuesByZoomLevel[j] = new double[mLenByZoomLevel[j]];
            mZoomFactorByZoomLevel[j] = mZoomFactorByZoomLevel[j - 1] / 2.0;
            for (int i = 0; i < mLenByZoomLevel[j]; i++) {
                mValuesByZoomLevel[j][i] =
                    0.5 * (mValuesByZoomLevel[j - 1][2 * i] +
                           mValuesByZoomLevel[j - 1][2 * i + 1]);
            }
        }


        if (numFrames > 5000) {
            mZoomLevel = 3;
        } else if (numFrames > 1000) {
            mZoomLevel = 2;
        } else if (numFrames > 300) {
            mZoomLevel = 1;
        } else {
            mZoomLevel = 0;
        }
        invalidate();
        if (listener !=null){
            isFinishLoadFile = true;
            listener.finishLoadFile();
        }

    }

    /**
     * Called the first time we need to draw when the zoom level has changed
     * or the screen is resized
     */
    private void computeIntsForThisZoomLevel() {
        int halfHeight = (getMeasuredHeight() / 2) - 1;
        mHeightsAtThisZoomLevel = new int[mLenByZoomLevel[mZoomLevel]];
        for (int i = 0; i < mLenByZoomLevel[mZoomLevel]; i++) {
            mHeightsAtThisZoomLevel[i] =
                (int)(mValuesByZoomLevel[mZoomLevel][i] * halfHeight);
        }
    }

    private OnJAudioViewListener listener;

    public void setOnJAudioViewListener(OnJAudioViewListener l){
        this.listener = l;
    }

    public interface OnJAudioViewListener {

        void finishLoadFile();

    }
}
