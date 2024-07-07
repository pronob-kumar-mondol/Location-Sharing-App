package com.example.locationsharingapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth

class FriendListAdapter(
    private val users: MutableList<User>,
    private val onSendRequest: (User) -> Unit,
    private val onCancelRequest: (User) -> Unit,
    private val onAcceptRequest: (User) -> Unit,
    private val onUnfriend: (User) -> Unit
) : RecyclerView.Adapter<FriendListAdapter.UserViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.bind(user)
    }

    override fun getItemCount(): Int = users.size

    fun updateUsers(newUsers: List<User>) {
        users.clear()
        users.addAll(newUsers)
        notifyDataSetChanged()
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val userName: TextView = itemView.findViewById(R.id.userName)
        private val userEmail: TextView = itemView.findViewById(R.id.userEmail)
        private val latitude: TextView = itemView.findViewById(R.id.latitude)
        private val longitude: TextView = itemView.findViewById(R.id.longitude)
        private val profilePic: ImageView = itemView.findViewById(R.id.profilePic)
        private val sendRequestButton: ImageView = itemView.findViewById(R.id.send_request_button)
        private val cancelRequestButton: ImageView = itemView.findViewById(R.id.cancel_request_sent)
        private val acceptRequestButton: ImageView = itemView.findViewById(R.id.accept_request_button)
        private val declineRequestButton: ImageView = itemView.findViewById(R.id.cancel_request_button)
        private val friendsButton: ImageView = itemView.findViewById(R.id.we_are_now_friends)
        private val locationButton: ImageView = itemView.findViewById(R.id.location)

        fun bind(user: User) {
            userName.text = user.name
            userEmail.text = user.email
            latitude.text = user.latitude.toString()
            longitude.text = user.longitude.toString()

            when {
                user.friendRequests.contains(FirebaseAuth.getInstance().currentUser?.uid) -> {
                    sendRequestButton.visibility = View.GONE
                    cancelRequestButton.visibility = View.GONE
                    acceptRequestButton.visibility = View.VISIBLE
                    declineRequestButton.visibility = View.VISIBLE
                    friendsButton.visibility = View.GONE
                    locationButton.visibility = View.GONE
                }
                user.friends.contains(FirebaseAuth.getInstance().currentUser?.uid) -> {
                    sendRequestButton.visibility = View.GONE
                    cancelRequestButton.visibility = View.GONE
                    acceptRequestButton.visibility = View.GONE
                    declineRequestButton.visibility = View.GONE
                    friendsButton.visibility = View.VISIBLE
                    locationButton.visibility = View.VISIBLE
                }
                else -> {
                    sendRequestButton.visibility = View.VISIBLE
                    cancelRequestButton.visibility = View.GONE
                    acceptRequestButton.visibility = View.GONE
                    declineRequestButton.visibility = View.GONE
                    friendsButton.visibility = View.GONE
                    locationButton.visibility = View.GONE
                }
            }

            sendRequestButton.setOnClickListener { onSendRequest(user) }
            cancelRequestButton.setOnClickListener { onCancelRequest(user) }
            acceptRequestButton.setOnClickListener { onAcceptRequest(user) }
            declineRequestButton.setOnClickListener { onCancelRequest(user) }
            friendsButton.setOnClickListener { onUnfriend(user) }
        }
    }
}
