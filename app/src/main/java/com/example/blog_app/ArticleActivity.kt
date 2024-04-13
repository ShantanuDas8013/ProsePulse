package com.example.blog_app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.blog_app.adapter.ArticleAdapter
import com.example.blog_app.adapter.BlogAdapter
import com.example.blog_app.databinding.ActivityArticleBinding
import com.example.blog_app.model.BlogItemModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ArticleActivity : AppCompatActivity() {
    private val binding:ActivityArticleBinding by lazy {
        ActivityArticleBinding.inflate(layoutInflater)
    }
    private lateinit var databaseReference: DatabaseReference
    private val auth = FirebaseAuth.getInstance()
    private lateinit var blogAdapter: ArticleAdapter
    private val EDIT_BLOG_REQUEST_CODE=123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.backbutton.setOnClickListener {
            finish()
        }
        val currentUserId=auth.currentUser?.uid
        val recyclerView=binding.articleRecyclerView
        recyclerView.layoutManager=LinearLayoutManager(this)

        if (currentUserId!=null){
            blogAdapter= ArticleAdapter(this, emptyList(),object :ArticleAdapter.OnItemClickListener{
                override fun onEditClick(blogItem: BlogItemModel) {
                    val intent=Intent(this@ArticleActivity,EditBlogActivity::class.java)
                    intent.putExtra("blogItem",blogItem)
                    startActivityForResult(intent,EDIT_BLOG_REQUEST_CODE)

                }

                override fun onReadMoreClick(blogItem: BlogItemModel) {
                    val intent=Intent(this@ArticleActivity,ReadMoreActivity::class.java)
                    intent.putExtra("blogItem",blogItem)
                    startActivity(intent)

                }

                override fun onDeleteClick(blogItem: BlogItemModel) {
                    deleteBlogPost(blogItem)

                }
            })
        }

        recyclerView.adapter=blogAdapter


        //get saved data from database
        databaseReference=FirebaseDatabase.getInstance("https://blog-app-234c1-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("blogs")
        databaseReference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val blogSavedList= ArrayList<BlogItemModel>()
                for (postSnapshot in snapshot.children){
                    val blogSaved=postSnapshot.getValue(BlogItemModel::class.java)
                    if (blogSaved!=null && currentUserId==blogSaved.userId){
                        blogSavedList.add(blogSaved)
                    }
                }
                blogAdapter.setData(blogSavedList)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ArticleActivity,"Error Loading Saved Blog 🙄",Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun deleteBlogPost(blogItem: BlogItemModel) {
        val postId = blogItem.postId
        val blogPostReference = databaseReference.child(postId)
        //remove the blog post
        blogPostReference.removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Blog Post Deleted Successfully", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(this, "Unable To Delete The Blog Post", Toast.LENGTH_SHORT).show()
            }
    
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode==EDIT_BLOG_REQUEST_CODE && resultCode== Activity.RESULT_OK){

        }
    }
}