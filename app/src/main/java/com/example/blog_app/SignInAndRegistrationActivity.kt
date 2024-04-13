package com.example.blog_app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.blog_app.databinding.ActivitySignInandRegistrationBinding
import com.example.blog_app.model.UserData
import com.example.blog_app.register.WelcomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class SignInAndRegistrationActivity : AppCompatActivity() {
    private val binding: ActivitySignInandRegistrationBinding by lazy {
        ActivitySignInandRegistrationBinding.inflate(layoutInflater)
    }
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        auth = FirebaseAuth.getInstance()
        database =
            FirebaseDatabase.getInstance("https://blog-app-234c1-default-rtdb.asia-southeast1.firebasedatabase.app/")
        storage = FirebaseStorage.getInstance()

        val action = intent.getStringExtra("action")
        if (action == "login") {
            binding.loginButton.visibility = View.VISIBLE
            binding.EmailAddress.visibility = View.VISIBLE
            binding.Password.visibility = View.VISIBLE


            binding.registerButton.visibility = View.GONE
            binding.newHereTextView.visibility = View.GONE
            binding.registerEmail.visibility = View.GONE
            binding.registerName.visibility = View.GONE
            binding.registerPassword.visibility = View.GONE
            binding.cardView.visibility = View.GONE



            binding.loginButton.setOnClickListener {
                val loginEmail = binding.EmailAddress.text.toString()
                val loginPassword = binding.Password.text.toString()
                if (loginEmail.isEmpty() || loginPassword.isEmpty()) {
                    Toast.makeText(this, "Please Fill All The Details", Toast.LENGTH_SHORT).show()
                } else {
                    auth.signInWithEmailAndPassword(loginEmail, loginPassword)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this, "Login Successful😊", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            } else {
                                Toast.makeText(this, "Login Failed. Try Again😐", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                }
            }
        } else if (action == "register") {
            binding.loginButton.visibility = View.GONE


            binding.registerButton.setOnClickListener {
                val registerName = binding.registerName.text.toString()
                val registerEmail = binding.registerEmail.text.toString()
                val registerPassword = binding.registerPassword.text.toString()

                if (registerName.isEmpty() || registerEmail.isEmpty() || registerPassword.isEmpty() || imageUri == null) {
                    Toast.makeText(this, "Please Fill All Details", Toast.LENGTH_SHORT).show()
                } else {

                    auth.createUserWithEmailAndPassword(registerEmail, registerPassword)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = auth.currentUser
                                auth.signOut()
                                user?.let {
                                    val userReference = database.getReference("users")
                                    val userId = user.uid
                                    val userData = UserData(registerName, registerEmail)
                                    userReference.child(userId).setValue(userData)
                                    val storageReference =
                                        storage.reference.child("person_image/$userId.jpg")
                                    storageReference.putFile(imageUri!!)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                storageReference.downloadUrl.addOnCompleteListener { imageUri ->
                                                    if (imageUri.isSuccessful) {
                                                        val imageUrl = imageUri.result.toString()

                                                        //save the image url to the real time database
                                                        userReference.child(userId)
                                                            .child("profileImage")
                                                            .setValue(imageUrl)
                                                    }
                                                }
                                            }
                                        }
                                    Toast.makeText(
                                        this,
                                        "User Registration Successful😎",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    startActivity(Intent(this, WelcomeActivity::class.java))
                                    finish()
                                }

                            } else {
                                Toast.makeText(
                                    this,
                                    "User Registration Failed🫥",
                                    Toast.LENGTH_SHORT
                                ).show()


                            }

                        }

                }
            }

        }

        //set on clicklistner for Choose image
        binding.cardView.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(
                Intent.createChooser(intent, "select Image"),
                PICK_IMAGE_REQUEST
            )

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            Glide.with(this)
                .load(imageUri)
                .apply(RequestOptions.circleCropTransform())
                .into(binding.regiserUserImage)
        }
    }
}