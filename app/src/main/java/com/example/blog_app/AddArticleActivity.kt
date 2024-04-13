package com.example.blog_app

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.blog_app.databinding.ActivityAddArticleBinding
import com.example.blog_app.model.BlogItemModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date

class AddArticleActivity : AppCompatActivity() {
    private val binding: ActivityAddArticleBinding by lazy {
        ActivityAddArticleBinding.inflate(layoutInflater)
    }


    private val databaseReference: DatabaseReference =
        FirebaseDatabase.getInstance("https://blog-app-234c1-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("blogs")
    private val userReference: DatabaseReference =
        FirebaseDatabase.getInstance("https://blog-app-234c1-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("users")
    private val auth = FirebaseAuth.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.imageButton.setOnClickListener {
            finish()
        }

        binding.AddBlog.setOnClickListener {

            val title = binding.BlogTitle.editText?.text.toString().trim()
            val description = binding.BlogDescription.editText?.text.toString().trim()

            if (title.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Please Fill Fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //get current user
            val user: FirebaseUser? = auth.currentUser

            if (user != null) {
                val userId = user.uid
                val userName = user.displayName ?: "Anonymous"
                val userImageUrl = user.photoUrl ?: ""

                //Fetch User Name and User profile from database

                userReference.child(userId)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val userData =
                                snapshot.getValue(com.example.blog_app.model.UserData::class.java)
                            if (userData != null) {
                                val userNameFromDB = userData.name
                                val userImageUrlFromDB = userData.profileImage


                                val currentDate = SimpleDateFormat("yyyy-MM-dd").format(Date())

                                //Create a blogItemModel
                                val blogItem = BlogItemModel(
                                    title,
                                    userNameFromDB,
                                    currentDate,
                                    description,
                                    userId,
                                    0,
                                    userImageUrlFromDB
                                )

                                val key = databaseReference.push().key
                                if (key != null) {
                                    blogItem.postId = key
                                    val blogReference = databaseReference.child(key)
                                    blogReference.setValue(blogItem).addOnCompleteListener {
                                        if (it.isSuccessful) {
                                            Toast.makeText(
                                                this@AddArticleActivity,
                                                "Successfully Added Blog",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            finish()
                                        } else {
                                            Toast.makeText(
                                                this@AddArticleActivity,
                                                "Failed To Add Blog",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }
                    })
            }

        }
    }
}