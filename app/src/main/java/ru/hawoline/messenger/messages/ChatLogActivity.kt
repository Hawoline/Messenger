package ru.hawoline.messenger.messages

import ru.hawoline.messenger.R
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*
import ru.hawoline.messenger.ProfileUserActivity
import ru.hawoline.messenger.model.ChatMessage
import ru.hawoline.messenger.model.User


class ChatLogActivity : AppCompatActivity() {

    var user: User? = null

    private val adapter = GroupAdapter<GroupieViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        messages_recycler_view_chat_log.adapter = adapter

        user = intent.getParcelableExtra(NewMessageActivity.USER_TAG)
        user?: finish()

        setupActionBar()
        val profileImageView = supportActionBar?.customView?.findViewById<ImageView>(R.id.profile_another_user_image_view)
        Picasso.get().load(user?.profilePhoto).into(profileImageView)

        profileImageView?.setOnClickListener {
            val intent = Intent(this, ProfileUserActivity::class.java)
            startActivity(intent)
        }

        send_button_chat_log.setOnClickListener {
            performSendMessage()
        }

        listenForMessages()
    }

    private fun setupActionBar() {
        val ab: ActionBar? = supportActionBar
        ab?.setDisplayShowCustomEnabled(true)
        val inflater = this
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val v: View = inflater.inflate(R.layout.profile_circle_layout, null)

        ab?.customView = v
        ab?.title = ""
    }

    private fun performSendMessage(){

        val text = new_message_editText_chat_log.text.toString().trim()
        val fromId = FirebaseAuth.getInstance().uid
        val toId = user?.uid

        if (fromId == null || text == "") {
            return
        }

        val reference = FirebaseDatabase.getInstance().getReference("/messages").push()

        val chatMessage = ChatMessage(reference.key!!, text, fromId, toId!!, System.currentTimeMillis() / 1000)
        reference.setValue(chatMessage)
            .addOnSuccessListener {
                new_message_editText_chat_log.text.clear()
                messages_recycler_view_chat_log.scrollToPosition(adapter.itemCount - 1)
            }

        val refFromUser = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")
        refFromUser.setValue(chatMessage)

        val refToUser = FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")
        refToUser.setValue(chatMessage)
    }

    private fun listenForMessages(){
        val ref = FirebaseDatabase.getInstance().getReference("/messages")

        ref.addChildEventListener(object: ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java) ?: return

                val currentUser = LatestMessagesActivity.currentUser

                Log.d("CurrentUser", currentUser!!.profilePhoto)

                if (chatMessage.fromId != chatMessage.toId){

                    if(chatMessage.fromId == FirebaseAuth.getInstance().uid &&
                        chatMessage.toId == user?.uid) {

                        val chatToItem = ChatToItem(chatMessage.text)

                        adapter.add(chatToItem)
                        messages_recycler_view_chat_log.scrollToPosition(adapter.itemCount - 1)
                    }

                    if (chatMessage.toId == FirebaseAuth.getInstance().uid &&
                        chatMessage.fromId == user?.uid) {

                        val chatFromItem = ChatFromItem(chatMessage.text)

                        adapter.add(chatFromItem)
                        messages_recycler_view_chat_log.scrollToPosition(adapter.itemCount - 1)
                    }
                }
            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }

        })
    }
}

class ChatFromItem(val text: String): Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.message_from_text_view.text = text
    }

    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }
}

class ChatToItem(val text: String): Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.message_to_text_view.text = text
    }

    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }
}
