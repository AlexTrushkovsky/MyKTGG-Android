package ua.pp.trushkovsky.MyKTGG

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_forgot_pass.*
import ua.pp.trushkovsky.MyKTGG.helpers.showDialogWith

class ForgotPass : DialogFragment() {
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

        forgotPassButton.setOnClickListener {
            val email = forgotPassMail.text ?: return@setOnClickListener
            sendRestoreTo(email.toString())
        }

    }

    fun sendRestoreTo(email: String) {
        if (email != ""){
            if (!email.contains("@")){
                showDialogWith("Помилка", "Ви ввели не дійсний email", context, null)
                return
            }
            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        showDialogWith("Готово", "Перевірте поштову скриньку", context, dismiss())
                    } else {
                        showDialogWith("Помилка", "Користувача з даним email не існує", context, null)
                    }
                }
        }else{
            showDialogWith("Помилка", "Всі поля обов'язкові до заповнення", context, null)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_forgot_pass, container, false)
    }

    companion object {
        const val TAG = "forgot_pass"
        fun display(fragmentManager: FragmentManager?): ForgotPass {
            val exampleDialog =
                ForgotPass()
            exampleDialog.show(
                fragmentManager!!,
                TAG
            )
            return exampleDialog
        }
    }
}