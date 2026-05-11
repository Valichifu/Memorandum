package com.example.memorandum.ui.WelcomeLogo

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.memorandum.R // FOARTE IMPORTANT: Importă R-ul proiectului tău
import kotlinx.coroutines.delay
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

@Composable
fun WelcomeScreen(navController: NavController) {
    // Încarcă animația din folderul raw
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.logo)
    )

    // Controlează progresul animației
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1 // Rulează o singură dată
    )

    // Navighează la listă când animația s-a terminat (progress = 1.0)
    LaunchedEffect(progress) { // Monitorizează schimbarea progresului
        if (progress >= 1f) {
            navController.navigate("note_list") {
                popUpTo("welcome") { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Gray),
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(250.dp)
        )
    }
}