package com.example.locationsharingapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ManagingFriendRequest : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var userAdapter: FriendListAdapter
    private val viewModel: UserViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_managing_friend_request)

        recyclerView = findViewById(R.id.recycler_view)

        userAdapter = FriendListAdapter(
            mutableListOf(),
            onSendRequest = { user -> viewModel.sendFriendRequest(user.userId) },
            onCancelRequest = { user -> viewModel.cancelFriendRequest(user.userId) },
            onAcceptRequest = { user -> viewModel.acceptFriendRequest(user.userId) },
            onUnfriend = { user -> viewModel.unfriend(user.userId) }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = userAdapter

        viewModel.getFriendRequests()

        viewModel.friendRequestsLiveData.observe(this) { users ->
            userAdapter.updateUsers(users)
        }
    }
}