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

package com.soubw.other;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;


public class JWaveFormView extends View {

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
     * 音频当前播放位置
     */
    private int currentLocation = -1;
    /**
     * 总时长
     */
    private int allLocation = 100;


    public JWaveFormView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFocusable(false);
        progressPaint = new Paint();
        progressPaint.setColor(Color.rgb(246, 131, 126));
        progressPaint.setAntiAlias(true);

        selectedLinePaint = new Paint();
        selectedLinePaint.setAntiAlias(false);
        selectedLinePaint.setColor(Color.parseColor("#00BFA5"));

        unSelectedLinePaint = new Paint();
        unSelectedLinePaint.setAntiAlias(false);
        unSelectedLinePaint.setColor(Color.parseColor("#535353"));

        currentLocation = -1;
    }

    public void setCurrentLocation(int pos) {
        currentLocation = pos;
    }

    public void setAllLocation(int allPos) {
        allLocation = allPos;
    }

    public int getAllLocation(){
        return allLocation;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        for (int i = 0; i < allLocation; i++) {
            Paint paint;
            if (i <= currentLocation) {
                paint = selectedLinePaint;
            } else {
                paint = unSelectedLinePaint;
            }
            paint.setStrokeWidth(2);
            int currentX = i * measuredWidth / allLocation;
            int temp = (int) (Math.random() * 20) * ((measuredHeight-topAndBottomMargin*2) / 40);
            int currentY1 = measuredHeight / 2 - temp;
            int currentY2 = measuredHeight / 2 + temp;
            canvas.drawLine(currentX, currentY1, currentX, currentY2, paint);
            if (i == currentLocation) {
                canvas.drawCircle(currentX, topAndBottomMargin / 4, topAndBottomMargin / 4, progressPaint);
                canvas.drawCircle(currentX, getMeasuredHeight() - topAndBottomMargin / 4, topAndBottomMargin / 4, progressPaint);
                canvas.drawLine(currentX, 0, i * getMeasuredWidth() / allLocation, measuredHeight, progressPaint);
            }
        }
    }

}
