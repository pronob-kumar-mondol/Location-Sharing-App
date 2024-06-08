package com.example.locationsharingapp

import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class MyProfileActivity : AppCompatActivity() {

    private val userViewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_my_profile)

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        userViewModel.getUsers().observe(this) { users ->
            val user = users.find { it.userId == userId }
            user?.let {
                findViewById<EditText>(R.id.nameEditText).setText(it.name)
                findViewById<EditText>(R.id.emailEditText).setText(it.email)
                findViewById<TextView>(R.id.locationTextView).text="Location: (${it.latitude}, ${it.longitude})"
            }
        }

        findViewById<TextView>(R.id.btn_save).setOnClickListener { onSaveClicked(userId) }

    }

    private fun onSaveClicked(userId: String) {
        val newName =findViewById<EditText>(R.id.nameEditText).text.toString()

        userViewModel.getUsers().observe(this) { users ->
            val user = users.find { it.userId == userId }
            user?.let {
                it.name = newName
                userViewModel.updateUser(it)
            }
        }
    }
}