package com.tato0980.familytasks

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_items.*
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_user_data.*


class UserData : AppCompatActivity() {

    var db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_data)

        ivBackUserData.setOnClickListener(View.OnClickListener {
            finish()
        })
        showUserinfo()


    }

    private fun showUserinfo() {
        var currentUser = FirebaseAuth.getInstance().currentUser
        if(currentUser != null){
            var email = FirebaseAuth.getInstance().currentUser!!.uid.toString()
            val myEmail = FirebaseAuth.getInstance().currentUser!!.email.toString()

            db.collection("Users").document(email).get().addOnSuccessListener {
                tvMyEmail.setText(myEmail)
                tvUserName.setText(it.get("name") as String?)
                tvPhoneNumber.setText(it.get("phone") as String?)
                tvGroupName.setText(it.get("group") as String?)
            }
        }
    }

}