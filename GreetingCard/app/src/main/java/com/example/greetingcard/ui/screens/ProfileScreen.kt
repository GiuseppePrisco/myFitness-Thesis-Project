package com.example.greetingcard.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.greetingcard.ui.theme.GreetingCardTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier
) {

    Scaffold(
        topBar = {

        },
        bottomBar = {

        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { }
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Menu"
                )
            }
        }
    ) { innerPadding ->

        Column(
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(innerPadding),
        ) {

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color.Green)
            ) {

            }
        }

    }

    /*SystemBackButtonHandler {
        MyAppRouter.navigateTo(MyAppRouter.previousScreen.value)
    }*/

}



@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfileScreenPreview() {
    GreetingCardTheme {
        ProfileScreen(
            modifier = Modifier.fillMaxSize()
        )
    }
}