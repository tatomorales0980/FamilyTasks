package com.tato0980.familytasks

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bitvale.lavafab.Child
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.tato0980.familytasks.CommonUtils.ItemListModel
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
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe


class RecycleView_todo : AppCompatActivity() {

    lateinit var tvLogout : TextView
    lateinit var currentGroupName : String
    lateinit var myEmail : String
    lateinit var myGroup : String
    lateinit var itemId : String
    lateinit var itemName : String
    lateinit var itemDescription : String
    lateinit var itemImageURl: String
    lateinit var itemStatus: String
    lateinit var myList : RecyclerView
    private lateinit var progressDialog: ProgressDialog
    var adapter = GroupAdapter<GroupieViewHolder>()
    var googleSignInClient : GoogleSignInClient? = null

    var db = FirebaseFirestore.getInstance()
    var arrayItems = ArrayList<ItemListModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycle_view_todo)

        tvLogout = findViewById(R.id.tvLogout)
        myList = findViewById(R.id.listItems)

        myEmail = FirebaseAuth.getInstance().currentUser!!.email.toString()


//        imageViewBack.setOnClickListener(View.OnClickListener {
//            finish()
//        })


        tvLogout.setOnClickListener {

            var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            val googleSignInClient = GoogleSignIn.getClient(this,gso)

            googleSignInClient.signOut()
                .addOnCompleteListener(this, OnCompleteListener<Void?> {
                    FirebaseAuth.getInstance().signOut()
                    var currentUser = FirebaseAuth.getInstance().currentUser

                    if(currentUser == null){
                        startActivity(Intent(this, MainActivity::class.java))
                    }
                })


            FirebaseAuth.getInstance().signOut()
            var currentUser = FirebaseAuth.getInstance().currentUser

            if(currentUser == null){
                startActivity(Intent(this, MainActivity::class.java))
            }

        }



        currentGroupName = "todo_test"

        setupViews()

        getListData()
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
    fun OnRefresh(addItem: String?) {
        if (addItem == "ItemAdded") {
            populateItems(currentGroupName)
        }
    }

    inner class groupieAdpt(items: ArrayList<ItemListModel>) : Item<GroupieViewHolder>(){
        override fun bind(viewHolder: GroupieViewHolder, position: Int) {

            if (arrayItems.get(position).image!!.isNotEmpty() || arrayItems.get(position).image!!.isNotBlank()) {
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
//                    viewHolder.itemView.llmain2.setBackgroundColor(Color.parseColor("#A8A32C"))
                    painted = 1
                    status = "0"
                    var message = arrayItems.get(position).name
                    Snackbar.make(it, message, Snackbar.LENGTH_SHORT)
                        .setAction("Action", null)
                        .show()
                        updateStatus(status, documentId, position)
            }
        }
        override fun getLayout(): Int {
            return R.layout.activity_custom_recycle_todo
        }
    }

    fun updateStatus(status: String, documentid: String, position: Int){

        db.collection(currentGroupName).document(documentid)
//            .update("status" , status)
            .delete()
            .addOnSuccessListener{
        }


//      DELETE PROCESS  ********** 111820  *********
        adapter.removeGroupAtAdapterPosition(position)
        arrayItems.removeAt(position)
        adapter!!.notifyDataSetChanged()
    }


    fun updatelist(){
        Toast.makeText(this, "ENTRE a UpdateList", Toast.LENGTH_SHORT).show()
        arrayItems.clear()
//        arrayItems.removeAll(arrayItems)
        adapter.clear()

        db.collection(currentGroupName).whereEqualTo("status", "1")
            .get()
            .addOnSuccessListener {

                var item = it.toObjects(ItemListModel::class.java)

                for (i in 0..item.size - 1) {
                    if (item.get(i) != null) {
                        itemId = item.get(i).id
                        itemImageURl = item.get(i).image
                        itemName = item.get(i).name
                        itemDescription = item.get(i).description
                        itemStatus = item.get(i).status
                        arrayItems.add(
                            ItemListModel(
                                "",
                                itemName,
                                currentGroupName,
                                itemDescription,
                                "",

                                itemImageURl,
                                itemId,
                                itemStatus
                            )
                        )
                    }
                }

                for (i in 0..arrayItems.size - 1) {
                    adapter.add(groupieAdpt(arrayItems))
                }

                adapter.notifyDataSetChanged()
            }

    }


    private fun setupViews() {
        lava_fab_bottom_right.setLavaBackgroundResColor(R.color.colorPurple)

        with(lava_fab_bottom_right) {
            setParentOnClickListener { lava_fab_bottom_right.trigger() }
            setChildOnClickListener(Child.TOP) { addItem() }
            setChildOnClickListener(Child.LEFT) { register() }
            setChildOnClickListener(Child.LEFT_TOP) { invite() }
            enableShadow()
        }
    }

    private fun invite()  {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(
                Intent.EXTRA_TEXT,
                "Try this Awesome App 'FamilyTask - Task Creator and Reminder' which helps you to keep track on your Tasks. " +

                        "Download and Install https://play.google.com/store/apps/details?id=com.productionapp.aatmanirbharcamscanner and use my Group Name to Join me : ${myGroup}."
            )
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    private fun addItem() {
        startActivity(Intent(this, RecycleView::class.java).apply {
            putExtra("display_editText", "no")
        })
    }

    private fun register() {
        startActivity(Intent(this, register::class.java).apply {
//            putExtra("display_editText","no")
        })
    }

    fun populateItems(currentGroupName: String){

        adapter.clear()
        myList.removeAllViews()
        arrayItems.clear()

        Log.d("nameisii", myGroup)

        db.collection("todo_test").whereEqualTo("group", myGroup)
            .get()
            .addOnSuccessListener {

                var item = it.toObjects(ItemListModel::class.java)

                for (i in 0..item.size-1) {
                    if (item.get(i) != null) {
                        itemId = item.get(i).id
                        itemImageURl = item.get(i).image
                        itemName = item.get(i).name
                        itemDescription = item.get(i).description
                        itemStatus = item.get(i).status
                        arrayItems.add(
                            ItemListModel(
                                "",
                                itemName,
                                currentGroupName,
                                itemDescription,
                                "",
                                itemImageURl,
                                itemId,
                                itemStatus
                            )
                        )
                    }
                }
                // Creating Adapter to Recycle View
//                var adapter = GroupAdapter<GroupieViewHolder>()


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
                        myEmail = user.get(i).email
                        myGroup = user.get(i).group
                        populateItems(currentGroupName)
                    }
                }
            }

    }

}