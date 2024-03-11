package com.example.greetingcard

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.example.greetingcard.ui.screens.createImageFile
import com.example.greetingcard.ui.theme.GreetingCardTheme
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import java.util.Objects


class CameraActivity() : AppCompatActivity() {

    private val TAG = CameraActivity::class.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "onCreate")

        val isOpenCVInitialized = OpenCVLoader.initDebug()
        if (isOpenCVInitialized){
            Log.d(TAG, "OpenCV was successfully initialized")
        }
        else {
            Log.d(TAG, "OpenCV did NOT successfully initialize")
        }



        setContent {
            GreetingCardTheme {
                val scope1 = rememberCoroutineScope()
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    //CameraScreen()

                    val context = LocalContext.current
                    val file = context.createImageFile()
                    val uri = FileProvider.getUriForFile(
                        Objects.requireNonNull(context),
                        BuildConfig.APPLICATION_ID + ".provider", file
                    )

                    var capturedImageUri by rememberSaveable {
                        mutableStateOf<Uri>(Uri.EMPTY)
                    }

                    val cameraLauncher =
                        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) {
                            capturedImageUri = uri
                        }

                    val permissionLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.RequestPermission()
                    ) {
                        if (it) {
                            Toast.makeText(context, "Permission Granted", Toast.LENGTH_SHORT).show()
                            cameraLauncher.launch(uri)
                        } else {
                            Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
                        }
                    }

                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(10.dp)
                            .verticalScroll(rememberScrollState()),
                            //.horizontalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Text(
                            text = "Here you can take a Photo and see many different effects!",
                            fontSize = 30.sp,
                            lineHeight = 35.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(40.dp))

                        Button(
                            onClick = {
                                val permissionCheckResult =
                                    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                                if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                                    cameraLauncher.launch(uri)
                                } else {
                                    // Request a permission
                                    permissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            }) {
                            Text(text = "Capture Image From Camera")
                        }
                        Log.d(TAG,"image path: $capturedImageUri")
                        /*Text(
                            text = "$capturedImageUri"
                        )*/


                        if (capturedImageUri.path?.isNotEmpty() == true) {

                            val originalBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), capturedImageUri)

                            if (originalBitmap != null) {

                                val modifier = Modifier.fillMaxWidth()
                                var myModifier = Modifier.fillMaxWidth()

                                if (originalBitmap.width < originalBitmap.height) {
                                    myModifier = modifier.then(Modifier.rotate(-90f))
                                }


                                Spacer(modifier = Modifier.height(40.dp))
                                Text(
                                    text = "Original Photo",
                                    modifier = Modifier.padding(8.dp)
                                )
                                AsyncImage(
                                    model = ImageRequest.Builder(context = LocalContext.current)
                                        .data(originalBitmap)
                                        .decoderFactory(ImageDecoderDecoder.Factory())
                                        .crossfade(true)
                                        .build(),
                                    error = painterResource(R.drawable.ic_broken_image),
                                    placeholder = painterResource(R.drawable.loading_img),
                                    contentDescription = null,
                                    modifier = myModifier
                                )

                                var bitmap = grayFilter(originalBitmap)
                                /*if (bitmap.width < bitmap.height) {
                                    myModifier = myModifier.then(Modifier.rotate(-90f))
                                }*/

                                Spacer(modifier = Modifier.height(40.dp))
                                Text(
                                    text = "Gray Filter",
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(8.dp)
                                )
                                AsyncImage(
                                    model = ImageRequest.Builder(context = LocalContext.current)
                                        .data(bitmap)
                                        .decoderFactory(ImageDecoderDecoder.Factory())
                                        .crossfade(true)
                                        .build(),
                                    error = painterResource(R.drawable.ic_broken_image),
                                    placeholder = painterResource(R.drawable.loading_img),
                                    contentDescription = null,
                                    modifier = modifier
                                )

                                bitmap = blurFilter(originalBitmap)

                                Spacer(modifier = Modifier.height(40.dp))
                                Text(
                                    text = "Blur Filter",
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(8.dp)
                                )
                                AsyncImage(
                                    model = ImageRequest.Builder(context = LocalContext.current)
                                        .data(bitmap)
                                        .decoderFactory(ImageDecoderDecoder.Factory())
                                        .crossfade(true)
                                        .build(),
                                    error = painterResource(R.drawable.ic_broken_image),
                                    placeholder = painterResource(R.drawable.loading_img),
                                    contentDescription = null,
                                    modifier = modifier
                                )

                                bitmap = gaussianBlurFilter(originalBitmap)

                                Spacer(modifier = Modifier.height(40.dp))
                                Text(
                                    text = "Gaussian Blur Filter",
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(8.dp)
                                )
                                AsyncImage(
                                    model = ImageRequest.Builder(context = LocalContext.current)
                                        .data(bitmap)
                                        .decoderFactory(ImageDecoderDecoder.Factory())
                                        .crossfade(true)
                                        .build(),
                                    error = painterResource(R.drawable.ic_broken_image),
                                    placeholder = painterResource(R.drawable.loading_img),
                                    contentDescription = null,
                                    modifier = modifier
                                )

                                bitmap = cannyFilter(originalBitmap)

                                Spacer(modifier = Modifier.height(40.dp))
                                Text(
                                    text = "Canny Filter",
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(8.dp)
                                )
                                AsyncImage(
                                    model = ImageRequest.Builder(context = LocalContext.current)
                                        .data(bitmap)
                                        .decoderFactory(ImageDecoderDecoder.Factory())
                                        .crossfade(true)
                                        .build(),
                                    error = painterResource(R.drawable.ic_broken_image),
                                    placeholder = painterResource(R.drawable.loading_img),
                                    contentDescription = null,
                                    modifier = modifier
                                )

                                bitmap = thresholdFilter(originalBitmap)

                                Spacer(modifier = Modifier.height(40.dp))
                                Text(
                                    text = "Threshold Filter",
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(8.dp)
                                )
                                AsyncImage(
                                    model = ImageRequest.Builder(context = LocalContext.current)
                                        .data(bitmap)
                                        .decoderFactory(ImageDecoderDecoder.Factory())
                                        .crossfade(true)
                                        .build(),
                                    error = painterResource(R.drawable.ic_broken_image),
                                    placeholder = painterResource(R.drawable.loading_img),
                                    contentDescription = null,
                                    modifier = modifier
                                )

                                bitmap = adaptiveThresholdFilter(originalBitmap)

                                Spacer(modifier = Modifier.height(40.dp))
                                Text(
                                    text = "Adaptive Threshold Filter",
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(8.dp)
                                )
                                AsyncImage(
                                    model = ImageRequest.Builder(context = LocalContext.current)
                                        .data(bitmap)
                                        .decoderFactory(ImageDecoderDecoder.Factory())
                                        .crossfade(true)
                                        .build(),
                                    error = painterResource(R.drawable.ic_broken_image),
                                    placeholder = painterResource(R.drawable.loading_img),
                                    contentDescription = null,
                                    modifier = modifier
                                )


                            }


                        }
                    }

                    /*SystemBackButtonHandler {
                        MyAppRouter.navigateTo(Screen.HomeScreen)
                    }*/



                }

            }
        }


    }


    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }


    private fun grayFilter(bitmap: Bitmap): Bitmap {

        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)

        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY)

        val grayBitmap = bitmap.copy(bitmap.config, true)
        Utils.matToBitmap(mat, grayBitmap)

        return grayBitmap
    }

    private fun blurFilter(bitmap: Bitmap, kSize: Size = Size(100.toDouble(), 120.toDouble()), anchor: Point = Point(10.0, 15.0)): Bitmap {

        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)

        Imgproc.blur(mat, mat, kSize, anchor)

        val blurBitmap = bitmap.copy(bitmap.config, true)
        Utils.matToBitmap(mat, blurBitmap)

        return blurBitmap
    }

    private fun gaussianBlurFilter(bitmap: Bitmap, kSize: Size = Size(125.toDouble(), 125.toDouble()), sigmaX: Double = 0.toDouble()): Bitmap {

        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)

        Imgproc.GaussianBlur(mat, mat, kSize, sigmaX)

        val gaussianBlurBitmap = bitmap.copy(bitmap.config, true)
        Utils.matToBitmap(mat, gaussianBlurBitmap)

        return gaussianBlurBitmap
    }

    private fun Mat.toGray(bitmap: Bitmap) {
        Utils.bitmapToMat(bitmap, this)
        Imgproc.cvtColor(this, this, Imgproc.COLOR_RGB2GRAY)
    }

    private fun cannyFilter(bitmap: Bitmap, threshold1: Double = 20.toDouble(), threshold2: Double = 255.toDouble()): Bitmap {

        val mat = Mat()
        //val grayBitmap = grayFilter(bitmap)
        //Utils.bitmapToMat(grayBitmap, mat)

        mat.toGray(bitmap)

        Imgproc.Canny(mat, mat, threshold1, threshold2)

        val cannyBitmap = bitmap.copy(bitmap.config, true)
        Utils.matToBitmap(mat, cannyBitmap)

        return cannyBitmap
    }

    private fun thresholdFilter(bitmap: Bitmap, thresh: Double = 50.toDouble(), maxVal: Double = 255.toDouble(), type:Int = Imgproc.THRESH_BINARY): Bitmap {

        val mat = Mat()
        //val grayBitmap = grayFilter(bitmap)
        //Utils.bitmapToMat(grayBitmap, mat)

        mat.toGray(bitmap)

        Imgproc.threshold(mat, mat, thresh, maxVal, type)

        val thresholdBitmap = bitmap.copy(bitmap.config, true)
        Utils.matToBitmap(mat, thresholdBitmap)

        return thresholdBitmap
    }

    private fun adaptiveThresholdFilter(bitmap: Bitmap, maxValue: Double = 255.toDouble(),
                                        adaptiveMethod: Int = Imgproc.ADAPTIVE_THRESH_MEAN_C,
                                        thresholdType: Int = Imgproc.THRESH_BINARY,
                                        blockSize: Int = 11,
                                        c: Double = 12.toDouble()): Bitmap {

        val mat = Mat()
        //val grayBitmap = grayFilter(bitmap)
        //Utils.bitmapToMat(grayBitmap, mat)

        mat.toGray(bitmap)

        Imgproc.adaptiveThreshold(mat, mat, maxValue, adaptiveMethod, thresholdType, blockSize, c)

        val adaptiveThresholdBitmap = bitmap.copy(bitmap.config, true)
        Utils.matToBitmap(mat, adaptiveThresholdBitmap)

        return adaptiveThresholdBitmap
    }


}

