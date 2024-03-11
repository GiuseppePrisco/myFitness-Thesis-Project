package com.example.greetingcard.ui.navigation

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

sealed class Screen {

    /*private var previousScreen: Screen = HomeScreen

    fun getPreviousScreen(): Screen {
        return previousScreen
    }

    fun setPreviousScreen(screen: Screen) {
        this.previousScreen = screen
    }*/


    object SignupScreen : Screen()
    object LoginScreen : Screen()
    object HomeScreen : Screen()
    object ResultScreen : Screen()
    object ExerciseScreen : Screen()
    object TrainingScreen : Screen()
    object SensorScreen : Screen()
    object MenuScreen : Screen()
    object CameraScreen : Screen()
    object ProfileScreen : Screen()
}


object MyAppRouter {

    var currentScreen: MutableState<Screen> = mutableStateOf(Screen.SignupScreen)
    var previousScreen: MutableState<Screen> = mutableStateOf(Screen.HomeScreen)

    fun navigateTo(destination : Screen){
        previousScreen.value = currentScreen.value
        currentScreen.value = destination
        //currentScreen.value.setPreviousScreen(previousScreen)

    }

}