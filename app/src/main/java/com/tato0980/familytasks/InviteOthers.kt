package com.tato0980.familytasks

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.tato0980.familytasks.CommonUtils.UserModel
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_custom_recycle_users.view.*
import kotlinx.android.synthetic.main.activity_invite_others.*


class InviteOthers : AppCompatActivity() {
    lateinit var txtMessage : String
    lateinit var myGroup : String
    lateinit var userName : String
    lateinit var userEmail : String
    lateinit var userPhone : String
    lateinit var userAdmin : String
    lateinit var myList : RecyclerView
    lateinit var admin : String

    val db = FirebaseFirestore.getInstance()
    val adapter = GroupAdapter<GroupieViewHolder>()
    var arrayUsers = ArrayList<UserModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invite_others)

//      adjust layout when soft keyboard appears
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        myList = findViewById(R.id.listUsers)
        admin = "N"

//      Bringing data from RecycleView (Group name)
        myGroup = intent.getStringExtra("groupName").toString()
        tvGroupName.setText("Your Group name : " + myGroup)

        var textMessage = "Try this Awesome App 'FamilyTask - Task Creator and Reminder' which helps you to keep track on your Tasks. " +

                "Download and Install https://play.google.com/store/apps/details?id=com.productionapp.aatmanirbharcamscanner and "
        etMessage.setText(textMessage)

        ivBackInvitation.setOnClickListener(View.OnClickListener {
            finish()
        })

        btnInvite.setOnClickListener {
         inviteOthers()
        }

        verifyAdmin()

    }


    fun inviteOthers(){
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(
                Intent.EXTRA_TEXT,
                etMessage.getText().toString() + "\nuse my Group Name to Join me :" + myGroup
            )
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    fun verifyAdmin(){
        var logedemail = FirebaseAuth.getInstance().currentUser!!.email.toString()
        var email = FirebaseAuth.getInstance().currentUser!!.uid.toString()
        db.collection("Users").document(email)
            .get()
            .addOnSuccessListener {
                admin = it.get("admin") as String
                populateUsers()
            }
    }

    fun populateUsers(){

        var adapter = GroupAdapter<GroupieViewHolder>()
        adapter.clear()
        listUsers.removeAllViews()

        db.collection("Users").whereEqualTo("group", myGroup)
            .get()
            .addOnSuccessListener {

                var users = it.toObjects(UserModel::class.java)

                for (i in 0..users.size-1) {
                    if (users.get(i) != null) {
                        userName = users.get(i).name
                        userEmail = users.get(i).email
                        userPhone = users.get(i).phone
                        userAdmin = users.get(i).admin
                        arrayUsers.add(
                            UserModel(
                                userEmail,
                                myGroup,
                                userName,
                                userPhone,
                                userAdmin
                            )
                        )
                    }
                }
                // Creating Adapter to Recycle View
//                var adapter = GroupAdapter<GroupieViewHolder>()


                for (i in 0..arrayUsers.size-1) {
                    adapter.add(groupieAdpt(arrayUsers))
                }

//                myList.layoutManager = StaggeredGridLayoutManager(this, 2)
                myList.layoutManager = LinearLayoutManager(
                    this,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
//                myList.layoutManager = GridLayoutManager(this, 2)
                myList.setAdapter(adapter)
            }

    }

    inner class groupieAdpt(var characters: ArrayList<UserModel>) : Item<GroupieViewHolder>() {

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            var logedemail = FirebaseAuth.getInstance().currentUser!!.email.toString()
            if (admin == "Y"){
                viewHolder.itemView.userName.text = arrayUsers.get(position).name
                viewHolder.itemView.userEmail.text = arrayUsers.get(position).email
                viewHolder.itemView.userPhone.text = arrayUsers.get(position).phone
//                viewHolder.itemView.btnDeleteUser.setVisibility(View.VISIBLE)
                viewHolder.itemView.btnDeleteUser.setOnClickListener() {
                    var deleteEmail = arrayUsers.get(position).email.toString()
                    deleteUser(deleteEmail, position)
                }
            } else {
                viewHolder.itemView.userName.text = arrayUsers.get(position).name
                viewHolder.itemView.userEmail.text = arrayUsers.get(position).email
                viewHolder.itemView.userPhone.text = arrayUsers.get(position).phone
            }
            if (logedemail == arrayUsers.get(position).email){
                viewHolder.itemView.tvAdmin.setVisibility(View.VISIBLE)
                viewHolder.itemView.cvUser.backgroundTintList = ColorStateList.valueOf(
                    Color.parseColor(
                        "#3F3F3F"
                    )
                )
            }
        }

        override fun getLayout(): Int {
            return R.layout.activity_custom_recycle_users
        }

    }

    fun deleteUser(email: String, position: Int) {
        Toast.makeText(this, "$email / $position ", Toast.LENGTH_SHORT).show()
    }

}


