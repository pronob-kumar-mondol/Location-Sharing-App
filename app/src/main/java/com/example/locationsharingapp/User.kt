package com.example.locationsharingapp

data class User(
    val userId: String = "",
    var name: String = "",
    val email: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)
