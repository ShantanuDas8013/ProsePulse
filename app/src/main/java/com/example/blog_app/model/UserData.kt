package com.example.blog_app.model

data class UserData(
    val name: String,
    val email: String,
    val profileImage: String = ""
) {
    constructor() : this("", "", "")
}
