package com.udacity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*
import timber.log.Timber
import java.sql.Time

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)

        //TODO GET INFORMATION FROM INTENT
        //TODO CHECK THE STATUS MESSAGE

        Timber.i("Name from Intent${intent.getStringExtra(R.string.message_download.toString())} ")
        //Set the file name
        repoFileNameId.setText(intent.getStringExtra(resources.getString(R.string.message_download)))
        repoStatusId.setText(intent.getStringExtra(resources.getString(R.string.label_file_status)))


        buttonDetailActivity.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

}




 
