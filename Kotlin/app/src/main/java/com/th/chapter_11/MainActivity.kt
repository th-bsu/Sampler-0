package com.th.chapter_11

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

// TH: gets reference design from https://github.com/PacktPublishing/How-to-Build-Android-Apps-with-Kotlin

class MainActivity : AppCompatActivity() {

    companion object {
        const val BACKEND   = 0
        const val FRONTEND  = 1
        const val DEBUG_ON  = 1
        const val DEBUG_OFF = 0
    }

    private val shelterEdit: EditText
        get() = findViewById(R.id.shelter)

    private val buttonSubmit: Button
        get() = findViewById(R.id.button_submit)

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonSubmit.setOnClickListener {

            val shelterForm = shelterEdit.text.toString().trim()

            // TH: helps with automated testing.
            shelterEdit.text.clear()

            if(shelterForm.isEmpty()){
                val toast = Toast.makeText(this,getString(R.string.shelter_form_error),Toast.LENGTH_LONG)
                toast.setGravity(Gravity.CENTER,0,0)
                toast.show()
            }
            else{

                // TH: transfers number of rows to next activity, when launched.
                // TH: transfers list of items ...
                // TH: https://dev.to/lawgimenez/pass-list-of-objects-in-intent-using-kotlin-3dom
                val intent = Intent(this, ShelterActivity::class.java).apply {
                    putExtra(ShelterActivity.LAYOUT_SHELTER, shelterForm)
                }.apply {
                    putExtra(ShelterActivity.LAYOUT_INTERFACE, BACKEND/*FRONTEND*/)
                }.apply{
                    putExtra(ShelterActivity.LAYOUT_DESIGN_FOR_TEST,DEBUG_ON/*DEBUG_OFF*/)
                }.apply {
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                startActivity(intent)

            }// shelterForm != Empty.

        }//submit.

    }//onCreate.

}