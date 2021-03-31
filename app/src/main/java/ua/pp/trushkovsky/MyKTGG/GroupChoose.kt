package ua.pp.trushkovsky.MyKTGG

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_group_choose.*
import kotlinx.android.synthetic.main.fragment_group_choose.groupPicker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import ua.pp.trushkovsky.MyKTGG.ui.settings.GroupNetworkController
import ua.pp.trushkovsky.MyKTGG.ui.settings.saveBoolToSharedPreferences
import ua.pp.trushkovsky.MyKTGG.ui.settings.saveIntToSharedPreferences
import ua.pp.trushkovsky.MyKTGG.ui.settings.saveStringToSharedPreferences

class GroupChoose : DialogFragment() {
    private var isStudent = true
        set(value) {
            field = value
            if (group_choose_first_subgroup != null && group_choose_second_subgroup != null) {
                group_choose_second_subgroup.isVisible = value
                group_choose_first_subgroup.isVisible = value
            }
            if (group_choose_changeUserType_button != null) {
                if (value) {
                    group_choose_changeUserType_button.text = "Обрати викладача"
                } else {
                    group_choose_changeUserType_button.text = "Обрати групу"
                }
            }
            setupPickerView()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.AppTheme_FullScreenDialog)
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window!!.setLayout(width, height)
            dialog.window!!.setWindowAnimations(R.style.AppTheme_Slide)
        }
        updateUserValues()
        group_choose_first_subgroup.isChecked = true
        group_choose_changeUserType_button.setOnClickListener {
            isStudent = !isStudent
        }
        confirm_button.setOnClickListener {
            saveGroup()
            dismiss()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupPickerView()
    }

    private fun setupPickerView() {
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

    fun updateUserValues() {
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
                    val subgroup = map["subgroup"]
                    val isStudent = map["isStudent"]
                    if (isStudent != null) {
                        this@GroupChoose.isStudent = isStudent.toString().toBoolean()
                    }
                    if (group != null && groupPicker != null) {
                        val items = groupPicker.items
                        for (item in items) {
                            if (item.title == group.toString()) groupPicker.setSelectedItem(item)
                        }
                    }
                    if (subgroup != null) {
                        if (subgroup.toString().toInt() == 0) {
                            group_choose_first_subgroup?.isChecked = true
                        } else {
                            group_choose_second_subgroup?.isChecked = true
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun saveGroup() {
        val group = groupPicker.selectedItem?.title
        if (group != null) {
            updateFirebaseValue("group", group)
        }
        var subgroup = 0
        if (group_choose_first_subgroup.isChecked) {
            subgroup = 0
        } else if (group_choose_second_subgroup.isChecked) {
            subgroup = 1
        }
        updateFirebaseValue("subgroup", subgroup)
        updateFirebaseValue("isStudent", isStudent)

        group?.let { saveStringToSharedPreferences("group", it, context) }
        saveIntToSharedPreferences("subgroup", subgroup, context)
        saveBoolToSharedPreferences("isStudent", isStudent, context)
    }

    private fun updateFirebaseValue(key: String, value: Any) {
        var userID = Firebase.auth.currentUser?.uid ?: return
        FirebaseDatabase.getInstance().reference
            .child("users")
            .child(userID)
            .child("public")
            .child(key).setValue(value)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_group_choose, container, false)
    }

    companion object {
        const val TAG = "group_choose"
        fun display(fragmentManager: FragmentManager?): GroupChoose {
            val exampleDialog =
                GroupChoose()
            exampleDialog.show(
                fragmentManager!!,
                TAG
            )
            return exampleDialog
        }
    }
}