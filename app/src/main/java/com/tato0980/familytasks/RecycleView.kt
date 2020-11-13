package com.tato0980.familytasks

import android.app.ProgressDialog
import android.content.ClipData
import android.content.Intent
import android.graphics.Color
import android.graphics.Insets.add
import android.icu.number.NumberFormatter.with
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import android.widget.Toast.makeText
import androidx.core.view.OneShotPreDrawListener.add
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tato0980.familytasks.CommonUtils.ItemModel
import com.tato0980.familytasks.CommonUtils.UserModel
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import io.reactivex.internal.util.BackpressureHelper.add
import kotlinx.android.synthetic.main.activity_custom_recycle_items.*
import kotlinx.android.synthetic.main.activity_custom_recycle_items.view.*
import kotlinx.android.synthetic.main.activity_items.*
import kotlinx.android.synthetic.main.activity_recycle_view.*

class RecycleView : AppCompatActivity() {

    lateinit var ivBack : ImageView
    lateinit var tvTitle : TextView
    lateinit var myEmail : String
    lateinit var itemId : String
    lateinit var itemName : String
    lateinit var itemDescription : String
    lateinit var itemImageURl: String
    lateinit var myList : RecyclerView
    lateinit var currentGroupName : String
    lateinit var llmain : LinearLayout
    private lateinit var progressDialog: ProgressDialog

    var arrayItems = ArrayList<ItemModel>()


    var db = FirebaseFirestore.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycle_view)

        tvTitle = findViewById<TextView>(R.id.tvTitle)
        myList = findViewById(R.id.listItems)
        ivBack = findViewById(R.id.imageViewBack)

        myEmail = FirebaseAuth.getInstance().currentUser!!.email.toString()

        val fab: View = findViewById(R.id.fabAddItem)

        fab.setOnClickListener { view ->
            startActivity(Intent(this@RecycleView,Items::class.java).apply {
                putExtra("item_name","")
            })
        }
        ivBack.setOnClickListener(View.OnClickListener {
            finish()
        })

        getUserData()

    }


    private fun getUserData() {
        db.collection("Users").whereEqualTo("email", myEmail).get()
            .addOnSuccessListener {

                var user = it.toObjects(UserModel::class.java)

                for (i in 0..user.size-1) {
                    if (user.get(i) != null) {
                        currentGroupName = user.get(i).group
                        populateItems(currentGroupName)
                    }
                }
            }

    }

    inner class groupieAdpt(characters: ArrayList<ItemModel>) : Item<GroupieViewHolder>() {

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            if (arrayItems.get(position).image.isNotEmpty()) {
                Picasso.get().load(arrayItems.get(position).image).into(viewHolder.itemView.ivItem)
            }

            viewHolder.itemView.tvItemName.text = arrayItems.get(position).name
            viewHolder.itemView.tvItemDescription.text = arrayItems.get(position).description

            val item_name = arrayItems.get(position).name
            val item_description = arrayItems.get(position).description
            val item_id = arrayItems.get(position).id
            val item_iamge = arrayItems.get(position).image

            var rowindex = position
            var painted = 0
            var message = ""
            var status = ""

            viewHolder.itemView.btnItemAdd.setOnClickListener() {
//              change LinearLayout background Color
                if (rowindex == position && painted == 0) {
                    viewHolder.itemView.setBackgroundColor(Color.parseColor("#E8E8E8"))
                    painted = 1
                    message = "Item ${arrayItems.get(position).name} Added to list"
                    status = "1"
                } else {
                    viewHolder.itemView.setBackgroundColor(Color.parseColor("#ffffff"))
                    painted = 0
                    message = "Item ${arrayItems.get(position).name} Removed from list "
                    status = "0"
                }
                Snackbar.make(it, message, Snackbar.LENGTH_LONG)
                    .setAction("Action", null)
                    .show()

//              Add items to TODO list
                addtodolist(item_name, item_description, item_id, item_iamge, status)

            }

            viewHolder.itemView.btnItemEdit.setOnClickListener() {
//                Toast.makeText(this@RecycleView, "Name: ${arrayItems.get(position).id}", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@RecycleView,Items::class.java).apply {
                putExtra("item_name",arrayItems.get(position).id)
                })
            }
        }

        override fun getLayout(): Int {
            return R.layout.activity_custom_recycle_items
        }

    }

    fun populateItems(myGroup: String){

        makeText(this, "-$myGroup-", Toast.LENGTH_SHORT).show()
//        db.collection(myGroup).whereEqualTo("group", myGroup).get()
//        db.collection(myGroup).whereEqualTo("group", "Test Group").get()
        currentGroupName = myGroup.toString()
        db.collection("GMAIL Group").get()
            .addOnSuccessListener {

                var item = it.toObjects(ItemModel::class.java)

                for (i in 0..item.size-1) {
                    if (item.get(i) != null) {
                        itemId = item.get(i).id
                        itemImageURl = item.get(i).image
                        itemName = item.get(i).name
                        itemDescription = item.get(i).description
                        arrayItems.add(ItemModel(myEmail,itemName,itemDescription,"",itemImageURl,itemId))
//                        Toast.makeText(this, "Item Name : $itemName", Toast.LENGTH_SHORT).show()
                    }
                }
                // Creating Adapter to Recycle View
                var adapter = GroupAdapter<GroupieViewHolder>()

                for (i in 0..arrayItems.size-1) {
                    adapter.add(groupieAdpt(arrayItems))
                }

//        myList.layoutManager = StaggeredGridLayoutManager(this, 2)
//        myList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false)
                myList.layoutManager = GridLayoutManager(this, 2)

                myList.setAdapter(adapter)
            }

    }


//    Add Items to TODO list on Fire Base
    fun addtodolist(name: String, description: String, id: String, image: String, status: String){
        makeText(this, "addtolist $name, $status", Toast.LENGTH_SHORT).show()
        progressDialog = ProgressDialog(this@RecycleView)
        progressDialog.setMessage("Saving Data on Server")
        progressDialog.setCancelable(false)
        progressDialog.show()

        var data = HashMap<String, String> ()
        data.put("email", FirebaseAuth.getInstance().currentUser!!.email.toString())
        data.put("id", id)
        data.put("name", name)
        data.put("description", description)
        data.put("image", image)
        data.put("status", status)
        data.put("qty", "1")


        db.collection("todo_test")
            .document(id)
            .set(data)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Successfully Loggedin", Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
            }
    }
}

