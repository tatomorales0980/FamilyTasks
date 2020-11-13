package com.tato0980.familytasks

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.bitvale.lavafab.Child
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_home_screen.*

class HomeScreen : AppCompatActivity()  {

    var db = FirebaseFirestore.getInstance()
    lateinit var btnlogout : TextView
    lateinit var tvGroup : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_screen)

        btnlogout = findViewById(R.id.tvLogout)
        tvGroup = findViewById<TextView>(R.id.tvGroup)

        var logedemail = FirebaseAuth.getInstance().currentUser!!.email.toString()
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
        getData()
        setupViews()
    }

    private fun setupViews() {
        lava_fab_bottom_right.setLavaBackgroundResColor(R.color.colorPurple)

        with(lava_fab_bottom_right) {
            setParentOnClickListener { lava_fab_bottom_right.trigger() }
            setChildOnClickListener(Child.TOP) { addItem() }
            setChildOnClickListener(Child.LEFT) { showToast() }
            setChildOnClickListener(Child.LEFT_TOP) { showToast() }
            enableShadow()
        }
    }

    private fun showToast() = Toast.makeText(this, "Child clicked", Toast.LENGTH_SHORT).show()

    fun getData() {
        var email = FirebaseAuth.getInstance().currentUser!!.uid.toString()

        db.collection("Users").document(email).get().addOnSuccessListener {
            tvGroup.setText(it.get("group") as String?)
        }
    }
    private fun addItem() {
        startActivity(Intent(this,RecycleView::class.java).apply {
            putExtra("display_editText","no")
        })
    }
}