package com.example.locationsharingapp

data class User(
    val userId: String = "",
    var name: String = "",
    val email: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    var friendStatus: FriendStatus = FriendStatus.NONE,
    var friendRequests: List<String> = emptyList(), // List of user IDs who sent friend requests
    var friends: List<String> = emptyList() // List of user IDs who are friends
)
enum class FriendStatus {
    NONE, REQUEST_SENT, REQUEST_RECEIVED, FRIENDS
}
