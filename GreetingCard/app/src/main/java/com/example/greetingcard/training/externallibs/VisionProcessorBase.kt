/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.greetingcard.training.externallibs

import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.Image.Plane
import android.os.Build.VERSION_CODES
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import androidx.annotation.GuardedBy
import androidx.annotation.RequiresApi
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.example.greetingcard.training.externallibs.GraphicOverlay.Graphic
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskExecutors
import com.google.android.gms.tasks.Tasks
import com.google.android.odml.image.MediaMlImageBuilder
import com.google.android.odml.image.MlImage
import com.google.mlkit.common.MlKitException
import com.google.mlkit.vision.common.InputImage
import java.io.ByteArrayOutputStream
import java.lang.Math.max
import java.lang.Math.min
import java.nio.ByteBuffer
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicBoolean


/**
 * Abstract base class for ML Kit frame processors. Subclasses need to implement {@link
 * #onSuccess(T, FrameMetadata, GraphicOverlay)} to define what they want to with the detection
 * results and {@link #detectInImage(VisionImage)} to specify the detector object.
 *
 * @param <T> The type of the detected feature.
 */
abstract class VisionProcessorBase<T>(context: Context) :
  VisionImageProcessor {

  companion object {
    const val MANUAL_TESTING_LOG = "LogTagForTest"
    private const val TAG = "VisionProcessorBase"
  }

  private var activityManager: ActivityManager =
    context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
  private val fpsTimer = Timer()
  private val executor =
    ScopedExecutor(TaskExecutors.MAIN_THREAD)

  // Whether this processor is already shut down
  private var isShutdown = false

  // Used to calculate latency, running in the same thread, no sync needed.
  private var numRuns = 0
  private var totalFrameMs = 0L
  private var maxFrameMs = 0L
  private var minFrameMs = Long.MAX_VALUE
  private var totalDetectorMs = 0L
  private var maxDetectorMs = 0L
  private var minDetectorMs = Long.MAX_VALUE

  // Frame count that have been processed so far in an one second interval to calculate FPS.
  private var frameProcessedInOneSecondInterval = 0
  private var framesPerSecond = 0

  // To keep the latest images and its metadata.
  @GuardedBy("this") private var latestImage: ByteBuffer? = null
  @GuardedBy("this") private var latestImageMetaData: FrameMetadata? = null
  // To keep the images and metadata in process.
  @GuardedBy("this") private var processingImage: ByteBuffer? = null
  @GuardedBy("this") private var processingMetaData: FrameMetadata? = null

  init {
    fpsTimer.scheduleAtFixedRate(
      object : TimerTask() {
        override fun run() {
          framesPerSecond = frameProcessedInOneSecondInterval
          frameProcessedInOneSecondInterval = 0
        }
      },
      0,
      1000
    )
  }

  // -----------------Code for processing live preview frame from CameraX API-----------------------
  @RequiresApi(VERSION_CODES.LOLLIPOP)
  @ExperimentalGetImage
  override fun processImageProxy(image: ImageProxy, graphicOverlay: GraphicOverlay) {
    val frameStartMs = SystemClock.elapsedRealtime()
    if (isShutdown) {
      return
    }
    var bitmap: Bitmap? = null
    if (!PreferenceUtils.isCameraLiveViewportEnabled(graphicOverlay.context)) {
      bitmap = BitmapUtils.getBitmap(image)
    }

    if (isMlImageEnabled(graphicOverlay.context)) {
      val mlImage =
        MediaMlImageBuilder(image.image!!).setRotation(image.imageInfo.rotationDegrees).build()
      requestDetectInImage(
        mlImage,
        graphicOverlay,
        /* originalCameraImage= */ bitmap,
        /* shouldShowFps= */ true,
        frameStartMs
      )
        // When the image is from CameraX analysis use case, must call image.close() on received
        // images when finished using them. Otherwise, new images may not be received or the camera
        // may stall.
        // Currently MlImage doesn't support ImageProxy directly, so we still need to call
        // ImageProxy.close() here.
        .addOnCompleteListener { image.close() }

      return
    }

    requestDetectInImage(
      InputImage.fromMediaImage(image.image!!, image.imageInfo.rotationDegrees),
      graphicOverlay,
      /* originalCameraImage= */ bitmap,
      /* shouldShowFps= */ true,
      frameStartMs
    )
      // When the image is from CameraX analysis use case, must call image.close() on received
      // images when finished using them. Otherwise, new images may not be received or the camera
      // may stall.
      .addOnCompleteListener { image.close() }
  }

  // -----------------Common processing logic-------------------------------------------------------
  private fun requestDetectInImage(
    image: InputImage,
    graphicOverlay: GraphicOverlay,
    originalCameraImage: Bitmap?,
    shouldShowFps: Boolean,
    frameStartMs: Long
  ): Task<T> {
    return setUpListener(
      detectInImage(image),
      graphicOverlay,
      originalCameraImage,
      shouldShowFps,
      frameStartMs
    )
  }

  private fun requestDetectInImage(
    image: MlImage,
    graphicOverlay: GraphicOverlay,
    originalCameraImage: Bitmap?,
    shouldShowFps: Boolean,
    frameStartMs: Long
  ): Task<T> {
    return setUpListener(
      detectInImage(image),
      graphicOverlay,
      originalCameraImage,
      shouldShowFps,
      frameStartMs
    )
  }

  private fun setUpListener(
    task: Task<T>,
    graphicOverlay: GraphicOverlay,
    originalCameraImage: Bitmap?,
    shouldShowFps: Boolean,
    frameStartMs: Long
  ): Task<T> {
    val detectorStartMs = SystemClock.elapsedRealtime()
    return task
      .addOnSuccessListener(
        executor,
        OnSuccessListener { results: T ->
          val endMs = SystemClock.elapsedRealtime()
          val currentFrameLatencyMs = endMs - frameStartMs
          val currentDetectorLatencyMs = endMs - detectorStartMs
          if (numRuns >= 500) {
            resetLatencyStats()
          }
          numRuns++
          frameProcessedInOneSecondInterval++
          totalFrameMs += currentFrameLatencyMs
          maxFrameMs = max(currentFrameLatencyMs, maxFrameMs)
          minFrameMs = min(currentFrameLatencyMs, minFrameMs)
          totalDetectorMs += currentDetectorLatencyMs
          maxDetectorMs = max(currentDetectorLatencyMs, maxDetectorMs)
          minDetectorMs = min(currentDetectorLatencyMs, minDetectorMs)

          // Only log inference info once per second. When frameProcessedInOneSecondInterval is
          // equal to 1, it means this is the first frame processed during the current second.
          if (frameProcessedInOneSecondInterval == 1) {
            Log.d(TAG, "Num of Runs: $numRuns")
            Log.d(
              TAG,
              "Frame latency: max=" +
                maxFrameMs +
                ", min=" +
                minFrameMs +
                ", avg=" +
                totalFrameMs / numRuns
            )
            Log.d(
              TAG,
              "Detector latency: max=" +
                maxDetectorMs +
                ", min=" +
                minDetectorMs +
                ", avg=" +
                totalDetectorMs / numRuns
            )
            val mi = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(mi)
            val availableMegs: Long = mi.availMem / 0x100000L
            Log.d(TAG, "Memory available in system: $availableMegs MB")
          }
          graphicOverlay.clear()
          if (originalCameraImage != null) {
            graphicOverlay.add(
              CameraImageGraphic(
                graphicOverlay,
                originalCameraImage
              )
            )
          }
          this@VisionProcessorBase.onSuccess(results, graphicOverlay)
          if (!PreferenceUtils.shouldHideDetectionInfo(graphicOverlay.context)) {
            graphicOverlay.add(
              InferenceInfoGraphic(
                graphicOverlay,
                currentFrameLatencyMs,
                currentDetectorLatencyMs,
                if (shouldShowFps) framesPerSecond else null
              )
            )
          }
          graphicOverlay.postInvalidate()
        }
      )
      .addOnFailureListener(
        executor,
        OnFailureListener { e: Exception ->
          graphicOverlay.clear()
          graphicOverlay.postInvalidate()
          val error = "Failed to process. Error: " + e.localizedMessage
          Toast.makeText(
              graphicOverlay.context,
              """
          $error
          Cause: ${e.cause}
          """.trimIndent(),
              Toast.LENGTH_SHORT
            )
            .show()
          Log.d(TAG, error)
          e.printStackTrace()
          this@VisionProcessorBase.onFailure(e)
        }
      )
  }

  override fun stop() {
    executor.shutdown()
    isShutdown = true
    resetLatencyStats()
    fpsTimer.cancel()
  }

  private fun resetLatencyStats() {
    numRuns = 0
    totalFrameMs = 0
    maxFrameMs = 0
    minFrameMs = Long.MAX_VALUE
    totalDetectorMs = 0
    maxDetectorMs = 0
    minDetectorMs = Long.MAX_VALUE
  }

  protected abstract fun detectInImage(image: InputImage): Task<T>

  protected open fun detectInImage(image: MlImage): Task<T> {
    return Tasks.forException(
      MlKitException(
        "MlImage is currently not demonstrated for this feature",
        MlKitException.INVALID_ARGUMENT
      )
    )
  }

  protected abstract fun onSuccess(results: T, graphicOverlay: GraphicOverlay)

  protected abstract fun onFailure(e: Exception)

  protected open fun isMlImageEnabled(context: Context?): Boolean {
    return false
  }
}


