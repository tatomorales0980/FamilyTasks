package com.tato0980.familytasks

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_register.*


class register : AppCompatActivity() {

    var db = FirebaseFirestore.getInstance()

    lateinit var newemail : EditText
    lateinit var newpass : EditText
    lateinit var newrepass : EditText
    lateinit var newname : EditText
    lateinit var newphone : EditText
    lateinit var newgroup : EditText
    lateinit var errormsg : String



    lateinit var btnlogout : TextView
    lateinit var btnsave : Button
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        
//      adjust layout when soft keyboard appears
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)


        var show = intent.getStringExtra("display_editText")

        if (show == "no"){
            var logedemail = FirebaseAuth.getInstance().currentUser!!.email.toString()
            etUserEmail.isEnabled  = false
            etUserEmail.setText(logedemail)
            etUserPassword.setVisibility(View.GONE)
            etUserRePassword.setVisibility(View.GONE)
        }

        btnlogout = findViewById(R.id.tvRLogout)
        btnsave = findViewById(R.id.btnSave)

        newemail = findViewById(R.id.etUserEmail)
        newpass = findViewById(R.id.etUserPassword)
        newrepass = findViewById(R.id.etUserRePassword)
        newname = findViewById(R.id.etUserName)
        newphone = findViewById(R.id.etPhoneNumber)
        newgroup = findViewById(R.id.etGroupName)

        btnlogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            var currentUser = FirebaseAuth.getInstance().currentUser

            if(currentUser == null){
                startActivity(Intent(this, MainActivity::class.java))
            }
        }
        btnsave.setOnClickListener {
            if (show != "no"){
                if (newpass.text.toString() == newrepass.text.toString()) {
                    if (newemail.text.toString().isNotEmpty() && newpass.text.toString()
                            .isNotEmpty() && newname.text.toString()
                            .isNotEmpty() && newphone.text.toString()
                            .isNotEmpty() && newgroup.text.toString().isNotEmpty()
                    ) {
                        checkField()
                    } else {
                        errormsg = "Password doesn't match"
                        showAlert()
                    }
                }
            } else {
                insertDatainFirebase()
            }

        }
    }

    fun checkField() {
//        Firebase create a NEW USER with Email and Password
//        revisar esta linea para que cree el usuario adelante
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(
            newemail.text.toString(),
            newpass.text.toString()
        ).addOnCompleteListener {

            if (it.isSuccessful){
                progressDialog = ProgressDialog(this@register)
                progressDialog.setMessage("Saving Data on Server")
                progressDialog.setCancelable(false)
                progressDialog.show()
                insertDatainFirebase()
            } else {
                errormsg = "Error saving data into the server"
                showAlert()
            }
        }
    }

    private fun insertDatainFirebase() {
        progressDialog = ProgressDialog(this@register)
        progressDialog.setMessage("Saving Data on Server")
        progressDialog.setCancelable(false)
        progressDialog.show()

        var data = HashMap<String, String> ()
        data.put("email", FirebaseAuth.getInstance().currentUser!!.email.toString())
        data.put("name", newname.text.toString())
        data.put("phone", newphone.text.toString())
        data.put("group", newgroup.text.toString())


        db.collection("Users")
            .document(FirebaseAuth.getInstance().currentUser!!.uid.toString())
            .set(data)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Successfully Loggedin", Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
                moveNextPage()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
            }
    }


    fun moveNextPage(){

        var currentUser = FirebaseAuth.getInstance().currentUser

        if(currentUser != null){
            startActivity(Intent(this, RecycleView_todo::class.java))
        }
    }

    private fun showAlert(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage(errormsg)
        builder.setPositiveButton("OK", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

}