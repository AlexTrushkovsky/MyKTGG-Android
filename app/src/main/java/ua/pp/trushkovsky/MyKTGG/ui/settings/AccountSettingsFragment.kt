package ua.pp.trushkovsky.MyKTGG.ui.settings

import android.app.Activity
import android.app.ProgressDialog
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
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.fragment_account_settings.*
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import ua.pp.trushkovsky.MyKTGG.R
import ua.pp.trushkovsky.MyKTGG.RegActivity
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
                    account_changeUserType_button.setText("Обрати викладача")
                } else {
                    account_changeUserType_button.setText("Обрати групу")
                }
            }
            setupPickerView()
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_account_settings, container, false)
        return root
    }

    fun setupPickerView() {
        CoroutineScope(Dispatchers.IO).async {
            val network = GroupNetworkController()
            val itemList = network.fetchData(isStudent)
            withContext(Dispatchers.Main) {
                with(groupPicker) {
                    items = itemList
                    setSelectedItem(items[0])
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isStudent = getBoolFromSharedPreferences("isStudent", this.activity)
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
                .compress(1024)			//Final image size will be less than 1 MB(Optional)
                .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                .start { resultCode, data ->
                    if (resultCode == Activity.RESULT_OK) {
                        //Image Uri will not be null for RESULT_OK
                        val fileUri = data?.data
                        accountUserImage.setImageURI(fileUri)
                        this.avatarIsChanged = true
                        if (fileUri != null) {
                            this.imageURL = fileUri
                        }
                    } else if (resultCode == ImagePicker.RESULT_ERROR) {
                        Toast.makeText(context, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Task Cancelled", Toast.LENGTH_SHORT).show()
                    }
                }
        }
        account_changeUserType_button.setOnClickListener {
            isStudent = !isStudent
        }
    }

    fun doneButtonAction() {
        val group = groupPicker.selectedItem?.title
        if (group != null) {
            updateFirebaseValue("group", group)
        }
        var subgroup = 0
        if (account_first_subgroup.isChecked) {
            subgroup = 0
        } else if (account_second_subgroup.isChecked) {
            subgroup = 1
        }
        updateFirebaseValue("subgroup", subgroup)
        updateFirebaseValue("isStudent", isStudent)
        val name = editTextTextPersonName.text.toString()
        if (name != "") {
            updateFirebaseValue("name", name)
        }
        if (avatarIsChanged) {
            if (accountUserImage != null) {
                accountUserImage.invalidate()
                val bitmap = accountUserImage.drawable ?: return
                val imageString = encodeTobase64(bitmap.toBitmap())
                if (imageString != null) {
                    saveStringToSharedPreferences("userImage", imageString, this.activity)
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
            val ref: StorageReference =
                Firebase.storage.reference.child("avatars").child(userID)
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

    fun logOut() {
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
                        if (subgroup == 0) {
                            account_first_subgroup.isChecked = true
                        } else {
                            account_second_subgroup.isChecked = true
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Settings", "$error")
                }
            })
    }

    fun fillDataFromDefaults() {
        val name = getStringFromSharedPreferences("name", activity)
        val group = getStringFromSharedPreferences("group", activity)
        val subgroup = getIntFromSharedPreferences("subgroup", activity)
        val image = getStringFromSharedPreferences("userImage", activity)
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

}
