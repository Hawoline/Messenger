package ru.hawoline.messenger.registerlogin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*
import ru.hawoline.messenger.R
import ru.hawoline.messenger.messages.LatestMessagesActivity
import ru.hawoline.messenger.model.User
import java.util.*

class RegisterActivity : AppCompatActivity() {

    var selectedPhotoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        register_button.setOnClickListener {
            performRegister()
        }

        select_photo_button.setOnClickListener {
            pickImageFromGallery()
        }

        already_have_an_account_text_view.setOnClickListener {
            intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE){
            selectedPhotoUri = data?.data

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            select_photo_image_view.setImageBitmap(bitmap)
            select_photo_button.alpha = 0.0f
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent,
            IMAGE_PICK_CODE
        )
    }


    private fun performRegister(){
        val email = email_register_edit_text.text.toString().trim();
        val password = password_register_edit_text.text.toString().trim();

        Log.d("RegisterActivity", email)
        Log.d("RegisterActivity", password)
        if(email.isEmpty() || password.isEmpty() || selectedPhotoUri == null){
            Toast.makeText(this, "Please fill all fields and select profile photo", Toast.LENGTH_LONG).show()
            return
        }
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                Log.d("Main", "User uid: ${it.user?.uid}")
                uploadImageToFirebaseStorage()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Your email is incorrect", Toast.LENGTH_LONG).show()
            }
    }

    private fun uploadImageToFirebaseStorage(){

        val filename = UUID.randomUUID().toString()
        val reference = FirebaseStorage.getInstance().getReference("/images/$filename")

        reference.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {

                reference.downloadUrl.addOnSuccessListener {
                    saveUserToFirebaseDatabase(it.toString())
                }
            }
    }

    private fun saveUserToFirebaseDatabase(profileImageUrl: String){
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        var user = User(uid, username_register_edit_text.text.toString(), profileImageUrl)

        ref.setValue(user)
            .addOnSuccessListener {
                val intent = Intent(this, LatestMessagesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
    }

    companion object {
        private const val IMAGE_PICK_CODE = 1000;
    }
}
