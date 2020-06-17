/*
 * Copyright 2018 Zihua Zeng (edvard_hua@live.com), Lang Feng (tearjeaker@hotmail.com)
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

package com.edvard.poseestimation
import android.util.Log
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.Style.FILL
import android.graphics.PointF
import android.util.AttributeSet
import android.view.View
import java.util.ArrayList
import java.util.Vector

/**
 * Created by edvard on 18-3-23.
 */

class DrawView : View {

  private var mRatioWidth = 0
  private var mRatioHeight = 0

  private val mDrawPoint = ArrayList<PointF>()
  private var mWidth: Int = 0
  private var mHeight: Int = 0
  private var mRatioX: Float = 0.toFloat()
  private var mRatioY: Float = 0.toFloat()
  private var mImgWidth: Int = 0
  private var mImgHeight: Int = 0
  val CocoPairs = arrayOf(intArrayOf(1, 2), intArrayOf(1, 5), intArrayOf(2, 3), intArrayOf(3, 4), intArrayOf(5, 6), intArrayOf(6, 7), intArrayOf(1, 8), intArrayOf(8, 9), intArrayOf(9, 10), intArrayOf(1, 11), intArrayOf(11, 12), intArrayOf(12, 13), intArrayOf(1, 0), intArrayOf(0, 14), intArrayOf(14, 16), intArrayOf(0, 15), intArrayOf(15, 17))

  private val mColorArray = intArrayOf(
      resources.getColor(R.color.color_top, null),
      resources.getColor(R.color.color_neck, null),
      resources.getColor(R.color.color_l_shoulder, null),
      resources.getColor(R.color.color_l_elbow, null),
      resources.getColor(R.color.color_l_wrist, null),
      resources.getColor(R.color.color_r_shoulder, null),
      resources.getColor(R.color.color_r_elbow, null),
      resources.getColor(R.color.color_r_wrist, null),
      resources.getColor(R.color.color_l_hip, null),
      resources.getColor(R.color.color_l_knee, null),
      resources.getColor(R.color.color_l_ankle, null),
      resources.getColor(R.color.color_r_hip, null),
      resources.getColor(R.color.color_r_knee, null),
      resources.getColor(R.color.color_r_ankle, null),
      resources.getColor(R.color.color_background, null),
      resources.getColor(R.color.color_top, null),
      resources.getColor(R.color.color_neck, null),
      resources.getColor(R.color.color_l_shoulder, null),
      resources.getColor(R.color.color_l_elbow, null),
      resources.getColor(R.color.color_l_wrist, null),
      resources.getColor(R.color.color_r_shoulder, null),
      resources.getColor(R.color.color_r_elbow, null),
      resources.getColor(R.color.color_r_wrist, null),
      resources.getColor(R.color.color_l_hip, null),
      resources.getColor(R.color.color_l_knee, null),
      resources.getColor(R.color.color_l_ankle, null),
      resources.getColor(R.color.color_r_hip, null),
      resources.getColor(R.color.color_r_knee, null),
      resources.getColor(R.color.color_r_ankle, null),
      resources.getColor(R.color.color_background, null),
      resources.getColor(R.color.color_top, null),
      resources.getColor(R.color.color_neck, null),
      resources.getColor(R.color.color_l_shoulder, null),
      resources.getColor(R.color.color_l_elbow, null),
      resources.getColor(R.color.color_l_wrist, null),
      resources.getColor(R.color.color_r_shoulder, null),
      resources.getColor(R.color.color_r_elbow, null),
      resources.getColor(R.color.color_r_wrist, null),
      resources.getColor(R.color.color_l_hip, null),
      resources.getColor(R.color.color_l_knee, null),
      resources.getColor(R.color.color_l_ankle, null),
      resources.getColor(R.color.color_r_hip, null),
      resources.getColor(R.color.color_r_knee, null),
      resources.getColor(R.color.color_r_ankle, null),
      resources.getColor(R.color.color_background, null),
      resources.getColor(R.color.color_top, null),
      resources.getColor(R.color.color_neck, null),
      resources.getColor(R.color.color_l_shoulder, null),
      resources.getColor(R.color.color_l_elbow, null),
      resources.getColor(R.color.color_l_wrist, null),
      resources.getColor(R.color.color_r_shoulder, null),
      resources.getColor(R.color.color_r_elbow, null),
      resources.getColor(R.color.color_r_wrist, null),
      resources.getColor(R.color.color_l_hip, null),
      resources.getColor(R.color.color_l_knee, null),
      resources.getColor(R.color.color_l_ankle, null),
      resources.getColor(R.color.color_r_hip, null),
      resources.getColor(R.color.color_r_knee, null)
  )

