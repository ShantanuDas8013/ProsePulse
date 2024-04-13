package com.example.blog_app

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.blog_app.databinding.ActivityReadMoreBinding
import com.example.blog_app.model.BlogItemModel

class ReadMoreActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReadMoreBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReadMoreBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.BackButton.setOnClickListener {
            finish()

        }

        val blogs = intent.getParcelableExtra<BlogItemModel>("blogItem")
        if (blogs != null) {
            //Retrieve user related data here e. x blog title etc.
            binding.TitleText.text = blogs.heading
            binding.UserName.text = blogs.userName
            binding.datePicker.text = blogs.date
            binding.blogdescription.text = blogs.post

            val userImageUrl = blogs.profileImage
            Glide.with(this)
                .load(userImageUrl)
                .apply(RequestOptions.circleCropTransform())
                .into(binding.profileImage)

        } else {
            Toast.makeText(this, "Failed To Load Blog", Toast.LENGTH_SHORT).show()
        }
    }
}