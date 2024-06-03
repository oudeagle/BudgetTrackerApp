import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
/*
CaptureImageButton is an auxillary function,
to encapsulate the entire process of taking a picture and storing it as
an attachment in the AddEntryViewModel
* */
fun CaptureImageButton(onImageCaptured: (Uri) -> Unit) {
    val context = LocalContext.current
    val imageFile = remember { createImageFile(context) } //Stores the value for the image file based on the Uri of the image.

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(), //Default android function to take pictures.
        onResult = { success ->
            /*
            * This lambda focuses on what should happen, if the user is happy with the picture
            * taken. It finds out the Uri of the image, and stores in a variable which is passed
            * and displayed as the attachment.
            * */
            if (success) {
                val imageUri = FileProvider.getUriForFile( //Stores the Uri for the image file.
                    context,
                    "${context.packageName}.fileprovider",
                    imageFile
                )
                onImageCaptured(imageUri) //Fulfills the lambda in AddEntry()
            } else {
                Log.e("CaptureImageButton", "Camera capture failed") //In the case that the user decides
                //not to provide permissions, used for debugging purposes.
                Toast.makeText(context, "Camera Permission Denied!", Toast.LENGTH_LONG).show() //Toast to show the user
                //that permissions were not provided.
            }
        }
    )

    /*
    * The below variable stores the current permission of the camera. If permission is granted,
    * then the launchCamera() function is allowed to be run.
    * Otherwise, the user is notified that the permission is not granted,
    * therefore they would not be able to take pictures.
     */
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                launchCamera(context, cameraLauncher, imageFile)
            } else {
                Log.e("CaptureImageButton", "Camera permission denied")
                Toast.makeText(context, "Camera Permission Denied!", Toast.LENGTH_LONG).show()
            }
        }
    )

    Button(onClick = {
        /* This lambda would check if camera permissions had been allowed before.
        * If yes, then launchCamera() function would launch
        * If no, then the user would be prompted to grant permissions once again.
        * */
        when {
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                launchCamera(context, cameraLauncher, imageFile)
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA) //If permissions were not asked, then ask for it.
            }
        }
    }) {
        Text("Capture Image")
    }
}


private fun createImageFile(context: Context): File {
    /* createImageFile, is another helper function, to define the default name of the image file
    * This can be defined as:
    * 1. A timestamp variable which stores the time the picture is taken
    * 2. The storage directory, defined in the xml - path_provider to store the images.
    * The function creates a file to send back to CaptureImageButton, to get the Uri. */
    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile(
        "JPEG_${timeStamp}_",
        ".jpg", //All pictures taken are jpeg
        storageDir
    )
}

private fun launchCamera(
    /* This is the make or break part of this entire file. This is function that actually
    launches the camera, allowing pictures to be taken.
    * This function is super simple in terms of functionality, it displays the camera app and
    when a picture is taken, the imageUri for the picture taken is computed.
    */
    context: Context,
    cameraLauncher: ActivityResultLauncher<Uri>,
    imageFile: File
) {
    val imageUri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile
    )
    cameraLauncher.launch(imageUri)
}
