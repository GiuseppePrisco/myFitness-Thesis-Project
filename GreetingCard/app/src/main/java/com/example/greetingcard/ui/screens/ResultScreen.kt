package com.example.greetingcard.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.example.greetingcard.R
import com.example.greetingcard.model.ExerciseData
import com.example.greetingcard.ui.ExerciseUiState
import com.example.greetingcard.ui.navigation.MyAppRouter
import com.example.greetingcard.ui.navigation.Screen
import com.example.greetingcard.ui.navigation.SystemBackButtonHandler
import com.example.greetingcard.ui.theme.GreetingCardTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDataCard(
    data: ExerciseData,
    onCurrentExerciseSelected: (ExerciseData) -> Unit,
    modifier: Modifier = Modifier
) {

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(4.dp),
        onClick = {
            onCurrentExerciseSelected(data)
        },
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = tween(
                    durationMillis = 300,
                    easing = LinearOutSlowInEasing
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp, 4.dp, 12.dp, 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    //text = data.id,
                    text = data.name,
                    /*
                    fontSize = titleFontSize,
                    fontWeight = titleFontWeight,

                     */
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(6f)
                        .padding(0.dp, 0.dp,4.dp, 8.dp)
                )
                IconButton(
                    //onClick = { expandedState = !expandedState },
                    onClick = { onCurrentExerciseSelected(data) },
                    modifier = Modifier
                        .weight(1f)
                        .alpha(0.7f)
                        .rotate(225f),
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Drop-Down Arrow"
                    )
                }
            }

            val infiniteTransition = rememberInfiniteTransition(label = "")
            val angle by infiniteTransition.animateFloat(
                initialValue = 0F,
                targetValue = 360F,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = LinearEasing)
                ), label = ""
            )
            var myModifier = Modifier.fillMaxWidth()
            var loaded by rememberSaveable { mutableStateOf(false) }

            if (!loaded) {
                myModifier = myModifier.then(Modifier.rotate(angle))
            }
            else {
                //Do Nothing
            }
            AsyncImage(
                model = ImageRequest.Builder(context = LocalContext.current)
                    .data(data.gifUrl)
                    //.data("https://v2.exercisedb.io/image/3646YaBraTZNSB")
                    .decoderFactory(ImageDecoderDecoder.Factory())
                    .crossfade(true)
                    .build(),
                error = painterResource(R.drawable.ic_broken_image),
                placeholder = painterResource(R.drawable.loading_img),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                onLoading = { loaded = false },
                onSuccess = { loaded = true },
                onError = { loaded = true },
                modifier = myModifier.then(
                    Modifier.fillMaxWidth()
                )
            )
        }
    }
}

@Composable
fun ExerciseDataGrid(
    dataList: List<ExerciseData>,
    onCurrentExerciseSelected: (ExerciseData) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(150.dp),
        contentPadding = PaddingValues(4.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        items(items = dataList, key = { data -> data.id})  {data ->
            ExerciseDataCard(
                data = data,
                onCurrentExerciseSelected = onCurrentExerciseSelected,
                modifier = modifier
                    .padding(4.dp)
                    .fillMaxWidth()
                    .aspectRatio(0.7f)
            )
        }
    }
}


@Composable
fun NoResultScreen(
    exerciseName: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.ic_broken_image), contentDescription = "",
            modifier = Modifier
                .size(250.dp)
        )
        Text(text = "No results found for \"${exerciseName}\"", modifier = Modifier.padding(16.dp))
    }
}


@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0F,
        targetValue = 360F,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing)
        ), label = ""
    )
    Image(
        modifier = modifier
            .size(200.dp)
            .rotate(angle),
        painter = painterResource(R.drawable.loading_img),
        contentDescription = ""
    )
}


@Composable
fun ErrorScreen(
    retryAction: () -> Unit,
    modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_connection_error), contentDescription = ""
        )
        Text(text = "Loading failed", modifier = Modifier.padding(16.dp))
        Button(onClick = retryAction) {
            Text(text = "Retry")
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    exerciseUiState: ExerciseUiState,
    exerciseName: String,
    onMenuButtonClicked: () -> Unit,
    onCurrentExerciseSelected: (ExerciseData) -> Unit,
    retryAction: () -> Unit,
    modifier: Modifier = Modifier
) {

    Scaffold(
        topBar = {

        },
        bottomBar = {

        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onMenuButtonClicked
            ) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Menu"
                )
            }
        }
    ) { innerPadding ->

        Column(
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(innerPadding)
        ) {

            when (exerciseUiState) {
                is ExerciseUiState.Loading -> LoadingScreen(modifier = modifier.fillMaxSize())
                is ExerciseUiState.Success ->
                    if (exerciseUiState.dataList.size != 0) {
                        ExerciseDataGrid(
                            dataList = exerciseUiState.dataList,
                            onCurrentExerciseSelected = onCurrentExerciseSelected,
                            modifier = modifier
                        )
                    }
                    else {
                        NoResultScreen(
                            exerciseName = exerciseName,
                            modifier = modifier.fillMaxSize()
                        )
                    }

                is ExerciseUiState.Error -> ErrorScreen(retryAction, modifier = modifier.fillMaxSize())
            }

        }

    }

    SystemBackButtonHandler {
        MyAppRouter.navigateTo(Screen.HomeScreen)
    }

}



@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ResultScreenPreview() {
    GreetingCardTheme {
        val mockData = List(20) { ExerciseData(
            "back",
            "cable",
            gifUrl = "https://v2.exercisedb.io/image/3646YaBraTZNSB",
            id = "$it alternate lateral pulldown",
            "alternate lateral pulldown",
            "lats",
            listOf("biceps", "rhomboids"),
            listOf(
                "Sit on the cable machine with your back straight and feet flat on the ground.",
                "Grasp the handles with an overhand grip, slightly wider than shoulder-width apart.",
                "Lean back slightly and pull the handles towards your chest, squeezing your shoulder blades together.",
                "Pause for a moment at the peak of the movement, then slowly release the handles back to the starting position.",
                "Repeat for the desired number of repetitions."
            )
        ) }
        ResultScreen(
            //ExerciseUiState.Success(listOf()),
            ExerciseUiState.Success(mockData),
            "pippo",
            {},
            {},
            {},
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