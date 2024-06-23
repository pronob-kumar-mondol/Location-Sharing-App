package com.example.locationsharingapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FriendListAdapter(
    private var users: List<User> = listOf(),
    private val onSendFriendRequest: (User) -> Unit,
    private val onAcceptFriendRequest: (User) -> Unit,
    private val onViewLocation: (User) -> Unit
) : RecyclerView.Adapter<FriendListAdapter.FriendViewHolder>() {

    fun setUsers(users: List<User>) {
        this.users = users
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val user = users[position]
        holder.bind(user)
    }

    override fun getItemCount(): Int = users.size

    inner class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val userName: TextView = itemView.findViewById(R.id.userName)
        private val userEmail: TextView = itemView.findViewById(R.id.userEmail)
        private val latitude: TextView = itemView.findViewById(R.id.latitude)
        private val longitude: TextView = itemView.findViewById(R.id.longitude)
        private val sendRequestButton: ImageView = itemView.findViewById(R.id.send_request_button)
        private val acceptRequestButton: ImageView = itemView.findViewById(R.id.accept_request_button)
        private val viewLocationButton: ImageView = itemView.findViewById(R.id.location)

        fun bind(user: User) {
            userName.text = user.name
            userEmail.text = user.email
            latitude.text = user.latitude.toString()
            longitude.text = user.longitude.toString()
            sendRequestButton.setOnClickListener {
                onSendFriendRequest(user)
            }
            acceptRequestButton.setOnClickListener {
                onAcceptFriendRequest(user)
            }
            viewLocationButton.setOnClickListener {
                onViewLocation(user)
            }
            sendRequestButton.visibility = if (user.isFriend) View.GONE else View.VISIBLE
            acceptRequestButton.visibility = if (user.hasPendingRequest) View.VISIBLE else View.GONE
            viewLocationButton.visibility = if (user.isFriend) View.VISIBLE else View.GONE
        }
    }
}
