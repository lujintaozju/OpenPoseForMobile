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

import android.app.Activity
import android.support.annotation.IntegerRes
import android.util.Log

import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import java.util.Vector
import java.util.Arrays
import java.nio.file.Paths
import java.io.IOException

class Human {
  var parts_coords = Array(18) { IntArray(2) }
  var coords_index_set = IntArray(18)
  var coords_index_assigned = BooleanArray(18)

  companion object {
    var parts = Arrays.asList("nose", "neck", "rShoulder", "rElbow", "rWist", "lShoulder", "lElbow",
            "lWrist", "rHip", "rKnee", "rAnkle", "lHip", "lKnee", "lAnkle", "rEye", "lEye", "rEar", "lEar")
  }
}
/**
 * Pose Estimator
 */
class ImageClassifierFloatInception private constructor(
    activity: Activity,
    imageSizeX: Int,
    imageSizeY: Int,
    private val outputW: Int,
    private val outputH: Int,
    modelPath: String,
    numBytesPerChannel: Int = 4 // a 32bit float value requires 4 bytes
  ) : ImageClassifier(activity, imageSizeX, imageSizeY, modelPath, numBytesPerChannel) {

  /**
   * An array to hold inference results, to be feed into Tensorflow Lite as outputs.
   * This isn't part of the super class, because we need a primitive array here.
   */
  private val heatMapArray: Array<Array<Array<FloatArray>>> =
    Array(1) { Array(outputW) { Array(outputH) { FloatArray(57) } } }

  private var mMat: Mat? = null

  private val NMS_Threshold = 0.05f
  private val Local_PAF_Threshold = 0.25f
  private val Part_Score_Threshold = 0.2f
  private val PAF_Count_Threshold = 5
  private val Part_Count_Threshold = 3
  private val MapHeight: Int = 54
  private val MapWidth: Int = 46
  private val HeatMapCount = 19
  private val MaxPairCount = 17
  private val PafMapCount = 38
  private val MaximumFilterSize = 5
  val CocoPairs = arrayOf(intArrayOf(1, 2), intArrayOf(1, 5), intArrayOf(2, 3), intArrayOf(3, 4), intArrayOf(5, 6), intArrayOf(6, 7), intArrayOf(1, 8), intArrayOf(8, 9), intArrayOf(9, 10), intArrayOf(1, 11), intArrayOf(11, 12), intArrayOf(12, 13), intArrayOf(1, 0), intArrayOf(0, 14), intArrayOf(14, 16), intArrayOf(0, 15), intArrayOf(15, 17))
  private val CocoPairsNetwork = arrayOf(intArrayOf(12, 13), intArrayOf(20, 21), intArrayOf(14, 15), intArrayOf(16, 17), intArrayOf(22, 23), intArrayOf(24, 25), intArrayOf(0, 1), intArrayOf(2, 3), intArrayOf(4, 5), intArrayOf(6, 7), intArrayOf(8, 9), intArrayOf(10, 11), intArrayOf(28, 29), intArrayOf(30, 31), intArrayOf(34, 35), intArrayOf(32, 33), intArrayOf(36, 37), intArrayOf(18, 19), intArrayOf(26, 27))
  private val outputNames: Array<String>? = null
  private val float_image: FloatArray? = null
  private val output_tensor: FloatArray? = null
  private val inWidth: Int = 0
  private val inHeight: Int = 0

  override fun addPixelValue(pixelValue: Int) {
    //bgr
    imgData!!.putFloat((pixelValue and 0xFF).toFloat())
    imgData!!.putFloat((pixelValue shr 8 and 0xFF).toFloat())
    imgData!!.putFloat((pixelValue shr 16 and 0xFF).toFloat())
  }

  override fun getProbability(labelIndex: Int): Float {
    //    return heatMapArray[0][labelIndex];
    return 0f
  }

  override fun setProbability(
    labelIndex: Int,
    value: Number
  ) {
    //    heatMapArray[0][labelIndex] = value.floatValue();
  }

  override fun getNormalizedProbability(labelIndex: Int): Float {
    return getProbability(labelIndex)
  }

  override fun runInference() {
    tflite?.run(imgData!!, heatMapArray)
    var mPrintTemp=Array(18) { FloatArray(2) }
    mPrintPointArray= Vector()
    if (mPrintPointArray!!.size == 0)
      mPrintPointArray!!.addElement(mPrintTemp)
    else { Log.d("BREAKING!!!!!","Not Cleaned!!") }
    if (!CameraActivity.isOpenCVInit)
      return

    // Gaussian Filter 5*5
    if (mMat == null)
      mMat = Mat(outputW, outputH, CvType.CV_32F)

    val tempArray = FloatArray(outputW * outputH)
    //Log.d("TfLiteCameraDemo","length = " + Integer.toString(outputW * outputH))
    val outTempArray = FloatArray(outputW * outputH)

    val coordinates = arrayOfNulls<Vector<IntArray> >(HeatMapCount-1)
    // eliminate duplicate part recognitions
    for (i in 0 until HeatMapCount - 1)
    {
      coordinates[i]=Vector()
      for (j in 0 until MapHeight)
      {
        for (k in 0 until MapWidth)
        {
          var max_value = 0f
          for (dj in -(MaximumFilterSize - 1) / 2 until (MaximumFilterSize + 1) / 2) {
            if (j + dj >= MapHeight || j + dj < 0) {
              break
            }
            for (dk in -(MaximumFilterSize - 1) / 2 until (MaximumFilterSize + 1) / 2) {
              if (k + dk >= MapWidth || k + dk < 0) {
                break
              }
              val value = heatMapArray[0][k + dk][j + dj][i]
              if (value > max_value) {
                max_value = value
              }
            }
          }
          if (max_value > NMS_Threshold)
          {
            if (max_value == heatMapArray[0][k][j][i])
            {
              coordinates[i]!!.addElement(intArrayOf(j, k))
              //Log.i("TestOutPut", "pic[$i] ($j,$k)")
            }
          }
        }
      }
    }


    // eliminate duplicate connections
    val pairs = arrayOfNulls<Vector<IntArray>>(MaxPairCount)
    val pairs_final = arrayOfNulls<Vector<IntArray>>(MaxPairCount)
    val pairs_scores = arrayOfNulls<Vector<Float>>(MaxPairCount)
    val pairs_scores_final = arrayOfNulls<Vector<Float>>(MaxPairCount)
    for (i in 0 until MaxPairCount) {
      pairs[i] = Vector()
      pairs_scores[i] = Vector()
      pairs_final[i] = Vector()
      pairs_scores_final[i] = Vector()
      val part_set = Vector<Int>()
      for (p1 in 0 until coordinates[CocoPairs[i][0]]!!.size) {
        for (p2 in 0 until coordinates[CocoPairs[i][1]]!!.size) {
          var count = 0
          var score = 0.0f
          val scores = FloatArray(10)
          val p1x = coordinates[CocoPairs[i][0]]!![p1][0]
          val p1y = coordinates[CocoPairs[i][0]]!![p1][1]
          val p2x = coordinates[CocoPairs[i][1]]!![p2][0]
          val p2y = coordinates[CocoPairs[i][1]]!![p2][1]
          val dx = (p2x - p1x).toFloat()
          val dy = (p2y - p1y).toFloat()
          val normVec = Math.sqrt(Math.pow(dx.toDouble(), 2.0) + Math.pow(dy.toDouble(), 2.0)).toFloat()
          if (normVec < 0.0001f) {    //p1==p2, Point of the same type
            break
          }

          val vx = dx / normVec
          val vy = dy / normVec
          for (t in 0..9) {
            val tx = (p1x.toFloat().toDouble() + (t * dx / 9).toDouble() + 0.5).toInt() //round
            val ty = (p1y.toFloat().toDouble() + (t * dy / 9).toDouble() + 0.5).toInt()
            scores[t] = vy * heatMapArray[0][ty][tx][HeatMapCount + CocoPairsNetwork[i][1]]
            scores[t] += vx * heatMapArray[0][ty][tx][HeatMapCount + CocoPairsNetwork[i][0]]  //Dot Product
          }
          for (h in 0..9) {
            if (scores[h] > Local_PAF_Threshold) {
              count += 1
              score += scores[h]
            }
          }

          //Log.i("Score "+Integer.toString(CocoPairs[i][0])+" -> "+Integer.toString(CocoPairs[i][1]), "($p1x,$p1y) -> ($p2x,$p2y) : $score  Count =  $count")
          if (score > Part_Score_Threshold && count > PAF_Count_Threshold) {
            var inserted = false
            val pair = intArrayOf(p1, p2)
            for (l in 0 until pairs[i]!!.size) {
              if (score > pairs_scores[i]!![l]) {             // Scores from large to small
                pairs[i]!!.insertElementAt(pair, l)
                pairs_scores[i]!!.insertElementAt(score, l)
                inserted = true
                break
              }
            }
            if (!inserted) {
              pairs[i]!!.addElement(pair)
              pairs_scores[i]!!.addElement(score)
            }
          }
        }
      }
      for (m in 0 until pairs[i]!!.size) {
        var conflict = false
        for (n in 0 until part_set!!.size) {
          if (pairs[i]!![m][0] == part_set!![n] || pairs[i]!![m][1] == part_set!![n]) {    //Conflict with Matched Points
            conflict = true
            break
          }
        }
        if (!conflict) {
          pairs_final[i]!!.addElement(pairs[i]!![m])
          pairs_scores_final[i]!!.addElement(pairs_scores[i]!![m])
          part_set.addElement(pairs[i]!![m][0])
          part_set.addElement(pairs[i]!![m][1])
        }
      }
    }

    val humans = Vector<Human>()
    val humans_final = Vector<Human>()
    for (i in 0 until MaxPairCount) {
      for (j in 0 until pairs_final[i]!!.size) {
        var merged = false
        val p1 = CocoPairs[i][0]
        val p2 = CocoPairs[i][1]
        val ip1 = pairs_final[i]!![j][0]
        val ip2 = pairs_final[i]!![j][1]
        for (k in 0 until humans.size) {
          val human = humans[k]
          if (ip1 == human.coords_index_set[p1] && human.coords_index_assigned[p1] || ip2 == human.coords_index_set[p2] && human.coords_index_assigned[p2]) {
            human.parts_coords[p1] = coordinates[p1]!![ip1]
            human.parts_coords[p2] = coordinates[p2]!![ip2]
            human.coords_index_set[p1] = ip1
            human.coords_index_set[p2] = ip2
            human.coords_index_assigned[p1] = true
            human.coords_index_assigned[p2] = true
            merged = true
            break
          }
        }
        if (!merged) {
          val human = Human()
          human.parts_coords[p1] = coordinates[p1]!![ip1]
          human.parts_coords[p2] = coordinates[p2]!![ip2]
          human.coords_index_set[p1] = ip1
          human.coords_index_set[p2] = ip2
          human.coords_index_assigned[p1] = true
          human.coords_index_assigned[p2] = true
          humans.addElement(human)
        }
      }
    }

    // remove people with too few parts
    for (i in 0 until humans.size) {
      var human_part_count = 0
      for (j in 0 until HeatMapCount - 1) {
        if (humans[i].coords_index_assigned[j]) {
          //Log.i("TestOutPut", "("+Integer.toString(humans[i].parts_coords[j][0])+","+Integer.toString(humans[i].parts_coords[j][1])+")")
          human_part_count += 1
        }
      }
      Log.i("TestOutPut", "Human Part Count =  $human_part_count")
      if (human_part_count > Part_Count_Threshold) {
        humans_final.addElement(humans[i])
      }
    }

    Log.i("TestOutPut", "Human_Final_num = "+Integer.toString(humans_final.size))


    for (i in 0 until humans_final.size) {
      for (j in 0 until HeatMapCount - 1) {
        if (humans_final[i].coords_index_assigned[j]) {
          mPrintPointArray!![i][j][0] = humans_final[i].parts_coords[j][0].toFloat()
          mPrintPointArray!![i][j][1] = humans_final[i].parts_coords[j][1].toFloat()
          //Log.i("TestOutPut", "(" + Integer.toString(humans_final[i].parts_coords[j][0]) + "," + Integer.toString(humans_final[i].parts_coords[j][1]) + ")")
        }
      }
      //Log.i("TestOutPut", "  ")
      if (i < humans_final.size-1) {
        var mPrintTemp0=Array(18) { FloatArray(2) }
        mPrintPointArray!!.addElement(mPrintTemp0)
      }
    }
    /*for (i in 0 until mPrintPointArray!!.size) {
      for (j in 0 until HeatMapCount - 1) {
        Log.i("mPrintArray when PeoNum = $i , J = $j ", Integer.toString(Math.round(mPrintPointArray!![i][j][0]))+","+Integer.toString(Math.round(mPrintPointArray!![i][j][1])))
      }
    }*/

  }

  private operator fun get(
    x: Int,
    y: Int,
    arr: FloatArray
  ): Float {
    return if (x < 0 || y < 0 || x >= outputW || y >= outputH) -1f else arr[x * outputW + y]
  }

  companion object {

    /**
     * Create ImageClassifierFloatInception instance
     *
     * @param imageSizeX Get the image size along the x axis.
     * @param imageSizeY Get the image size along the y axis.
     * @param outputW The output width of model
     * @param outputH The output height of model
     * @param modelPath Get the name of the model file stored in Assets.
     * @param numBytesPerChannel Get the number of bytes that is used to store a single
     * color channel value.
     */
    fun create(
      activity: Activity,
      imageSizeX: Int = 432,
      imageSizeY: Int = 368,
      outputW: Int = 46,
      outputH: Int = 54,
      modelPath: String = "graph.tflite",
      numBytesPerChannel: Int = 4
    ): ImageClassifierFloatInception =
      ImageClassifierFloatInception(
          activity,
          imageSizeX,
          imageSizeY,
          outputW,
          outputH,
          modelPath,
          numBytesPerChannel)
  }
}

