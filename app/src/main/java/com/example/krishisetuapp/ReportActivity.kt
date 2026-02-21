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

    val fusedLocationClient: FusedLocationProviderClient =
        remember { LocationServices.getFusedLocationProviderClient(context) }

    var isCheckingWeather by remember { mutableStateOf(false) }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { granted ->

            if (granted) {
                fetchLocation(
                    context = context,
                    fusedLocationClient = fusedLocationClient,
                    scope = scope
                )
            } else {
                Toast.makeText(
                    context,
                    "Location permission denied",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    Surface(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "📄 KrishiSetu Report & Weather",
                fontSize = 22.sp
            )

            Spacer(modifier = Modifier.height(30.dp))

            // -----------------------
            // PDF BUTTON
            // -----------------------
            Button(
                onClick = {
                    val generator = PdfReportGenerator(context)
                    val success = generator.generateReport()

                    Toast.makeText(
                        context,
                        if (success)
                            "PDF generated in Documents/KrishiSetu"
                        else
                            "Failed to generate PDF",
                        Toast.LENGTH_LONG
                    ).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Generate Soil Report PDF")
            }

            Spacer(modifier = Modifier.height(20.dp))

            // -----------------------
            // WEATHER BUTTON
            // -----------------------
            Button(
                onClick = {

                    val permissionGranted =
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED

                    if (permissionGranted) {

                        fetchLocation(
                            context = context,
                            fusedLocationClient = fusedLocationClient,
                            scope = scope
                        )

                    } else {
                        permissionLauncher.launch(
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Check Rain Before Fertilizer")
            }
        }
    }
}

// ------------------------------------------------
// LOCATION + WEATHER LOGIC
// ------------------------------------------------
private fun fetchLocation(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    scope: CoroutineScope
) {

    val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
        Toast.makeText(
            context,
            "Please enable GPS",
            Toast.LENGTH_LONG
        ).show()

        context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        return
    }

    fusedLocationClient.lastLocation
        .addOnSuccessListener { location ->

            if (location != null) {

                val latitude = location.latitude
                val longitude = location.longitude

                scope.launch(Dispatchers.IO) {

                    try {

                        val helper = WeatherApiHelper()

                        val willRain =
                            helper.willRainInNext48Hours(
                                latitude,
                                longitude,
                                BuildConfig.OPEN_WEATHER_API_KEY
                            )

                        withContext(Dispatchers.Main) {

                            if (willRain) {
                                Toast.makeText(
                                    context,
                                    "🚨 Rain expected in next 48 hours.\nDelay fertilizer application!",
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

                    } catch (e: Exception) {

                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                "Weather check failed",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }

            } else {
                Toast.makeText(
                    context,
                    "Unable to fetch location. Try again.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        .addOnFailureListener {
            Toast.makeText(
                context,
                "Location fetch failed",
                Toast.LENGTH_LONG
            ).show()
        }
}