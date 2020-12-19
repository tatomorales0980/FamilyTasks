package com.tato0980.familytasks

import android.Manifest
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.tato0980.familytasks.CommonUtils.ItemModel
import com.tato0980.familytasks.CommonUtils.UserModel
import kotlinx.android.synthetic.main.activity_custom_recycle_items.view.*
import kotlinx.android.synthetic.main.activity_items.*
import kotlinx.android.synthetic.main.activity_register.view.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlin.collections.HashMap
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class Items : AppCompatActivity() {

    lateinit var myItemID : String
    private lateinit var progressDialog: ProgressDialog
    lateinit var btnsave : Button
    lateinit var btnBack : ImageView
    lateinit var itemName : EditText
    lateinit var itemDescription : EditText
    lateinit var myEmail : String
    lateinit var myGroupName : String
    lateinit var currentGroup : String
    lateinit var imageLocation: String
    var imageUri : Uri? = null

    var db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_items)

//      adjust layout when soft keyboard appears
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

//      Bringing data from RecycleView (item name) if needs to be EDITED
        var item_id = intent.getStringExtra("item_name")
        var group_id = intent.getStringExtra("group")
        myItemID = item_id.toString()
        myGroupName = group_id.toString()

        btnsave = findViewById<Button>(R.id.btnSaveItem)
        btnBack = findViewById<ImageView>(R.id.imageViewBack)
        itemName = findViewById(R.id.etItemName)
        itemDescription = findViewById(R.id.etItemDescription)

        myEmail = FirebaseAuth.getInstance().currentUser!!.email.toString()

        btnsave.setOnClickListener() {
            checkNullValues()
        }

        btnDelete.setOnClickListener {
            db.collection(myGroupName)
                .whereEqualTo("name",itemName.text.toString())
                .get()
                .addOnSuccessListener {
                    var user = it.toObjects(ItemModel::class.java)

                    for (i in 0..user.size-1) {
                        db.collection(myGroupName).document(user.get(i).id).delete().addOnSuccessListener {
                            var int = Intent(this, RecycleView :: class.java)
                            startActivity(int)
                        }
                    }
                }
        }

        getUserData()
        if (myItemID.isNotEmpty()) {
            btnsave.setText("Update")
            bringItemData()
        }

        btnBack.setOnClickListener(View.OnClickListener {
            startActivity(Intent(this,RecycleView::class.java).apply {
            })
            finish()
        })

        cameraButton.setOnClickListener(){
            checkPermision("camera")
        }

        galleryButton.setOnClickListener(){
            checkPermision("gallery")
        }
    }

    fun bringItemData(){
        db.collection(myGroupName).whereEqualTo("id", myItemID)
            .get()
            .addOnSuccessListener {

            var item = it.toObjects(ItemModel::class.java)

            for (i in 0..item.size-1) {
                if (item.get(i) != null) {
//                  Updating image
                    if (item.get(i).image.isNotEmpty()){
                        Picasso.get().load(item.get(i).image).into(imageItem)
                    }
                    etItemName.setText(item.get(i).name)
                    etItemDescription.setText(item.get(i).description)
                    imageLocation = item.get(i).image
                }
            }
        }
    }

    private fun getUserData() {
         db.collection("Users").whereEqualTo("email", myEmail).get()
             .addOnSuccessListener {
                 var user = it.toObjects(UserModel::class.java)

                 for (i in 0..user.size-1) {
                     if (user.get(i) != null) {
                            myGroupName = user.get(i).group
                     }
                 }
             }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 100){
            var bmp = data!!.getExtras()!!.get("data") as Bitmap

            imageItem.setImageBitmap(bmp)
            imageUri = getImageUri(this,bmp)

        } else if (requestCode == 101){
            imageItem.setImageURI(data?.data)
            imageUri = data?.data
        }
    }


    fun getImageUri(inContext: Context, inImage: Bitmap): Uri? {

        val generator = Random()
        var n = 10000
        n = generator.nextInt(n)

        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(
            inContext.getContentResolver(),
            inImage,
            "ItemImage-$n",
            null
        )

        return Uri.parse(path)
    }


    private fun insertDatainFirebase(image: String) {
        progressDialog = ProgressDialog(this@Items)
        progressDialog.setMessage("Saving Data on Server")
        progressDialog.setCancelable(false)
        progressDialog.show()

//      Check if we need to update or Save 11/10/20
        var rString = ""
        if (myItemID.isEmpty()){
            rString = getRandomString(10)
            imageLocation = image

        }else {
            rString = myItemID
//          Line addes 12/02/20 updating Item
            imageLocation = image
        }

        var data = HashMap<String, String> ()
        data.put("group", myGroupName)
        data.put("name", itemName.text.toString())
        data.put("description", itemDescription.text.toString())
        data.put("email", myEmail)
        data.put("image", imageLocation)
        data.put("id", rString)

        db.collection(myGroupName)
            .document(rString)
            .set(data)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Successfully Saved", Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()

            }
            .addOnFailureListener {
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
            }

        EventBus.getDefault().post("ItemAddedtoList")

        finish()
    }


    private fun uploadImageToFirebase() {

        if (imageUri == null) {
            insertDatainFirebase( "")
        } else {

            val ref = FirebaseStorage.getInstance().getReference("/Items/${getRandomString(10)}")

            ref.putFile(imageUri!!)
                .addOnSuccessListener {
                    Log.d("SocietyLogs", "Image uploaded")
                    Log.d("myImage", imageUri!!.toString())
                    ref.downloadUrl.addOnSuccessListener {
                        it.toString()
                        insertDatainFirebase(it.toString())
                    }
                }
                .addOnFailureListener {

                }
        }
    }

    private fun checkNullValues() {

        if (itemName.text.toString().isEmpty() ) {
            itemName.error = "Please Enter Name"
        } else if (itemDescription.text.toString().isEmpty() ) {
            itemDescription.error = "Please Enter Description"
        } else {
            uploadImageToFirebase()
        }
    }

    private fun checkPermision(s: String) {
        askPermission(
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        ){

            if (s == "camera") {
                var intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(intent, 100)
            } else {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                startActivityForResult(intent, 101)
            }


        }.onDeclined { e ->
            if (e.hasDenied()) {

                AlertDialog.Builder(this@Items)
                    .setMessage("Please accept our permissions")
                    .setPositiveButton("yes") { dialog, which ->
                        e.askAgain();
                    } //ask again
                    .setNegativeButton("no") { dialog, which ->
                        dialog.dismiss();
                    }
                    .show();
            }

            if(e.hasForeverDenied()) {

                e.goToSettings();
            }
        }
    }


    fun getRandomString(length: Int) : String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }
}