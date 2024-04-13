package com.example.blog_app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.blog_app.databinding.ActivityEditBlogBinding
import com.example.blog_app.model.BlogItemModel
import com.google.firebase.database.FirebaseDatabase

class EditBlogActivity : AppCompatActivity() {

    private val binding: ActivityEditBlogBinding by lazy {
        ActivityEditBlogBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        binding.imageButton.setOnClickListener {
            finish()
        }

        val blogItemModel = intent.getParcelableExtra<BlogItemModel>("blogItem")
        binding.BlogTitle.editText?.setText(blogItemModel?.heading)
        binding.BlogDescription.editText?.setText(blogItemModel?.post)


        binding.UpdateBlog.setOnClickListener {
            val UpdatedTitle = binding.BlogTitle.editText?.text.toString().trim()
            val UpdatedDescription = binding.BlogDescription.editText?.text.toString().trim()

            if (UpdatedTitle.isEmpty() || UpdatedDescription.isEmpty()) {
                Toast.makeText(this, "Please Fill All The Details", Toast.LENGTH_SHORT).show()
            } else {
                blogItemModel?.heading = UpdatedTitle
                blogItemModel?.post = UpdatedDescription

                if (blogItemModel != null) {
                    updateDataInFirebase(blogItemModel)
                }
            }
        }
    }

    private fun updateDataInFirebase(blogItemModel: BlogItemModel) {

        val databaseReference =
            FirebaseDatabase.getInstance("https://blog-app-234c1-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("blogs")
        val postId = blogItemModel.postId

        databaseReference.child(postId).setValue(blogItemModel)
            .addOnSuccessListener {
                Toast.makeText(this, "Blog Updated Successfully", Toast.LENGTH_SHORT).show()
                finish()
            }.addOnFailureListener {
                Toast.makeText(this, "Blog Not Updated", Toast.LENGTH_SHORT).show()
                finish()
            }

    }
}