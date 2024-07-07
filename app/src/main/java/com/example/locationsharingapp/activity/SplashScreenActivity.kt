package com.example.locationsharingapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash_screen)

        val splashTimeOut = 1000 // 2 seconds
        Thread {
            Thread.sleep(splashTimeOut.toLong())
            auth = FirebaseAuth.getInstance()

            // Check if the user is already signed in
            if (auth.currentUser != null) {
                // User is signed in, navigate to FriendListActivity
                startActivity(Intent(this, FriendListActivity::class.java))
            } else {
                // User is not signed in, navigate to AuthActivity
                startActivity(Intent(this, AuthActivity::class.java))
            }
            finish()
        }.start()

    }
}