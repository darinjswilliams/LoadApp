package com.udacity

import android.app.DownloadManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.content_main.view.*
import timber.log.Timber


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

            if(!NO_RADIO_BUTTON_CLICKED.equals(URL)) {
                download()
            } else {
                Timber.i("Radio Button Clicked, Change state to clicked")
                custom_button.buttonState = ButtonState.Clicked
            }

        }

    }


    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
        }
    }

    private fun download() {
        Timber.i("Download file: ${URL}")

                val request =
                    DownloadManager.Request(Uri.parse(URL))
                        .setTitle(getString(R.string.app_name))
                        .setDescription(getString(R.string.app_description))
                        .setRequiresCharging(false)
                        .setAllowedOverMetered(true)
                        .setAllowedOverRoaming(true)

                val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                downloadID =
                    downloadManager.enqueue(request)// enqueue puts the download request in the queue.

                custom_button.buttonState = ButtonState.Loading

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
    }


}
