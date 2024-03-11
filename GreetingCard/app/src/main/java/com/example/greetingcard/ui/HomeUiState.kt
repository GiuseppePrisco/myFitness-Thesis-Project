package com.example.greetingcard.ui

import com.example.greetingcard.model.ExerciseData
import com.example.greetingcard.model.RepsData

data class HomeUiState(
    val darkMode: Boolean = false,
    val currentExercise: ExerciseData = ExerciseData("","", "", "", "", "", listOf(""), listOf(""))
)

sealed interface ExerciseUiState {
    data class Success(val dataList: List<ExerciseData>) : ExerciseUiState
    object Error : ExerciseUiState
    object Loading : ExerciseUiState
}

sealed interface RepsUiState {
    data class Success(val dataList: List<RepsData>) : RepsUiState
    object Error : RepsUiState
    object Loading : RepsUiState
}