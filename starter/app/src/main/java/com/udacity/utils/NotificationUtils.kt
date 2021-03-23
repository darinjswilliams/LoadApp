package com.udacity.utils

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.provider.Settings.Global.getString
import androidx.core.app.NotificationCompat
import com.udacity.DetailActivity
import com.udacity.R
import timber.log.Timber

//Notification ID
private val NOTIFICATION_ID = 0

/**
 * Builds and delivers the notification.
 *
 * @param context, activity context.
 */
fun NotificationManager.sendNotification(
    messageBody: String,
    applicationContext: Context,
    downloadStatus: String
) {

    //Create Intent
    val contentIntent = Intent(applicationContext, DetailActivity::class.java)
        .putExtra(applicationContext.getString(R.string.message_download), messageBody)
        .putExtra(applicationContext.getString(R.string.label_file_status), downloadStatus)

    //Set Status and name of download file in Intent
    Timber.i("File Name ${messageBody}")
    Timber.i("Status of file ${downloadStatus}")
//    contentIntent.putExtra(R.string.message_download, messageBody)
//    contentIntent.putExtra(R.string.label_file_status, downloadStatus)

//    //Create Pending Intent with the BackStack, The pendingintent will open the application
//    val contentPendingIntent: PendingIntent? = TaskStackBuilder.create(applicationContext).run {
//        // Add the intent, which inflates the back stack
//        addNextIntentWithParentStack(contentIntent)
//        getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
//    }

    val contentPendingIntent = PendingIntent.getActivity(
        applicationContext,
        NOTIFICATION_ID,
        contentIntent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    //Custom Notification
    val cloudImage = BitmapFactory.decodeResource(
        applicationContext.resources,
        R.drawable.cloudimage
    )

    val bigPicStyle = NotificationCompat.BigPictureStyle()
        .bigLargeIcon(cloudImage)
        .bigLargeIcon(null)

    //Build the notification
    val builder = NotificationCompat.Builder(
        applicationContext,
        applicationContext.getString(R.string.loadapp_notification_channel_id)
    )


        //Set title, text and icon to builder
        .setSmallIcon(R.drawable.ic_baseline_cloud_download_24)
        .setContentTitle(applicationContext.getString(R.string.notification_title))
        .setContentText(applicationContext.getString(R.string.notification_description))
        .setContentIntent(contentPendingIntent)
        .setAutoCancel(true)
        .setStyle(bigPicStyle)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setLargeIcon(cloudImage).addAction(
            R.drawable.ic_baseline_cloud_download_24,
            applicationContext.getString(R.string.notification_action_message),
            contentPendingIntent
        )



    Timber.i("Here is the status: ${downloadStatus}")
    //Call Nofity with unique id
    notify(NOTIFICATION_ID, builder.build())
}