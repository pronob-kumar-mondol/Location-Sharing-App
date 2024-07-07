package com.example.locationsharingapp

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.runtime.Composable
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.lemillion.android.fab.FabItem
import com.lemillion.android.fab.MultiFloatingActionButton
import de.hdodenhof.circleimageview.CircleImageView

class FriendListActivity : AppCompatActivity() {

    private val viewModel: UserViewModel by viewModels()
    private lateinit var userAdapter: FriendListAdapter
    lateinit var fab:FloatingActionButton
    lateinit var fab_main:FloatingActionButton
    lateinit var fab1:FloatingActionButton
    lateinit var fab2:FloatingActionButton
    lateinit var fab3:FloatingActionButton
    lateinit var fab4:FloatingActionButton
    private lateinit var recyclerView: RecyclerView
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var firestore: FirebaseFirestore
    private lateinit var fAuth: FirebaseAuth
    private lateinit var searchView: SearchView
    private lateinit var circleImg: CircleImageView
    private lateinit var txt1: TextView
    private lateinit var txt2: TextView
    private var isFabOpen = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_list)

        fab=findViewById(R.id.fab)
        fab_main=findViewById(R.id.fab_main)
        fab1=findViewById(R.id.fab1)
        fab2=findViewById(R.id.fab2)
        fab3=findViewById(R.id.fab3)
        fab4=findViewById(R.id.fab4)
        searchView = findViewById(R.id.search_view)
        recyclerView = findViewById(R.id.recycler_view)
        circleImg = findViewById(R.id.circleImageView)
        txt1 = findViewById(R.id.textView3)
        txt2 = findViewById(R.id.xtt)


        userAdapter = FriendListAdapter(
            mutableListOf(),
            onSendRequest = { user -> viewModel.sendFriendRequest(user.userId) },
            onCancelRequest = { user -> viewModel.cancelFriendRequest(user.userId) },
            onAcceptRequest = { user -> viewModel.acceptFriendRequest(user.userId) },
            onUnfriend = { user -> viewModel.unfriend(user.userId) }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = userAdapter



        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { viewModel.searchUsers(it) }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

        searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                // Hide views when SearchView is focused
                circleImg.visibility = View.GONE
                txt1.visibility = View.GONE
                txt2.visibility = View.GONE
            } else {
                // Show views when SearchView loses focus
                circleImg.visibility = View.VISIBLE
                txt1.visibility = View.VISIBLE
                txt2.visibility = View.VISIBLE
            }
        }

        viewModel.usersLiveData.observe(this) { users ->
            userAdapter.updateUsers(users)
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        firestore = FirebaseFirestore.getInstance()
        fAuth = FirebaseAuth.getInstance()

        fab.setOnClickListener {
            Log.d("FriendListActivity", "FAB Clicked")
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.d("FriendListActivity", "Requesting permissions")
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE)
            } else {
                Log.d("FriendListActivity", "Permissions granted, getting last location")
                getLastLocation()
            }
        }



        fab_main.setOnClickListener {
            if (isFabOpen) {
                closeFabMenu()
            } else {
                openFabMenu()
            }
            toggleFab()
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
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener(this) { location: Location? ->
                if (location != null) {
                    Log.d("FriendListActivity", "Location: ${location.latitude}, ${location.longitude}")
                    val latitude = location.latitude
                    val longitude = location.longitude
                    saveLocationToFirestore(latitude, longitude)
                    // Launch GoogleMapActivity after getting the location
                    startActivity(Intent(this, GoogleMapActivity::class.java))
                }else{
                    Log.d("FriendListActivity", "Location is null")
                }
            }
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
                Log.d("FriendListActivity", "Permissions granted from request")
                getLastLocation()
            } else {
                Log.d("FriendListActivity", "Permissions denied")
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }




    private fun openFabMenu() {
        isFabOpen = true

        fab1.visibility = View.VISIBLE
        fab2.visibility = View.VISIBLE
        fab3.visibility = View.VISIBLE
        fab4.visibility = View.VISIBLE

        val fab1Animator = ObjectAnimator.ofPropertyValuesHolder(
            fab1,
            PropertyValuesHolder.ofFloat("translationY", -5f)
        ).apply {
            duration = 300
            interpolator = AccelerateDecelerateInterpolator()
        }
        val fab2Animator = ObjectAnimator.ofPropertyValuesHolder(
            fab2,
            PropertyValuesHolder.ofFloat("translationY", -10f)
        ).apply {
            duration = 300
            interpolator = AccelerateDecelerateInterpolator()
        }
        val fab3Animator = ObjectAnimator.ofPropertyValuesHolder(
            fab3,
            PropertyValuesHolder.ofFloat("translationY", -15f)
        ).apply {
            duration = 300
            interpolator = AccelerateDecelerateInterpolator()
        }
        val fab4Animator = ObjectAnimator.ofPropertyValuesHolder(
            fab4,
            PropertyValuesHolder.ofFloat("translationY", -20f)
        ).apply {
            duration = 300
            interpolator = AccelerateDecelerateInterpolator()
        }

        fab1Animator.start()
        fab2Animator.start()
        fab3Animator.start()
        fab4Animator.start()
    }

    private fun closeFabMenu() {
        isFabOpen = false

        val fab1Animator = ObjectAnimator.ofPropertyValuesHolder(
            fab1,
            PropertyValuesHolder.ofFloat("translationY", 0f)
        ).apply {
            duration = 300
            interpolator = AccelerateDecelerateInterpolator()
        }
        val fab2Animator = ObjectAnimator.ofPropertyValuesHolder(
            fab2,
            PropertyValuesHolder.ofFloat("translationY", 0f)
        ).apply {
            duration = 300
            interpolator = AccelerateDecelerateInterpolator()
        }
        val fab3Animator = ObjectAnimator.ofPropertyValuesHolder(
            fab3,
            PropertyValuesHolder.ofFloat("translationY", 0f)
        ).apply {
            duration = 300
            interpolator = AccelerateDecelerateInterpolator()
        }
        val fab4Animator = ObjectAnimator.ofPropertyValuesHolder(
            fab4,
            PropertyValuesHolder.ofFloat("translationY", 0f)
        ).apply {
            duration = 300
            interpolator = AccelerateDecelerateInterpolator()
        }

        fab1Animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                if (!isFabOpen) {
                    fab1.visibility = View.GONE
                    fab2.visibility = View.GONE
                    fab3.visibility = View.GONE
                    fab4.visibility = View.GONE
                }
            }
        })

        fab1Animator.start()
        fab2Animator.start()
        fab3Animator.start()
        fab4Animator.start()
    }

    private fun toggleFab() {
        val rotation = if (isFabOpen) 45f else 0f
        val colorResId = if (isFabOpen) R.color.red else R.color.blue

        Log.d("MainActivity", "toggleFab: isFabOpen = $isFabOpen, colorResId = $colorResId")

        ObjectAnimator.ofFloat(fab_main, "rotation", rotation).apply {
            duration = 300
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }

        fab_main.backgroundTintList = ContextCompat.getColorStateList(this, colorResId)

        // Directly apply color for testing
        val color = ContextCompat.getColor(this, colorResId)
        fab_main.setBackgroundColor(color)

    }


}