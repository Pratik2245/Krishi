package com.example.krishisetuapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*

class ReportActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                ReportScreen()
            }
        }
    }
}

@Composable
fun ReportScreen() {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val fusedLocationClient =
        remember { LocationServices.getFusedLocationProviderClient(context) }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {}

    Surface(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(40.dp))

            Text("📄 KrishiSetu Report & Weather", fontSize = 22.sp)

            Spacer(Modifier.height(40.dp))

            // ================= PDF BUTTON =================
            Button(
                onClick = {

                    val permissionGranted =
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED

                    if (!permissionGranted) {
                        permissionLauncher.launch(
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                        return@Button
                    }

                    fetchLocation(context, fusedLocationClient) { lat, lng ->

                        val currentUser = FirebaseAuth.getInstance().currentUser

                        if (currentUser == null) {
                            Toast.makeText(context, "User not logged in", Toast.LENGTH_LONG).show()
                            return@fetchLocation
                        }

                        val uid = currentUser.uid
                        val db = FirebaseFirestore.getInstance()

// 🔥 Directly fetch using UID (FAST & CORRECT)
                        db.collection("users")
                            .document(uid)
                            .get()
                            .addOnSuccessListener { userDoc ->

                                if (!userDoc.exists()) {
                                    Toast.makeText(
                                        context,
                                        "User document not found",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    return@addOnSuccessListener
                                }

                                val name = userDoc.getString("name") ?: "Unknown"
                                val village = userDoc.getString("village") ?: "Unknown"

                                // 🔥 FETCH LATEST SOIL RECORD
                                db.collection("soil_records")
                                    .orderBy("temperature")
                                    .limitToLast(1)
                                    .get()
                                    .addOnSuccessListener { soilSnapshot ->

                                        if (soilSnapshot.isEmpty) {
                                            Toast.makeText(
                                                context,
                                                "No soil records found",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            return@addOnSuccessListener
                                        }

                                        val soilDoc =
                                            soilSnapshot.documents[0]

                                        val predictedN =
                                            soilDoc.getDouble("predicted_N")?.toFloat() ?: 0f
                                        val predictedP =
                                            soilDoc.getDouble("predicted_P")?.toFloat() ?: 0f
                                        val predictedK =
                                            soilDoc.getDouble("predicted_K")?.toFloat() ?: 0f

                                        val phValue =
                                            soilDoc.getDouble("ph")?.toFloat() ?: 0f

                                        val soilType =
                                            soilDoc.getString("soil_type") ?: "Unknown"

                                        val moisture =
                                            soilDoc.getDouble("moisture")?.toFloat() ?: 0f

                                        val temperature =
                                            soilDoc.getDouble("temperature")?.toFloat() ?: 0f

                                        val cropsList =
                                            soilDoc.get("recommended_crops") as? List<*>
                                                ?: emptyList<Any>()

                                        val crops =
                                            cropsList.joinToString(", ")

                                        val sampleList =
                                            List(5) { index ->
                                                SampleData(
                                                    sampleNo = index + 1,
                                                    n = predictedN,
                                                    p = predictedP,
                                                    k = predictedK,
                                                    ph = phValue,
                                                    soilType = soilType,
                                                    moisture = moisture,
                                                    temperature = temperature,
                                                    crops = crops
                                                )
                                            }

                                        val reportData = ReportData(
                                            name = name,
                                            village = village,
                                            latitude = lat,
                                            longitude = lng,
                                            samples = sampleList,
                                            avgN = predictedN,
                                            avgP = predictedP,
                                            avgK = predictedK,
                                            avgPh = phValue,
                                            avgMoisture = moisture,
                                            avgTemp = temperature,
                                            predictedCrops = cropsList.map { it.toString() }
                                        )

                                        val success =
                                            PdfReportGenerator(context)
                                                .generateReport(reportData)

                                        Toast.makeText(
                                            context,
                                            if (success)
                                                "PDF Generated Successfully"
                                            else
                                                "PDF Generation Failed",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                            }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
            ) {
                Text("Generate Soil Report PDF")
            }

            Spacer(Modifier.height(20.dp))

            // ================= WEATHER BUTTON =================
            Button(
                onClick = {

                    val permissionGranted =
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED

                    if (!permissionGranted) {
                        permissionLauncher.launch(
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                        return@Button
                    }

                    fetchLocation(context, fusedLocationClient) { lat, lng ->

                        scope.launch(Dispatchers.IO) {

                            val helper = WeatherApiHelper()

                            val willRain =
                                helper.willRainInNext48Hours(
                                    lat,
                                    lng,
                                    BuildConfig.OPEN_WEATHER_API_KEY
                                )

                            withContext(Dispatchers.Main) {

                                if (willRain) {
                                    Toast.makeText(
                                        context,
                                        "🚨 Rain expected in next 48 hours.\nDelay fertilizer!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        context,
                                        "✅ No rain expected.\nSafe to apply fertilizer.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
            ) {
                Text("Check Rain Before Fertilizer")
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Suppress("MissingPermission")
private fun fetchLocation(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    onLocationReady: (Double, Double) -> Unit
) {

    val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
        context.startActivity(
            Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        )
        return
    }

    fusedLocationClient.lastLocation
        .addOnSuccessListener { location ->
            if (location != null) {
                onLocationReady(location.latitude, location.longitude)
            } else {
                Toast.makeText(
                    context,
                    "Location unavailable",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
}