package ru.hawoline.messenger.registerlogin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import ru.hawoline.messenger.R
import ru.hawoline.messenger.messages.LatestMessagesActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        login_button.setOnClickListener {
            performLogin()
        }
    }

    private fun performLogin(){
        val email = email_login_edit_text.text.toString().trim()
        val password = password_login_edit_text.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()){
            Toast.makeText(this, "Fill the all fields", Toast.LENGTH_LONG).show()
            return
        }
        
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val intent = Intent(this, LatestMessagesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
    }
}
