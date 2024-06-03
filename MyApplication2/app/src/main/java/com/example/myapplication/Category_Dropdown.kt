package com.example.myapplication

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Category_Dropdown(viewModel: AddEntryViewModel) {
   /* The below function is an important auxillary function for the addEntryViewModel. This is one of the
   * most important functionalities of any budget tracker, namely the categorisation of expenses.
   * This is achieved by creating a pre-determined list of categories which can picked by the user in a
   * dropdown list. */
   var expanded by remember { mutableStateOf(false) } //Boolean variable to store the state of whether the dropdown menu is expanded
   // or not.
   val categories = arrayOf( //The list of pre-ordained categories. I have assumed a set of categories that would
      //fit most types of expenses.
      Categories.Housing.name,
      Categories.Entertainment.name,
      Categories.Transportation.name,
      Categories.Food.name,
      Categories.Groceries.name,
      Categories.Utilities.name,
      Categories.Healthcare.name
   )

   Box(modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 30.dp)
   )
   {
      Row(Modifier.align(Alignment.Center)) {
         ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {// Short lambda to expand
            // the dropdown or not.
            /* This dropdown menu box houses all the different categories,
            * which allows the user to pick a particular category. */
            TextField(
               value = viewModel.category.value, //This is the category that is chosen and stored in the addEntryViewModel.
               onValueChange = {},
               readOnly = true, //To not allow user to change the value of the category.
               trailingIcon = {ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)},
               modifier= Modifier.menuAnchor(),
               label= { Text(text = "Category")}
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
               /* This part of the dropdown is the actual dropdown itself. This displays all the
               * categories and allows the value of the TextField to be changed through a tap. */
               categories.forEach {
                     item ->
                  DropdownMenuItem(text = { Text(text=item) }, onClick = { viewModel.onCategoryChange(item)
                     expanded = false
                  })
               }
            }
         }
      }

   }
}