package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.udacity.utils.sendNotification
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import timber.log.Timber
import java.io.File


class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0

    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action
    private var URL: String = NO_RADIO_BUTTON_CLICKED


    private lateinit var buttonState: ButtonState


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)


        //Create a Channel
        createChannel(
            getString(R.string.loadapp_notification_channel_id),
            getString(R.string.app_name)
        )
        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))


        radioBtnGroup.setOnCheckedChangeListener { radioGroup, _ ->
            URL = when (radioGroup.checkedRadioButtonId) {
                R.id.glide -> URL_GLIDE
                R.id.loadApp -> URL_LOAD_APP
                R.id.retrofit -> URL_RETROFIT
                else -> NO_RADIO_BUTTON_CLICKED
            }
        }

        custom_button.setOnClickListener {

            if (!NO_RADIO_BUTTON_CLICKED.equals(URL)) {
                download()
            } else {
                Timber.i("Radio Button Clicked, Change state to clicked")
                custom_button.buttonState = ButtonState.Clicked
            }

        }

    }


    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Timber.i("Download Receiver")
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

            val action = intent?.action

            if (downloadID == id) {
                if (action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                    val query = DownloadManager.Query()
                        .setFilterById(intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0));
                    val manager =
                        context!!.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    val cursor: Cursor = manager.query(query)
                    if (cursor.moveToFirst()) {
                        if (cursor.count > 0) {
                            val status =
                                cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))

                            //Change Buttong state to completed
                            custom_button.buttonState = ButtonState.Completed
                            when (status) {
                                DownloadManager.STATUS_SUCCESSFUL -> {
                                    notificationManager.sendNotification(
                                        URL, applicationContext,
                                        resources.getString(R.string.message_success)
                                    )

                                    Timber.i("The status is Successful: ${status} ")
                                }
                                else -> {
                                    notificationManager.sendNotification(
                                        URL,
                                        applicationContext,
                                        resources.getString(R.string.message_failure)
                                    )
                                    Timber.i("The status is Failure: ${status} ")
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    private fun download() {
        Timber.i("Download file: ${URL}")

        //NotificationManager
        notificationManager = ContextCompat.getSystemService(
            applicationContext,
            NotificationManager::class.java
        ) as NotificationManager

        val direct = File(getExternalFilesDir(null), "/repos")
        if (!direct.exists()) {
            Timber.i("make new directory")
            direct.mkdirs()
        }

        val request =
            DownloadManager.Request(Uri.parse(URL))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
                .setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    REPO_FOR_FILE_DOWNLOAD
                )


        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request)// enqueue puts the download request in the queue.


        custom_button.buttonState = ButtonState.Loading

    }

    private fun createChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )


            //Enable Lights when notification is shown
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.BLUE
            notificationChannel.enableVibration(true)
            notificationChannel.description = getString(R.string.app_description)

            val notificationManager = getSystemService(
                NotificationManager::class.java
            )

            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    companion object {
        private const val URL =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val CHANNEL_ID = "channelId"

        private const val URL_GLIDE = "https://github.com/bumptech/glide"
        private const val URL_LOAD_APP =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter"
        private const val URL_RETROFIT = "https://github.com/square/retrofit"
        private const val NO_RADIO_BUTTON_CLICKED = "-1"
        private const val REPO_FOR_FILE_DOWNLOAD = "/repos/repository.zip"
    }


}


