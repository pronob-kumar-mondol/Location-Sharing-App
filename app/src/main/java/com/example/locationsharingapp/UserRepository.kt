package com.example.locationsharingapp

import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore

class UserRepository {

    private val db=FirebaseFirestore.getInstance()

    fun getUsers():MutableLiveData<List<User>>{
        val userLiveData=MutableLiveData<List<User>>()
        db.collection("users").get().addOnCompleteListener(){
            if (it.isSuccessful){
                val userList=it.result!!.toObjects(User::class.java)
                userLiveData.value=userList
            }
        }
        return userLiveData
    }

    fun updateUser(user: User){
        db.collection("users").document(user.userId).set(user)
    }

    fun addUser(user: User){
        db.collection("users").document(user.userId).set(user)
    }


}