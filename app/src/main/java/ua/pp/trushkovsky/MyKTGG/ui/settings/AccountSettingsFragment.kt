package ua.pp.trushkovsky.MyKTGG.ui.settings

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
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
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.fragment_account_settings.*
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import ua.pp.trushkovsky.MyKTGG.R
import ua.pp.trushkovsky.MyKTGG.RegActivity
import ua.pp.trushkovsky.MyKTGG.helpers.getBoolFromSharedPreferences
import ua.pp.trushkovsky.MyKTGG.helpers.getIntFromSharedPreferences
import ua.pp.trushkovsky.MyKTGG.helpers.getStringFromSharedPreferences
import ua.pp.trushkovsky.MyKTGG.helpers.saveStringToSharedPreferences
import java.io.ByteArrayOutputStream


@Suppress("DEPRECATION")
class AccountSettingsFragment : Fragment() {

    var imageURL = Any()
    var avatarIsChanged = false
    var isStudent = true
        set(value) {
            field = value
            if (account_first_subgroup != null && account_second_subgroup != null) {
                account_second_subgroup.isVisible = value
                account_first_subgroup.isVisible = value
            }
            if (account_changeUserType_button != null) {
                if (value) {
                    account_changeUserType_button.text = "Обрати викладача"
                } else {
                    account_changeUserType_button.text = "Обрати групу"
                }
            }
            setupPickerView()
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_account_settings, container, false)
    }

    private fun setupPickerView() {
        groupPicker?.isVisible = false
        account_group_progress?.isVisible = true
        shadow_view?.isVisible = true
        segmentedControl?.isVisible = false
        CoroutineScope(Dispatchers.IO).async {
            val network = GroupNetworkController()
            val itemList = network.fetchData(isStudent)
            withContext(Dispatchers.Main) {
                with(groupPicker) {
                    items = itemList
                    setSelectedItem(items[0])
                    val currentGroup =
                        getStringFromSharedPreferences(
                            "group",
                            context
                        )
                    for ((index, group) in items.withIndex()) {
                        if (group.title == currentGroup) {
                            setSelectedItem(items[index])
                        }
                    }
                    segmentedControl?.isVisible = true
                    groupPicker?.isVisible = true
                    account_group_progress?.isVisible = false
                    shadow_view?.isVisible = false
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isStudent =
            getBoolFromSharedPreferences(
                "isStudent",
                this.activity
            )
        setupPickerView()
        fillDataFromDefaults()
    }

    override fun onStart() {
        super.onStart()
        updateUserValues()
        logOutButton.setOnClickListener {
            logOut()
        }
        showUIDbutton.setOnClickListener {
            var userID = Firebase.auth.currentUser?.uid
            if (userID != null) {
                uidTextField.setText(userID)
                uidTextField.isVisible = true
                showUIDbutton.isVisible = false
            }
        }
        account_back_button.setOnClickListener {
            activity?.onBackPressed()
        }
        account_save_button.setOnClickListener {
            doneButtonAction()
        }
        accountUserImage.setOnClickListener {
            ImagePicker.with(this)
                .compress(1024)//Final image size will be less than 1 MB(Optional)
                .crop(400F, 400F)
                .maxResultSize(400, 400)	//Final image resolution will be less than 1080 x 1080(Optional)
                .start { resultCode, data ->
                    if (resultCode == Activity.RESULT_OK) {
                        val fileUri = data?.data
                        accountUserImage.setImageURI(fileUri)
                        this.avatarIsChanged = true
                        if (fileUri != null) {
                            this.imageURL = fileUri
                        }
                    }
                }
        }
        account_changeUserType_button.setOnClickListener {
            isStudent = !isStudent
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            val fileUri = data?.data
            accountUserImage.setImageURI(fileUri)
        }
    }

    fun doneButtonAction() {
        val group = groupPicker.selectedItem?.title
        if (group != null) {
            updateFirebaseValue("group", group)
        }
        var subgroup = 0
        if (account_first_subgroup != null && account_second_subgroup != null) {
            if (account_first_subgroup.isChecked) {
                subgroup = 0
            } else if (account_second_subgroup.isChecked) {
                subgroup = 1
            }
        }
        updateFirebaseValue("subgroup", subgroup)
        updateFirebaseValue("isStudent", isStudent)
        val name = editTextTextPersonName?.text.toString()
        if (name != "") {
            updateFirebaseValue("name", name)
        }
        if (avatarIsChanged) {
            if (accountUserImage != null) {
                accountUserImage.invalidate()
                val bitmap = accountUserImage.drawable ?: return
                val imageString = encodeTobase64(bitmap.toBitmap())
                if (imageString != null) {
                    saveStringToSharedPreferences(
                        "userImage",
                        imageString,
                        this.activity
                    )
                }
            }
            uploadImage()
        }
        activity?.onBackPressed()
    }

    @Suppress("DEPRECATION")
    private fun uploadImage() {
            val image = imageURL as? Uri ?: return
            var userID = Firebase.auth.currentUser?.uid ?: return
            val progressDialog = ProgressDialog(context)
            progressDialog.setTitle("Завантаження...")
            progressDialog.show()
            val ref = Firebase.storage.reference.child("avatars").child(userID)
            ref.putFile(image)
                .addOnSuccessListener {
                    progressDialog.dismiss()
                    ref.downloadUrl.addOnSuccessListener {
                        updateFirebaseValue("avatarUrl", it.toString())
                    }

                }
                .addOnFailureListener {
                    progressDialog.dismiss()
                }
                .addOnProgressListener { taskSnapshot ->
                    val progress: Double = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
                    progressDialog.setMessage("Завантаження " + progress.toInt() + "%")
                }
    }

    private fun updateFirebaseValue(key: String, value: Any) {
        var userID = Firebase.auth.currentUser?.uid ?: return
        FirebaseDatabase.getInstance().reference
            .child("users")
            .child(userID)
            .child("public")
            .child(key).setValue(value)
    }

    private fun logOut() {
        Firebase.auth.signOut()
        Log.d("BottomNav", "verifying login")
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null) {
            Log.d("BottomNav", "need to relogin")
            val settings = activity?.getSharedPreferences("default", Context.MODE_PRIVATE)
            settings?.edit()?.clear()?.apply()
            val intent = Intent (activity, RegActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } else {
            Log.d("BottomNav", "user is already logged in")
        }
    }

    private fun updateUserValues() {
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
                    var subgroup: Int? = null
                    if (map["subgroup"] != null) {
                        subgroup = map["subgroup"].toString().toInt()
                    }
                    val group = map["group"].toString()
                    val isstudent = map["isStudent"].toString().toBoolean()

                    if (isstudent != isStudent) {
                        isStudent = isStudent
                    }

                    Log.e("AccountSettings", "$url")
                    if (accountUserImage != null) {
                        if (url != "null") {
                            Glide.with(requireContext()).load(url).into(accountUserImage)
                        } else {
                            accountUserImage.setImageResource(R.drawable.avatar_placeholder)
                        }
                    }
                    if (editTextTextPersonName != null) {
                        editTextTextPersonName.setText(name)
                    }
                    if (groupPicker != null) {
                        val items = groupPicker.items
                        for (item in items) {
                            if (item.title == group) groupPicker.setSelectedItem(item)
                        }
                    }
                    if (account_second_subgroup != null && account_first_subgroup != null) {
                        if (subgroup == 1) {
                            account_second_subgroup.isChecked = true
                        } else {
                            account_first_subgroup.isChecked = true
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Settings", "$error")
                }
            })
    }

    private fun fillDataFromDefaults() {
        val name =
            getStringFromSharedPreferences(
                "name",
                activity
            )
        val group =
            getStringFromSharedPreferences(
                "group",
                activity
            )
        val subgroup =
            getIntFromSharedPreferences(
                "subgroup",
                activity
            )
        val image =
            getStringFromSharedPreferences(
                "userImage",
                activity
            )
        if (name != "" && editTextTextPersonName != null) {
            editTextTextPersonName.setText(name)
        }
        if (group != "" && groupPicker != null) {
            val items = groupPicker.items
            for (item in items) {
                if (item.title == group) groupPicker.setSelectedItem(item)
            }
        }
        if (subgroup != -1 && userName != null) {
            if (subgroup == 0) {
                account_first_subgroup.isChecked = true
            } else {
                account_second_subgroup.isChecked = true
            }
        }
        if (accountUserImage != null && image != "") {
            val image = decodeBase64(image)
            if (image != null) accountUserImage.setImageBitmap(image)
        }
    }

    private fun encodeTobase64(image: Bitmap): String? {
        val baos = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val b: ByteArray = baos.toByteArray()
        val imageEncoded: String = Base64.encodeToString(b, Base64.DEFAULT)
        Log.d("Image Log:", imageEncoded)
        return imageEncoded
    }

    private fun decodeBase64(input: String?): Bitmap? {
        val decodedByte = Base64.decode(input, 0)
        return BitmapFactory
            .decodeByteArray(decodedByte, 0, decodedByte.size)
    }

}
