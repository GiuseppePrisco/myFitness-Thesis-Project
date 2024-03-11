package com.example.greetingcard

import android.graphics.Paint
import android.graphics.Color as color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.os.Vibrator
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.greetingcard.ui.screens.SensorScreen
import com.example.greetingcard.ui.theme.GreetingCardTheme
import kotlin.math.pow
import android.view.WindowManager
import android.view.Surface
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import java.util.Random


class RotationSensorActivity() : AppCompatActivity() {

    private val TAG = RotationSensorActivity::class.simpleName

    private val pitch = mutableStateOf(0f)
    private val roll = mutableStateOf(0f)
    private val yaw = mutableStateOf(0f)

    private val score = mutableStateOf(0)
    private val spawned = mutableStateOf(false)
    private val randomX = mutableStateOf(0f)
    private val randomY = mutableStateOf(0f)

    private var toneGenerator: ToneGenerator? = null
    private lateinit var myWindowManager: WindowManager
    private lateinit var sensorManager: SensorManager

    var rainbowDetector = object : SensorEventListener {
        override fun onSensorChanged(sensorEvent: SensorEvent) {}
        override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}
    }

    @OptIn(ExperimentalTextApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "onCreate")

        myWindowManager = this.window.windowManager
        toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME)

        setContent {
            GreetingCardTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Box(){

                        val textMeasurer = rememberTextMeasurer()
                        val style = TextStyle(
                            fontSize = 30.sp,
                            color = Color.Black,
                            //background = Color.Red.copy(alpha = 0.2f)
                        )

                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val canvasWidth = size.width
                            val canvasHeight = size.height

                            drawText(
                                textMeasurer = textMeasurer,
                                text = "Score: ${score.value}",
                                style = style,
                                topLeft = Offset(x = 5f, y = 5f)
                            )
                            drawLine(
                                start = Offset(x = 0f, y = canvasHeight/2f),
                                end = Offset(x = canvasWidth, y = canvasHeight/2f),
                                color = Color.Blue,
                                strokeWidth = 10f
                            )
                            drawLine(
                                start = Offset(x = canvasWidth/2f, y = 0f),
                                end = Offset(x = canvasWidth/2f, y = canvasHeight),
                                color = Color.Blue,
                                strokeWidth = 10f
                            )

                            val dx = ((roll.value*canvasWidth)/180f)
                            val dy = -((pitch.value*canvasHeight)/180f)

                            val center = Offset(x = canvasWidth/2f+dx, y = canvasHeight/2f+dy)
                            val radius = size.minDimension/8f

                            rotate(degrees = -yaw.value, center) {
                                drawCircle(
                                    Brush.sweepGradient(
                                        colors = listOf(
                                            Color(color.parseColor("#33004c")), Color(color.parseColor("#4600d2")),
                                            Color(color.parseColor("#0000ff")), Color(color.parseColor("#0099ff")),
                                            Color(color.parseColor("#00eeff")), Color(color.parseColor("#00FF7F")),
                                            Color(color.parseColor("#48FF00")), Color(color.parseColor("#B6FF00")),
                                            Color(color.parseColor("#FFD700")), Color(color.parseColor("#FF0000")),
                                            Color(color.parseColor("#33004c"))
                                        ),
                                        center = center
                                    ),
                                    radius = radius,
                                    center = center,
                                    style = Stroke(width = 25f)
                                )
                            }


                            if (!spawned.value) {
                                spawned.value = true
                                var legal = false

                                val minX = canvasWidth*1/4
                                val maxX = canvasWidth*3/4
                                val minY = canvasHeight*1/3
                                val maxY = canvasHeight*3/4

                                while (!legal) {
                                    val rX = Random()
                                    randomX.value = minX +rX.nextFloat() * (maxX - minX)
                                    val rY = Random()
                                    randomY.value = minY +rY.nextFloat() * (maxY - minY)

                                    legal = !((randomX.value < center.x + radius && randomX.value > center.x - radius)
                                            && (randomY.value < center.y + radius && randomY.value > center.y - radius))
                                }

                            }

                            drawCircle(
                                color = Color.Red,
                                radius = radius/4f,
                                center = Offset(x = randomX.value, y = randomY.value)
                            )

                            // ball is hit
                            if ( (randomX.value < center.x + radius && randomX.value > center.x - radius)
                                && (randomY.value < center.y + radius && randomY.value > center.y - radius) ) {
                                score.value++
                                spawned.value = false

                                val vibrator = (getSystemService(VIBRATOR_SERVICE) as Vibrator)
                                vibrator.vibrate(100)
                                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ONE_MIN_BEEP)
                            }

                        }
                    }

                }
            }
        }

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        rainbowDetector = object : SensorEventListener {

            override fun onSensorChanged(sensorEvent: SensorEvent) {

                val rotationMatrix = FloatArray(9)
                val rotationVector = FloatArray(3)
                val orientation = FloatArray(3)

                rotationVector[0] = sensorEvent.values[0]
                rotationVector[1] = sensorEvent.values[1]
                rotationVector[2] = sensorEvent.values[2]

                SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVector)

                /*val (worldAxisForDeviceAxisX, worldAxisForDeviceAxisY) = when (myWindowManager.defaultDisplay.rotation) {
                    Surface.ROTATION_0 -> Pair(SensorManager.AXIS_X, SensorManager.AXIS_Y)
                    Surface.ROTATION_90 -> Pair(SensorManager.AXIS_MINUS_Y, SensorManager.AXIS_X)
                    Surface.ROTATION_180 -> Pair(SensorManager.AXIS_MINUS_X, SensorManager.AXIS_MINUS_Y)
                    Surface.ROTATION_270 -> Pair(SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X)
                    else -> Pair(SensorManager.AXIS_X, SensorManager.AXIS_Y)
                }

                val adjustedRotationMatrix = FloatArray(9)

                SensorManager.remapCoordinateSystem(rotationMatrix, worldAxisForDeviceAxisX,
                    worldAxisForDeviceAxisY, adjustedRotationMatrix)*/

                SensorManager.getOrientation(rotationMatrix, orientation)

                pitch.value = Math.toDegrees(orientation[1].toDouble()).toFloat()
                roll.value = Math.toDegrees(orientation[2].toDouble()).toFloat()
                yaw.value = Math.toDegrees(orientation[0].toDouble()).toFloat()

            }

            override fun onAccuracyChanged(sensor: Sensor, i: Int) {

            }

        }

        sensorManager.registerListener(rainbowDetector, rotationSensor, SensorManager.SENSOR_DELAY_FASTEST)

    }


    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        sensorManager.unregisterListener(rainbowDetector)
    }

}

