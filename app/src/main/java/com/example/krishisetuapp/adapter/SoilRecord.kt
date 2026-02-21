package com.example.krishisetuapp

data class SoilRecord(
    val moisture: Double?,
    val ph: Double?,
    val temperature: Double?,
    val soilColor: String?,
    val soilType: String?,
    val predictedN: Double?,
    val predictedP: Double?,
    val predictedK: Double?,
    val crops: List<String>?,
    val timestamp: Long?
)