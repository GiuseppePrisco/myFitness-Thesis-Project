package com.example.greetingcard.data

import com.example.greetingcard.model.ExerciseData
import com.example.greetingcard.network.ExerciseApi

interface ExerciseDataRepository {
    suspend fun getExerciseData(path1: String, path2: String): List<ExerciseData>
}

class NetworkExerciseDataRepository() : ExerciseDataRepository {
    override suspend fun getExerciseData(path1: String, path2: String): List<ExerciseData> {
        return ExerciseApi.externalRetrofitService.getData(path1, path2)
    }
}

