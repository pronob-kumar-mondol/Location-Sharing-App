package com.example.locationsharingapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class UserViewModel: ViewModel() {

    private val repository = UserRepository(FirebaseFirestore.getInstance())

    private val _usersLiveData = MutableLiveData<List<User>>()
    var usersLiveData: LiveData<List<User>> = _usersLiveData
    private val _friendRequestsLiveData = MutableLiveData<List<User>>()
    val friendRequestsLiveData: LiveData<List<User>> = _friendRequestsLiveData


    fun addUser(user: User) {
        repository.addUser(user)
    }

    fun getUsers(): LiveData<List<User>> {
        if (usersLiveData == null) {
            usersLiveData = repository.getUsers()
        }
        return usersLiveData!!
    }

    fun updateUser(user: User) {
        repository.updateUser(user)
    }

    fun searchUsers(query: String) {
        viewModelScope.launch {
            val users = repository.searchUsers(query)
            _usersLiveData.value = users
        }
    }

    fun getFriendRequests() {
        viewModelScope.launch {
            val requests = repository.getFriendRequests()
            _friendRequestsLiveData.value = requests
        }
    }

    fun sendFriendRequest(userId: String) {
        viewModelScope.launch {
            repository.sendFriendRequest(userId)
            updateUserStatus(userId, FriendStatus.REQUEST_SENT)
        }
    }

    fun acceptFriendRequest(userId: String) {
        viewModelScope.launch {
            repository.acceptFriendRequest(userId)
            updateUserStatus(userId, FriendStatus.FRIENDS)
        }
    }

    fun cancelFriendRequest(userId: String) {
        viewModelScope.launch {
            repository.cancelFriendRequest(userId)
            updateUserStatus(userId, FriendStatus.NONE)
        }
    }

    fun unfriend(userId: String) {
        viewModelScope.launch {
            repository.unfriend(userId)
            updateUserStatus(userId, FriendStatus.NONE)
        }
    }

    private fun updateUserStatus(userId: String, status: FriendStatus) {
        val updatedUsers = _usersLiveData.value?.map {
            if (it.userId == userId) it.copy(friendStatus = status) else it
        } ?: emptyList()
        _usersLiveData.value = updatedUsers
    }

}