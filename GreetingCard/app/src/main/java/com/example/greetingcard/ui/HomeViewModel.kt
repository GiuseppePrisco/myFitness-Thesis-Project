package com.example.greetingcard.ui


import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.network.HttpException
import com.example.greetingcard.data.NetworkExerciseDataRepository
import com.example.greetingcard.data.NetworkPoseDataRepository
import com.example.greetingcard.data.NetworkRepsDataRepository
import com.example.greetingcard.model.ExerciseData
import com.example.greetingcard.model.MyPose
import com.example.greetingcard.model.RepsData
import com.example.greetingcard.training.classification.RepetitionCounter
import com.example.greetingcard.ui.navigation.MyAppRouter
import com.example.greetingcard.ui.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import java.io.IOException

class HomeViewModel : ViewModel() {

    private val TAG = HomeViewModel::class.simpleName

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    var exerciseUiState: ExerciseUiState by mutableStateOf(ExerciseUiState.Loading)
        private set

    var repsUiState: RepsUiState by mutableStateOf(RepsUiState.Loading)
        private set

    var username by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    var exerciseName by mutableStateOf("")
        private set

    var bodyPartSelected by mutableStateOf("back")
        private set

    var stepCounter by mutableStateOf(0)
        private set

    var repCounters = java.util.ArrayList<RepetitionCounter?>()

   /* fun getRepCounters() : java.util.ArrayList<RepetitionCounter?>{
        return repCounters
    }*/

    fun updateUsername(insertedUsername: String) {
        username = insertedUsername
    }

    fun updatePassword(insertedPassword: String) {
        password = insertedPassword
    }

    fun updateExerciseName(insertedExerciseName: String) {
        exerciseName = insertedExerciseName
    }

    fun updateBodyPartSelected(insertedBodyPartSelected: String) {
        bodyPartSelected = insertedBodyPartSelected
    }

    fun updateStepCounter(detectedStep: Int) {
        stepCounter = detectedStep
    }

