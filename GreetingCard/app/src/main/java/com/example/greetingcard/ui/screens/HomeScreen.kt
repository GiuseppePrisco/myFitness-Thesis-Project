package com.example.greetingcard.ui.screens

import android.app.Activity
import android.content.Intent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.greetingcard.AccelerometerSensorActivity
import com.example.greetingcard.CameraActivity
import com.example.greetingcard.ui.HomeViewModel
import com.example.greetingcard.ui.theme.GreetingCardTheme


@Composable
fun RadioOptionsLayout(
    bodyPartSelected: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val radioOptions = listOf("back","cardio","chest","lower arms","lower legs",
        "neck","shoulders","waist","upper arms","upper legs")
    //var selectedOption by rememberSaveable { mutableStateOf(radioOptions[0]) }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        //verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.padding(8.dp),
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(

            ) {
                for (i in  0..radioOptions.size/2-1) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                                .selectable(
                                    selected = (radioOptions[i] == bodyPartSelected),
                                    onClick = {
                                        onOptionSelected(radioOptions[i])
                                    }
                                )
                    ) {
                        RadioButton(
                            selected = (radioOptions[i] == bodyPartSelected),
                            onClick = {
                                onOptionSelected(radioOptions[i])
                            },
                            /*colors = RadioButtonDefaults.colors(
                                selectedColor = Color.Green,
                                unselectedColor = Color.Magenta
                            )*/
                        )
                        Text(
                            text = radioOptions[i],
                            //style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 0.dp)
                        )
                    }
                }
            }
            Column(

            ) {
                for (i in  radioOptions.size/2..radioOptions.size-1) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .selectable(
                                selected = (radioOptions[i] == bodyPartSelected),
                                onClick = {
                                    onOptionSelected(radioOptions[i])
                                }
                            )
                    ) {
                        RadioButton(
                            selected = (radioOptions[i] == bodyPartSelected),
                            onClick = {
                                onOptionSelected(radioOptions[i])
                            },
                            /*colors = RadioButtonDefaults.colors(
                                selectedColor = Color.Green,
                                unselectedColor = Color.Magenta
                            )*/
                        )
                        Text(
                            text = radioOptions[i],
                            //style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 0.dp)
                        )
                    }
                }
            }
        }

    }


}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandableMenu(
    exerciseName: String,
    bodyPartSelected: String,
    onExerciseNameChanged: (String) -> Unit,
    onBodyPartSelectedChanged: (String) -> Unit,
    onSearchButton1Clicked: () -> Unit,
    onSearchButton2Clicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedState by rememberSaveable { mutableStateOf(true) }
    val rotationState by animateFloatAsState(
        targetValue = if (expandedState) 180f else 0f, label = ""
    )



    Card(
        // elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(4.dp),
        onClick = {
            expandedState = !expandedState
        },
        modifier = Modifier
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
                    text = "Choose an Exercise",

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
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FormLayout(
                        value = exerciseName,
                        onValueChange = onExerciseNameChanged,
                        label = "Exercise name",
                        placeholder = "Insert the name of the Exercise",
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        visualTransformation = VisualTransformation.None,
                        modifier = modifier
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = onSearchButton1Clicked,
                            //onClick = { homeViewModel.getApiData("random", mapOf("title" to "cat")) },
                            modifier = Modifier
                                .padding(20.dp)

                        ) {
                            Text(
                                text = "Search"
                            )
                        }
                    }
                }

            }
        }
    }
    Text(
        text = "Or by the Body Part",
        fontSize = 20.sp
    )
    Card(
        // elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(4.dp),
        onClick = {
            expandedState = !expandedState
        },
        modifier = Modifier
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
                    text = "Choose a specific Body Part",

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
                        .rotate(rotationState - 180f),
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Drop-Down Arrow"
                    )
                }
            }
            if (!expandedState) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {

                    RadioOptionsLayout(
                        bodyPartSelected = bodyPartSelected,
                        onOptionSelected = onBodyPartSelectedChanged
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = onSearchButton2Clicked,
                            // onClick = { homeViewModel.getApiData("random", mapOf("title" to "hello")) },
                            modifier = Modifier
                                .padding(20.dp)
                        ) {
                            Text(
                                text = "Search"
                            )
                        }
                    }
                }



            }


        }
    }
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    exerciseName: String,
    bodyPartSelected: String,
    onExerciseNameChanged: (String) -> Unit,
    onBodyPartSelectedChanged: (String) -> Unit,
    onSearchButton1Clicked: () -> Unit,
    onSearchButton2Clicked: () -> Unit,
    onTrainingButtonClicked: () -> Unit,
    onCameraButtonClicked: () -> Unit,
    //onSensorButtonClicked: () -> Unit,
    homeViewModel: HomeViewModel,
    onMenuButtonClicked: () -> Unit,
    modifier: Modifier = Modifier
) {

    val activity = LocalContext.current as Activity
    
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
            modifier = modifier
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Refine your Search",
                fontSize = 40.sp
            )
            Text(
                text = "You can either search for an Exercise by its name",
                fontSize = 20.sp
            )
            ExpandableMenu(
                exerciseName = exerciseName,
                bodyPartSelected = bodyPartSelected,
                onExerciseNameChanged = onExerciseNameChanged,
                onBodyPartSelectedChanged = onBodyPartSelectedChanged,
                onSearchButton1Clicked = onSearchButton1Clicked,
                onSearchButton2Clicked = onSearchButton2Clicked,
                modifier = modifier
            )


            Button(
                onClick = { onTrainingButtonClicked() },
                /*onClick = { activity.startActivity(
                    Intent(activity,TrainingActivity::class.java))
                },*/
                modifier = Modifier
                    .padding(20.dp)
            ) {
                Text(
                    text = "Training Screen"
                )
            }


            Button(
                onClick = { activity.startActivity(
                    Intent(activity, AccelerometerSensorActivity()::class.java))
                    //onSensorButtonClicked
                },
                modifier = Modifier
                    .padding(20.dp)
            ) {
                Text(
                    text = "Sensors"
                )
            }

            Button(
                onClick = { activity.startActivity(
                    Intent(activity, CameraActivity()::class.java))
                },
                modifier = Modifier
                    .padding(20.dp)
            ) {
                Text(
                    text = "OpenCV Camera"
                )
            }

            Button(
                onClick = { onCameraButtonClicked() },
                modifier = Modifier
                    .padding(20.dp)
            ) {
                Text(
                    text = "Server Camera Screen"
                )
            }

        }

    }
}



@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    GreetingCardTheme {
        HomeScreen(
            "press",
            "back",
            {},
            {},
            {},
            {},
            {},
            {},
            viewModel(),
            //{},
            //{},
            {},
            modifier = Modifier.fillMaxSize()
        )
    }
}