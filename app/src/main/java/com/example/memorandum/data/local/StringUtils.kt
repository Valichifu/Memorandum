package com.example.memorandum.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight


fun String.highlightSearchQuery(
    highlightColor: Color = Color(0xFFB7E4C7) // Culoarea highlight
): AnnotatedString {
    val text = this


    return buildAnnotatedString {
        var currentIndex = 0

        while (true) {
            // Cautarea marker start
            val startTag = text.indexOf("[start]", currentIndex)

            //Daca nu sunt markeri se opreste functia
            if (startTag == -1) {
                append(text.substring(currentIndex))
                break
            }

            // Cautam marker end
            val endTag = text.indexOf("[end]", startTag)
            if (endTag == -1) {//Distrugem functia daca end nu este sa nu se strice aplicatia
                append(text.substring(currentIndex))
                break
            }

            // Se adauga textulul ne-stilizat
            append(text.substring(currentIndex, startTag))

            //Stilizarea textului (HIGHLIGHT)
            pushStyle(
                SpanStyle(
                    fontWeight = FontWeight.Bold, //
                    background = highlightColor   //
                )
            )


            append(text.substring(startTag + 7, endTag))

            //inchidem highlight
            pop()


            currentIndex = endTag + 5
        }
    }
}