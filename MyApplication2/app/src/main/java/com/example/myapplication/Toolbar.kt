package com.example.myapplication

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

/* The below code contains all the toolbars for the three screens in the application.
* These bars allow the user to have a clear idea as to where they are in the app, as well as help with navigation. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Toolbar(){
   // This topbar is the bar for the budgetList screen.
   // It simply shows the app name.
   TopAppBar(title = { Text(text = "BudgeTrack") },
      colors=topAppBarColors(
      containerColor = MaterialTheme.colorScheme.primaryContainer,
      titleContentColor = MaterialTheme.colorScheme.primary
   ))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEntryToolbar(
   //This topbar is for the Add Entry screen.
   //This makes the user aware of the screen they are at, with the functionality of moving
   //back to the budget list screen.
   navController: NavController
){
   TopAppBar(title = { Text(text = "Add a New Entry") },
      colors=topAppBarColors(
         containerColor = MaterialTheme.colorScheme.primaryContainer,
         titleContentColor = MaterialTheme.colorScheme.primary
      ),
      navigationIcon = {
         IconButton(onClick = { navController.popBackStack() }) {
            Icon(imageVector = Icons.Filled.ArrowBack , contentDescription = "Back")
         }
      }
   )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailedEntryToolbar(
   //This topbar is for the detailed entry screen.
   //This again makes the user aware of where they are at in terms of their app. Also provides
   //functionality to move back to the budget screen.
   navController: NavController
){
   TopAppBar(title = { Text(text = "More Info") },
      colors=topAppBarColors(
         containerColor = MaterialTheme.colorScheme.primaryContainer,
         titleContentColor = MaterialTheme.colorScheme.primary
      ),
      navigationIcon = {
         IconButton(onClick = { navController.popBackStack() }) {
            Icon(imageVector = Icons.Filled.ArrowBack , contentDescription = "Back")
         }
      })
}
