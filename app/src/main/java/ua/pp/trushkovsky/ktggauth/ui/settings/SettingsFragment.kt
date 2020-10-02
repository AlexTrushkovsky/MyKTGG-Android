package ua.pp.trushkovsky.ktggauth.ui.settings

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import ua.pp.trushkovsky.ktggauth.R

class SettingsFragment : PreferenceFragmentCompat() {

    @SuppressLint("ResourceType")
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.fragment_settings, rootKey)
    }
}