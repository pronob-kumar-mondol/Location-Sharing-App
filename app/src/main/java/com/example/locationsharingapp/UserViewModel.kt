package com.example.locationsharingapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class UserViewModel: ViewModel() {

    private val userRepository = UserRepository()
    private var usersLiveData: MutableLiveData<List<User>>?=null
    private var friendsLiveData: MutableLiveData<List<User>> = MutableLiveData()
    private var searchResultsLiveData: MutableLiveData<List<User>> = MutableLiveData()


    init {
        fetchFriends()
    }

    fun getUsers(): LiveData<List<User>> {
        if (usersLiveData == null) {
            usersLiveData = userRepository.getUsers()
        }
        return usersLiveData!!
    }

    fun getFriends(): LiveData<List<User>> = friendsLiveData

    fun getSearchResults(): LiveData<List<User>> = searchResultsLiveData

    fun updateUser(user: User) {
        userRepository.updateUser(user)
    }

    fun addUser(user: User) {
        userRepository.addUser(user)
    }

    fun sendFriendRequest(targetUid: String) {
        userRepository.sendFriendRequest(targetUid)
    }

    fun acceptFriendRequest(senderUid: String) {
        userRepository.acceptFriendRequest(senderUid)
    }



    fun searchUsers(query: String) {
        userRepository.searchUsers(query).observeForever { users ->
            searchResultsLiveData.value = users
        }
    }

    private fun fetchFriends() {
        userRepository.getFriends().observeForever { friends ->
            friendsLiveData.value = friends
        }
    }
}