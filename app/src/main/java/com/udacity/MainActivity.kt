package com.udacity

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.udacity.databinding.ActivityMainBinding
import com.udacity.utils.sendNotification
import com.udacity.utils.showSnackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import timber.log.Timber
import java.io.File
import java.util.jar.Manifest


class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0

    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action
    private var URL: String = NO_RADIO_BUTTON_CLICKED
    private lateinit var FILE_NAME: String


    private lateinit var buttonState: ButtonState

    private lateinit var binding: ActivityMainBinding
    private lateinit var layout: View


    //Permission check
    // Register the permissions callback, which handles the user's response to the
    // system permissions dialog. Save the return value, an instance of
    // ActivityResultLauncher. You can use either a val, as shown in this snippet,
    // or a lateinit var in your onAttach() or onCreate() method.
    val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your
                // app.

                //Call Download
                layout.showSnackbar(
                    R.string.write_permission_granted, Snackbar.LENGTH_LONG, R.string.message_ok
                )
                {
                    download()
                }

            } else {
                // Explain to the user that the feature is unavailable because the
                // features requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
                layout.showSnackbar(
                    R.string.write_permission_denied, Snackbar.LENGTH_LONG, R.string.message_ok
                )
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        layout = binding.root

        setContentView(layout)
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

            FILE_NAME = when (radioGroup.checkedRadioButtonId) {
                R.id.glide -> getString(R.string.glide)
                R.id.loadApp -> getString(R.string.loadApp)
                R.id.retrofit -> getString(R.string.retrofit)
                else -> NO_RADIO_BUTTON_CLICKED
            }

            Timber.i("HERE IS THE FILE NAME ${FILE_NAME}")
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
                        .setFilterById(intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0))
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
                                        FILE_NAME, applicationContext,
                                        resources.getString(R.string.message_success)
                                    )

                                    Timber.i("The status is Successful: ${status} plus ${FILE_NAME} ")
                                }
                                else -> {
                                    notificationManager.sendNotification(
                                        FILE_NAME,
                                        applicationContext,
                                        resources.getString(R.string.message_failure)
                                    )
                                    Timber.i("The status is Failure: ${status}  plus ${FILE_NAME} ")
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

        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            == PackageManager.PERMISSION_GRANTED
        ) {

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
        } else {
            requestWriteExternalStoragePermission()
        }

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

    private fun requestWriteExternalStoragePermission() {

        // Provide an additional rationale to the user if the permission was not granted
        // and the user would benefit from additional context for the use of the permission.
        // Display a SnackBar with a button to request the missing permission.
        if (shouldShowRequestPermissionRationale(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            layout.showSnackbar(
                R.string.write_permission_required, Snackbar.LENGTH_LONG, R.string.message_ok
            )
            {
                requestPermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        } else {
            //Directly ask for Permission
            layout.showSnackbar(
                R.string.write_permission_not_available, Snackbar.LENGTH_LONG, R.string.message_ok
            )
            {
                requestPermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Timber.i("Register Receiver Again")
        super.registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    override fun onPause() {
        super.onPause()
        Timber.i("Pause: Register Receiver")
        unregisterReceiver(receiver)

    }

    companion object {
        //        private const val URL =
//            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val CHANNEL_ID = "channelId"

        private const val URL_GLIDE = "https://github.com/bumptech/glide"
        private const val URL_LOAD_APP =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter"
        private const val URL_RETROFIT = "https://github.com/square/retrofit"
        private const val NO_RADIO_BUTTON_CLICKED = "-1"
        private const val REPO_FOR_FILE_DOWNLOAD = "/repos/repository.zip"
    }


}


