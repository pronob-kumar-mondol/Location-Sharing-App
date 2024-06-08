package com.example.locationsharingapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton

class FriendListActivity : AppCompatActivity() {

    private val userViewModel: UserViewModel by viewModels()
    private lateinit var adapter: FriendListAdapter
    lateinit var fab:FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_friend_list)

        fab=findViewById(R.id.fab)

        val recycler_view = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recycler_view)
        adapter = FriendListAdapter()
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.adapter = adapter

        // Observe users LiveData from ViewModel
        userViewModel.getUsers().observe(this) { users ->
            adapter.setUsers(users)
        }

        fab.setOnClickListener {
            startActivity(Intent(this,GoogleMapActivity::class.java))
        }





    }
}