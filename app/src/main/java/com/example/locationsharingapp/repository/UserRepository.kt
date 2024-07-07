package com.example.locationsharingapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository(private val firestore: FirebaseFirestore) {

    fun addUser(user: User) {
        firestore.collection("users").document(user.userId)
            .set(user)
    }

    private val auth = FirebaseAuth.getInstance()
    private val usersLiveData = MutableLiveData<List<User>>()
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

    fun updateUser(user: User) {
        firestore.collection("users").document(user.userId)
            .set(user)
    }


    private val currentUser get() = FirebaseAuth.getInstance().currentUser?.uid

    suspend fun searchUsers(query: String): List<User> {
        val result = firestore.collection("users")
            .whereEqualTo("name", query)
            .get()
            .await()
        return result.map { doc -> doc.toObject(User::class.java) }
    }

    suspend fun getFriendRequests(): List<User> {
        val result = firestore.collection("users")
            .whereArrayContains("friendRequests", currentUser!!)
            .get()
            .await()
        return result.map { doc -> doc.toObject(User::class.java) }
    }

    suspend fun sendFriendRequest(userId: String) {
        val currentUserRef = firestore.collection("users").document(currentUser!!)
        val userRef = firestore.collection("users").document(userId)

        firestore.runBatch { batch ->
            batch.update(currentUserRef, "friendRequests", FieldValue.arrayUnion(userId))
            batch.update(userRef, "friendRequests", FieldValue.arrayUnion(currentUser))
        }.await()
    }

    suspend fun acceptFriendRequest(userId: String) {
        val currentUserRef = firestore.collection("users").document(currentUser!!)
        val userRef = firestore.collection("users").document(userId)

        firestore.runBatch { batch ->
            batch.update(currentUserRef, "friends", FieldValue.arrayUnion(userId))
            batch.update(userRef, "friends", FieldValue.arrayUnion(currentUser))
            batch.update(currentUserRef, "friendRequests", FieldValue.arrayRemove(userId))
            batch.update(userRef, "friendRequests", FieldValue.arrayRemove(currentUser))
        }.await()
    }

    suspend fun cancelFriendRequest(userId: String) {
        val currentUserRef = firestore.collection("users").document(currentUser!!)
        val userRef = firestore.collection("users").document(userId)

        firestore.runBatch { batch ->
            batch.update(currentUserRef, "friendRequests", FieldValue.arrayRemove(userId))
            batch.update(userRef, "friendRequests", FieldValue.arrayRemove(currentUser))
        }.await()
    }

    suspend fun unfriend(userId: String) {
        val currentUserRef = firestore.collection("users").document(currentUser!!)
        val userRef = firestore.collection("users").document(userId)

        firestore.runBatch { batch ->
            batch.update(currentUserRef, "friends", FieldValue.arrayRemove(userId))
            batch.update(userRef, "friends", FieldValue.arrayRemove(currentUser))
        }.await()
    }
}
