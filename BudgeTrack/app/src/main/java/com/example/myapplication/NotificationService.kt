package com.example.myapplication

import android.Manifest
import android.app.Activity
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.PermissionChecker
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import kotlin.random.Random

@RequiresApi(Build.VERSION_CODES.O)
fun createNotification(context: Context, allocation: Double){
    /* This below function exists to create the various pieces that is required to make notifications work.
    * This is done to keep the user aware of their spending at all times, even when they are not using the app.
    * The notification SHOULD show the total expenditure so far. */
    val notificationManager = NotificationManagerCompat.from(context) //Create an instance of NotificationManageCompat class
    //to manage notifications.

    val notificationID = Random.nextInt() //Random number generated for the ID for identifying the notification.

    //Defining the name and channel of the notification
    val channelId = "Total Allocation"
    val channelName = "BudgeTrack"
    //Denoting the importance of the notification
    val importance = NotificationManager.IMPORTANCE_HIGH

    val notificationChannel = NotificationChannel(channelId, channelName, importance) //Creating a notification channel using the aforementioned variables.
    //Notification channels are required for android API 26 and above.
    notificationManager.createNotificationChannel(notificationChannel)

    //Now, we will have to actually build the notification itself:
    //It would require an icon, title, the body of the notification and the priority of the notification.
    val builder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.money)
        .setContentTitle("BudgeTrack")
        .setContentText("Total Spending: ${allocation}£")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
    //The below piece of code mirrors the one for the camera permissions. If the permission
    //has already been granted, simply show notifications. However, if permissions have not yet
    //been granted, then request permissions from the user.
    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        ActivityCompat.requestPermissions(
            context as Activity,
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            123
        )
        // TODO: Consider calling
        //    ActivityCompat#requestPermissions
        // here to request the missing permissions, and then overriding
        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
        //                                          int[] grantResults)
        // to handle the case where the user grants the permission. See the documentation
        // for ActivityCompat#requestPermissions for more details.
        return
    }
    notificationManager.cancelAll() //This line exists so that any changes to the entries in budgetList should also recreate the notification,
    //with the new total spending. This means that the old notification with the old spending amount must be removed.
    notificationManager.notify(notificationID, builder.build()) //Finally, display the notification
}

