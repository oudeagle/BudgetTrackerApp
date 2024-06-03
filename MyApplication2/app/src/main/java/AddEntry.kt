import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.myapplication.AddEntryToolbar
import com.example.myapplication.AddEntryViewModel
import com.example.myapplication.BudgetEntry
//import com.example.myapplication.CameraScreen
//import com.example.myapplication.CameraScreenButton
import com.example.myapplication.Category_Dropdown
import com.example.myapplication.MainActivity
import com.example.myapplication.SharedViewModel
import com.example.myapplication.ui.theme.Burgundy
import com.example.myapplication.ui.theme.GreenDark
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

@SuppressLint("Recycle")
/* The function below is relevant for the attachments.
* The problem that I had encountered was that when saving the attachment, it would obviously
* store only the uri for the picture. This leads to the problem,
* which is that when the record is retrieved from the database, it cannot
* use the uri to find the image within the system. Therefore, we would need to find the actual path
* of the file based on the Uri. This is what this function aims to do. */
fun getRealPathFromURI(uri: Uri, context: Context): String? {
    val returnCursor = context.contentResolver.query(uri, null, null, null, null)
    val nameIndex =  returnCursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
    returnCursor.moveToFirst()
    val name = returnCursor.getString(nameIndex)
    val file = File(context.filesDir, name)
    try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val outputStream = FileOutputStream(file)
        var read = 0
        val maxBufferSize = 1 * 1024 * 1024
        val bytesAvailable: Int = inputStream?.available() ?: 0
        val bufferSize = Math.min(bytesAvailable, maxBufferSize)
        val buffers = ByteArray(bufferSize)
        while (inputStream?.read(buffers).also {
                if (it != null) {
                    read = it
                }
            } != -1) {
            outputStream.write(buffers, 0, read)
        }
        Log.e("File Size", "Size " + file.length())
        inputStream?.close()
        outputStream.close()
        Log.e("File Path", "Path " + file.path)

    } catch (e: java.lang.Exception) {
        Log.e("Exception", e.message!!)
    }
    return file.path
}

