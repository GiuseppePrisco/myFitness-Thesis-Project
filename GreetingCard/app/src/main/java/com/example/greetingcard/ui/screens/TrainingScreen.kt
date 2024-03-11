package com.example.greetingcard.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
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
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date


@Composable
fun RepsDataColumn(
    dataList: List<RepsData>,
    modifier: Modifier = Modifier
) {
    /*Column(
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
        //contentPadding = PaddingValues(4.dp),
        modifier = modifier.fillMaxWidth()
    ) {*/
    /*LazyVerticalGrid(
        columns = GridCells.Adaptive(150.dp),
        contentPadding = PaddingValues(4.dp),
        modifier = modifier.fillMaxWidth()
    ) {*/

    for (data in dataList) {
        Card(
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {

                /*Text(
                    text = data.id,
                    modifier = Modifier
                        .weight(1f)
                )*/

                Text(
                    text = data.exerciseName,
                    modifier = Modifier
                        .weight(1f)
                )
                Text(
                    text = data.repCount.toString(),
                )
            }
        }
    }

}



@Composable
fun TrainingScreen(
    repsUiState: RepsUiState,
    homeViewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {

    Column(
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(8.dp)
            .verticalScroll(rememberScrollState())
    ) {

        val activity = LocalContext.current as Activity

        Text(
            text = "Repetitions data recorded in the Application",
            fontSize = 50.sp,
            lineHeight = 55.sp,
            textAlign = TextAlign.Center,
            /*modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)*/
        )

        when (repsUiState) {
            is RepsUiState.Loading -> LoadingScreen(modifier = modifier.fillMaxSize())
            is RepsUiState.Success ->
                if (repsUiState.dataList.size != 0) {
                    RepsDataColumn(
                        dataList = repsUiState.dataList,
                        //onCurrentExerciseSelected = onCurrentExerciseSelected,
                        modifier = modifier
                    )
                }
                else {
                    Text(
                        text = "No Reps data found"
                    )

                }

            is RepsUiState.Error -> Text(text = "Unable to load Reps data")
            //is RepsUiState.Error -> ErrorScreen(retryAction, modifier = modifier.fillMaxSize())
        }


        Button(
            onClick = { activity.startActivity(
                Intent(activity, TrainingActivity(homeViewModel)::class.java)
            )
            },
            // onClick = { homeViewModel.getApiData("random", mapOf("title" to "hello")) },
            modifier = Modifier
                .padding(20.dp)
        ) {
            Text(
                text = "Training"
            )
        }



    }

    SystemBackButtonHandler {
        MyAppRouter.navigateTo(Screen.HomeScreen)
    }

    /*val context = LocalContext.current
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
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = {
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
        Text(
            text = "$capturedImageUri"
        )

        val scope = rememberCoroutineScope()
        Button(
            onClick = {
                scope.launch{
                    detect(context, capturedImageUri)}
                }

        ) {
            Text(text = "Detect Pose")
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
    }*/

}


fun detect(context: Context, uri: Uri) {
    val options = AccuratePoseDetectorOptions.Builder()
        .setDetectorMode(AccuratePoseDetectorOptions.SINGLE_IMAGE_MODE)
        .build()

    Log.d("", "OPTIONS $options")

    val poseDetector = PoseDetection.getClient(options)

    var image: InputImage = InputImage.fromFilePath(context, uri)
    try {
        image = InputImage.fromFilePath(context, uri)
    } catch (e: IOException) {
        e.printStackTrace()
    }

    Log.d("", "IMAGE $image")

    val result = poseDetector.process(image)
        .addOnSuccessListener { pose ->
            // Task completed successfully
            // ...
            val allPoseLandmarks = pose.getAllPoseLandmarks()

            val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
            val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
            val leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
            val rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)
            val leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)
            val rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)
            val leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
            val rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)
            val leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE)
            val rightKnee = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE)
            val leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE)
            val rightAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE)
            val leftPinky = pose.getPoseLandmark(PoseLandmark.LEFT_PINKY)
            val rightPinky = pose.getPoseLandmark(PoseLandmark.RIGHT_PINKY)
            val leftIndex = pose.getPoseLandmark(PoseLandmark.LEFT_INDEX)
            val rightIndex = pose.getPoseLandmark(PoseLandmark.RIGHT_INDEX)
            val leftThumb = pose.getPoseLandmark(PoseLandmark.LEFT_THUMB)
            val rightThumb = pose.getPoseLandmark(PoseLandmark.RIGHT_THUMB)
            val leftHeel = pose.getPoseLandmark(PoseLandmark.LEFT_HEEL)
            val rightHeel = pose.getPoseLandmark(PoseLandmark.RIGHT_HEEL)
            val leftFootIndex = pose.getPoseLandmark(PoseLandmark.LEFT_FOOT_INDEX)
            val rightFootIndex = pose.getPoseLandmark(PoseLandmark.RIGHT_FOOT_INDEX)
            val nose = pose.getPoseLandmark(PoseLandmark.NOSE)
            val leftEyeInner = pose.getPoseLandmark(PoseLandmark.LEFT_EYE_INNER)
            val leftEye = pose.getPoseLandmark(PoseLandmark.LEFT_EYE)
            val leftEyeOuter = pose.getPoseLandmark(PoseLandmark.LEFT_EYE_OUTER)
            val rightEyeInner = pose.getPoseLandmark(PoseLandmark.RIGHT_EYE_INNER)
            val rightEye = pose.getPoseLandmark(PoseLandmark.RIGHT_EYE)
            val rightEyeOuter = pose.getPoseLandmark(PoseLandmark.RIGHT_EYE_OUTER)
            val leftEar = pose.getPoseLandmark(PoseLandmark.LEFT_EAR)
            val rightEar = pose.getPoseLandmark(PoseLandmark.RIGHT_EAR)
            val leftMouth = pose.getPoseLandmark(PoseLandmark.LEFT_MOUTH)
            val rightMouth = pose.getPoseLandmark(PoseLandmark.RIGHT_MOUTH)

            Log.d("","LEFT EAR ${leftEar}")
            Log.d("","LEFT EAR POSITION ${leftEar?.position}")
        }
        .addOnFailureListener { e ->
            // Do Nothing
        }

    Log.d("", "RESULT $result")
}


/*fun Context.createImageFile(): File {
    // Create an image file name
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    val imageFileName = "JPEG_" + timeStamp + "_"
    val image = File.createTempFile(
        imageFileName,
        ".jpg",
        externalCacheDir
    )
    return image
}*/


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TrainingScreenPreview() {
    GreetingCardTheme {
        val mockData = List(5) { RepsData(
            "0",
            "Jumping Jacks",
            1000
        )}
        TrainingScreen(
            RepsUiState.Success(mockData),
            viewModel(),
            modifier = Modifier.fillMaxSize()
        )
    }
}

/*
bodyPart:"back"
equipment:"cable"
gifUrl:"https://v2.exercisedb.io/image/3646YaBraTZNSB"
id:"0007"
name:"alternate lateral pulldown"
target:"lats"
secondaryMuscles:
0:"biceps"
1:"rhomboids"
instructions:
0:"Sit on the cable machine with your back straight and feet flat on the ground."
1:"Grasp the handles with an overhand grip, slightly wider than shoulder-width apart."
2:"Lean back slightly and pull the handles towards your chest, squeezing your shoulder blades together."
3:"Pause for a moment at the peak of the movement, then slowly release the handles back to the starting position."
4:"Repeat for the desired number of repetitions."
 */