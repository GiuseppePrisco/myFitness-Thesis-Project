package com.example.greetingcard.model

data class Position(val x: Float, val y: Float, val z: Float)
data class PoseLandmark(val type: String, val position: Position, val inFrameLikelihood: Float)
data class MyPose(val poseLandmarks: List<PoseLandmark>, val zRotation: Float)