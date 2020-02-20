package ru.hawoline.messenger.messages

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_latest_messages.*
import kotlinx.android.synthetic.main.latest_message_row.view.*
import ru.hawoline.messenger.R
import ru.hawoline.messenger.model.ChatMessage
import ru.hawoline.messenger.model.User
import ru.hawoline.messenger.registerlogin.RegisterActivity

class LatestMessagesActivity : AppCompatActivity() {

    companion object {
        var currentUser: User? = null
    }

    val adapter = GroupAdapter<GroupieViewHolder>()
    val latestMessages = HashMap<String, ChatMessage>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_messages)

        verifyUserIsLoggedIn()

        listenForLatestMessages()

        adapter.setOnItemClickListener { item, view ->
            val row = item as LatestMessageItem
            val user = row.user

            val intent = Intent(this, ChatLogActivity::class.java)
            intent.putExtra(NewMessageActivity.USER_TAG, user)
            startActivity(intent)
        }
    }

    private fun verifyUserIsLoggedIn(){
        val uid = FirebaseAuth.getInstance().uid

        if (uid == null){
            intent = Intent(this, RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            return
        }

        fetchCurrentUser()
    }

    private fun fetchCurrentUser(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                currentUser = p0.getValue(User::class.java)
            }
        })
    }

    private fun refreshRecyclerView(){
        adapter.clear()
        latestMessages.forEach {
            adapter.add(LatestMessageItem(it.value))
        }
    }

    private fun listenForLatestMessages(){
        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("latest-messages/$fromId")

        ref.addChildEventListener(object: ChildEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java)?: return

                latestMessages[p0.key!!] = chatMessage
                refreshRecyclerView()
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java)?: return
                latestMessages[p0.key!!] = chatMessage
                refreshRecyclerView()
            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }

        })

        latest_messages_recycler_view.adapter = adapter
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_new_message -> {
                val intent = Intent(this, NewMessageActivity::class.java)
                startActivity(intent)
            }

            R.id.menu_sign_out -> {
                FirebaseAuth.getInstance().signOut()

                intent = Intent(this, RegisterActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
}

class LatestMessageItem(val chatMessage: ChatMessage): Item<GroupieViewHolder> (){

    var user:User? = null

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        val userId: String

        if (chatMessage.fromId == FirebaseAuth.getInstance().uid){
            userId = chatMessage.toId
        } else {
            userId = chatMessage.fromId
        }

        val ref = FirebaseDatabase.getInstance().getReference("/users/$userId")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                user = p0.getValue(User::class.java)

                viewHolder.itemView.username_text_view_latest_message.text = user?.username

                Picasso.get().load(user?.profilePhoto).into(viewHolder.itemView.profile_image_view_latest_message)
            }
        })
        viewHolder.itemView.latest_message_text_view.text = chatMessage.text


//        Picasso.get().load(user.profilePhoto).into(viewHolder.itemView.profile_image_view_latest_message)
    }

    override fun getLayout(): Int {
        return R.layout.latest_message_row
    }
}