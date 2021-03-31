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
import com.facebook.FacebookSdk.getApplicationContext
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import kotlinx.android.synthetic.main.fragment_settings.*
import ua.pp.trushkovsky.MyKTGG.R
import ua.pp.trushkovsky.MyKTGG.R.drawable.def_green
import ua.pp.trushkovsky.MyKTGG.R.drawable.def_red
import java.io.ByteArrayOutputStream


@Suppress("DEPRECATION")
class SettingsFragment : Fragment() {

    var isStudent = true
        set(value) {
            field = value
            checkVerification()
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }


    fun subscribeToNews(bool: Boolean) {
        if (bool) {
            Firebase.messaging.subscribeToTopic("news").addOnCompleteListener {
                if (it.isSuccessful) {
                    saveBoolToSharedPreferences("isSubscribedToNews", bool, context)
                    Toast.makeText(context, "Тепер вам будуть надходити повідомлення про новини", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Виникла помилка ${it.exception?.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Firebase.messaging.unsubscribeFromTopic("news").addOnCompleteListener {
                if (it.isSuccessful) {
                    saveBoolToSharedPreferences("isSubscribedToNews", bool, context)
                    Toast.makeText(context, "Вам більше не будуть надходити повідомлення про новини", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Don`t Subscribed to news: ${it.exception?.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun subscribeToChanges() {
        val group = getStringFromSharedPreferences("group", context)
        val transliterated = transliterate(group.trim())
            Firebase.messaging.subscribeToTopic("changesOf$transliterated").addOnCompleteListener {
                if (it.isSuccessful) {
                    saveStringToSharedPreferences("changesPush", group, context)
                    val toast = Toast.makeText(getApplicationContext(), "Тепер вам будуть надходити повідомлення про заміни $group", Toast.LENGTH_SHORT) ?: return@addOnCompleteListener
                    toast.show()
                } else {
                    val toast = Toast.makeText(getApplicationContext(), "Виникла помилка ${it.exception?.localizedMessage}", Toast.LENGTH_SHORT) ?: return@addOnCompleteListener
                    toast.show()
                }
            }
    }

    fun unsubscribeFromChanges() {
        val group = getStringFromSharedPreferences("changesPush", context)
        if (group != "") {
            val transliterated = transliterate(group.trim())
            Firebase.messaging.unsubscribeFromTopic("changesOf$transliterated").addOnCompleteListener {
                if (it.isSuccessful) {
                    saveStringToSharedPreferences("changesPush", "", context)
                    Toast.makeText(context, "Вам більше не будуть надходити повідомлення про заміни $group", Toast.LENGTH_SHORT).show()
                    changeNotificationsSwitch?.isChecked = false
                } else {
                    Toast.makeText(context, "Виникла помилка ${it.exception?.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun subscribeToGroupPushes() {
        val group = getStringFromSharedPreferences("group", context)
        val transliterated = transliterate(group.trim())
        Firebase.messaging.subscribeToTopic(transliterated).addOnCompleteListener {
            if (it.isSuccessful) saveStringToSharedPreferences("groupOfPushes", group, context)
        }
    }
    fun unsubscribeFromGroupPushes() {
        val group = getStringFromSharedPreferences("groupOfPushes", context)
        if (group != "") {
            val transliterated = transliterate(group.trim())
            Firebase.messaging.unsubscribeFromTopic("changesOf$transliterated").addOnCompleteListener {
                if (it.isSuccessful) saveStringToSharedPreferences("groupOfPushes", "", context)
            }
        }
    }

    private fun transliterate(nonLatin: String): String {
        val dic = mapOf('а' to 'a', 'б' to 'b','в' to 'v',
            'г' to 'g', 'д' to 'd', 'е' to 'e', 'ё' to 'y','ж' to 'z',
            'з' to 'z', 'и' to 'i', 'і' to 'i', 'ї' to 'i', 'й' to 'y',
            'к' to 'k', 'л' to 'l', 'м' to 'm', 'н' to 'n', 'о' to 'o',
            'п' to 'p', 'р' to 'r', 'с' to 's', 'т' to 't', 'у' to 'u',
            'ф' to 'f', 'х' to 'h', 'ц' to 'c', 'ч' to 'c', 'ш' to 's',
            'щ' to 's', 'ы' to 'i', 'э' to 'e', 'є' to 'e', 'ю' to 'u',
            'я' to 'a', 'ь' to ' ', '(' to ' ', ')' to ' ')
        var transliterated = ""
        for (i in nonLatin) {
            val char = dic[i.toLowerCase()]
            transliterated += if (char != null) {
                dic[i.toLowerCase()]
            } else {
                i
            }
        }
        return transliterated.filter { !it.isWhitespace() }
    }

    override fun onStart() {
        super.onStart()
        fillDataFromDefaults()
        updateUserValues()
        checkVerification()

        if (getBoolFromSharedPreferences("isSubscribedToNews", context)) newsNotifiactionsSwitch.isChecked = true
        newsNotifiactionsSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            subscribeToNews(isChecked)
        }
        if (getStringFromSharedPreferences("changesPush", context) != "") changeNotificationsSwitch.isChecked = true
        changeNotificationsSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                subscribeToChanges()
            } else {
                unsubscribeFromChanges()
            }
        }
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
        this.isStudent = isStudent
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
        if (userSubGroup != null) {
            if (isStudent) {
                userSubGroup.text =
                    if (subgroup == 0) "1 підгрупа" else "2 підгрупа"
                saveIntToSharedPreferences("subgroup", subgroup, activity)
            } else {
                userSubGroup.text = "Викладач"
            }
        }
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

    fun checkVerification() {
        if (isStudent) {
            settings_verified_image?.isVisible = false
            no_verification_banner?.isVisible = false
        } else {
            var userID = Firebase.auth.currentUser?.uid ?: return
            FirebaseDatabase.getInstance().reference
                .child("users")
                .child(userID)
                .child("secure")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.value == null) { return }
                        val map = snapshot.value as Map<String, Any>
                        Log.e("Settings", "$map")
                        if (map["verified"] != null) {
                            val group = getStringFromSharedPreferences("group", activity)
                            if (group == map["verified"].toString()) {
                                settings_verified_image?.setImageResource(def_green)
                                no_verification_banner?.isVisible = false
                            } else {
                                settings_verified_image?.setImageResource(def_red)
                                no_verification_banner?.isVisible = true
                            }
                        } else {
                            settings_verified_image.setImageResource(def_red)
                            no_verification_banner?.isVisible = true
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
            settings_verified_image?.isVisible = true
        }
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
                    if (map["avatarUrl"] != null) {
                        val url = map["avatarUrl"].toString()
                        if (userImage != null) {
                            if (url != "null") {
                                Glide.with(requireContext()).load(url).into(userImage)
                            } else {
                                userImage.setImageResource(R.drawable.avatar_placeholder)
                            }
                        }
                    }
                    if (map["name"] != null) {
                        val name = map["name"].toString()
                        if (userName != null) {
                            userName.text = name
                            saveStringToSharedPreferences("name", name, activity)
                        }
                    }
                    if (map["isStudent"] != null) {
                        val isStudent = map["isStudent"].toString().toBoolean()
                        this@SettingsFragment.isStudent = isStudent
                        saveBoolToSharedPreferences("isStudent", isStudent, activity)
                    }
                    if (map["subgroup"] != null) {

                        val subgroup = map["subgroup"].toString().toInt()
                        if (userSubGroup != null) {
                            if (isStudent) {
                                userSubGroup.text =
                                    if (subgroup == 0) "1 підгрупа" else "2 підгрупа"
                                saveIntToSharedPreferences("subgroup", subgroup, activity)
                            } else {
                                userSubGroup.text = "Викладач"
                            }
                        }
                    }
                    if (map["group"] != null) {
                        val group = map["group"].toString()
                        if (userGroup != null) {
                            userGroup.text = group
                            if (group != getStringFromSharedPreferences("group", context)) {
                                unsubscribeFromChanges()
                                unsubscribeFromGroupPushes()
                                saveStringToSharedPreferences("group", group, activity)
                                subscribeToGroupPushes()
                                subscribeToChanges()
                            }
                        }
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
