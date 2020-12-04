package com.tato0980.familytasks

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import android.widget.Toast.makeText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.tato0980.familytasks.CommonUtils.ItemModel
import com.tato0980.familytasks.CommonUtils.UserModel
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_custom_recycle_items.*
import kotlinx.android.synthetic.main.activity_custom_recycle_items.view.*
import kotlinx.android.synthetic.main.activity_items.*
import kotlinx.android.synthetic.main.activity_recycle_view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe


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
    lateinit var etSearch: EditText
    private lateinit var progressDialog: ProgressDialog


    var db = FirebaseFirestore.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycle_view)

        tvTitle = findViewById<TextView>(R.id.tvTitle)
        myList = findViewById(R.id.listItems)
        ivBack = findViewById(R.id.imageViewBack)
        etSearch = findViewById(R.id.etSearch)

        myEmail = FirebaseAuth.getInstance().currentUser!!.email.toString()



        val fab: View = findViewById(R.id.fabAddItem)

        fab.setOnClickListener { view ->
            startActivity(Intent(this@RecycleView, Items::class.java).apply {
                putExtra("item_name", "")
            })
        }
        ivBack.setOnClickListener(View.OnClickListener {
            finish()
        })

        getUserData()

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                populateItems(currentGroupName,s.toString().toLowerCase())
            }
        })

    }


    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this)) EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe
    fun OnRefreshList(addItem: String?) {
        if (addItem == "ItemAddedtoList") {
            populateItems(currentGroupName,"")
        }
    }

    private fun getUserData() {
        db.collection("Users").whereEqualTo("email", myEmail).get()
            .addOnSuccessListener {

                var user = it.toObjects(UserModel::class.java)

                for (i in 0..user.size-1) {
                    if (user.get(i) != null) {
                        currentGroupName = user.get(i).group
                        populateItems(currentGroupName,"")
                    }
                }
            }

    }

    inner class groupieAdpt(var characters:  ItemModel ) : Item<GroupieViewHolder>() {

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            if (characters.image.isNotEmpty() && characters.image != "") {
                Picasso.get().load(characters.image).into(viewHolder.itemView.ivItem)
            }
//            else {
//               viewHolder.itemView.ivItem.setImageDrawable(resources.getDrawable(R.drawable.orange_fruit))
//            }

            viewHolder.itemView.tvItemName.text = characters.name
            viewHolder.itemView.tvItemDescription.text = characters.description

            val item_name = characters.name
            val item_description = characters.description
            val item_id = characters.id
            val item_iamge = characters.image

            var rowindex = position
            var painted = 0
            var message = ""
            var status = ""

            viewHolder.itemView.btnItemAdd.setOnClickListener() {
//              change LinearLayout background Color
                if (rowindex == position && painted == 0) {
                    viewHolder.itemView.setBackgroundColor(Color.parseColor("#E8E8E8"))
                    painted = 1
                    message = "Item ${characters.name} Added to list"
                    status = "1"
                } else {
                    viewHolder.itemView.setBackgroundColor(Color.parseColor("#ffffff"))
                    painted = 0
                    message = "Item ${characters.name} Removed from list "
                    status = "0"
                }

//              Add items to TODO list
                addtodolist(
                    item_name,
                    item_description,
                    item_id,
                    item_iamge,
                    currentGroupName,
                    status,
                    message,
                    viewHolder.itemView.btnItemAdd
                )

            }

            viewHolder.itemView.btnItemEdit.setOnClickListener() {
                startActivity(Intent(this@RecycleView, Items::class.java).apply {
                    putExtra("item_name", characters.id)
                    putExtra("group",currentGroupName)
                })
            }
        }

        override fun getLayout(): Int {
            return R.layout.activity_custom_recycle_items
        }

    }

    fun populateItems(myGroup: String, toLowerCase: String){
        var adapter = GroupAdapter<GroupieViewHolder>()
        adapter.clear()
        myList.removeAllViews()


        currentGroupName = myGroup.toString()
        db.collection(currentGroupName).orderBy("name")
            .get()
            .addOnSuccessListener {

                var item = it.toObjects(ItemModel::class.java)

                for (i in 0..item.size-1) {
                    if (toLowerCase.isEmpty()) {
                        adapter.add(groupieAdpt(item.get(i)))
                    }else  {

                        val itemname: CharArray = item.get(i).name.toCharArray()
                        val name: CharArray = toLowerCase.toCharArray()

                        while (itemname[i] == name[i]) {
                            adapter.add(groupieAdpt(item.get(i)))
                        }


                }}



//        myList.layoutManager = StaggeredGridLayoutManager(this, 2)
//        myList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false)
                myList.layoutManager = GridLayoutManager(this, 2)

                myList.setAdapter(adapter)
                myList.setHasFixedSize(true)
            }

    }

    //    Add Items to TODO list on Fire Base
    fun addtodolist(
        name: String,
        description: String,
        id: String,
        image: String,
        group: String,
        status: String,
        message: String,
        btnItemAdd: View
    ){
//        makeText(this, "addtolist $name, $status", Toast.LENGTH_SHORT).show()
        progressDialog = ProgressDialog(this@RecycleView)
        progressDialog.setMessage("Saving Data on Server")
        progressDialog.setCancelable(false)
        progressDialog.show()

        var data = HashMap<String, String> ()
        data.put("email", FirebaseAuth.getInstance().currentUser!!.email.toString())
        data.put("id", id)
        data.put("name", name)
        data.put("group", group)
        data.put("description", description)
        data.put("image", image)
        data.put("status", status)
        data.put("qty", "1")


        db.collection("todo_test")
            .document(id)
            .set(data)
            .addOnSuccessListener {

                EventBus.getDefault().post("ItemAdded")

                progressDialog.dismiss()

                Snackbar.make(btnItemAdd, message, Snackbar.LENGTH_LONG)
                    .setAction("Action", null)
                    .show()

                progressDialog.dismiss()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
            }
    }
}