  private val circleRadius: Float by lazy {
    dip(4).toFloat()
  }

  private val mPaint: Paint by lazy {
    Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG).apply {
      style = FILL
      strokeWidth = dip(2).toFloat()
      textSize = sp(13).toFloat()
    }
  }

  private val mPaint1: Paint by lazy {
    Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG).apply {
      style = FILL
      strokeWidth = dip(2).toFloat()
      textSize = sp(13).toFloat()
    }
  }

  constructor(context: Context) : super(context)

  constructor(
    context: Context,
    attrs: AttributeSet?
  ) : super(context, attrs)

  constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int
  ) : super(context, attrs, defStyleAttr)

  fun setImgSize(
    width: Int,
    height: Int
  ) {
    mImgWidth = width
    mImgHeight = height
    requestLayout()
  }

  /**
   * Scale according to the device.
   * @param point 2*14
   */
  fun setDrawPoint(
    point: Vector<Array<FloatArray>>,
    ratio: Float
  ) {
    mDrawPoint.clear()
    var tempX: Float
    var tempY: Float
    for (peo in 0 until point.size)
      for (i in 0..17) {
        tempX = point[peo][i][0] / ratio / mRatioX *96/54
        tempY = point[peo][i][1] / ratio / mRatioY *96/46
        //Log.d("TfLiteCameraDemo People $peo"," i = "+Integer.toString(i)+"  point 0,1 = "+Integer.toString(Math.round(tempX))+" , "+Integer.toString(Math.round(tempY)))
        mDrawPoint.add(PointF(tempX, tempY))
      }
  }

  /**
   * Sets the aspect ratio for this view. The size of the view will be measured based on the ratio
   * calculated from the parameters. Note that the actual sizes of parameters don't matter, that is,
   * calling setAspectRatio(2, 3) and setAspectRatio(4, 6) make the same result.
   *
   * @param width  Relative horizontal size
   * @param height Relative vertical size
   */
  fun setAspectRatio(
    width: Int,
    height: Int
  ) {
    if (width < 0 || height < 0) {
      throw IllegalArgumentException("Size cannot be negative.")
    }
    mRatioWidth = width
    mRatioHeight = height
    requestLayout()
  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    if (mDrawPoint.isEmpty()) return
    var nextPointF: PointF? = null
    var nowPointF: PointF? = null
    mPaint.color = 0xff6fa8dc.toInt()
    var PeopleNum = mDrawPoint.size / 18
    Log.d("People Number for Draw",Integer.toString(PeopleNum))
    for (People in 0 until PeopleNum) {
      var NBow=0;
      var NLie=0;
      nowPointF = mDrawPoint[People * 18 + 1]
      nextPointF = mDrawPoint[People * 18 + 8]
      if (nextPointF.x > 0.3f && nextPointF.y > 0.3f && nowPointF.x > 0.3f && nowPointF.y > 0.3f)
        if (Math.abs(nowPointF.x - nextPointF.x) > 0.7*Math.abs(nowPointF.y - nextPointF.y))
          mPaint.color = 0xffffff00.toInt()
      nowPointF = mDrawPoint[People * 18 + 1]
      nextPointF = mDrawPoint[People * 18 + 11]
      if (nextPointF.x > 0.3f && nextPointF.y > 0.3f && nowPointF.x > 0.3f && nowPointF.y > 0.3f)
        if (Math.abs(nowPointF.x - nextPointF.x) > 0.7*Math.abs(nowPointF.y - nextPointF.y))
          mPaint.color = 0xffffff00.toInt()
      nowPointF = mDrawPoint[People * 18 + 8]
      nextPointF = mDrawPoint[People * 18 + 9]
      if (nextPointF.x > 0.3f && nextPointF.y > 0.3f && nowPointF.x > 0.3f && nowPointF.y > 0.3f)
        if (Math.abs(nowPointF.x - nextPointF.x) > 0.7*Math.abs(nowPointF.y - nextPointF.y))
          mPaint.color = 0xff00ff00.toInt()
      nowPointF = mDrawPoint[People * 18 + 11]
      nextPointF = mDrawPoint[People * 18 + 12]
      if (nextPointF.x > 0.3f && nextPointF.y > 0.3f && nowPointF.x > 0.3f && nowPointF.y > 0.3f)
        if (Math.abs(nowPointF.x - nextPointF.x) > 0.7*Math.abs(nowPointF.y - nextPointF.y))
          mPaint.color = 0xff00ff00.toInt()

      for (i in 0..17) {
        nowPointF = mDrawPoint[People * 18 + i]
        if (nowPointF.x > 0.3f && nowPointF.y > 0.3f) {
          for (j in CocoPairs)
            if (j[0] == i) {
              nextPointF = mDrawPoint[People * 18 + j[1]]
              if (nextPointF.x > 0.3f && nextPointF.y > 0.3f) {
                //Log.i("People $People Point ", Integer.toString(People*18+i)+ " -> " + Integer.toString(People*18+j[1]))
                //Log.i("   ","$nowPointF --> $nextPointF")
                if ((nowPointF.x - nextPointF.x) * (nowPointF.x - nextPointF.x) + (nowPointF.y - nextPointF.y) * ((nowPointF.x - nextPointF.x)) > 50000)
                  Log.i("BREAKING NEWS!!", " Line Toooooo Long")
                else
                  canvas.drawLine(nowPointF.x, nowPointF.y, nextPointF.x, nextPointF.y, mPaint)
              }
            }
        }
      }

      mPaint.color = 0xff6fa8dc.toInt()
    }

    for ((index, pointF) in mDrawPoint.withIndex()) {
      mPaint.color = mColorArray[index % 18]
      //Log.d("TfLiteCameraDemo","Index = "+Integer.toString(index)+"  pointF XY = "+Integer.toString(Math.round(pointF.x))+" , "+Integer.toString(Math.round(pointF.y)))
      canvas.drawCircle(pointF.x, pointF.y, circleRadius, mPaint)
    }
  }

  override fun onMeasure(
    widthMeasureSpec: Int,
    heightMeasureSpec: Int
  ) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec)

    val width = View.MeasureSpec.getSize(widthMeasureSpec)
    val height = View.MeasureSpec.getSize(heightMeasureSpec)
    if (0 == mRatioWidth || 0 == mRatioHeight) {
      setMeasuredDimension(width, height)
    } else {
      if (width < height * mRatioWidth / mRatioHeight) {
        mWidth = width
        mHeight = width * mRatioHeight / mRatioWidth
      } else {
        mWidth = height * mRatioWidth / mRatioHeight
        mHeight = height
      }
    }

    setMeasuredDimension(mWidth, mHeight)
    Log.d("TfLiteCameraDemo","mImgWidth = "+Integer.toString(mImgWidth)+"  mImgHeight = "+Integer.toString(mImgHeight))
    Log.d("TfLiteCameraDemo","   mWidth = "+Integer.toString(mWidth)+"  mHeight = "+Integer.toString(mHeight))
    mRatioX = 192.0f / mWidth
    mRatioY = 192.0f / mHeight
  }
}



