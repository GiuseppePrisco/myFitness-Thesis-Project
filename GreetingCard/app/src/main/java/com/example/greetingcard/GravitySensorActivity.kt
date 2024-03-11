package com.example.greetingcard

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.os.Vibrator
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.marginBottom
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.core.view.marginTop


class GravitySensorActivity() : AppCompatActivity() {

    private val TAG = GravitySensorActivity::class.simpleName

    private var toneGenerator: ToneGenerator? = null
    private lateinit var sensorManager: SensorManager

    var ballDetector = object : SensorEventListener {
        override fun onSensorChanged(sensorEvent: SensorEvent) {}
        override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "onCreate")

        toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME)

        setContentView(R.layout.activity_sensors)
        val frame = findViewById<ImageView>(R.id.frame)
        val ball = findViewById<View>(R.id.ball)
        var scoreText: TextView = findViewById<TextView>(R.id.score_text)

        /*val frameWidth = frame.width
        val frameHeight = frame.height*/
        val frameMarginStart = frame.marginStart
        val frameMarginTop = frame.marginTop
        val frameMarginEnd = frame.marginEnd
        val frameMarginBottom = frame.marginBottom

        //Sets the radius
        val radius = 25

        //Gets the screens height and width
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val devHeight = displayMetrics.heightPixels
        val devWidth = displayMetrics.widthPixels

        //ball.x = devWidth / 2f - radius
        //ball.y = 0f

        var player1Score = 0
        var player2Score = 0

        Log.d("", "DEVICE W: $devWidth")
        Log.d("", "DEVICE H: $devHeight")
        /*Log.d("", "FRAME W: $frameWidth")
        Log.d("", "FRAME H: $frameHeight")*/
        Log.d("", "BALL x: ${ball.x}")
        Log.d("", "BALL y: ${ball.y}")
        Log.d("", "FRAME margin start: $frameMarginStart")
        Log.d("", "FRAME margin top: $frameMarginTop")


        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        val gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)

        ballDetector = object : SensorEventListener {

            override fun onSensorChanged(sensorEvent: SensorEvent) {

                val xPosition = ball.x
                val yPosition = ball.y

                val multiplier = 1

                /*Log.d("", "BALL W: ${ball.x}")
                Log.d("", "BALL H: ${ball.y}")*/

                //The coordinates the ball moves to
                val nextX = (xPosition - sensorEvent.values[0])*multiplier
                val nextY = (yPosition + sensorEvent.values[1])*multiplier
                //val nextY = yPosition


                //moves the ball LEFT
                if (nextX + radius >= frameMarginStart) {
                    ball.x = nextX
                } else {
                    onFrameHit()
                    ball.x = nextX + 35
                }

                //moves the ball to the TOP
                if (nextY + radius >= frameMarginTop) {
                    ball.y = nextY
                } else {
                    if (nextX + radius > devWidth / 3 && nextX + radius < devWidth * 2 / 3 ) {
                        player1Score++
                        scoreText.setText("Score: $player1Score - $player2Score")
                        ball.x = devWidth / 2f - radius
                        ball.y = devHeight / 2f - radius
                        onGoal()
                    }
                    else {
                        onFrameHit()
                        ball.y = nextY + 35
                    }

                }

                //moves the ball to the RIGHT
                if (nextX + radius > devWidth - frameMarginEnd) {
                    onFrameHit()
                    ball.x = nextX - 35
                }

                //moves the ball to the BOTTOM
                if (nextY + radius > devHeight - frameMarginBottom) {
                    if (nextX + radius > devWidth / 3 && nextX + radius < devWidth * 2 / 3 ) {
                        player2Score++
                        scoreText.setText("Score: $player1Score - $player2Score")
                        ball.x = devWidth / 2f - radius
                        ball.y = devHeight / 2f - radius
                        onGoal()
                    }
                    else {
                        onFrameHit()
                        ball.y = nextY - 35
                    }

                }
            }

            override fun onAccuracyChanged(sensor: Sensor, i: Int) {

            }


        }

        sensorManager.registerListener(ballDetector, gravitySensor, SensorManager.SENSOR_DELAY_GAME)

    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        sensorManager.unregisterListener(ballDetector)
    }


    private fun onFrameHit() {
        //Vibrator
        val vibrator = (getSystemService(VIBRATOR_SERVICE) as Vibrator)
        vibrator.vibrate(200)

        //Sound
        toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ONE_MIN_BEEP)
    }

    private fun onGoal() {
        //Vibrator
        val vibrator = (getSystemService(VIBRATOR_SERVICE) as Vibrator)
        vibrator.vibrate(400)

        //Sound
        toneGenerator?.startTone(ToneGenerator.TONE_CDMA_CONFIRM)
    }

}

