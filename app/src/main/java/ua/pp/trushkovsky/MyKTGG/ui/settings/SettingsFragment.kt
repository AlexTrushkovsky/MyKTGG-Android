package ua.pp.trushkovsky.MyKTGG.ui.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_settings.*
import ua.pp.trushkovsky.MyKTGG.R
import ua.pp.trushkovsky.MyKTGG.ui.news.d.BASE_NEWS_URL
import java.io.ByteArrayOutputStream


@Suppress("DEPRECATION")
class SettingsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_settings, container, false)

        return root
    }


    override fun onStart() {
        super.onStart()
        fillDataFromDefaults()
        updateUserValues()
        userInfo.setOnClickListener {
            val navController = NavHostFragment.findNavController(this)
            navController.navigate(R.id.action_navigation_settings_to_navigation_account_settings2)
        }
        ktgg_pay_button.setOnClickListener {
            val intent = Intent(Intent(Intent.ACTION_VIEW))
            intent.data = Uri.parse("https://next.privat24.ua/payments/form/%7B%22token%22:%22e287b0fa-9f54-487f-9ed3-cb4f67e9a2cb%22%7D")
            context?.let { it1 -> ContextCompat.startActivity(it1, intent, null) }
        }
        goto_ktgg_website_button.setOnClickListener {
            val intent = Intent(Intent(Intent.ACTION_VIEW))
            intent.data = Uri.parse("https://ktgg.kiev.ua")
            context?.let { it1 -> ContextCompat.startActivity(it1, intent, null) }
        }
        report_error_button.setOnClickListener {
            val intent = Intent(Intent(Intent.ACTION_VIEW))
            intent.data = Uri.parse("https://t.me/esen1n25")
            context?.let { it1 -> ContextCompat.startActivity(it1, intent, null) }
        }
        donate_button.setOnClickListener {
            val intent = Intent(Intent(Intent.ACTION_VIEW))
            intent.data = Uri.parse("https://t.me/esen1n25")
            context?.let { it1 -> ContextCompat.startActivity(it1, intent, null) }
        }
        mark_googleplay_button.setOnClickListener {
            val uri = Uri.parse("market://details?id=myktgg")
            val myAppLinkToMarket = Intent(Intent.ACTION_VIEW, uri)
            try {
                startActivity(myAppLinkToMarket)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, "Impossible to find an application for the market", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun fillDataFromDefaults() {
        val name = getStringFromSharedPreferences("name", activity)
        val group = getStringFromSharedPreferences("group", activity)
        val subgroup = getIntFromSharedPreferences("subgroup", activity)
        val image = getStringFromSharedPreferences("userImage", activity)
        val isStudent = getBoolFromSharedPreferences("isStudent", activity)
        if (name != "" && userName != null) {
            userName.text = name
        }
        if (group != "" && userGroup != null) {
            userGroup.text = group
        }
        if (subgroup != -1 && userSubGroup != null) {
            userSubGroup.text = if (subgroup == 0) "1 підгрупа" else "2 підгрупа"
        }
        if (userImage != null && image != "") {
            val image = decodeBase64(image)
            if (image != null) userImage.setImageBitmap(image)
        }
        userSubGroup.isVisible = !isStudent
    }

    fun encodeTobase64(image: Bitmap): String? {
        val baos = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val b: ByteArray = baos.toByteArray()
        val imageEncoded: String = Base64.encodeToString(b, Base64.DEFAULT)
        Log.d("Image Log:", imageEncoded)
        return imageEncoded
    }

    fun decodeBase64(input: String?): Bitmap? {
        val decodedByte = Base64.decode(input, 0)
        return BitmapFactory
            .decodeByteArray(decodedByte, 0, decodedByte.size)
    }

    fun updateUserValues() {
        var userID = Firebase.auth.currentUser?.uid ?: return
        FirebaseDatabase.getInstance().reference
            .child("users")
            .child(userID)
            .child("public")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.value == null) { return }
                    val map = snapshot.value as Map<String, Any>
                    Log.e("Settings", "$map")
                    val url = map["avatarUrl"].toString()
                    val name = map["name"].toString()
                    val subgroup = map["subgroup"].toString().toInt()
                    val group = map["group"].toString()
                    val isStudent = map["isStudent"].toString().toBoolean()
                    saveBoolToSharedPreferences("isStudent", isStudent, activity)

                    if (userSubGroup != null) {
                        userSubGroup.isVisible = isStudent
                    }

                    Log.e("Settings", url)
                    if (userImage != null) {
                        if (url != "null") {
                            Glide.with(requireContext()).load(url).into(userImage)
                        } else {
                            userImage.setImageResource(R.drawable.avatar_placeholder)
                        }
                    }
                    if (userName != null) {
                        userName.text = name
                        saveStringToSharedPreferences("name", name, activity)
                    }
                    if (userGroup != null) {
                        userGroup.text = group
                        saveStringToSharedPreferences("group", group, activity)
                    }
                    if (userSubGroup != null && isStudent) {
                        userSubGroup.text = if (subgroup == 0) "1 підгрупа" else "2 підгрупа"
                        saveIntToSharedPreferences("subgroup", subgroup, activity)
                    }
                    if (userImage != null) {
                        userImage.invalidate()
                        val bitmap = userImage.drawable ?: return
                        val imageString = encodeTobase64(bitmap.toBitmap())
                        if (imageString != null) {
                            saveStringToSharedPreferences("userImage", imageString, activity)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Settings", "$error")
                }
            })
    }
}
