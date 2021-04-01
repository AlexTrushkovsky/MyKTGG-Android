package ua.pp.trushkovsky.MyKTGG

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.trendyol.medusalib.navigator.Navigator


class BottomNavActivity : AppCompatActivity(), Navigator.NavigatorListener {

    lateinit var navigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        verifyUserLoggedIn()
        checkUserGroup()
        //subscribeToNews()
        setContentView(R.layout.activity_bottom_nav)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        navView.setOnNavigationItemReselectedListener {}
        navView.setupWithNavController(navController)
    }

    private fun verifyUserLoggedIn() {
        Log.d("BottomNav", "verifying login")
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null) {
            Log.d("BottomNav", "need to relogin")
            val intent = Intent(this, RegActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } else {
            Log.d("BottomNav", "user is already logged in")
        }
    }

    private fun checkUserGroup() {
        val userID = Firebase.auth.currentUser?.uid ?: return
        FirebaseDatabase.getInstance().reference
            .child("users")
            .child(userID)
            .child("public")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.value == null) { return }
                    val map = snapshot.value as Map<String, Any>
                    val group = map["group"]
                    val isStudent = map["isStudent"]
                    if (group == null || isStudent == null) {
                        GroupChoose.display(supportFragmentManager)
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun subscribeToNews() {
        Firebase.messaging.subscribeToTopic("testChannel01032021").addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.i("messaging", "Subscribed to news")
                    Toast.makeText(baseContext, "Subscribed to news", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("messaging", "Don`t Subscribed to news: ${it.exception}")
                    Toast.makeText(baseContext, "Don`t Subscribed to news: ${it.exception}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onTabChanged(tabIndex: Int) {
        when (tabIndex) {
            0 -> {
                if (navigation.selectedItemId != R.id.navigation_home) navigation.selectedItemId = R.id.navigation_home
            }
            1 -> {
                if (navigation.selectedItemId != R.id.navigation_news) navigation.selectedItemId = R.id.navigation_news
            }
            2 -> {
                if (navigation.selectedItemId != R.id.navigation_timetable) navigation.selectedItemId = R.id.navigation_timetable
            }
            3 -> {
                if (navigation.selectedItemId != R.id.navigation_settings) navigation.selectedItemId = R.id.navigation_settings
            }
        }
    }
}