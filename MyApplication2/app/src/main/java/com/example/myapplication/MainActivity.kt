package com.example.myapplication

import AddEntry
import HorizontalAddEntry
import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.media.Image
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.ui.theme.BabyBlue
import com.example.myapplication.ui.theme.Beige
import com.example.myapplication.ui.theme.GreenHex
import com.example.myapplication.ui.theme.Lavenderish
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.Reddish
import com.example.myapplication.ui.theme.Salmon
import com.google.accompanist.permissions.rememberPermissionState
import com.google.firebase.FirebaseApp.initializeApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.File
import java.util.concurrent.ExecutorService
import kotlin.random.Random


//The below enum class is the pre-ordained list of categories for types of expenses.
enum class Categories(){
    Housing,
    Food,
    Transportation,
    Entertainment,
    Utilities,
    Healthcare,
    Groceries
}

//This is the pivotal class that is used throughout the program. This class expects a definition for:
//1. A name for the entry
//2. The allocation (spent) amount
//3. A categorisation of the spending in question
//4. The file path for the image that maybe attached to the entry
data class BudgetEntry(val name: String = "", val allocation: Double = 0.0, val category: String = "", val file: String = "")


class SharedViewModel: ViewModel(){
    /* The SharedViewModel class is responsible for the management and maintenance of the budgetList entries.
    * This includes all the functionality expected to store and retrieve data from Google's Firebase Database.*/
    private val _budgetEntries = mutableStateListOf<BudgetEntry>() //mutable list to hold all budget entries.
    //This piece of code adds a read-only property to _budgetentries.
    //This allows other parts of the code to access, but not manipulate _budgetEntries.
    val budgetEntries: List<BudgetEntry>
        get() = _budgetEntries

