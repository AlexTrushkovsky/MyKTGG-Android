package ua.pp.trushkovsky.MyKTGG

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
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
import kotlinx.android.synthetic.main.fragment_group_choose.account_changeUserType_button
import kotlinx.android.synthetic.main.fragment_group_choose.account_first_subgroup
import kotlinx.android.synthetic.main.fragment_group_choose.account_group_progress
import kotlinx.android.synthetic.main.fragment_group_choose.account_second_subgroup
import kotlinx.android.synthetic.main.fragment_group_choose.groupPicker
import kotlinx.android.synthetic.main.fragment_group_choose.segmentedControl
import kotlinx.android.synthetic.main.fragment_group_choose.shadow_view
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import ua.pp.trushkovsky.MyKTGG.helpers.getStringFromSharedPreferences
import ua.pp.trushkovsky.MyKTGG.helpers.saveBoolToSharedPreferences
import ua.pp.trushkovsky.MyKTGG.helpers.saveIntToSharedPreferences
import ua.pp.trushkovsky.MyKTGG.helpers.saveStringToSharedPreferences
import ua.pp.trushkovsky.MyKTGG.ui.settings.*

class GroupChoose : DialogFragment() {
    private var isStudent = true
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
            dialog.window!!.statusBarColor = ContextCompat.getColor(requireContext(), R.color.appLightColor)
            dialog.window!!.setLayout(width, height)
            dialog.window!!.setWindowAnimations(R.style.AppTheme_Slide)
        }
        updateUserValues()
        account_first_subgroup.isChecked = true
        account_changeUserType_button.setOnClickListener {
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

    fun setupPickerView() {
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
                            account_first_subgroup?.isChecked = true
                        } else {
                            account_second_subgroup?.isChecked = true
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
        if (account_first_subgroup.isChecked) {
            subgroup = 0
        } else if (account_second_subgroup.isChecked) {
            subgroup = 1
        }
        updateFirebaseValue("subgroup", subgroup)
        updateFirebaseValue("isStudent", isStudent)

        group?.let {
            saveStringToSharedPreferences(
                "group",
                it,
                context
            )
        }
        saveIntToSharedPreferences(
            "subgroup",
            subgroup,
            context
        )
        saveBoolToSharedPreferences(
            "isStudent",
            isStudent,
            context
        )
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