package com.example.memorandum.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun NoteListScreen(
    onNoteClick: (String) -> Unit,
    onAddNote: () -> Unit
) {

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Setari
        IconButton(
            onClick = { },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Settings,
                contentDescription = "Setari")
        }

        // Inapoi
        IconButton(
            onClick = { },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 60.dp, top = 16.dp)
        ) {
            Icon(Icons.Default.ArrowBack,
                contentDescription = "Inapoi")
        }

        // Titlu
        Text(
            text = "NOTES",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 28.dp)
        )

        Text(
            text = "06/05/2026",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 65.dp)
        )

        //Container Notite
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 90.dp)
                .width(340.dp)
                .height(60.dp)
                .background(
                    color = Color.LightGray,
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            Text(
                text = "Name",
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 16.dp)
            )

            Icon(
                imageVector = Icons.Default.MoreHoriz,
                contentDescription = "Editare",
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp)
            )
        }

        // Filtru
        IconButton(
            onClick = { },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.FilterList,
                contentDescription = "Filtru")
        }

        // Lista notite - schematic
        Text(
            text = "Empty...",
            modifier = Modifier
                .align(Alignment.Center)
        )

        // Search jos
        OutlinedTextField(
            value = "",
            onValueChange = { },
            placeholder = { Text("Search...") },
            leadingIcon = {
                Icon(Icons.Default.Search,
                    contentDescription = "Cautare")
            },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 16.dp)
        )

        // Adauga nota
        IconButton(
            onClick = { },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp)
        ) {
            Icon(Icons.Default.Add,
                contentDescription = "Adauga nota")
        }
    }
}