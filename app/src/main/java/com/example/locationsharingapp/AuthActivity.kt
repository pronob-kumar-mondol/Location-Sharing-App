package com.example.locationsharingapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class AuthActivity : AppCompatActivity() {
    private val userViewModel: UserViewModel by viewModels()

    lateinit var btn_login: Button
    lateinit var btn_register: Button
    lateinit var toggleAuthModeTextView: TextView
    lateinit var nameEditText: EditText
    lateinit var emailEditText: EditText
    lateinit var passwordEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_auth)



        btn_login = findViewById(R.id.btn_login)
        btn_register = findViewById(R.id.btn_register)
        toggleAuthModeTextView = findViewById(R.id.toggleAuthModeTextView)
        nameEditText = findViewById(R.id.nameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)

        btn_login.setOnClickListener { onLoginClicked() }
        btn_register.setOnClickListener { onRegisterClicked() }
        toggleAuthModeTextView.setOnClickListener { onToggleAuthModeClicked() }

    }

    private fun onToggleAuthModeClicked() {
        if (btn_register.visibility == View.GONE) {
            btn_register.visibility = View.VISIBLE
            nameEditText.visibility = View.VISIBLE
            btn_login.visibility = View.GONE
            toggleAuthModeTextView.text = "Already have an account? Login here."
        } else {
            btn_register.visibility = View.GONE
            nameEditText.visibility = View.GONE
            btn_login.visibility = View.VISIBLE
            toggleAuthModeTextView.text = "Don't have an account? Register here."
        }

    }

    private fun onLoginClicked() {
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    startActivity(Intent(this, FriendListActivity::class.java))
                    finish()
                } else {
                    // Handle login failure
                }
            }
    }

    private fun onRegisterClicked() {
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()
        val name = nameEditText.text.toString()

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = task.result?.user?.uid ?: ""
                    val user = User(userId, name, email)
                    userViewModel.addUser(user)
                    startActivity(Intent(this, FriendListActivity::class.java))
                    finish()
                } else {
                    // Handle registration failure
                }
            }
    }
}