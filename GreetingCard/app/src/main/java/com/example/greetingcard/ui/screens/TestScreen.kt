package com.example.greetingcard.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.greetingcard.R
import com.example.greetingcard.ui.theme.GreetingCardTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormLayout(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    keyboardOptions: KeyboardOptions,
    visualTransformation: VisualTransformation,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    TextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        keyboardOptions = keyboardOptions,
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        visualTransformation = visualTransformation
    )
}

@Composable
fun MenuSwitchOption(
    text: String,
    checked: Boolean,
    onCheckedChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp)
    ) {
        Text(
            text = text
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChanged,
            modifier = modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.End)
        )
    }
}

@Composable
fun TestScreen(
    username: String,
    password: String,
    onUsernameChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    darkMode: Boolean,
    onDarkModeChanged: (Boolean) -> Unit,
    onLoginButtonClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {

    val background = painterResource(R.drawable.background)
    val compass = painterResource(R.drawable.compass)

    Box {
        Image(
            painter = background,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alpha = 0.8f,
            modifier = Modifier
                .fillMaxSize()
        )
        Column(
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .padding(8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Welcome to my App!",
                fontSize = 70.sp,
                lineHeight = 75.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Please Login to the App",
                fontSize = 20.sp,
                lineHeight = 25.sp,
                modifier = Modifier
                    .padding(16.dp)
                //.align(alignment = Alignment.CenterHorizontally)
            )
            FormLayout(
                value = username,
                onValueChange = onUsernameChanged,
                label = stringResource(R.string.username),
                placeholder = stringResource(R.string.insert_your_username),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                visualTransformation = VisualTransformation.None,
                modifier = modifier
            )
            FormLayout(
                value = password,
                onValueChange = onPasswordChanged,
                label = stringResource(R.string.password),
                placeholder = stringResource(R.string.insert_your_password),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                visualTransformation = PasswordVisualTransformation(),
                modifier = modifier
            )
            MenuSwitchOption(
                text = stringResource(R.string.dark_mode),
                checked = darkMode,
                onCheckedChanged = onDarkModeChanged,
            )
            Button(
                onClick = { onLoginButtonClicked(username) },
                modifier = modifier
            ) {
                Text(
                    text = "Login"
                )
            }
            Image(
                painter = compass,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .padding(8.dp)
                //.align(alignment = Alignment.CenterHorizontally)
            )

        }
    }

    /*var monument: Monument = Monument("Fontana di Trevi", "Italy")
    var monumentList = mutableListOf<Monument>()
    for (i in 0..9) {
        monumentList.add(monument)
    }*/

    // since it is a lazy column (which cannot be placed inside a column - HomePageContent), I should make
    // the HomePageContent a lazyColumn and place the previously defined column inside the first item of the
    // lazyColumn
    // EX. https://stackoverflow.com/questions/71883094/jetpack-compose-lazycolumn-inside-scrollabe-column
    // MonumentList(monumentList = monumentList)

}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TestScreenPreview() {
    GreetingCardTheme {
        TestScreen(
            username = "",
            password = "",
            onUsernameChanged = {  },
            onPasswordChanged = {  },
            darkMode = true,
            onDarkModeChanged = {  },
            onLoginButtonClicked = {
                
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}