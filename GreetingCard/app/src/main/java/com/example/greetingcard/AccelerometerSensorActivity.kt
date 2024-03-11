package com.example.greetingcard

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.greetingcard.ui.screens.SensorScreen
import com.example.greetingcard.ui.theme.GreetingCardTheme
import kotlin.math.pow


class AccelerometerSensorActivity() : AppCompatActivity() {

    private val TAG = AccelerometerSensorActivity::class.simpleName

    private var toneGenerator: ToneGenerator? = null
    private lateinit var sensorManager: SensorManager

    var stepDetector = object : SensorEventListener {
        override fun onSensorChanged(sensorEvent: SensorEvent) {}
        override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}
    }

    //val homeViewModel = homeViewModel

    private val STEP_COUNTER = "stepCounter"

    private var previousIntensity: Float = 0f
    //private var stepCounter = homeViewModel.stepCounter
    private var stepCounter = 0

    //private var updateStepCounter = updateStepCounter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "onCreate")

        if (savedInstanceState != null) {
            stepCounter = savedInstanceState.getInt(STEP_COUNTER, 0)
        }

        toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME)

        setContent {
            GreetingCardTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    SensorScreen(stepCounter)
                }
            }
        }

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        val accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        stepDetector = object : SensorEventListener {

            override fun onSensorChanged(sensorEvent: SensorEvent) {

                if (sensorEvent != null) {

                    val accelerationVector = FloatArray(3)

                    accelerationVector[0] = sensorEvent.values[0]
                    accelerationVector[1] = sensorEvent.values[1]
                    accelerationVector[2] = sensorEvent.values[2]

                    val currentIntensity = kotlin.math.sqrt(accelerationVector[0].pow(2) + accelerationVector[1].pow(2) + accelerationVector[2].pow(2))
                    val intensityDelta = currentIntensity - previousIntensity
                    previousIntensity = currentIntensity

                    if (intensityDelta > 4) {

                        stepCounter++

                        updateStepCounter()

                        setContent {
                            GreetingCardTheme {
                                // A surface container using the 'background' color from the theme
                                Surface(
                                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                                ) {
                                    SensorScreen(stepCounter)
                                }
                            }
                        }

                    }

                }
            }

            override fun onAccuracyChanged(sensor: Sensor, i: Int) {

            }

        }

        sensorManager.registerListener(stepDetector, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL)

    }


    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        sensorManager.unregisterListener(stepDetector)
    }

    private fun updateStepCounter() {

        /*val vibrator = (getSystemService(VIBRATOR_SERVICE) as Vibrator)
        vibrator.vibrate(100)


        if (stepCounter % 10 == 0) {
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ONE_MIN_BEEP)
        }*/

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putInt(STEP_COUNTER, stepCounter)
    }

}

