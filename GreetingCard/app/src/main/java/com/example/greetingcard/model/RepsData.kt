package com.example.greetingcard.model

import kotlinx.serialization.Serializable

@Serializable
data class RepsData(

    val id: String,
    val exerciseName: String,
    val repCount: Int

)