    fun setDarkMode(value: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(darkMode = value)
        }
    }

    fun setCurrentExercise(exercise: ExerciseData) {
        _uiState.update { currentState ->
            currentState.copy(currentExercise = exercise)
        }
    }


    val isUserLoggedIn: MutableLiveData<Boolean> = MutableLiveData()
    var firstTimeAppLaunched = true

    fun logout() {

        val firebaseAuth = FirebaseAuth.getInstance()

        firebaseAuth.signOut()

        val authStateListener = FirebaseAuth.AuthStateListener {
            if (it.currentUser == null) {
                Log.d(TAG, "Inside sign out success")
                MyAppRouter.navigateTo(Screen.LoginScreen)
            } else {
                Log.d(TAG, "Inside sign out is not complete")
            }
        }

        firebaseAuth.addAuthStateListener(authStateListener)

    }


    fun checkForActiveSession() {
        if (FirebaseAuth.getInstance().currentUser != null) {
            Log.d(TAG, "Valid session")
            isUserLoggedIn.value = true
        } else {
            Log.d(TAG, "User is not logged in")
            isUserLoggedIn.value = false
        }
    }


    val emailId: MutableLiveData<String> = MutableLiveData()

    fun getUserData() {
        FirebaseAuth.getInstance().currentUser?.also {
            it.email?.also { email ->
                emailId.value = email
            }
        }
    }

    fun deleteUser() {

        FirebaseAuth
            .getInstance()
            .currentUser!!
            .delete()
            .addOnCompleteListener {
                Log.d(TAG, "Inside delete user OnCompleteListener")
                Log.d(TAG, " isSuccessful = ${it.isSuccessful}")

                if (it.isSuccessful) {
                    MyAppRouter.navigateTo(Screen.SignupScreen)
                }
            }
            .addOnFailureListener {
                Log.d(TAG, "Inside delete user OnFailureListener")
                Log.d(TAG, "Exception= ${it.message}")
                Log.d(TAG, "Exception= ${it.localizedMessage}")
            }

    }


    fun getApiData(path1: String, path2: String) {
    // fun getApiData(path: String, filters: Map<String, String>) {
        viewModelScope.launch {
            exerciseUiState = ExerciseUiState.Loading
            Log.d(TAG, "getApiData")
            exerciseUiState = try {
                val exerciseDataRepository = NetworkExerciseDataRepository()
                // val result = exerciseDataRepository.getExerciseData(path)[0]
                // val listResult = ExerciseApi.retrofitService.getPhotos(path, filters)
                ExerciseUiState.Success(exerciseDataRepository.getExerciseData(path1, path2))
            } catch (e: IOException) {
                ExerciseUiState.Error
            } catch (e: HttpException) {
                ExerciseUiState.Error
            }
        }
    }


    fun getRepsData() {
        // fun getApiData(path: String, filters: Map<String, String>) {
        viewModelScope.launch {
            repsUiState = RepsUiState.Loading
            Log.d(TAG, "getRepsData")
            repsUiState = try {
                val repsDataRepository = NetworkRepsDataRepository()
                // val result = exerciseDataRepository.getExerciseData(path)[0]
                // val listResult = ExerciseApi.retrofitService.getPhotos(path, filters)
                RepsUiState.Success(repsDataRepository.getRepsData())
            } catch (e: IOException) {
                RepsUiState.Error
            } catch (e: HttpException) {
                RepsUiState.Error
            }
        }
    }


    fun postRepsData() {
        // fun getApiData(path: String, filters: Map<String, String>) {
        viewModelScope.launch {
            repsUiState = RepsUiState.Loading
            Log.d(TAG, "postRepsData")

            try {
                val repsDataRepository = NetworkRepsDataRepository()
                Log.d(TAG, "REP COUNTERS: $repCounters")
                var repsList = mutableListOf<RepsData>()
                if(repCounters.size != 0) {
                    for (i in 1..5) {
                        val data = RepsData(i.toString(),repCounters[i-1]!!.className,repCounters[i-1]!!.numRepeats)
                        repsList.add(data)
                    }
                    for (elem in repsList) {
                        Log.d(TAG, "REP COUNTERS CLASS NAMES: ${elem.exerciseName}")
                        Log.d(TAG, "REP COUNTERS NUM REPEATS: ${elem.repCount}")
                    }

                }

                val result = async {
                    for (elem in repsList) {
                        var res = repsDataRepository.postRepsData(elem)
                        Log.d(TAG, "RESPONSE: ${res}")
                    }
                }
                result.await()

                getRepsData()

            } catch (e: IOException) {
                RepsUiState.Error
            } catch (e: HttpException) {
                RepsUiState.Error
            }
        }
    }




    fun uploadImage(imageUri: MultipartBody.Part) {
        viewModelScope.launch {
            Log.d(TAG, "uploadImage")
            try {
                val startUpload = System.currentTimeMillis()
                val poseDataRepository = NetworkPoseDataRepository()
                val response = poseDataRepository.uploadImage(imageUri)
                val responseBody = response.body()
                if (response.isSuccessful && response.body() != null) {
                    val gson = Gson()
                    val poseType = object : TypeToken<List<MyPose>>() {}.type
                    val poses: List<MyPose> = gson.fromJson(responseBody!!.string(), poseType)
                    // Log the landmarks
                    poses.forEach { pose ->
                        // Log the landmarks
                        pose.poseLandmarks.forEach { landmark ->
                            Log.d("PoseEstimation", "${landmark.type}: x=${landmark.position.x}, y=${landmark.position.y}, z=${landmark.position.z}, inFrameLikelihood=${landmark.inFrameLikelihood}")
                        }
                    }
                } else {
                    Log.e("PoseEstimation", "Error: ${response.errorBody()?.string()}")
                }
                val endUpload = System.currentTimeMillis()
                Log.d("Performance", "Time taken to upload image: ${endUpload - startUpload} ms")
            } catch (e: Exception) {
                Log.e("PoseEstimation", "Error: ${e.message}")
            }
        }

    }






}