/** An interface to process the images with different vision detectors and custom image models.  */
interface VisionImageProcessor {
  /** Processes ImageProxy image data, e.g. used for CameraX live preview case.  */
  @Throws(MlKitException::class)
  fun processImageProxy(image: ImageProxy, graphicOverlay: GraphicOverlay)

  /** Stops the underlying machine learning model and release resources.  */
  fun stop()
}


/**
 * Wraps an existing executor to provide a [.shutdown] method that allows subsequent
 * cancellation of submitted runnables.
 */
class ScopedExecutor(private val executor: Executor) : Executor {
  private val shutdown = AtomicBoolean()
  override fun execute(command: Runnable) {
    // Return early if this object has been shut down.
    if (shutdown.get()) {
      return
    }
    executor.execute {

      // Check again in case it has been shut down in the mean time.
      if (shutdown.get()) {
        return@execute
      }
      command.run()
    }
  }

  /**
   * After this method is called, no runnables that have been submitted or are subsequently
   * submitted will start to execute, turning this executor into a no-op.
   *
   *
   * Runnables that have already started to execute will continue.
   */
  fun shutdown() {
    shutdown.set(true)
  }
}


/** Graphic instance for rendering inference info (latency, FPS, resolution) in an overlay view.  */
class InferenceInfoGraphic(
  private val overlay: GraphicOverlay,
  private val frameLatency: Long,
  private val detectorLatency: Long,
  // Only valid when a stream of input images is being processed. Null for single image mode.
  private var framesPerSecond: Int?
) : Graphic(overlay) {
  private val textPaint: Paint
  private var showLatencyInfo = true

  init {
    framesPerSecond = framesPerSecond
    textPaint = Paint()
    textPaint.color = TEXT_COLOR
    textPaint.textSize = TEXT_SIZE
    textPaint.setShadowLayer(5.0f, 0f, 0f, Color.BLACK)
    postInvalidate()
  }

  /** Creates an [InferenceInfoGraphic] to only display image size.  */
  constructor(overlay: GraphicOverlay) : this(overlay, 0, 0, null) {
    showLatencyInfo = false
  }

  @Synchronized
  override fun draw(canvas: Canvas) {
    val x = TEXT_SIZE * 0.5f
    val y = TEXT_SIZE * 1.5f
    canvas.drawText(
      "InputImage size: " + overlay.imageHeight + "x" + overlay.imageWidth,
      x,
      y,
      textPaint
    )
    if (!showLatencyInfo) {
      return
    }
    // Draw FPS (if valid) and inference latency
    if (framesPerSecond != null) {
      canvas.drawText(
        "FPS: $framesPerSecond, Frame latency: $frameLatency ms",
        x,
        y + TEXT_SIZE,
        textPaint
      )
    } else {
      canvas.drawText("Frame latency: $frameLatency ms", x, y + TEXT_SIZE, textPaint)
    }
    canvas.drawText(
      "Detector latency: $detectorLatency ms", x, y + TEXT_SIZE * 2, textPaint
    )
  }

  companion object {
    private const val TEXT_COLOR = Color.WHITE
    private const val TEXT_SIZE = 60.0f
  }
}


