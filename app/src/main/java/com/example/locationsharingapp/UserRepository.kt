package com.example.locationsharingapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserRepository {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val usersLiveData: MutableLiveData<List<User>> = MutableLiveData()
    private val friendsLiveData: MutableLiveData<List<User>> = MutableLiveData()
    private val friendRequestsLiveData: MutableLiveData<List<User>> = MutableLiveData()

    fun getUsers(): MutableLiveData<List<User>> {
        val currentUserUid = auth.currentUser?.uid
        firestore.collection("users")
            .get()
            .addOnSuccessListener { result ->
                val users = result.map { document ->
                    document.toObject(User::class.java)
                }.filter { it.userId != currentUserUid }
                usersLiveData.value = users
            }
        return usersLiveData
    }

    fun getFriends(): MutableLiveData<List<User>> {
        val currentUserUid = auth.currentUser?.uid ?: return friendsLiveData
        firestore.collection("users").document(currentUserUid)
            .get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)
                val friendsUids = user?.friends ?: emptyList()
                if (friendsUids.isNotEmpty()) {
                    fetchFriends(friendsUids)
                } else {
                    friendsLiveData.value = emptyList()
                }
            }
        return friendsLiveData
    }

    private fun fetchFriends(friendsUids: List<String>) {
        firestore.collection("users")
            .whereIn("uid", friendsUids)
            .get()
            .addOnSuccessListener { result ->
                val friends = result.map { it.toObject(User::class.java) }
                friendsLiveData.value = friends
            }
            .addOnFailureListener { e ->
                friendsLiveData.value = emptyList() // Or handle the error appropriately
            }
    }

    fun getFriendRequests(): MutableLiveData<List<User>> {
        val currentUserUid = auth.currentUser?.uid ?: return friendRequestsLiveData
        firestore.collection("users").document(currentUserUid)
            .get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)
                val friendRequestUids = user?.friendRequests ?: emptyList()
                if (friendRequestUids.isNotEmpty()) {
                    fetchFriendRequests(friendRequestUids)
                } else {
                    friendRequestsLiveData.value = emptyList()
                }
            }
        return friendRequestsLiveData
    }

    private fun fetchFriendRequests(friendRequestUids: List<String>) {
        firestore.collection("users")
            .whereIn("uid", friendRequestUids)
            .get()
            .addOnSuccessListener { result ->
                val requests = result.map { it.toObject(User::class.java) }
                friendRequestsLiveData.value = requests
            }
            .addOnFailureListener { e ->
                friendRequestsLiveData.value = emptyList() // Or handle the error appropriately
            }
    }

    fun updateUser(user: User) {
        firestore.collection("users").document(user.userId)
            .set(user)
    }

    fun addUser(user: User) {
        firestore.collection("users").document(user.userId)
            .set(user)
    }

    fun sendFriendRequest(targetUid: String) {
        val currentUserUid = auth.currentUser?.uid ?: return
        val currentUserDoc = firestore.collection("users").document(currentUserUid)
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(currentUserDoc)
            val currentRequests = snapshot.get("friendRequests") as? List<String> ?: emptyList<String>()
            if (!currentRequests.contains(targetUid)) {
                val updatedRequests = currentRequests + targetUid
                transaction.update(currentUserDoc, "friendRequests", updatedRequests)
            }
        }
    }

    fun acceptFriendRequest(senderUid: String) {
        val currentUserUid = auth.currentUser?.uid ?: return
        val currentUserDoc = firestore.collection("users").document(currentUserUid)
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(currentUserDoc)
            val currentRequests = snapshot.get("friendRequests") as? List<String> ?: emptyList<String>()
            if (currentRequests.contains(senderUid)) {
                val updatedRequests = currentRequests - senderUid
                val updatedFriends = (snapshot.get("friends") as? List<String> ?: emptyList<String>()) + senderUid
                transaction.update(currentUserDoc, "friendRequests", updatedRequests)
                transaction.update(currentUserDoc, "friends", updatedFriends)

                // Update sender's friends list as well
                val senderDoc = firestore.collection("users").document(senderUid)
                val senderSnapshot = transaction.get(senderDoc)
                val senderFriends = (senderSnapshot.get("friends") as? List<String> ?: emptyList<String>()) + currentUserUid
                transaction.update(senderDoc, "friends", senderFriends)
            }
        }
    }

    fun searchUsers(query: String): LiveData<List<User>> {
        val searchResults = MutableLiveData<List<User>>()
        firestore.collection("users")
            .whereEqualTo("name", query)
            .get()
            .addOnSuccessListener { result ->
                val users = result.map { it.toObject(User::class.java) }
                searchResults.value = users
            }
        return searchResults
    }
}
