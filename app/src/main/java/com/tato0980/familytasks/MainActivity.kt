package com.tato0980.familytasks

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.RelativeLayout
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class MainActivity : AppCompatActivity() {

    // Error https://stackoverflow.com/questions/47437678/why-do-i-get-com-google-android-gms-common-api-apiexception-10

    // XML Google SingIn variable ID
    lateinit var rvSigninGoogle : RelativeLayout

    // WHY = 1000?
    val RC_SIGN_IN = 1000

    // Sing In variable client
    var googleSignInClient : GoogleSignInClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rvSigninGoogle = findViewById<RelativeLayout>(R.id.rvSigninGoogle)

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
    }

    override fun onResume() {
        super.onResume()
        moveNextPage()
    }

    // Override function
    // OnActivityResult 3 parameters
    // requestCode = RC_SIGN_IN
    // data = data bring from activity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == RC_SIGN_IN){
            // Get the Sinin data
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            // Decrypt data from variable account
            val account = task.getResult(ApiException::class.java)
            Toast.makeText(this, "$account", Toast.LENGTH_SHORT).show()
            firebaseAuthWithGoogle(account)
        }
    }

    fun firebaseAuthWithGoogle(acct : GoogleSignInAccount?){
        var credential = GoogleAuthProvider.getCredential(acct?.idToken,null)
        FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener {
                task ->
            if(task.isSuccessful){
                moveNextPage()
            }
        }
    }

    fun moveNextPage(){
        var currentUser = FirebaseAuth.getInstance().currentUser

        if(currentUser != null){
            startActivity(Intent(this,HomeScreen::class.java))
        }
    }


}

