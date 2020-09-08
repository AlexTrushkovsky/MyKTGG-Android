package ua.pp.trushkovsky.ktggauth.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import ua.pp.trushkovsky.ktggauth.BottomNavActivity
import ua.pp.trushkovsky.ktggauth.R
import ua.pp.trushkovsky.ktggauth.RegActivity

class HomeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        root.signOut.setOnClickListener {
            Firebase.auth.signOut()
            Log.d("BottomNav", "verifying login")
            val uid = FirebaseAuth.getInstance().uid
            if (uid == null) {
                Log.d("BottomNav", "need to relogin")
                val intent = Intent (getActivity(), RegActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } else {
                Log.d("BottomNav", "user is already logged in")
            }
        }
        return root
    }
}