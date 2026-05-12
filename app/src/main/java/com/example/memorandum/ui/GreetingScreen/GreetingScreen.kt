package com.example.memorandum.ui.GreetingScreen
import androidx.compose.ui.graphics.Color
import android.os.Build.VERSION.SDK_INT
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.example.memorandum.R
import androidx.compose.ui.unit.dp
//Aici A fost Bogdan :3
import  androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import kotlinx.coroutines.delay


@Composable
fun GreetingScreen(navController: NavController) {

        val context = LocalContext.current//Permitem transmiterea informatiei(logo.gif)
       
    LaunchedEffect(Unit) {//Animatia lucreaza numai o data-Unit
        delay(3000)// timpul pentru animatie 3 sec


    navController.navigate("note_list"){ //functia tine minte la ce ecran suntem
        popUpTo("greeting") { inclusive = true }//sterge ecranul din stack screens

            }}
            val imageLoader = ImageLoader.Builder(context)
                .components {
                    if (SDK_INT >= 28) { //Versiunea android
                        add(ImageDecoderDecoder.Factory())  //Decoder gif pentru versiunea noua
                    } else {
                        add(GifDecoder.Factory())
                    }}
            .build()
        Box( //Setarile boxului si continutului
            modifier = Modifier
                .fillMaxSize() //Box pe tot ecranul
                .background(Color.White),
            contentAlignment = Alignment.TopCenter, //centreaza la sus de ecran
        )
        {
            AsyncImage(
            model = ImageRequest.Builder(context)
                .data(R.raw.welcome_logo)
                .build(),
                contentDescription = null,//Descriptie pentru oameni orbi,screen audio reading,Android cere asa ceva,dar nu implementam
            imageLoader = imageLoader,
                modifier=Modifier
                .padding(top = 100.dp) // Distanta de sus
                .width(150.dp)        // Latimea
            )
        }
        }
