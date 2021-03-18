package ua.pp.trushkovsky.MyKTGG

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.*
import com.google.firebase.auth.FirebaseAuth.getInstance
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class RegActivity : AppCompatActivity() {
    private val RC_SIGN_IN = 9001
    private var auth = Firebase.auth
    private var callbackManager: CallbackManager? = null

    var signUpStatus = false
    var singUp = true
        set(value) {
        val title = RegScreenTitle
        val regOrLogButton = registerButton
        val loginButton = loginSubButton
        val nameField = userNameRegisterField
        if (value == true) {
            title.text = "Створити Аккаунт"
            regOrLogButton.text = "Зареєструватися"
            loginButton.text = "Увійти"
            nameField.visibility = View.VISIBLE
        } else {
            title.text = "Вхід"
            regOrLogButton.text = "Увійти"
            loginButton.text = "Зареєструватися"
            nameField.visibility = View.INVISIBLE
        }
            field = value
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        registerButton.setOnClickListener {
            regOrLogIn()
        }
        loginSubButton.setOnClickListener {
            this.singUp = this.signUpStatus
            this.signUpStatus = !this.signUpStatus
        }
        signInWithGoogle.setOnClickListener {
            signInWithGoogle()
        }
        signInWithFacebook.setOnClickListener {
            signInWithFacebook()
        }
        signInWithTeams.setOnClickListener {
            signInWithTeams()
        }
        forgotPassRegisterActivity.setOnClickListener {
            ForgotPass.display(supportFragmentManager)
        }
    }

    private fun signInWithGoogle() {
        Log.d("SignIn", "Trying to sign in with google Acc")
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        callbackManager = CallbackManager.Factory.create()
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun signInWithTeams() {
        Log.d("SignIn", "Trying to sign in with teams Acc")
        val provider: OAuthProvider.Builder = OAuthProvider.newBuilder("microsoft.com")
        val pendingResultTask = auth.getPendingAuthResult()
        if (pendingResultTask != null) {
            pendingResultTask
                .addOnSuccessListener {
                    Log.e("teams", "${it.user}")
                }
                .addOnFailureListener {
                    Log.e("teams", "${it.localizedMessage}")
                }
        } else {
            Log.e("teams", "starting sign in flow")
            auth.startActivityForSignInWithProvider(this, provider.build())
                .addOnSuccessListener {
                    Log.e("teams", "logged in")
                    Log.e("teams", "${it.additionalUserInfo}, ${it.user}")
                    val intent = Intent(this, BottomNavActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    // User is signed in.
                    // IdP data available in
                    // authResult.getAdditionalUserInfo().getProfile().
                    // The OAuth access token can also be retrieved:
                    // authResult.getCredential().getAccessToken().
                    // The OAuth ID token can also be retrieved:
                    // authResult.getCredential().getIdToken().
                }
                .addOnFailureListener {
                    Log.e("teams", it.localizedMessage)
                }
        }
    }


    private fun firebaseAuthWithGoogle(idToken: String) {
        val progress = RegProgressBar
        val dialogBuilder = AlertDialog.Builder(this)
        progress.visibility = View.VISIBLE
        dialogBuilder.setIcon(R.drawable.common_google_signin_btn_icon_dark)
        dialogBuilder.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
            dialog.dismiss()
        })
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("signInWith", "signInWithCredential:success")
                    val user = auth.currentUser
                    val intent = Intent(this, BottomNavActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                } else {
                    var progress = RegProgressBar
                    var dialogBuilder = AlertDialog.Builder(this)
                    progress.visibility = View.VISIBLE
                    dialogBuilder.setIcon(R.drawable.common_google_signin_btn_icon_dark)
                    dialogBuilder.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                        dialog.dismiss()
                    })
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager?.onActivityResult(requestCode, resultCode, data)
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d("TAG", "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w("TAG", "Google sign in failed", e)
                // ...
            }
        }
    }

    private fun signInWithFacebook() {
        callbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().logInWithReadPermissions(this, listOf("public_profile","email"))
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult>{
            override fun onSuccess(result: LoginResult?) {
                handleFacebookAccesstoken(result?.accessToken)
            }

            override fun onCancel() {

            }

            override fun onError(error: FacebookException?) {

            }

        })
    }

    fun handleFacebookAccesstoken(token: AccessToken?) {
        var credential = FacebookAuthProvider.getCredential(token?.token!!)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("signInWith", "signInWithCredential:success")
                    val user = auth.currentUser
                    val intent = Intent(this, BottomNavActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                } else {
                    var progress = RegProgressBar
                    var dialogBuilder = AlertDialog.Builder(this)
                    progress.visibility = View.VISIBLE
                    dialogBuilder.setIcon(R.drawable.common_google_signin_btn_icon_dark)
                    dialogBuilder.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                        dialog.dismiss()
                    })
                }
            }
    }

    private fun logIn(dialogBuilder: AlertDialog.Builder, progress: ProgressBar) {
        Log.d("MainActivity", "Logging in...")
        val email = userEmailRegisterField.text.toString()
        val password = userPasswordRegisterField.text.toString()
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Всі поля мають бути заповненими", Toast.LENGTH_SHORT).show()
            progress.visibility = View.INVISIBLE
            return
        }
        getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener {
            if (!it.isSuccessful) return@addOnCompleteListener
            Log.d("MainActivity", "user successfully logged in")
            progress.visibility = View.INVISIBLE
            val intent = Intent(this, BottomNavActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)

        }.addOnFailureListener {
            Log.d("MainActivity", "Failed to login: ${it.message}")
            val localizedMessage = it.localizedMessage
            dialogBuilder.setTitle("Помилка")
            if (localizedMessage == "The email address is badly formatted.") {
                dialogBuilder.setMessage("Будь-ласка, введіть правильну електронну адресу!")
            } else if (localizedMessage == "There is no user record corresponding to this identifier. The user may have been deleted.") {
                dialogBuilder.setMessage("Аккаунту не знайдено. Перевірте правильність вводу і спробуйте ще раз!")
            } else if (localizedMessage == "The password is invalid or the user does not have a password.") {
                dialogBuilder.setMessage("Пароль невірний. Спробуйте ще раз, або натисніть 'Забули пароль', щоб відновити доступ.")
            } else if (localizedMessage == "A network error (such as timeout, interrupted connection or unreachable host) has occurred.") {
                dialogBuilder.setMessage("Немає зв'язку з сервером. Перевірте з'єднання або cпробуйте пізніше.")
            } else {
                dialogBuilder.setMessage("Сталася невідома помилка")
            }
            val alertDialog = dialogBuilder.create()
            alertDialog.show()
            progress.visibility = View.INVISIBLE
        }
    }

    private fun register(dialogBuilder: AlertDialog.Builder, progress: ProgressBar) {
        Log.d("MainActivity", "Creating acc in...")
        val email = userEmailRegisterField.text.toString()
        val password = userPasswordRegisterField.text.toString()
        val userName = userNameRegisterField.text.toString()
        if (email.isEmpty() || password.isEmpty() || userName.isEmpty()) {
            Toast.makeText(this, "Всі поля мають бути заповненими", Toast.LENGTH_SHORT).show()
            progress.visibility = View.INVISIBLE
            return
        }
        Log.d("MainActivity", "UserName is: " + userName)
        Log.d("MainActivity", "Email is: " + email)
        Log.d("MainActivity", "Password is: $password")

        getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener {
            if (!it.isSuccessful) return@addOnCompleteListener
            // else
            Log.d("MainActivity", "User with uid:${it.result!!.user!!.uid} is successfully created")

            getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener
                Log.d("MainActivity", "user successfully logged in")
                progress.visibility = View.INVISIBLE
                val intent = Intent(this, BottomNavActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)

            }


        }.addOnFailureListener {
            Log.d("MainActivity", "Failed to crate user: ${it.message}")
            val localizedMessage = it.localizedMessage
            dialogBuilder.setTitle("Помилка")
            if (localizedMessage == "The email address is badly formatted.") {
                dialogBuilder.setMessage("Будь-ласка, введіть правильну електронну адресу!")
            } else if (localizedMessage == "There is no user record corresponding to this identifier. The user may have been deleted.") {
                dialogBuilder.setMessage("Аккаунту не знайдено. Перевірте правильність вводу і спробуйте ще раз!")
            } else if (localizedMessage == "The given password is invalid. [ Password should be at least 6 characters ]") {
                dialogBuilder.setMessage("Ваш пароль закороткий. Вкажіть пароль, що містить більше 6 символів.")
            } else if (localizedMessage == "A network error (such as timeout, interrupted connection or unreachable host) has occurred.") {
                dialogBuilder.setMessage("Немає зв'язку з сервером. Перевірте з'єднання або cпробуйте пізніше.")
            } else {
                dialogBuilder.setMessage("Сталася невідома помилка")
            }
            val alertDialog = dialogBuilder.create()
            alertDialog.show()
            progress.visibility = View.INVISIBLE
        }
    }

    private fun regOrLogIn(){
        var progress = RegProgressBar
        var dialogBuilder = AlertDialog.Builder(this)
        progress.visibility = View.VISIBLE
        dialogBuilder.setIcon(R.drawable.common_google_signin_btn_icon_dark)
        dialogBuilder.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
            dialog.dismiss()
        })
        if (!signUpStatus) {
            register(progress = progress, dialogBuilder = dialogBuilder)
        } else {
            logIn(progress = progress, dialogBuilder = dialogBuilder)
        }
    }
}