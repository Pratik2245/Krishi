package com.example.krishisetuapp

data class ReportData(
    val name: String,
    val village: String,
    val latitude: Double,
    val longitude: Double,
    val samples: List<SampleData>,
    val avgN: Float,
    val avgP: Float,
    val avgK: Float,
    val avgPh: Float,
    val avgMoisture: Float,
    val avgTemp: Float,
    val predictedCrops: List<String>
)

data class SampleData(
    val sampleNo: Int,
    val n: Float,
    val p: Float,
    val k: Float,
    val ph: Float,
    val soilType: String,
    val moisture: Float,
    val temperature: Float,
    val crops: String
)