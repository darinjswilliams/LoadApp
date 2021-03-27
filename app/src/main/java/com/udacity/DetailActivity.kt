package com.udacity

import android.app.NotificationManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.udacity.databinding.ActivityDetailBinding
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*
import kotlinx.android.synthetic.main.content_detail.view.*
import timber.log.Timber
import java.sql.Time
import java.util.zip.Inflater

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private lateinit var layout: View
    private lateinit var notificationManager: NotificationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetailBinding.inflate(layoutInflater)
        layout = binding.root

        setContentView(layout)
        setSupportActionBar(toolbar)

        //Dismiss Notification
        notificationManager = ContextCompat.getSystemService(
                applicationContext,
                NotificationManager::class.java
        ) as NotificationManager

        notificationManager.cancelAll()


        Timber.i("Name from Intent${intent.getStringExtra(R.string.message_download.toString())} ")
        layout.repoFileNameId.text = intent.getStringExtra(resources.getString(R.string.message_download))
        layout.repoStatusId.text = intent.getStringExtra(resources.getString(R.string.label_file_status))

        buttonDetailActivity.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

}




 
