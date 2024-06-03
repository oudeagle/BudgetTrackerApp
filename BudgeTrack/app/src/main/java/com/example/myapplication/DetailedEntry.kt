package com.example.myapplication

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.myapplication.ui.theme.GreenHex
import java.io.File

@Composable
fun DetailedEntry(
    /* The below function is used to provide more detail to every entry in the budgetList
    * This screen is navigated to using the navController, and provides information about the particular entry.
    * This includes the name, category, allocation and any prior attachments already provided to this entry.
    * To show the right entry, we also pass the index of the entry in the list. */

    BudgetEntry:List<BudgetEntry>,
    itemIndex: Int,
    navController: NavController
) {
    val imageFile = File(
        BudgetEntry[itemIndex].file //The imageFile variable stores the file path of the image in the
        //entry.
    )
    var imageBit: Bitmap? = null
    if(imageFile.exists()){ //If the image exists, then figure out its bitmap and store it in imageBit.
        imageBit = BitmapFactory.decodeFile(imageFile.absolutePath)
    }

//    var selectedImage by remember {
//        mutableStateOf<Uri?>(null)
//    }
//
//    val singlePhotoPicker = rememberLauncherForActivityResult(contract = ActivityResultContracts.PickVisualMedia(),
//        onResult ={ uri ->  selectedImage = uri }
//    )
    Scaffold(topBar = { DetailedEntryToolbar(navController)},
        ) {
        innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(vertical = 150.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            //The below text is the text displaying the name of the entry.
            Text(text = BudgetEntry[itemIndex].name,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold)
            Text(text = BudgetEntry[itemIndex].allocation.toString() + "£", fontSize = 20.sp)
            //The below text is the text displaying the category of the entry.
            Text(text = "Category: ${BudgetEntry[itemIndex].category}",
                modifier = Modifier.padding(vertical = 25.dp),
                fontSize = 18.sp
            )
            Column(
                modifier = Modifier.padding(innerPadding)
            ) {
                AsyncImage( //If the image exists, it will be stored in the aforementioned imageBit variable.
                    //This image is then displayed here.
                    model = imageBit,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize(),
                    contentScale = ContentScale.Fit

                )
            }
        }
    }

}

@Composable
fun HorizontalDetailedView(
    /* This is simply another version of the same function, to accomodate for horizontal viewing.
    * */
    BudgetEntry:List<BudgetEntry>,
    itemIndex: Int,
    navController: NavController
){
    val imageFile = File(
        BudgetEntry[itemIndex].file
    )
    var imageBit: Bitmap? = null
    if(imageFile.exists()){
        imageBit = BitmapFactory.decodeFile(imageFile.absolutePath)
    }
    Scaffold(topBar = { DetailedEntryToolbar(navController)},
    ) {
            innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = BudgetEntry[itemIndex].name,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold)
            Text(text = BudgetEntry[itemIndex].allocation.toString() + "£", fontSize = 20.sp)
            Text(text = "Category: ${BudgetEntry[itemIndex].category}",
                modifier = Modifier.padding(vertical = 25.dp),
                fontSize = 18.sp
            )
            Column(
                modifier = Modifier.padding(innerPadding)
            ) {
                AsyncImage(
                    model = imageBit,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .size(150.dp),
                    contentScale = ContentScale.Fit

                )
            }
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun DetailedEntryPreview(){
//    DetailedEntry(
//        BudgetEntry = SharedViewModel.budgetEntries ,
//        item = BudgetEntry(
//            name = "Mortgage Payment",
//            allocation = 1200.00,
//            category = Categories.Housing.name
//        )
//    )
//}