@SuppressLint("RememberReturnType")
@Composable
/* The below function, is the function which utilises the addEntryViewModel to accept user's input, and
* displays this data in a particular format through the SharedViewModel in the budgetList screen.
*  */
fun AddEntry(
    sharedViewModel: SharedViewModel, //To pass data along to the budgetList screen.
    addEntryViewModel: AddEntryViewModel, //To handle the fields being manipulated in AddEntryViewModel
    navController: NavHostController //Using a navigation component to help move between the addEntry screen and
    // the budgetList screen.
) {
    var selectedImage by remember { // This variable stores the image chosen by the user,
        //either through the photo picker, or the image taken with the camera.
        mutableStateOf<Uri?>(null)
    }

    val singlePhotoPicker =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.PickVisualMedia(),
            onResult = { uri -> selectedImage = uri }
        ) //This variable stores the image chosen by the user through the photo picker.
    //Uses rememberLauncherForActivityResult to display the photo picker, where the user can pick only
    // ONE picture.

    val image = selectedImage?.let { getRealPathFromURI(it, LocalContext.current) } //This variable stores the file path for the picture chosen from either
    //the photo picker or the camera.
    val context = LocalContext.current
    Scaffold(topBar = { AddEntryToolbar(navController) }) { innerPadding -> //Simply implementing the topBar for the addEntry screen.
        // This is where all the inputs required for a new entry is taken.
        Column {
            TextField(
                 //This textField takes in the value for the name of the expense.
                value = addEntryViewModel.name.value,
                onValueChange = addEntryViewModel::onNameChange,
                label = { Text(text = "Name") },
                maxLines = 1,
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxWidth()
                    .padding(10.dp)
            )
            TextField(
                //THis textField takes in the value for the amount that the user has spent on a particular expense.
                // This value taken as a Double, is converted to string.
                value = addEntryViewModel.allocation.doubleValue.toString(),
                onValueChange = { onAddValueChange(it, addEntryViewModel) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), // The keyboard type is defaulted to a number keyboard,
                // to prevent users from implementing any alphabets.
                maxLines = 1,
                label = { Text(text = "Spent") },
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth()
            )
            Row(
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 10.dp, horizontal = 10.dp)
            ) {
                Button(onClick = {
                    singlePhotoPicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly) //The photo picker implementation is
                        // finally used here. The picker is triggered by a button press.
                    )
                }) {
                    Text(text = "Add Attachment")
                }
                CaptureImageButton { uri ->
                    selectedImage = uri
                } //This button, called CaptureImageButton, through a button press triggers the camera to be displayed (permission permitting)
                //to take a picture. Once a picture is taken, it is then passed to the selectedImage variable in this file.
            }
            Row(Modifier.align(Alignment.CenterHorizontally))
            {
                AsyncImage(
                    //This AsyncImage is from the coil library, which is used to load and display an image.
                    //In this context, it is used to provide a provide the user with a preview of the image chosen/captured.
                    model = selectedImage,
                    contentDescription = null,
                    modifier = Modifier.size(100.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Row()
            {
                Category_Dropdown(viewModel = addEntryViewModel) //This is a dropdown menu, defined in another file, Category_Dropdown
                //This dropdown allows the user to define what category could the spending be categorised by.
            }
            Row() {
                Box(Modifier.weight(1f)) {
                    Button(
                        //The button below is the cancel button,
                        // its only functionality is to use the navigation component, navController to move back to the
                        // budgetList screen if the user decides to not to add a new entry.
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.padding(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            Burgundy // Preconceived notion for users is that reddish colors represent 'no'.
                        )
                    ) {
                        Text(text = "Cancel")
                    }
                    Button(
                        /* The below button is used to take the values from the view model and store them into actual variables.
                        * The name, amount spent (allocation), category and any image is stored into variables and is coalesced into a new
                        * instance of the data class budgetEntry.
                        * This entry is appended the list of pre-existing list of budgetEntries. */
                        onClick = {
                            val name = addEntryViewModel.name.value // name of entry stored into a variable.
                            val allocation = addEntryViewModel.allocation.value //the spent amount stored in a variable.
                            addEntryViewModel.attachment.value = image.toString() //The image file path is converted to a string,
                            //then stored in a variable.
                            if (name.isNotEmpty() || allocation > 0.0) { // A verification step to make sure that the user has input something
                                //into the name and allocation fields.
                                val category = addEntryViewModel.category.value //Category of expense is stored in a variable
                                val newEntry = BudgetEntry( //All of the above variables are combined into a new budgetEntry, ready to be appended.
                                    name,
                                    allocation,
                                    category,
                                    addEntryViewModel.attachment.value
                                )
                                sharedViewModel.addBudgetEntry(newEntry) //The prior created newEntry is appended to the budgetList of the sharedViewModel.
                                /* After appending the values to the budgetList, the inputted fields are emptied to allow
                                * for future entries. */
                                addEntryViewModel.name.value = ""
                                addEntryViewModel.allocation.doubleValue = 0.0
                                addEntryViewModel.attachment.value = ""
                                navController.navigate(route = "budgetList") //The navigation component navigates
                                //to the budgetList screen.

                            } else {
                                //The below appears when the user does not either enter a name or a spending amount.
                                Toast.makeText(
                                    context,
                                    "Please enter something for name and/or allocation.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            GreenDark //The pre-conceived notion for humans is that green means 'yes'
                        )
                    )
                    {
                        Text("Save")
                    }
                }
            }
        }
    }
}


@SuppressLint("RememberReturnType")
@Composable
/* The below version of the function triggers, when the device is kept in landscape rather than the usual portrait.
* There is a single major change to the function, where we change the column which encompasses all the fields required for input,
* into a LazyColumn which allows scrollability. */
fun HorizontalAddEntry(
    sharedViewModel: SharedViewModel,
    addEntryViewModel: AddEntryViewModel,
    navController: NavHostController
) {
    var selectedImage by remember {
        mutableStateOf<Uri?>(null)
    }

    val singlePhotoPicker =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.PickVisualMedia(),
            onResult = { uri -> selectedImage = uri }
        )

    val image = selectedImage?.let { getRealPathFromURI(it, LocalContext.current) }
    val context = LocalContext.current
    val showCameraScreen = remember { mutableStateOf(false) }

    Scaffold(topBar = { AddEntryToolbar(navController) }) { innerPadding ->
        LazyColumn( //This is the single major change to the function. To allow for scrolling, I have converted the column to a
            // lazy column.
            modifier = Modifier.padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {//Since lazy column requires 'items' for it to be functional, we would have to encapsulate each input requirement into its
                // own itemScope.
                TextField(
                    value = addEntryViewModel.name.value,
                    onValueChange = addEntryViewModel::onNameChange,
                    label = { Text(text = "Name") },
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                )
            }
            item {
                TextField(
                    value = addEntryViewModel.allocation.doubleValue.toString(), // Convert Double to String
                    onValueChange = { onAddValueChange(it, addEntryViewModel) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    maxLines = 1,
                    label = { Text(text = "Spent") },
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxWidth()
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(onClick = {
                        singlePhotoPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }) {
                        Text(text = "Add Attachment")
                    }
                    CaptureImageButton {
                        uri -> selectedImage = uri
                    }
                }
            }
            item {
                    AsyncImage(
                        /* There are few minor issues with how images are rendered in a horizontal position */
                        model = selectedImage,
                        contentDescription = null,
                        modifier = Modifier
                            .size(100.dp),
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.Center
                    )
            }
            item {
                Category_Dropdown(viewModel = addEntryViewModel)
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.padding(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            Burgundy
                        )
                    ) {
                        Text(text = "Cancel")
                    }
                    Button(
                        onClick = {
                            val name = addEntryViewModel.name.value
                            val allocation = addEntryViewModel.allocation.doubleValue
                            addEntryViewModel.attachment.value = image.toString()
                            if (name.isNotEmpty() || allocation > 0.0) {
                                val category = addEntryViewModel.category.value
                                val newEntry = BudgetEntry(
                                    name,
                                    allocation,
                                    category,
                                    addEntryViewModel.attachment.value
                                )
                                sharedViewModel.addBudgetEntry(newEntry)
                                addEntryViewModel.name.value = ""
                                addEntryViewModel.allocation.doubleValue = 0.0
                                addEntryViewModel.attachment.value = ""
                                navController.navigate(route = "budgetList")
                            } else {
                                Toast.makeText(
                                    context,
                                    "Please enter something for name and/or allocation.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        },
                        modifier = Modifier
                            .padding(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            GreenDark
                        )
                    )
                    {
                        Text("Save")
                    }
                }
            }
        }
    }
}


fun onAddValueChange(value: String, viewModel: AddEntryViewModel) {
    // Auxillary function to help convert the value inputted for spent value to double
    //If not possible, display a number format exception.
    try {
        viewModel.onAllocationChange(value.toDouble())
    } catch (e: NumberFormatException) {
        Log.e("Adhi", "Could not convert input to Double", e)
    }
}
@Preview(showBackground = true)
@Composable
fun AddEntryPreview(){
    AddEntry(sharedViewModel = SharedViewModel(), addEntryViewModel = AddEntryViewModel(),  navController = rememberNavController())
}
