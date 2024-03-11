package com.example.greetingcard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.greetingcard.ui.navigation.MyAppRouter
import com.example.greetingcard.ui.navigation.SystemBackButtonHandler
import com.example.greetingcard.ui.theme.GreetingCardTheme


@Composable
fun MenuScreen(
    email: String,
    onLogoutButtonClicked: () -> Unit,
    onDeleteUserButtonClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.LightGray)
    ) {

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
                Text(
                    text = "Your email:",
                    modifier = Modifier
                        .weight(1f)
                )
                Text(
                    text = email,
                )
            }
        }

        OutlinedButton(
            colors = buttonColors(
                containerColor = Color.DarkGray,
                contentColor = Color.White,
                disabledContainerColor = Color.Gray,
                disabledContentColor = Color.Black
            ),
            onClick = onLogoutButtonClicked,
            modifier = Modifier
        ) {
            Text(
                text = "Logout"
            )
        }

        OutlinedButton(
            colors = buttonColors(
                containerColor = Color.Red,
                contentColor = Color.Black,
                disabledContainerColor = Color.Gray,
                disabledContentColor = Color.Black
            ),
            onClick = onDeleteUserButtonClicked,
            modifier = Modifier
        ) {
            Text(
                text = "Delete Account"
            )
        }

    }

    SystemBackButtonHandler {
        MyAppRouter.navigateTo(MyAppRouter.previousScreen.value)
    }

}



@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MenuScreenPreview() {
    GreetingCardTheme {
        MenuScreen(
            "pippo_pluto@gmail.com",
            onLogoutButtonClicked = {},
            onDeleteUserButtonClicked = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}