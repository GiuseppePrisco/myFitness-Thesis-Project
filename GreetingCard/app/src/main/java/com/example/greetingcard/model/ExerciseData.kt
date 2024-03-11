package com.example.greetingcard.model

import kotlinx.serialization.Serializable

@Serializable
data class ExerciseData(

    /*
    val id: String,
    @SerialName(value = "img_src")
    val imgSrc: String,
     */

    val bodyPart: String,
    val equipment: String,
    val gifUrl: String,
    val id: String,
    val name: String,
    val target: String,
    val secondaryMuscles: List<String>,
    val instructions: List<String>


)