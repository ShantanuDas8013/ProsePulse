package com.example.blog_app

import android.os.Bundle
import android.widget.ImageButton

import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.blog_app.adapter.BlogAdapter
import com.example.blog_app.databinding.ActivitySavedArticlesBinding
import com.example.blog_app.model.BlogItemModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SavedArticlesActivity : AppCompatActivity() {

    private val binding: ActivitySavedArticlesBinding by lazy {
        ActivitySavedArticlesBinding.inflate(layoutInflater)
    }

    private val savedBlogArticles = mutableListOf<BlogItemModel>()
    private lateinit var blogAdapter: BlogAdapter
    private val auth = FirebaseAuth.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        //Initialize blog adapter
        blogAdapter = BlogAdapter(savedBlogArticles.filter { it.isSaved }.toMutableList())
        val recyclerView = binding.savedArticleRecyclerView
        recyclerView.adapter = blogAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userReference =
                FirebaseDatabase.getInstance("https://blog-app-234c1-default-rtdb.asia-southeast1.firebasedatabase.app/")
                    .getReference("users").child(userId).child("saveBlogPosts")

            userReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (postSnapshot in snapshot.children) {
                        val postId = postSnapshot.key
                        val isSaved = postSnapshot.value as Boolean
                        if (postId != null && isSaved) {
                            //Fetch the corresponding blog time on postId using a co-routine
                            CoroutineScope(Dispatchers.IO).launch {
                                val blogItem = fetchBlogItem(postId)
                                if (blogItem != null) {
                                    savedBlogArticles.add(blogItem)
                                    launch(Dispatchers.Main) {
                                        blogAdapter.updateData(savedBlogArticles)
                                    }
                                }
                            }
                        }
                    }

                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        }


        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }
    }

    private suspend fun fetchBlogItem(postId: String): BlogItemModel? {
        val blogReference =
            FirebaseDatabase.getInstance("https://blog-app-234c1-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("blogs")
        return try {
            val dataSnapshot = blogReference.child(postId).get().await()
            val blogData = dataSnapshot.getValue(BlogItemModel::class.java)
            blogData
        } catch (e: Exception) {
            null
        }
    }
}
