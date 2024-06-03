package com.example.myapplication

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.theme.BabyBlue
import com.example.myapplication.ui.theme.Beige
import com.example.myapplication.ui.theme.Burgundy
import com.example.myapplication.ui.theme.GreenDark
import com.example.myapplication.ui.theme.GreenHex
import com.example.myapplication.ui.theme.Lavenderish
import com.example.myapplication.ui.theme.Reddish
import com.example.myapplication.ui.theme.Salmon
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun BudgetList(entries: List<BudgetEntry>, onAddEntry: () -> Unit, Categories:List<CategoryAllocation>, navHostController: NavHostController) {
    /* The below function is essentially the landing screen, if you will, for the application.
    * This screen contains four major things:
    * 1. A piechart which visualises the percentage of expenditure for every category in the budgetList
    * 2. Every single entry within the list of entries, which is passed into this function.
    * 3. A single line showing the total expenditure so far.
    * 4. A button which navigates the user to the addEntryViewModel
    * Throughout this function, I will highlight these four aspects and any other relevant information. */
    Scaffold (topBar = { Toolbar()} // A simple topbar to which covers the top part of the screen,
        // also highlighting the name of the app
        , floatingActionButton = {
            FloatingActionButton(onClick = onAddEntry, // This is the button mentioned as 4., which allows the user to navigate into
                //the addEntryViewModel to add a new entry
                containerColor = GreenDark) //Green is used as it is the most visible colour in the spectrum.
            {
                Icon( //A plus icon is used, as a preconceived notion that a plus symbol means to add something.
                    Icons.Default.Add,
                    contentDescription = "Add",
                    tint=Color.White //White is used as it has a strong contrast with the forest green button.
                )
            }
        }
    ){
        innerPadding ->
    Column(modifier = Modifier.padding(innerPadding)) {
            PieChart(categories = Categories) //Here, we are calling the auxillary function of PieChart, as well as passing the
        //categories that are passed on to this function, to display the piechart in this screen. This fulfills the
        //1. mentioned at top.
            LazyColumn(modifier = Modifier
                /* This lazycolumn houses all the entries in budgetList,
                * Each entry is displayed in appropriate manner using the BudgetEntryItem
                * function enumerated below. */
                .padding(10.dp)
                .padding(vertical = 15.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(entries) { entry ->
                    BudgetEntryItem(entry, navHostController, entries.indexOf(entry))
                }
            }
        }
    }
}

@Composable
fun BudgetEntryItem(entry: BudgetEntry, navHostController: NavHostController, index: Int) {
    /* The below BudgetEntryItem function converts the raw data within an entry into box that you see in the
    * budgetList screen.  */
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = when (entry.category) {
                    //Based on the category of the entry, we change the color of the box.
                    Categories.Housing.name -> GreenHex
                    Categories.Food.name -> Reddish
                    Categories.Transportation.name -> BabyBlue
                    Categories.Entertainment.name -> Lavenderish
                    Categories.Groceries.name -> Beige
                    Categories.Utilities.name -> Salmon
                    else -> Color.Gray //Simply for error handling purposes.
                }
            )
            .padding(10.dp)
            .clickable { navHostController.navigate(route = "Detailed/$index") } //We make this row clickable
            // to navigate the user to the detailedEntry screen. For helping with the navigation purpose, we will manipulate the route
            // to include the index of the entry.
    ) {
        Column {
        Text(
            //This text houses the name of the entry
            text = entry.name,
            fontSize = 20.sp,
            modifier = Modifier.padding(vertical=10.dp)
        )
            //This text houses the name of the category in a smaller font, as to highlight the entry name.
            Text(text = entry.category,
                fontSize = 15.sp
                )
        }
        Spacer(modifier = Modifier.weight(1f))
        Column {
            Text(text = "%.2f£".format(entry.allocation), modifier = Modifier.padding(vertical=10.dp), fontWeight= FontWeight.Bold, fontSize = 20.sp) // Format allocation with 2 decimal places
            Row(modifier= Modifier.align(Alignment.End)) {
                // The below button is used to remove the entry, not only from the SharedViewModel but from the firebase database as well.
                //This prevents any surprises, where a user may remove an entry from the viewModel however it reappears when the app is reopened.
                Button(
                    onClick = { removeBudgetEntry(entry) },
                    border = BorderStroke(1.dp, Color.Red), //Red is used to denote something as negative.
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Burgundy
                    )
                ) {
                    Text(text = "X") // We use 'X' to also denote something that can be removed.
                }
            }
        }
    }
}

fun removeBudgetEntry(entry: BudgetEntry) {
    val database = FirebaseDatabase.getInstance()
    val budgetRef = database.getReference("budgetList")

    budgetRef.orderByChild("name").equalTo(entry.name).addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            for (snapshot in dataSnapshot.children) {
                val entryKey = snapshot.key
                if (entryKey != null) {
                    budgetRef.child(entryKey).removeValue()
                        .addOnSuccessListener {
                            Log.d(MainActivity.TAG, "Budget entry removed from database")
                        }
                        .addOnFailureListener { e ->
                            Log.e(MainActivity.TAG, "Error removing budget entry from database", e)
                        }
                    return
                }
            }
            Log.d(MainActivity.TAG, "Budget entry not found in database")
        }

        override fun onCancelled(databaseError: DatabaseError) {
            Log.e(MainActivity.TAG, "Database error: ${databaseError.message}")
        }
    })
}


//@Preview(showBackground= true)
//@Composable
//fun budget_preview(){
//    BudgetList(entries = listOf(BudgetEntry("Movies", 50.00, Categories.Entertainment.name), BudgetEntry("Mortgage Payments", 1250.00, Categories.Housing.name))) {
//
//}
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HorizontalBudgetList(
    entries: List<BudgetEntry>,
    onAddEntry: () -> Unit,
    Categories: List<CategoryAllocation>,
    navHostController: NavHostController
) {
    Scaffold(
        topBar = { Toolbar() },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddEntry,
                containerColor = GreenDark
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add",
                    tint = Color.White
                )
            }
        }
    ) { innerPadding ->
        Row(modifier = Modifier.padding(innerPadding)) {
            HorizontalPieChart(categories = Categories)
            LazyColumn(modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(entries) { entry ->
                    BudgetEntryItem(entry, navHostController, entries.indexOf(entry))
                }
            }
        }
    }
}

@Composable
fun OrientationChange(){
    val configuration = LocalConfiguration.current
    val isHorizontal = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
}