/** Draw camera image to background.  */
class CameraImageGraphic(overlay: GraphicOverlay?, private val bitmap: Bitmap) :
  Graphic(overlay) {
  override fun draw(canvas: Canvas) {
    canvas.drawBitmap(bitmap, transformationMatrix, null)
  }
}


/** Utils functions for bitmap conversions.  */
object BitmapUtils {
  private const val TAG = "BitmapUtils"

  /** Converts NV21 format byte buffer to bitmap.  */
  fun getBitmap(data: ByteBuffer, metadata: FrameMetadata): Bitmap? {
    data.rewind()
    val imageInBuffer = ByteArray(data.limit())
    data[imageInBuffer, 0, imageInBuffer.size]
    try {
      val image = YuvImage(
        imageInBuffer, ImageFormat.NV21, metadata.width, metadata.height, null
      )
      val stream = ByteArrayOutputStream()
      image.compressToJpeg(Rect(0, 0, metadata.width, metadata.height), 80, stream)
      val bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size())
      stream.close()
      return rotateBitmap(bmp, metadata.rotation, false, false)
    } catch (e: java.lang.Exception) {
      Log.e("VisionProcessorBase", "Error: " + e.message)
    }
    return null
  }

  /** Converts a YUV_420_888 image from CameraX API to a bitmap.  */
  @RequiresApi(VERSION_CODES.LOLLIPOP)
  @ExperimentalGetImage
  fun getBitmap(image: ImageProxy): Bitmap? {
    val frameMetadata = FrameMetadata.Builder()
      .setWidth(image.width)
      .setHeight(image.height)
      .setRotation(image.imageInfo.rotationDegrees)
      .build()
    val nv21Buffer = yuv420ThreePlanesToNV21(
      image.image!!.planes, image.width, image.height
    )
    return getBitmap(nv21Buffer, frameMetadata)
  }

  /** Rotates a bitmap if it is converted from a bytebuffer.  */
  private fun rotateBitmap(
    bitmap: Bitmap, rotationDegrees: Int, flipX: Boolean, flipY: Boolean
  ): Bitmap {
    val matrix = Matrix()

    // Rotate the image back to straight.
    matrix.postRotate(rotationDegrees.toFloat())

    // Mirror the image along the X or Y axis.
    matrix.postScale(if (flipX) -1.0f else 1.0f, if (flipY) -1.0f else 1.0f)
    val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

    // Recycle the old bitmap if it has changed.
    if (rotatedBitmap != bitmap) {
      bitmap.recycle()
    }
    return rotatedBitmap
  }

  /**
   * Converts YUV_420_888 to NV21 bytebuffer.
   *
   *
   * The NV21 format consists of a single byte array containing the Y, U and V values. For an
   * image of size S, the first S positions of the array contain all the Y values. The remaining
   * positions contain interleaved V and U values. U and V are subsampled by a factor of 2 in both
   * dimensions, so there are S/4 U values and S/4 V values. In summary, the NV21 array will contain
   * S Y values followed by S/4 VU values: YYYYYYYYYYYYYY(...)YVUVUVUVU(...)VU
   *
   *
   * YUV_420_888 is a generic format that can describe any YUV image where U and V are subsampled
   * by a factor of 2 in both dimensions. [Image.getPlanes] returns an array with the Y, U and
   * V planes. The Y plane is guaranteed not to be interleaved, so we can just copy its values into
   * the first part of the NV21 array. The U and V planes may already have the representation in the
   * NV21 format. This happens if the planes share the same buffer, the V buffer is one position
   * before the U buffer and the planes have a pixelStride of 2. If this is case, we can just copy
   * them to the NV21 array.
   */
  private fun yuv420ThreePlanesToNV21(
    yuv420888planes: Array<Plane>, width: Int, height: Int
  ): ByteBuffer {
    val imageSize = width * height
    val out = ByteArray(imageSize + 2 * (imageSize / 4))
    if (areUVPlanesNV21(yuv420888planes, width, height)) {
      // Copy the Y values.
      yuv420888planes[0].buffer[out, 0, imageSize]
      val uBuffer = yuv420888planes[1].buffer
      val vBuffer = yuv420888planes[2].buffer
      // Get the first V value from the V buffer, since the U buffer does not contain it.
      vBuffer[out, imageSize, 1]
      // Copy the first U value and the remaining VU values from the U buffer.
      uBuffer[out, imageSize + 1, 2 * imageSize / 4 - 1]
    } else {
      // Fallback to copying the UV values one by one, which is slower but also works.
      // Unpack Y.
      unpackPlane(yuv420888planes[0], width, height, out, 0, 1)
      // Unpack U.
      unpackPlane(yuv420888planes[1], width, height, out, imageSize + 1, 2)
      // Unpack V.
      unpackPlane(yuv420888planes[2], width, height, out, imageSize, 2)
    }
    return ByteBuffer.wrap(out)
  }

  /** Checks if the UV plane buffers of a YUV_420_888 image are in the NV21 format.  */
  private fun areUVPlanesNV21(planes: Array<Plane>, width: Int, height: Int): Boolean {
    val imageSize = width * height
    val uBuffer = planes[1].buffer
    val vBuffer = planes[2].buffer

    // Backup buffer properties.
    val vBufferPosition = vBuffer.position()
    val uBufferLimit = uBuffer.limit()

    // Advance the V buffer by 1 byte, since the U buffer will not contain the first V value.
    vBuffer.position(vBufferPosition + 1)
    // Chop off the last byte of the U buffer, since the V buffer will not contain the last U value.
    uBuffer.limit(uBufferLimit - 1)

    // Check that the buffers are equal and have the expected number of elements.
    val areNV21 = vBuffer.remaining() == 2 * imageSize / 4 - 2 && vBuffer.compareTo(uBuffer) == 0

    // Restore buffers to their initial state.
    vBuffer.position(vBufferPosition)
    uBuffer.limit(uBufferLimit)
    return areNV21
  }

  /**
   * Unpack an image plane into a byte array.
   *
   *
   * The input plane data will be copied in 'out', starting at 'offset' and every pixel will be
   * spaced by 'pixelStride'. Note that there is no row padding on the output.
   */
  private fun unpackPlane(
    plane: Plane, width: Int, height: Int, out: ByteArray, offset: Int, pixelStride: Int
  ) {
    val buffer = plane.buffer
    buffer.rewind()

    // Compute the size of the current plane.
    // We assume that it has the aspect ratio as the original image.
    val numRow = (buffer.limit() + plane.rowStride - 1) / plane.rowStride
    if (numRow == 0) {
      return
    }
    val scaleFactor = height / numRow
    val numCol = width / scaleFactor

    // Extract the data in the output buffer.
    var outputPos = offset
    var rowStart = 0
    for (row in 0 until numRow) {
      var inputPos = rowStart
      for (col in 0 until numCol) {
        out[outputPos] = buffer[inputPos]
        outputPos += pixelStride
        inputPos += plane.pixelStride
      }
      rowStart += plane.rowStride
    }
  }
}


/** Describing a frame info.  */
class FrameMetadata private constructor(val width: Int, val height: Int, val rotation: Int) {

  /** Builder of [FrameMetadata].  */
  class Builder {
    private var width = 0
    private var height = 0
    private var rotation = 0
    fun setWidth(width: Int): Builder {
      this.width = width
      return this
    }

    fun setHeight(height: Int): Builder {
      this.height = height
      return this
    }

    fun setRotation(rotation: Int): Builder {
      this.rotation = rotation
      return this
    }

    fun build(): FrameMetadata {
      return FrameMetadata(width, height, rotation)
    }
  }
}






