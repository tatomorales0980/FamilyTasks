package com.tato0980.familytasks

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tato0980.familytasks.CommonUtils.ItemListModel
import com.tato0980.familytasks.CommonUtils.UserModel
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
    lateinit var joinmyGroup : String
    lateinit var findGroup : String
    lateinit var userAdmin : String
    lateinit var rString : String


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
        userAdmin = "Y"
        findGroup = "no"
        rString = ""

//        btnsave.isEnabled = false


        ivBackRegister.setOnClickListener(View.OnClickListener {
            finish()
        })
//        newgroup.setOnFocusChangeListener(OnFocusChangeListener { v, hasFocus ->
//            if (!hasFocus) {
//                checkGroup()
//                btnsave.isEnabled = true
//            }
//        })


        btnlogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            var currentUser = FirebaseAuth.getInstance().currentUser

            if(currentUser == null){
                startActivity(Intent(this, MainActivity::class.java))
            }
        }
        btnsave.setOnClickListener {
            checkGroup()
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

    fun checkGroup() {
        db.collection("Users").whereEqualTo("group", newgroup.getText().toString())
            .get()
            .addOnSuccessListener {
                var user = it.toObjects(UserModel::class.java)

                loop@ for (i in 0..user.size - 1) {
                    if (user.get(i) != null) {
                        var group = user.get(i).group
                        if (group == newgroup.getText().toString()) {
                            findGroup = "yes"
                            userAdmin = "N"
                            rString=""
                            Toast.makeText(this, "You have successfully joined +${newgroup.getText().toString()}" , Toast.LENGTH_SHORT).show()
//                            dialogAlert()
//                            return@addOnSuccessListener
                            break@loop
                        }else {
                            findGroup = "no"
                            userAdmin = "Y"
                            rString = getRandomString(3)
                        }
                    }
                }
            }
            if (findGroup == "no"){
                rString = getRandomString(3)
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
        data.put("group", rString+newgroup.text.toString())
        data.put("admin", userAdmin)


        db.collection("Users")
            .document(FirebaseAuth.getInstance().currentUser!!.uid.toString())
            .set(data)
            .addOnSuccessListener {


                progressDialog.dismiss()
                Toast.makeText(this, "Successfully user created ${newname.text.toString()}", Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
                moveNextPage()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Something went wrong saving data", Toast.LENGTH_SHORT).show()
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

    private fun dialogAlert() {
            // build alert dialog
            val dialogBuilder = AlertDialog.Builder(this)

            // set message of alert dialog
            dialogBuilder.setMessage(getString(R.string.messageAlert) + " " +newgroup.getText().toString())
                // if the dialog is cancelable
                .setCancelable(false)
                // positive button text and action
                .setPositiveButton("Ok", DialogInterface.OnClickListener {
//                        dialog, id -> finish()
                        dialog, id ->
                    joinmyGroup = "yes"
                })
                // negative button text and action
//                .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, id ->
//                    dialog.cancel()
//                })

            // create dialog box
            val alert = dialogBuilder.create()
            // set title for alert dialog box
            alert.setTitle(getString(R.string.messageAlertTitle))
            // show alert dialog
            alert.show()
    }


    fun getRandomString(length: Int) : String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

}