    init { //initialization Block
        //This initialization block is to set up firebase referencing and listener for the addition of
        //entries.
        val budgetDatabase  = FirebaseDatabase.getInstance()
        val budgetRef = budgetDatabase.getReference("budgetList")
        // Add a listener to the database reference to look for any changes.
        budgetRef.addValueEventListener(object: ValueEventListener{
            //When any data is changed, call the below function.
            override fun onDataChange(snapshot: DataSnapshot) {
                _budgetEntries.clear() //Completely clear the pre-existing list.
                for (snap in snapshot.children){
                    val budgetEntry = snap.getValue(BudgetEntry::class.java) //Convert the snapshot
                    // to budgetEntry
                    if(budgetEntry != null){
                        _budgetEntries.add(budgetEntry) //Add the entry to the list.
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
    fun addBudgetEntry(entry: BudgetEntry){
        //The below function is used to add an entry to our private list.
        _budgetEntries.add(entry)
        addToDatabase()
    }

    private fun addToDatabase() {
        //This function is facilitate the addition of a new entry into the Firebase Database.
        val budgetDatabase = FirebaseDatabase.getInstance() //Look for a firebase database
        val budgetRef = budgetDatabase.getReference("budgetList")
        budgetRef.setValue(_budgetEntries) //Set the value for the reference 'budgetList' as the new array
            //of budget entries
            .addOnSuccessListener { //If successful, log an appropriate message.
                Log.d(MainActivity.TAG, "Another Entry Added to the Database!")
            }
            .addOnFailureListener{ e ->  //If unsuccessful, log an appropriate message.
                Log.e(MainActivity.TAG, "Addition of Entry failed!", e)
            }
    }

    @SuppressLint("SuspiciousIndentation")
    fun calculateCategoryAllocations(): List<CategoryAllocation> {
        /* This below function is an auxilllary function to
        * calculate the spending of each category
        * Used in the Pie Chart composable */
    val categoryMap = mutableMapOf<String, Double>()
        //initialize the mapping with zero allocation for every category.
        Categories.entries.forEach {
            categories ->  categoryMap[categories.name] = 0.0
        }

        //Sumup the spending for each category
        _budgetEntries.forEach{
            entry ->
            val currentAllocation = categoryMap[entry.category] ?: 0.0
            categoryMap[entry.category] = currentAllocation + entry.allocation
        }

        return Categories.entries.map {
            //return a list of CategoryAllocation Objects for the aid of pie chart allocation
            categories ->
            val allocation = categoryMap[categories.name] ?: 0.0
            CategoryAllocation(categories.name, allocation, getColorForCategory(categories.name))
        }
    }
}

//Function to get a color based on category names
private fun getColorForCategory(category: String): Color {
    return when (category) {
        Categories.Housing.name -> GreenHex
        Categories.Food.name -> Reddish
        Categories.Transportation.name -> BabyBlue
        Categories.Entertainment.name -> Lavenderish
        Categories.Groceries.name -> Beige
        Categories.Utilities.name -> Salmon
        else -> Color.Gray
    }
}

//Find the total spending when passed a list of
//budget entries
fun findTotalAllocation(entries: List<BudgetEntry>): Double {
    var total_allocation = 0.0
    for (entry in entries) {
        total_allocation += entry.allocation
    }
    return total_allocation
}





class MainActivity : ComponentActivity() {
    /* This is the mainActivity for this project. This is where all the functionalities discussed
    * in every other file in this project coalesce into a singular place.
    * The navigation component to move across the different screens is initialized and
    * implemented. */
    private val SharedViewModel by viewModels<SharedViewModel>()
    private val addEntryViewModel = AddEntryViewModel()



    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeApp(this)
        setContent {
            val total_spending = findTotalAllocation(SharedViewModel.budgetEntries) //We find the total spending using the function described above
            //This is used for the notification as seen below.
            MyApplicationTheme {
                createNotification(this, total_spending) //We create a notification
                //using the function outlined in notificationservice.kt, where we pass the total_spending to be used
                //as the body text of the notification
                Surface(color= MaterialTheme.colorScheme.background) {
                    val configuration = LocalConfiguration.current //This is used to figure out whether the screen is kept horizontally
                    //or vertically.
                    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE //This is a boolean variable
                    //which checks if the orientation is landscape. This is used to show different layouts depending.
                    val navController:NavHostController = rememberNavController() //This is the navController, our navigation component
                    //for the app, that provides the ability to move across all the various screens in the app.
//                    val viewModel = AddEntryViewModel() //Another
                    NavHost(navController = navController, startDestination = "budgetList"){//We mention that the landing page of our
                        //application is a location called 'budgetList', we will outline what budgetList is below.
                        composable(route= "budgetList"){
                            //This is where the budgetList function defined in BudgetList.kt is finally used. We pass the budgetEntries from the viewModel,
                            //what to do when the action button is clicked (navigate to the addEntryScreen, which is the addEntryViewModel), and all the categories
                            //defined above.
                            //We also use the isLandscape variable to show different layouts depending on the orientation
                            if(isLandscape){
                                HorizontalBudgetList(
                                    entries = SharedViewModel.budgetEntries, onAddEntry = {navController.navigate("addEntryScreen")}, Categories= SharedViewModel.calculateCategoryAllocations(), navController
                                )
                            }
                            else
                            BudgetList(entries = SharedViewModel.budgetEntries, onAddEntry = {navController.navigate("addEntryScreen")}, Categories= SharedViewModel.calculateCategoryAllocations(), navController)
                        }
                        composable(route="addEntryScreen") {
                            //Here, we describe the route for the onAddEntry Lambda, namely 'addEntryScreen'. This just moves us to the
                            //AddEntry function outlined in AddEntry.kt. We also use the isLandscape variable here.
                            if(isLandscape){
                                HorizontalAddEntry(
                                    sharedViewModel = SharedViewModel,
                                    addEntryViewModel = addEntryViewModel,
                                    navController = navController
                                )
                            }
                            else{
                                AddEntry(
                                    sharedViewModel = SharedViewModel,
                                    addEntryViewModel = addEntryViewModel,
                                    navController = navController
                                )
                            }

                        }
                        composable(route = "Detailed/{index}",
                            //Here, we try to navigate to the DetailedView function defined in DetailedEntry.kt
                            //For that purpose, we have to extract the possible integer in '{index}', to navigate
                            //to the appropriate entry's detailedView
                            //We again use the isLandscape variable to show the alternative layout when kept in landscape.
                        arguments = listOf(
                            navArgument(name = "index"){
                                type = NavType.IntType
                            }
                        )
                        ){
                            index ->
                            if(isLandscape){
                                index.arguments?.getInt("index")?.let {
                                HorizontalDetailedView(
                                    BudgetEntry = SharedViewModel.budgetEntries,
                                    itemIndex = it,
                                    navController = navController
                                )
                                }
                            }
                            else {
                                index.arguments?.getInt("index")?.let {
                                    DetailedEntry(
                                        BudgetEntry = SharedViewModel.budgetEntries,
                                        itemIndex = it,
                                        navController = navController
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

    }
    companion object {
        const val TAG = "MainActivity" // A simple TAG constant created for logging purposes.
    }
}

