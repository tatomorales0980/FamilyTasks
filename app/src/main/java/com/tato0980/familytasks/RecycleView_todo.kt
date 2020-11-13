package com.tato0980.familytasks

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bitvale.lavafab.Child
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.tato0980.familytasks.CommonUtils.ItemListModel
import com.tato0980.familytasks.CommonUtils.ItemModel
import com.tato0980.familytasks.CommonUtils.UserModel
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_custom_recycle_items.view.*
import kotlinx.android.synthetic.main.activity_custom_recycle_items.view.ivItem
import kotlinx.android.synthetic.main.activity_custom_recycle_items.view.tvItemDescription
import kotlinx.android.synthetic.main.activity_custom_recycle_items.view.tvItemName
import kotlinx.android.synthetic.main.activity_custom_recycle_todo.*
import kotlinx.android.synthetic.main.activity_custom_recycle_todo.view.*
import kotlinx.android.synthetic.main.activity_home_screen.*
import kotlinx.android.synthetic.main.activity_recycle_view.*
import kotlinx.android.synthetic.main.activity_register.*

class RecycleView_todo : AppCompatActivity() {

    lateinit var tvLogout : TextView
    lateinit var currentGroupName : String
    lateinit var myEmail : String
    lateinit var itemId : String
    lateinit var itemName : String
    lateinit var itemDescription : String
    lateinit var itemImageURl: String
    lateinit var itemStatus: String
    lateinit var myList : RecyclerView
    private lateinit var progressDialog: ProgressDialog

    var db = FirebaseFirestore.getInstance()
    var arrayItems = ArrayList<ItemListModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycle_view_todo)

        tvLogout = findViewById(R.id.tvLogout)
        myList = findViewById(R.id.listItems)


        imageViewBack.setOnClickListener(View.OnClickListener {
            finish()
        })
        tvLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            var currentUser = FirebaseAuth.getInstance().currentUser

            if(currentUser == null){
                startActivity(Intent(this,MainActivity::class.java))
            }
        }

        setupViews()
        populateItems("todo_test")
    }

//    inner class groupieAdpt(items: ArrayList<ItemListModel>) : Item<GroupieViewHolder>(){

    inner class groupieAdpt(characters: ArrayList<ItemListModel>) : Item<GroupieViewHolder>() {
        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            if (arrayItems.get(position).image.isNotEmpty()) {
                Picasso.get().load(arrayItems.get(position).image).into(viewHolder.itemView.ivItem)
            }
            viewHolder.itemView.tvItemName.text = arrayItems.get(position).name
            viewHolder.itemView.tvItemDescription.text = arrayItems.get(position).description

//          CHECK THIS, to many variables
            var rowindex = position
            var painted = 0
            var status = ""
            var documentId = arrayItems.get(position).id
            viewHolder.itemView.setOnClickListener() {
                if (rowindex == position && painted == 0) {
//                    viewHolder.itemView.ivCheck.setVisibility(View.VISIBLE)
                    viewHolder.itemView.llmain2.setBackgroundColor(Color.parseColor("#A8A32C"))
                    painted = 1
                    status = "0"
                    var message = arrayItems.get(position).name + " Completed"
                    Snackbar.make(it, message, Snackbar.LENGTH_LONG)
                        .setAction("Action", null)
                        .show()
                } else {
//                    viewHolder.itemView.ivCheck.setVisibility(View.INVISIBLE)
                    viewHolder.itemView.llmain2.setBackgroundColor(Color.parseColor("#673AB7"))
                    painted = 0
                    status = "1"
                }
                updateStatus(status, documentId)
            }



        }
        override fun getLayout(): Int {
            return R.layout.activity_custom_recycle_todo
        }
    }

    fun updateStatus(status : String, documentid : String){
        currentGroupName = "todo_test"
        db.collection(currentGroupName).document(documentid)
            .update("status" , status)
            .addOnSuccessListener{

        }

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

    private fun addItem() {
        startActivity(Intent(this,RecycleView::class.java).apply {
            putExtra("display_editText","no")
        })
    }

    fun populateItems(myGroup: String){

        currentGroupName = myGroup.toString()
        db.collection(currentGroupName).whereEqualTo("status", "1").get()
            .addOnSuccessListener {

                var item = it.toObjects(ItemListModel::class.java)

                for (i in 0..item.size-1) {
                    if (item.get(i) != null) {
                        itemId = item.get(i).id
                        itemImageURl = item.get(i).image
                        itemName = item.get(i).name
                        itemDescription = item.get(i).description
                        itemStatus = item.get(i).status
                        arrayItems.add(ItemListModel("",itemName,itemDescription,"",itemImageURl,itemId, itemStatus))
                    }
                }
                // Creating Adapter to Recycle View
                var adapter = GroupAdapter<GroupieViewHolder>()

                for (i in 0..arrayItems.size-1) {
                    adapter.add(groupieAdpt(arrayItems))
                }

//                myList.layoutManager = StaggeredGridLayoutManager(this, 2)
//                myList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false)
//                myList.layoutManager = GridLayoutManager(this, 2)

                myList.setAdapter(adapter)
            }

    }


    private fun getListData() {
        db.collection("Users").whereEqualTo("email", myEmail).get()
            .addOnSuccessListener {

                var user = it.toObjects(UserModel::class.java)

                for (i in 0..user.size-1) {
                    if (user.get(i) != null) {
                        currentGroupName = user.get(i).group
                        myEmail = user.get(i).email
                        populateItems(currentGroupName)
                    }
                }
            }

    }

}