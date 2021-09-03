package ua.pp.trushkovsky.MyKTGG

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
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
import com.trendyol.medusalib.navigator.Navigator


class BottomNavActivity : AppCompatActivity(), Navigator.NavigatorListener {

    lateinit var navigation: BottomNavigationView

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupView()
        verifyUserLoggedIn()
        checkUserGroup()
        setContentView(R.layout.activity_bottom_nav)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        navView.setOnNavigationItemReselectedListener {}
        navView.setupWithNavController(navController)
    }

    private fun setupView() {
        val win = window ?: return
        val context = applicationContext ?: return
        val decorView = win.decorView

        win.navigationBarColor = ContextCompat.getColor(context, R.color.appLightColor)
        win.statusBarColor = ContextCompat.getColor(context, R.color.appLightColor)

        when {
            Build.VERSION.SDK_INT >= 27 -> {
                decorView.systemUiVisibility = FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS or
                        View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR or
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
            Build.VERSION.SDK_INT in 23..26 -> {
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                win.setFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
                )
            }
            else -> {
                win.setFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                )
                win.setFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
                )
            }
        }
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
        Log.e("test","checkin userGroup")
        val userID = Firebase.auth.currentUser?.uid ?: return
        FirebaseDatabase.getInstance().reference
            .child("users")
            .child(userID)
            .child("public")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.e("test","data change userGroup")
                    if (snapshot.value == null) {
                        if (!hasOpenedDialogs(this@BottomNavActivity)) {
                            GroupChoose.display(supportFragmentManager)
                        }
                        return
                    }
                    val map = snapshot.value as Map<*, *>
                    val group = map["group"]
                    val isStudent = map["isStudent"]
                    if (group == null || isStudent == null) {
                        if (!hasOpenedDialogs(this@BottomNavActivity)) {
                            GroupChoose.display(supportFragmentManager)
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun hasOpenedDialogs(activity: FragmentActivity): Boolean {
        val fragments: List<Fragment> =
            activity.supportFragmentManager.fragments
        for (fragment in fragments) {
            if (fragment is DialogFragment) {
                return true
            }
        }
        return false
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