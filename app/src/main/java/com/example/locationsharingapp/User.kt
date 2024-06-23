package com.example.locationsharingapp

data class User(
    val userId: String = "",
    var name: String = "",
    val email: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val friendRequests: List<String> = emptyList(),
    val friends: List<String> = emptyList()
){
    val isFriend: Boolean
        get() = friends.isNotEmpty()

    val hasPendingRequest: Boolean
        get() = friendRequests.isNotEmpty()
}
