package com.example.greetingcard.ui.screens

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.greetingcard.GravitySensorActivity
import com.example.greetingcard.RotationSensorActivity
import com.example.greetingcard.ui.theme.GreetingCardTheme


@Composable
fun SensorScreen(
    stepCounter: Int,
    //updateStepCounter: (Int) -> Unit,
    modifier: Modifier = Modifier
) {

    val activity = LocalContext.current as Activity

    /*val activity = LocalContext.current as Activity
    activity.startActivity(
        Intent(activity, SensorActivity(updateStepCounter)::class.java))*/

    Column(
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {

        Text(
            text = "Steps counted so far:",
            fontSize = 80.sp,
            lineHeight = 85.sp,
            textAlign = TextAlign.Center
        )

        Text(
            text = stepCounter.toString(),
            fontSize = 80.sp
        )

        Button(
            onClick = { activity.startActivity(
                Intent(activity, GravitySensorActivity()::class.java)
            )
            },
            modifier = Modifier
                .padding(20.dp)
        ) {
            Text(
                text = "Play Soccer Minigame"
            )
        }

        Button(
            onClick = { activity.startActivity(
                Intent(activity, RotationSensorActivity()::class.java)
            )
            },
            modifier = Modifier
                .padding(20.dp)
        ) {
            Text(
                text = "Move Rainbow Ball"
            )
        }



    }

    /*SystemBackButtonHandler {
        MyAppRouter.navigateTo(Screen.HomeScreen)
    }*/


}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SensorScreenPreview() {
    GreetingCardTheme {
        SensorScreen(
            55,
            //{},
            //{},
            modifier = Modifier.fillMaxSize()
        )
    }
}