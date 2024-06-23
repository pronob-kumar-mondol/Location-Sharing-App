package com.example.locationsharingapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FriendListActivity : AppCompatActivity() {

    private val userViewModel: UserViewModel by viewModels()
    private lateinit var adapter: FriendListAdapter
    lateinit var fab:FloatingActionButton
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var firestore: FirebaseFirestore
    private lateinit var fAuth: FirebaseAuth
    private lateinit var searchView: SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_list)

        fab=findViewById(R.id.fab)
        searchView = findViewById(R.id.search_view)

        val recycler_view = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recycler_view)
        adapter = FriendListAdapter(
            emptyList(),
            onSendFriendRequest = { user ->
                userViewModel.sendFriendRequest(user.userId)
                notifyUser(user.userId, "Friend request sent")
            },
            onAcceptFriendRequest = { user ->
                userViewModel.acceptFriendRequest(user.userId)
                notifyUser(user.userId, "Friend request accepted")
            },
            onViewLocation = { user ->
                // Handle view location action
                val intent = Intent(this, GoogleMapActivity::class.java)
                intent.putExtra("user_uid", user.userId)
                startActivity(intent)
            }
        )
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.adapter = adapter

        // Observe users LiveData from ViewModel
        userViewModel.getUsers().observe(this) { users ->
            adapter.setUsers(users)
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    userViewModel.searchUsers(it)
                    userViewModel.getSearchResults().observe(this@FriendListActivity) { users ->
                        adapter.setUsers(users)
                    }
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        firestore = FirebaseFirestore.getInstance()
        fAuth = FirebaseAuth.getInstance()

        fab.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE)
            } else {
                getLastLocation()
            }
        }


    }

    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener(this, OnSuccessListener { location: Location? ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    saveLocationToFirestore(latitude, longitude)
                    startActivity(Intent(this, GoogleMapActivity::class.java))
                }
            })
    }

    private fun saveLocationToFirestore(latitude: Double, longitude: Double) {
        val locationData = hashMapOf(
            "latitude" to latitude,
            "longitude" to longitude
        )
        firestore.collection("users").document(fAuth.currentUser?.uid.toString())
            .update(locationData as Map<String, Any>)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(this, "Location saved to Firestore", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save location to Firestore: $e", Toast.LENGTH_SHORT).show()
            }
    }

    private fun notifyUser(userId: String, message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation()
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}