package com.example.locationsharingapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class UserViewModel: ViewModel() {

    private val userRepository = UserRepository()
    private var usersLiveData: MutableLiveData<List<User>>?=null

    fun getUsers(): LiveData<List<User>> {
        if (usersLiveData == null) {
            usersLiveData = userRepository.getUsers()
        }
        return usersLiveData!!
    }

    fun updateUser(user: User) {
        userRepository.updateUser(user)
    }

    fun addUser(user: User) {
        userRepository.addUser(user)
    }
}