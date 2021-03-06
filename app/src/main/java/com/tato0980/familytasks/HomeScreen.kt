package com.tato0980.familytasks

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_home_screen.*

class HomeScreen : AppCompatActivity() {

    lateinit var btnlogout : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_screen)

        btnlogout = findViewById(R.id.tvLogout)

        var logedemail = FirebaseAuth.getInstance().currentUser!!.email.toString()
//        Toast.makeText(this, "$logedemail", Toast.LENGTH_SHORT).show()
        tvEmail.text = FirebaseAuth.getInstance().currentUser!!.email.toString()

        btnlogout.setOnClickListener {
            Toast.makeText(this, "click?", Toast.LENGTH_SHORT).show()
            FirebaseAuth.getInstance().signOut()
            var currentUser = FirebaseAuth.getInstance().currentUser

            if(currentUser == null){
                startActivity(Intent(this,MainActivity::class.java))
            }
        }

        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            var currentUser = FirebaseAuth.getInstance().currentUser

            if(currentUser == null){
                startActivity(Intent(this,MainActivity::class.java))
            }
        }
    }
}