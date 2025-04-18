package com.das.myqrcode

import android.content.Intent.EXTRA_TEXT
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.das.myqrcode.ui.generator.GenerateBarCode
import com.das.myqrcode.ui.generator.GenerateQRCode
import com.das.myqrcode.ui.scanner.QRScanner

class MainActivity : ComponentActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            MainPage()

        }

    }

    @Preview
    @Composable
    fun MainPage() {

        val navController = rememberNavController()

        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current

        // Get the initial intent (from when the activity was first created)
        val initialIntent = context as? MainActivity ?: return

        val currentIntent = remember { initialIntent.intent }

        LaunchedEffect(context) {
            lifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_START) {
                    // Check if there is any new intent
                    val data = currentIntent.getStringExtra(EXTRA_TEXT) ?: ""
                    if (data.isNotEmpty()) {
                        navController.navigate("generator/$data")
                    }

                }
            })
        }

        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            Scaffold { paddingValues ->

                // 2. Define the NavigationGraph (screens and routes)
                NavHost(
                    navController = navController,
                    startDestination = "MainPage",
                    modifier = Modifier.padding(paddingValues)
                ) {
                    composable("MainPage") {
                        CustomTheme {
                            MainPageContent(navController)
                        }
                    }
                    composable("generator/{newIntent}") { backStackEntry ->

                        val newIntent = backStackEntry.arguments?.getString("newIntent")
                        CustomTheme {
                            GenerateQRCode(
                                onNavigateBack = {
                                    // Navigate to Screen B
                                    navController.navigate("MainPage") {
                                        popUpTo("MainPage") { inclusive = true }
                                    }
                                },
                                textValue = newIntent.orEmpty()
                            )
                        }
                    }
                    composable("scanner") {
                        CustomTheme {
                            QRScanner(onNavigateBack = {
                                navController.navigate("MainPage") {
                                    popUpTo("MainPage") { inclusive = true }
                                }
                            }
                            )

                        }
                    }
                    composable("GenerateBarCode") {
                        CustomTheme {
                            GenerateBarCode(
                                onNavigateBack = {
                                    // Navigate to Screen B
                                    navController.navigate("MainPage") {
                                        popUpTo("MainPage") { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }


    }

    @Composable
    fun MainPageContent(navController: NavHostController) {

        val nothing = ""
        Scaffold { paddingValues ->

            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(16.dp)
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
//                        .clip(RoundedCornerShape(25.dp))
//                        .shadow(8.dp, shape = RoundedCornerShape(12.dp))
//                        .padding(10.dp)
                ) {

                    // Header Text
                    Text(
                        text = "Welcome",
                        style = MaterialTheme.typography.headlineLarge,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 35.dp)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.Center)
                            .wrapContentSize(Alignment.Center)
                    ) {

                        // Generate QR Code Button
                        Button(
                            onClick = {
                                navController.navigate("generator/$nothing")
                            },
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .width(250.dp)
                                .padding(vertical = 12.dp) // Spacing between buttons
                                .scale(1.05f) // Slight scale effect on hover
                        ) {
                            Icon(
                                painter = rememberVectorPainter(Icons.Default.QrCode),
                                contentDescription = "QR Code Generator",
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Generate QR")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // QR Code Scanner Button
                        Button(
                            onClick = {
                                navController.navigate("GenerateBarCode")
                            },
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .width(250.dp)
                                .padding(vertical = 12.dp)
                                .scale(1.05f) // Slight scale effect on hover
                        ) {
                            Icon(
                                painter = rememberVectorPainter(Icons.Default.QrCode2),
                                contentDescription = "QR Code Scanner",
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "BarCode Generator")
                        }
                    }
                }

                // Floating Action Button (FAB) for additional action
                FloatingActionButton(
                    onClick = {
                        navController.navigate("scanner")

                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .size(56.dp),
                    containerColor = Color(0xFF6200EE), // FAB background color
                    contentColor = Color.White
                ) {
                    Icon(
                        Icons.Default.QrCodeScanner,
                        contentDescription = "Scan",
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        }
    }
}