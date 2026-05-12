package com.example.memorandum.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material.icons.filled.Tune
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.Folder


@Composable
fun NoteListScreen(
    onNoteClick: (String) -> Unit,
    onAddNote: () -> Unit,
    onSettings: () -> Unit,
    viewModel: NoteListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val backgroundColor = Color(0xFFF7F2EC)
    val cardColor = Color(0xFFF7F2EC)
    val accentColor = Color(0xFF8A7352)
    val textColor = Color(0xFF1E1E1E)

    var showFilterMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        IconButton(
            onClick = onSettings,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 60.dp, top = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Setari"
            )
        }

        IconButton(
            onClick = { },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(all = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = "Inapoi"
            )
        }

        Text(
            text = "NOTES",
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 28.dp)
        )

        when (val state = uiState) {
            is NoteListUiState.Loading -> {
                Text(
                    text = "Se incarca...",
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is NoteListUiState.Error -> {
                Text(
                    text = state.message,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is NoteListUiState.Success -> {
                if (state.notes.isEmpty()) {
                    Text(
                        text = "Nu exista notite",
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 95.dp, bottom = 80.dp)
                    ) {
                        items(state.notes) { note ->
                            Box(modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .width(340.dp)
                                .height(60.dp)
                                .clickable {
                                    onNoteClick(note.id.toString())
                                }
                            ) {
                                Text(
                                    text = note.title,
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
                        }
                    }
                }
            }
        }
        //Fitru Button
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 16.dp)
        ) {
            IconButton(
                onClick = { showFilterMenu = true }
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Filtru",
                    tint = textColor
                )
            }
            //Meniul Filtrelor
            DropdownMenu(
                expanded = showFilterMenu,
                onDismissRequest = { showFilterMenu = false },
                modifier = Modifier
                    .width(210.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .background(
                        color = cardColor,
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "Cele mai noi",
                            fontWeight = FontWeight.SemiBold,
                            color = textColor
                        )
                    },
                    onClick = {
                        viewModel.sortBy(SortType.CREATED_AT_DESC)
                        showFilterMenu = false
                    }
                )

                DropdownMenuItem(
                    text = {
                        Text(
                            text = "Actualizate recent",
                            fontWeight = FontWeight.SemiBold,
                            color = textColor
                        )
                    },
                    onClick = {
                        viewModel.sortBy(SortType.UPDATED_AT_DESC)
                        showFilterMenu = false
                    }
                )

                DropdownMenuItem(
                    text = {
                        Text(
                            text = "A-Z",
                            fontWeight = FontWeight.SemiBold,
                            color = textColor
                        )
                    },
                    onClick = {
                        viewModel.sortBy(SortType.ALPHABETICAL_ASC)
                        showFilterMenu = false
                    }
                )
            }
        }
        //Bara de cautare
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                viewModel.onSearchQueryChanged(it)
            },
            placeholder = {
                Text("Search notes...")
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Cautare"
                )
            },
            trailingIcon = {
                IconButton(
                    onClick = onAddNote
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Adauga nota",
                        tint = accentColor
                    )
                }
            },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 70.dp, bottom = 16.dp)
                .width(270.dp)
                .height(55.dp),
            shape = RoundedCornerShape(18.dp)
        )
    }
}