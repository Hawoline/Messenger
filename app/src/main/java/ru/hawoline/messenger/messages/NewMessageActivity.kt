package ru.hawoline.messenger.messages

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.user_row_new_message_layout.view.*
import ru.hawoline.messenger.R
import ru.hawoline.messenger.model.User

class NewMessageActivity : AppCompatActivity() {

    companion object{
        const val USER_TAG = "Username"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)

        fetchUsers()
    }

    private fun fetchUsers(){
        val ref = FirebaseDatabase.getInstance().getReference("/users")

        ref.addListenerForSingleValueEvent(object: ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {
                val adapter = GroupAdapter<GroupieViewHolder>()

                p0.children.forEach{
                    val user = it.getValue(User::class.java)

                    val currentUserUid = FirebaseAuth.getInstance().uid
                    val anotherUserUid = user?.uid

                    val sameUid = currentUserUid == anotherUserUid

                    if (!sameUid){
                        adapter.add(UserItem(user!!))
                    }
                }

                adapter.setOnItemClickListener { item, view ->

                    val userItem = item as UserItem
                    val intent = Intent(view.context, ChatLogActivity::class.java)
                    intent.putExtra(USER_TAG, userItem.user)
                    startActivity(intent)

                    finish()
                }
                new_message_recycler_view.adapter = adapter
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }
}

class UserItem(val user: User): Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.username_text_view_new_message.text = user.username

        Picasso.get().load(user.profilePhoto).into(viewHolder.itemView.profile_circle_image_view)
    }

    override fun getLayout(): Int {
        return R.layout.user_row_new_message_layout
    }
}
