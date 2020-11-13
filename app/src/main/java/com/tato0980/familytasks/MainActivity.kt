package com.tato0980.familytasks

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    // Error https://stackoverflow.com/questions/47437678/why-do-i-get-com-google-android-gms-common-api-apiexception-10

    lateinit var myemail : String
    lateinit var mypass : String
    lateinit var tvregister : TextView

    // XML Google SingIn variable ID
    lateinit var rvSigninGoogle : RelativeLayout

    // WHY = 1000?
    val RC_SIGN_IN = 1000

    private lateinit var progressDialog: ProgressDialog

    var db = FirebaseFirestore.getInstance()
    lateinit var detailmsg : String

    // SingIn variable client
    var googleSignInClient : GoogleSignInClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rvSigninGoogle = findViewById<RelativeLayout>(R.id.rvSigninGoogle)
        tvregister = findViewById(R.id.tvRegister)


        // gso Variable containe the configuration
        // Google SingIn Builder Authentication
        // Bring token from FireBase
        // Request user email
        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        // Connect Google Sing In with gso(variable)
        googleSignInClient = GoogleSignIn.getClient(this,gso)


        // Show the option that google sing in has
        // showme the Intent with the email to choose
        // Select account and comeback to the activity
        rvSigninGoogle.setOnClickListener {
            var signInIntent = googleSignInClient?.signInIntent
            startActivityForResult(signInIntent,RC_SIGN_IN)
        }

        btnSingUp.setOnClickListener {
            // Creating a new user
            if (etEmail.text.isNotEmpty() && etPassword.text.isNotEmpty()){
                checkField()
            }

        }

        // Register button action
        tvregister.setOnClickListener {
            intent = Intent(this, register::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        moveNextPage()
    }

    fun checkField() {
        // Take email and password data
        myemail = etEmail.text.toString()
        mypass = etPassword.text.toString()


        //Firebase create a NEW USER with Email and Password
//        FirebaseAuth.getInstance().createUserWithEmailAndPassword(myemail, mypass).addOnCompleteListener {
        // if the user is already created (singIn code)
        FirebaseAuth.getInstance().signInWithEmailAndPassword(myemail, mypass).addOnCompleteListener {

            if (it.isSuccessful){
                startActivity(Intent(this,HomeScreen::class.java))
//                I dont need this line here, needs to register
//                progressDialog = ProgressDialog(this@MainActivity)
//                progressDialog.setMessage("Saving Data on Server")
//                progressDialog.setCancelable(false)
//                progressDialog.show()
//                insertDatainFirebase()
            } else {
                detailmsg = "The Email is not Registered"
                showAlert()
            }
        }
    }



    // Override function
    // OnActivityResult 3 parameters
    // requestCode = RC_SIGN_IN
    // data = data bring from activity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == RC_SIGN_IN){
            // Get the Singin data
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            // Decrypt data from variable account
            val account = task.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account)
        }
    }

    fun firebaseAuthWithGoogle(acct : GoogleSignInAccount?){

        var credential = GoogleAuthProvider.getCredential(acct?.idToken,null)
        FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener {
                task ->
            if(task.isSuccessful){
//                insertDatainFirebase()
                moveNextPage()
            } else {
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
//                progressDialog.dismiss()
            }
        }
            .addOnFailureListener {
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
            }
    }

    private fun insertDatainFirebase() {

        progressDialog = ProgressDialog(this@MainActivity)
        progressDialog.setMessage("Saving Data on Server")
        progressDialog.setCancelable(false)
        progressDialog.show()

        var data = HashMap<String,String> ()
        data.put("email",FirebaseAuth.getInstance().currentUser!!.email.toString())
//        data.put("email",email)

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
//          My code 10/28
//            var email = FirebaseAuth.getInstance().currentUser!!.email.toString()
//            Don't verify it is coming from GOOGLE login
            var email = FirebaseAuth.getInstance().currentUser!!.uid.toString()


            db.collection("Users").document(email).get().addOnSuccessListener {
                var name = it.get("name") as String?
                var phone = it.get("phone") as String?
                var group = it.get("group") as String?
                if (name != null || phone != null || group != null){
                    startActivity(Intent(this,RecycleView_todo::class.java))
                } else {
                    startActivity(Intent(this,register::class.java).apply {
                        putExtra("display_editText","no")
                    })
                }
            }
//          *****************************
        }
    }

    private fun showAlert(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage(detailmsg)
        builder.setPositiveButton("OK", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }
}

