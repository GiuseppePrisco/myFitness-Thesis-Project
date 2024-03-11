package com.example.greetingcard.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.example.greetingcard.BuildConfig
import com.example.greetingcard.R
import com.example.greetingcard.TrainingActivity
import com.example.greetingcard.model.RepsData
import com.example.greetingcard.ui.HomeViewModel
import com.example.greetingcard.ui.RepsUiState
import com.example.greetingcard.ui.navigation.MyAppRouter
import com.example.greetingcard.ui.navigation.Screen
import com.example.greetingcard.ui.navigation.SystemBackButtonHandler
import com.example.greetingcard.ui.theme.GreetingCardTheme
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.buffer
import okio.sink
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Objects


@Composable
fun CameraScreen(
    homeViewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {

    //val scope = rememberCoroutineScope()

    val context = LocalContext.current
    //val uri = context.createUri()
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
            //realPath = getPathFromContentUri(context, capturedImageUri)!!
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
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
            Text(text = "Capture Image to be sent to the Server ")
        }
        Text(
            text = "uri: $capturedImageUri"
        )
        Text(
            text = "uri.path: ${capturedImageUri.path}"
        )

        val scope = rememberCoroutineScope()
        Button(
            onClick = {
                scope.launch{
                    detect(context, capturedImageUri)
                }
            }

        ) {
            Text(text = "Detect Pose")
        }


        val resolutions = listOf(400, 800, 1200, 1600, 2000)

        Column {
            resolutions.forEach { resolution ->
                Button(
                    onClick = {
                        try {
                            Log.d("Performance", "--------------------------------------------------")
                            Log.d("Performance", "Processing a new image (resized)")

                            // Resize the image
                            val startResizeImage = System.currentTimeMillis()
                            val resizedBitmap = resizeImage(context, capturedImageUri, resolution)
                            val endResizeImage = System.currentTimeMillis()
                            Log.d("Performance", "Time taken to resize image: ${endResizeImage - startResizeImage} ms")

                            // Convert the resized bitmap to a byte array
                            val startCompress = System.currentTimeMillis()
                            val byteArrayOutputStream = ByteArrayOutputStream()
                            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
                            val byteArray = byteArrayOutputStream.toByteArray()
                            val endCompress = System.currentTimeMillis()
                            Log.d("Performance", "Time taken to compress image: ${endCompress - startCompress} ms")

                            // Create the RequestBody from the byte array
                            val startRequestBody = System.currentTimeMillis()
                            val requestFile = byteArray.toRequestBody("multipart/form-data".toMediaTypeOrNull())
                            val body = MultipartBody.Part.createFormData("image", "upload.jpg", requestFile!!)
                            val endRequestBody = System.currentTimeMillis()
                            Log.d("Performance", "Time taken to create request body: ${endRequestBody - startRequestBody} ms")

                            // TODO DO NOT REMOVE THIS CODE - IT WAS THE PREVIOUS VERSION OF THE FUNCTIONING CODE
                            /*val inputStream = context.contentResolver.openInputStream(capturedImageUri)
                            val requestFile = inputStream?.use { it.readBytes() }?.toRequestBody("multipart/form-data".toMediaTypeOrNull())
                            val body = MultipartBody.Part.createFormData("image", "upload.jpg", requestFile!!)*/


                            // TODO DO NOT REMOVE THIS CODE - IT US USED TO SAVE A TEST IMAGE
                            /*val outputFile = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "test.jpg")
                            val outputStream = FileOutputStream(outputFile)
                            val bufferedSink = outputStream.sink().buffer()
                            requestFile?.writeTo(bufferedSink)
                            Log.d("Upload", "Image written to ${outputFile.absolutePath}")*/


                            homeViewModel.uploadImage(body)

                        } catch (e: IOException) {
                            // Handle the exception
                            Log.e("Upload", "Error uploading image", e)
                        }
                    }
                ) {
                    Text(text = "Upload Image at $resolution px")
                }
            }
        }

        Button(
            onClick = {
                try {
                    Log.d("Performance", "--------------------------------------------------")
                    Log.d("Performance", "Processing a new image (not resized)")

                    // Create the RequestBody from the byte array
                    val startRequestBody = System.currentTimeMillis()
                    val inputStream = context.contentResolver.openInputStream(capturedImageUri)
                    val requestFile = inputStream?.use { it.readBytes() }?.toRequestBody("multipart/form-data".toMediaTypeOrNull())
                    val body = MultipartBody.Part.createFormData("image", "upload.jpg", requestFile!!)
                    val endRequestBody = System.currentTimeMillis()
                    Log.d("Performance", "Time taken to create request body: ${endRequestBody - startRequestBody} ms")

                    homeViewModel.uploadImage(body)

                } catch (e: IOException) {
                    // Handle the exception
                    Log.e("Upload", "Error uploading image", e)
                }
            }
        ) {
            Text(text = "Upload Image at original resolution")
        }
        


        if (capturedImageUri.path?.isNotEmpty() == true) {

            AsyncImage(
                model = ImageRequest.Builder(context = LocalContext.current)
                    .data(capturedImageUri)
                    //.data("https://v2.exercisedb.io/image/3646YaBraTZNSB")
                    .decoderFactory(ImageDecoderDecoder.Factory())
                    .crossfade(true)
                    .build(),
                error = painterResource(R.drawable.ic_broken_image),
                placeholder = painterResource(R.drawable.loading_img),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth()
            )

        }
    }

    SystemBackButtonHandler {
        MyAppRouter.navigateTo(Screen.HomeScreen)
    }



}


fun resizeImage(context: Context, uri: Uri, maxWidth: Int): Bitmap {
    val inputStream = context.contentResolver.openInputStream(uri)
    val originalBitmap = BitmapFactory.decodeStream(inputStream)
    val originalWidth = originalBitmap.width
    val originalHeight = originalBitmap.height
    val aspectRatio = originalWidth.toFloat() / originalHeight.toFloat()
    val newHeight = maxWidth / aspectRatio
    return Bitmap.createScaledBitmap(originalBitmap, maxWidth, newHeight.toInt(), false)
}


fun getPathFromContentUri(context: Context, contentUri: Uri): String? {
    var filePath: String? = null
    val projection = arrayOf(MediaStore.Images.Media.DATA)
    context.contentResolver.query(contentUri, projection, null, null, null)?.apply {
        val columnIndex = getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        if (moveToFirst()) {
            filePath = getString(columnIndex)
        }
        close()
    }
    return filePath?.let { "file://$it" }
}


// ORIGINAL
fun Context.createImageFile(): File {
    // Create an image file name
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    val imageFileName = "JPEG_" + timeStamp + "_"
    val image = File.createTempFile(
        imageFileName,
        ".jpg",
        externalCacheDir
    )
    return image
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CameraScreenPreview() {
    GreetingCardTheme {
        val mockData = List(5) { RepsData(
            "0",
            "Jumping Jacks",
            1000
        )}
        CameraScreen(
            viewModel(),
            modifier = Modifier.fillMaxSize()
        )
    }
}

