package com.example.blog_app

import android.content.Intent
import android.util.Log
import android.os.Bundle
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.blog_app.adapter.BlogAdapter
import com.example.blog_app.databinding.ActivityMainBinding
import com.example.blog_app.model.BlogItemModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private lateinit var databaseReference: DatabaseReference
    private val blogItems = mutableListOf<BlogItemModel>()
    private lateinit var auth: FirebaseAuth
    private lateinit var blogAdapter: BlogAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //to go save article page
        binding.saveArticleButton.setOnClickListener {
            startActivity(Intent(this, SavedArticlesActivity::class.java))
        }

        //to go profile activity
        binding.ProfileImage.setOnClickListener{
            startActivity(Intent(this,ProfileActivity::class.java))
        }//to go profile activity
        binding.cardView2.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }


        auth = FirebaseAuth.getInstance()
        databaseReference =
            FirebaseDatabase.getInstance("https://blog-app-234c1-default-rtdb.asia-southeast1.firebasedatabase.app/").reference.child(
                "blogs"
            )

        // Initialize the recycler view and set adapter
        val recyclerView = binding.blogRecyclerView
        blogAdapter = BlogAdapter(blogItems)
        recyclerView.adapter = blogAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // SearchView setup
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filter(newText)
                return true
            }
        })

        // Fetch Data from Database
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                blogItems.clear()
                for (snapshot in snapshot.children) {
                    val blogItem = snapshot.getValue(BlogItemModel::class.java)
                    if (blogItem != null) {
                        blogItems.add(blogItem)
                    }
                }
                // Reverse the list
                blogItems.reverse()

                // Log the size of blogItems
                Log.d("MainActivity", "Total blog items: ${blogItems.size}")

                // Notify The Adapter that the data has changed
                blogAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Blog Loading Failed", Toast.LENGTH_SHORT).show()
            }
        })


        binding.floatingAddArticleButton.setOnClickListener {
            startActivity(Intent(this, AddArticleActivity::class.java))
        }

        // Load user profile image
        val userId = auth.currentUser?.uid
        if (userId != null) {
            loadUserProfileImage(userId)
        }
    }

    private fun loadUserProfileImage(userId: String) {
        val userReference =
            FirebaseDatabase.getInstance("https://blog-app-234c1-default-rtdb.asia-southeast1.firebasedatabase.app/").reference.child(
                "users"
            ).child(userId)

        userReference.child("profileImage").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val profileImageUrl = snapshot.getValue(String::class.java)

                if (profileImageUrl != null) {
                    Glide.with(this@MainActivity)
                        .load(profileImageUrl)
                        .into(binding.ProfileImage)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Error Loading", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun filter(query: String?) {
        val filteredList: MutableList<BlogItemModel> = mutableListOf()

        if (query.isNullOrBlank()) {
            filteredList.addAll(blogItems)
        } else {
            val lowerCaseQuery = query.toLowerCase()

            for (blogItem in blogItems) {
                val heading = blogItem.heading?.toLowerCase()
                val userName = blogItem.userName?.toLowerCase()

                if (!heading.isNullOrEmpty() && heading.contains(lowerCaseQuery) ||
                    !userName.isNullOrEmpty() && userName.contains(lowerCaseQuery)) {
                    filteredList.add(blogItem)
                }
            }
        }

        // Log the size of filteredList
        Log.d("MainActivity", "Filtered blog items: ${filteredList.size}")

        blogAdapter.updateData(filteredList)
    }


}
