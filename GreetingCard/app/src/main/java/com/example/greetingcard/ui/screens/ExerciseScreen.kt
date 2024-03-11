package com.example.greetingcard.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
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
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.example.greetingcard.R
import com.example.greetingcard.model.ExerciseData
import com.example.greetingcard.ui.navigation.MyAppRouter
import com.example.greetingcard.ui.navigation.Screen
import com.example.greetingcard.ui.navigation.SystemBackButtonHandler
import com.example.greetingcard.ui.theme.GreetingCardTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandableCard(
    title: String,
    description: List<String>,
    modifier: Modifier = Modifier
) {

    var expandedState by rememberSaveable { mutableStateOf(false) }
    val rotationState by animateFloatAsState(
        targetValue = if (expandedState) 180f else 0f, label = ""
    )

    Card(
        // elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(4.dp),
        onClick = {
            expandedState = !expandedState
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
                .padding(12.dp, 0.dp, 12.dp, 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    /*
                    fontSize = titleFontSize,
                    fontWeight = titleFontWeight,
                     */
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(6f),
                )
                IconButton(
                    onClick = {
                        expandedState = !expandedState
                    },
                    modifier = Modifier
                        .weight(1f)
                        .alpha(0.7f)
                        .rotate(rotationState),
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Drop-Down Arrow"
                    )
                }
            }
            if (expandedState) {
                if (description.size != 0) {
                    val bullet = "\u2022"
                    val paragraphStyle = ParagraphStyle(textIndent = TextIndent(restLine = 12.sp))
                    description.forEach {
                        Text(
                            buildAnnotatedString {
                                withStyle(style = paragraphStyle) {
                                    append(bullet)
                                    append("\t\t")
                                    append(it)
                                }
                            },
                            /*
                            text = "\t\t\t\u2022\t\t\t$it",
                            style = TextStyle(
                                lineBreak = "\t"
                            ),
                            */
                            modifier = Modifier
                                .padding(4.dp)


                            /*
                            fontSize = descriptionFontSize,
                            fontWeight = descriptionFontWeight,
                            maxLines = descriptionMaxLines,
                            overflow = TextOverflow.Ellipsis
                             */
                        )
                    }
                }
                else {
                    Text(
                        text = "No additional information",
                        modifier = Modifier
                            .padding(4.dp)
                    )
                }

            }

        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseScreen(
    currentExercise: ExerciseData,
    onMenuButtonClicked: () -> Unit,
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
                .padding(8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                //text = "currentExercise.name",
                text = currentExercise.name,
                modifier = Modifier
                    .padding(4.dp)
            )

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
                    .data(currentExercise.gifUrl)
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Body Part:",
                    modifier = Modifier
                        .weight(1f)
                )
                Text(
                    text = currentExercise.bodyPart,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Equipment:",
                    modifier = Modifier
                        .weight(1f)
                )
                Text(
                    text = currentExercise.equipment,
                )
            }
            ExpandableCard(
                title = "Instructions",
                description = currentExercise.instructions
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Target Muscle:",
                    modifier = Modifier
                        .weight(1f)
                )
                Text(
                    text = currentExercise.target,
                )
            }
            ExpandableCard(
                title = "Secondary Muscles",
                description = currentExercise.secondaryMuscles
            )

        }

    }

    SystemBackButtonHandler {
        MyAppRouter.navigateTo(Screen.ResultScreen)
    }

}



@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ExerciseScreenPreview() {
    GreetingCardTheme {
        val mockData = ExerciseData(
            "back",
            "cable",
            gifUrl = "https://v2.exercisedb.io/image/3646YaBraTZNSB",
            id = "0007",
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
        )
        ExerciseScreen(
            mockData,
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