package com.example.greetingcard

import android.content.ContentResolver
import android.util.Log
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.greetingcard.ui.HomeViewModel
import com.example.greetingcard.ui.navigation.MyAppRouter
import com.example.greetingcard.ui.navigation.Screen
import com.example.greetingcard.ui.screens.CameraScreen
import com.example.greetingcard.ui.screens.ExerciseScreen
import com.example.greetingcard.ui.screens.HomeScreen
import com.example.greetingcard.ui.screens.LoginScreen
import com.example.greetingcard.ui.screens.MenuScreen
import com.example.greetingcard.ui.screens.ProfileScreen
import com.example.greetingcard.ui.screens.ResultScreen
import com.example.greetingcard.ui.screens.SensorScreen
import com.example.greetingcard.ui.screens.SignupScreen
import com.example.greetingcard.ui.screens.TrainingScreen
import com.example.greetingcard.ui.theme.GreetingCardTheme


enum class MyAppScreen() {
    Login,
    Home,
    Result,
    Exercise,
    Sensor,
    Profile,
    Menu,
    Training,
    Camera
}

@Composable
fun MyApp(
    homeViewModel: HomeViewModel = viewModel(),
    navController: NavHostController = rememberNavController(),
    //startDestination: String,
    //firebaseAuth: FirebaseAuth,
    modifier: Modifier = Modifier
) {


    homeViewModel.checkForActiveSession()

    if (homeViewModel.isUserLoggedIn.value == true && homeViewModel.firstTimeAppLaunched == true) {
        homeViewModel.firstTimeAppLaunched = false
        MyAppRouter.navigateTo(Screen.HomeScreen)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {

        val homeUiState by homeViewModel.uiState.collectAsState()
        var search = true

        Crossfade(targetState = MyAppRouter.currentScreen, label = "pages") { currentState ->

            Log.d("TEST", "${currentState.value}")

            when (currentState.value) {
                is Screen.SignupScreen -> {
                    SignupScreen()
                }

                is Screen.LoginScreen -> {
                    LoginScreen()
                }

                is Screen.HomeScreen -> {
                    HomeScreen(
                        exerciseName = homeViewModel.exerciseName,
                        bodyPartSelected = homeViewModel.bodyPartSelected,
                        onExerciseNameChanged = { homeViewModel.updateExerciseName(it) },
                        onBodyPartSelectedChanged = { homeViewModel.updateBodyPartSelected(it) },
                        onSearchButton1Clicked = {
                            search = true

                            //homeViewModel.getApiData("photos")
                            if (homeViewModel.exerciseName.length == 0) homeViewModel.updateExerciseName(" ")
                            homeViewModel.getApiData("name", homeViewModel.exerciseName)

                            MyAppRouter.navigateTo(Screen.ResultScreen)
                            //navController.navigate(MyAppScreen.Result.name)
                        },
                        onSearchButton2Clicked = {
                            search = false

                            //homeViewModel.getApiData("photos")
                            homeViewModel.getApiData("bodyPart", homeViewModel.bodyPartSelected)

                            MyAppRouter.navigateTo(Screen.ResultScreen)
                            //navController.navigate(MyAppScreen.Result.name)
                        },
                        onMenuButtonClicked = {
                            MyAppRouter.navigateTo(Screen.MenuScreen)
                            //navController.navigate(MyAppScreen.Menu.name)
                        },
                        onTrainingButtonClicked = {

                            homeViewModel.getRepsData()

                            MyAppRouter.navigateTo(Screen.TrainingScreen)
                            //navController.navigate(MyAppScreen.Training.name)
                        },
                        onCameraButtonClicked = {
                            MyAppRouter.navigateTo(Screen.CameraScreen)
                            //navController.navigate(MyAppScreen.Camera.name)
                        },
                        /*onSensorButtonClicked = {
                            navController.navigate(MyAppScreen.Sensor.name)
                        },*/
                        homeViewModel = homeViewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                is Screen.ResultScreen -> {
                    ResultScreen(
                        exerciseUiState = homeViewModel.exerciseUiState,
                        exerciseName = homeViewModel.exerciseName,
                        onMenuButtonClicked = {
                            MyAppRouter.navigateTo(Screen.MenuScreen)
                            //navController.navigate(MyAppScreen.Menu.name)
                        },
                        onCurrentExerciseSelected = {
                            homeViewModel.setCurrentExercise(it)
                            MyAppRouter.navigateTo(Screen.ExerciseScreen)
                            //navController.navigate(MyAppScreen.Exercise.name)
                        },
                        retryAction = {
                            if(search) { homeViewModel.getApiData("name", homeViewModel.exerciseName) }
                            else { homeViewModel.getApiData("bodyPart", homeViewModel.bodyPartSelected) }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                is Screen.ExerciseScreen -> {
                    ExerciseScreen(
                        currentExercise = homeUiState.currentExercise,
                        onMenuButtonClicked = {
                            MyAppRouter.navigateTo(Screen.MenuScreen)
                            //navController.navigate(MyAppScreen.Menu.name)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                is Screen.TrainingScreen -> {
                    TrainingScreen(
                        repsUiState = homeViewModel.repsUiState,
                        homeViewModel = homeViewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                is Screen.SensorScreen -> {
                    SensorScreen(
                        stepCounter = homeViewModel.stepCounter,
                        //updateStepCounter = { homeViewModel.updateStepCounter(it) },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                is Screen.MenuScreen -> {
                    homeViewModel.getUserData()
                    MenuScreen(
                        email = homeViewModel.emailId.value.toString(),
                        onLogoutButtonClicked = {
                            homeViewModel.logout()
                        },
                        onDeleteUserButtonClicked = {
                            homeViewModel.deleteUser()
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                is Screen.CameraScreen -> {
                    CameraScreen(
                        homeViewModel = homeViewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                is Screen.ProfileScreen -> {
                    ProfileScreen()
                }

            }
        }


        //val homeUiState by homeViewModel.uiState.collectAsState()
        //var search = true

        /*NavHost(
            navController = navController,
            startDestination = MyAppRouter.currentScreen.toString(),
            //startDestination = MyAppScreen.Login.name,
            //modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = MyAppScreen.Login.name) {
                LoginScreen(
                    username = homeViewModel.username,
                    password = homeViewModel.password,
                    onUsernameChanged = { homeViewModel.updateUsername(it) },
                    onPasswordChanged = { homeViewModel.updatePassword(it) },
                    darkMode = homeUiState.darkMode,
                    onDarkModeChanged = { homeViewModel.setDarkMode(it) },
                    onLoginButtonClicked = {


                        // homeViewModel.getApiData("photos")

                        // homeViewModel.getApiData("random", mapOf("title" to "cat"))

                        //THE REAL ONES WILL BE
                        //homeViewModel.getApiData("/name/{name}")
                        //homeViewModel.getApiData("/bodyPart/{bodyPart}")


                        //createAccount(homeViewModel.username, homeViewModel.password)

                        navController.navigate(MyAppScreen.Home.name)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            composable(route = MyAppScreen.Home.name) {
                HomeScreen(
                    exerciseName = homeViewModel.exerciseName,
                    bodyPartSelected = homeViewModel.bodyPartSelected,
                    onExerciseNameChanged = { homeViewModel.updateExerciseName(it) },
                    onBodyPartSelectedChanged = { homeViewModel.updateBodyPartSelected(it) },
                    onSearchButton1Clicked = {
                        search = true

                        //homeViewModel.getApiData("photos")
                        if (homeViewModel.exerciseName.length == 0) homeViewModel.updateExerciseName(" ")
                        homeViewModel.getApiData("name", homeViewModel.exerciseName)

                        navController.navigate(MyAppScreen.Result.name)
                    },
                    onSearchButton2Clicked = {
                        search = false

                        //homeViewModel.getApiData("photos")
                        homeViewModel.getApiData("bodyPart", homeViewModel.bodyPartSelected)

                        navController.navigate(MyAppScreen.Result.name)
                    },
                    onMenuButtonClicked = {
                        navController.navigate(MyAppScreen.Menu.name)
                    },
                    onTrainingButtonClicked = {

                        homeViewModel.getRepsData()

                        navController.navigate(MyAppScreen.Training.name)
                    },
                    *//*onSensorButtonClicked = {
                        navController.navigate(MyAppScreen.Sensor.name)
                    },*//*
                    homeViewModel = homeViewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
            composable(route = MyAppScreen.Result.name) {
                ResultScreen(
                    exerciseUiState = homeViewModel.exerciseUiState,
                    exerciseName = homeViewModel.exerciseName,
                    onMenuButtonClicked = {
                        navController.navigate(MyAppScreen.Menu.name)
                    },
                    onCurrentExerciseSelected = {
                        homeViewModel.setCurrentExercise(it)
                        navController.navigate(MyAppScreen.Exercise.name)
                    },
                    retryAction = {
                        if(search) { homeViewModel.getApiData("name", homeViewModel.exerciseName) }
                        else { homeViewModel.getApiData("bodyPart", homeViewModel.bodyPartSelected) }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            composable(route = MyAppScreen.Exercise.name) {
                ExerciseScreen(
                    currentExercise = homeUiState.currentExercise,
                    onMenuButtonClicked = {
                        navController.navigate(MyAppScreen.Menu.name)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            composable(route = MyAppScreen.Training.name) {
                TrainingScreen(
                    repsUiState = homeViewModel.repsUiState,
                    homeViewModel = homeViewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
            composable(route = MyAppScreen.Sensor.name) {
                SensorScreen(
                    stepCounter = homeViewModel.stepCounter,
                    //updateStepCounter = { homeViewModel.updateStepCounter(it) },
                    modifier = Modifier.fillMaxSize()
                )
            }
            composable(route = MyAppScreen.Profile.name) {

            }
            composable(route = MyAppScreen.Menu.name) {
                MenuScreen(
                    onExitButtonClicked = {
                        logout(homeViewModel, navController)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

        }*/




    }

}




    /*Scaffold(

    ) { innerPadding ->

    }*/



/*
    Box {
        Image(
            painter = background,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alpha = 0.8f,
            modifier = Modifier
                .fillMaxSize()
        )
        HomePageContent(
            username = homeViewModel.username,
            password = homeViewModel.password,
            onUsernameChanged = { homeViewModel.updateUsername(it) },
            onPasswordChanged = { homeViewModel.updatePassword(it) },
            darkMode = homeUiState.darkMode,
            onDarkModeChanged = { homeViewModel.updateDarkMode(it) },
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        )
    }
 */



/*private fun logout(
    viewModel: HomeViewModel,
    navController: NavHostController
) {
    viewModel.logout()
    navController.popBackStack(MyAppScreen.Login.name, inclusive = false)
}*/



@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomePagePreview() {
    GreetingCardTheme {
        MyApp(
            viewModel(),
            rememberNavController(),
            //MyAppScreen.Login.name,
            //Firebase.auth
        )
    }
}