package ua.pp.trushkovsky.MyKTGG

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth.getInstance
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthCredential
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_reg.*
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import ua.pp.trushkovsky.MyKTGG.helpers.getIntFromGlobalPreferences
import ua.pp.trushkovsky.MyKTGG.helpers.saveIntToGlobalPreferences
import ua.pp.trushkovsky.MyKTGG.helpers.stringSimilatity
import ua.pp.trushkovsky.MyKTGG.pages.PagesDialog
import ua.pp.trushkovsky.MyKTGG.ui.settings.GroupNetworkController
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.URL

class RegActivity : AppCompatActivity(), CoroutineScope by MainScope() {
    private val RC_SIGN_IN = 9001
    private var auth = Firebase.auth
    private var callbackManager: CallbackManager? = null

    var signUpStatus = false
    var singUp = true
        set(value) {
        val title = RegScreenTitle
        val regOrLogButton = regButton
        val loginButton = loginSubButton
        val nameField = userNameRegisterField
        if (value) {
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


        window.statusBarColor = ContextCompat.getColor(this, R.color.appLightColor)
        setContentView(R.layout.activity_reg)
        showPagesIfNeeded()
        regButton.setOnClickListener {
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

    @SuppressLint("ResourceType")
    fun startIndication() {
        window.statusBarColor = ContextCompat.getColor(this, R.color.statusBarRegColor)
        val va = ValueAnimator.ofFloat(0f, 100f)
        val mDuration = 500 //in millis
        va.duration = mDuration.toLong()
        va.addUpdateListener { animation ->
            blurred_background.setBlurRadius(animation.animatedValue as Float)
            blurred_background.alpha = (animation.animatedValue as Float)/100
            RegProgressBar.alpha = (animation.animatedValue as Float)/100}
        va.start()
    }

    fun stopIndication() {
        val va = ValueAnimator.ofFloat(100f, 0f)
        val mDuration = 500 //in millis
        va.duration = mDuration.toLong()
        va.addUpdateListener { animation ->
            window.statusBarColor = ContextCompat.getColor(this, R.color.appLightColor)
            blurred_background.setBlurRadius(animation.animatedValue as Float)
            blurred_background.alpha = (animation.animatedValue as Float)/100
            RegProgressBar.alpha = (animation.animatedValue as Float)/100}
        va.start()
    }

    private fun showPagesIfNeeded() {
        val id = "countOfEnter"
        val countOfEnter =
            getIntFromGlobalPreferences(id, this)
        if (countOfEnter == -1) {
            saveIntToGlobalPreferences(id, 1, this)
            PagesDialog.display(supportFragmentManager)
        } else {
            saveIntToGlobalPreferences(id, countOfEnter + 1, this)
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
        val pendingResultTask = auth.pendingAuthResult
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
                    startIndication()
                    val intent = Intent(this, BottomNavActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)

                    val userID = Firebase.auth.currentUser?.uid ?: return@addOnSuccessListener
                    FirebaseDatabase.getInstance().reference
                        .child("users")
                        .child(userID)
                        .child("public")
                        .addValueEventListener(object : ValueEventListener {
                            override fun onCancelled(error: DatabaseError) {}
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.value == null) {
                                    val displayName = it.user?.displayName
                                    if (displayName != null) {
                                        updateFirebaseValue("name", displayName)
                                    }
                                    val credential = it.credential as? OAuthCredential
                                    val token = credential?.accessToken
                                    getTeamsAvatar(token)
                                    getTeamsGroup(token)
                                    return
                                }
                                val map = snapshot.value as Map<String, Any>
                                Log.e("Settings", "$map")
                                if (map["avatarUrl"] == null) {
                                    val credential = it.credential as? OAuthCredential
                                    getTeamsAvatar(credential?.accessToken)
                                }
                                if (map["name"] == null) {
                                    val displayName = it.user?.displayName
                                    if (displayName != null) {
                                        updateFirebaseValue("name", displayName)
                                    }
                                }
                                if (map["group"] == null) {
                                    val credential = it.credential as? OAuthCredential
                                    getTeamsGroup(credential?.accessToken)
                                }
                            }
                        })
                }
                .addOnFailureListener {
                    stopIndication()
                    Log.e("teams", it.localizedMessage)
                }
        }
    }

    fun getTeamsAvatar(token: String?) {
        if (token != null) {
            val url = "https://graph.microsoft.com/v1.0/me/photo/\$value"
            val request = Request.Builder().url(url).header("Authorization", "Bearer $token").build()
            val client = OkHttpClient()
            launch {
                val image = CoroutineScope(Dispatchers.IO).async {
                    return@async client.newCall(request).execute().body()?.bytes() ?: return@async null
                }.await()
                //val encodeByte  = Base64.decode(image, Base64.DEFAULT) ?: return@launch
                val bitmap = BitmapFactory.decodeByteArray(image, 0, image.size) ?: return@launch
                val uri = getImageUri(this@RegActivity, bitmap)
                uploadImage(uri)
                Log.i("image", "$image")
            }
        }
    }

    fun getTeamsGroup(token: String?) {
        if (token != null) {
            val url = "https://graph.microsoft.com/beta/me/jobtitle/\$value"
            val request = Request.Builder().url(url).header("Authorization", "Bearer $token").build()
            val client = OkHttpClient()
            launch {
                val group = CoroutineScope(Dispatchers.IO).async {
                    return@async client.newCall(request).execute().body()?.string() ?: return@async null
                }.await()
                if (group.length <= 15) {
                    findMostSimilarGroup(group)
                }
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        startIndication()
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("signInWith", "signInWithCredential:success")

                    val userID = Firebase.auth.currentUser?.uid ?: return@addOnCompleteListener
                    FirebaseDatabase.getInstance().reference
                        .child("users")
                        .child(userID)
                        .child("public")
                        .addValueEventListener(object : ValueEventListener {
                            override fun onCancelled(error: DatabaseError) {}
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val avatar = account.photoUrl
                                val displayName = account.displayName
                                if (snapshot.value == null) {
                                    if (avatar != null) {
                                        try {
                                            val url = URL(avatar.toString())
                                            launch {
                                                val image = CoroutineScope(Dispatchers.IO).async {
                                                    return@async BitmapFactory.decodeStream(
                                                        url.openConnection().getInputStream()
                                                    )
                                                }.await()
                                                val uri = getImageUri(this@RegActivity, image)
                                                uploadImage(uri)
                                            }
                                        } catch (e: IOException) {
                                            Log.e("Google avatar error", "$e")
                                        }
                                        uploadImage(avatar)
                                    }
                                    if (displayName != null) {
                                        updateFirebaseValue("name", displayName)
                                    }
                                    return
                                }
                                val map = snapshot.value as Map<String, Any>
                                Log.e("Settings", "$map")
                                if (map["avatarUrl"] == null) {
                                    if (avatar != null) {
                                        try {
                                            val url = URL(avatar.toString())
                                            launch {
                                                val image = CoroutineScope(Dispatchers.IO).async {
                                                    return@async BitmapFactory.decodeStream(
                                                        url.openConnection().getInputStream()
                                                    )
                                                }.await()
                                                val uri = getImageUri(this@RegActivity, image)
                                                uploadImage(uri)
                                            }
                                        } catch (e: IOException) {
                                            Log.e("Google avatar error", "$e")
                                        }
                                        uploadImage(avatar)
                                    }
                                }
                                if (map["name"] == null) {
                                    if (displayName != null) {
                                        updateFirebaseValue("name", displayName)
                                    }
                                }
                            }
                        })


                    val intent = Intent(this, BottomNavActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                } else {
                    var dialogBuilder = AlertDialog.Builder(this)
                    stopIndication()
                    dialogBuilder.setIcon(R.drawable.main_appicon)
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
                firebaseAuthWithGoogle(account)
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
                val token = result?.accessToken ?: return
                val request = GraphRequest.newMeRequest(token) { json, response ->
                    handleFacebookAccesstoken(token, json)
                }

                val parameters = Bundle()
                parameters.putString("fields", "name,email,id,picture.type(large)")
                request.parameters = parameters
                request.executeAsync()


            }

            override fun onCancel() {

            }

            override fun onError(error: FacebookException?) {

            }

        })
    }

    fun handleFacebookAccesstoken(token: AccessToken, facebookUser: JSONObject) {
        startIndication()
        var credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("signInWith", "signInWithCredential:success")
                    val user = auth.currentUser
                    val intent = Intent(this, BottomNavActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)

                    if (facebookUser.has("picture")) {
                        try {
                            launch {
                                val profilePicUrl = facebookUser.getJSONObject("picture").getJSONObject("data").getString("url")
                                val url = URL(profilePicUrl)
                                val image = CoroutineScope(Dispatchers.IO).async {
                                    return@async BitmapFactory.decodeStream(
                                        url.openConnection().getInputStream()
                                    )
                                }.await()
                                val uri = getImageUri(this@RegActivity, image)
                                uploadImage(uri)
                            }
                        } catch (e: IOException) {
                            Log.e("Facebook avatar error", "$e")
                        }
                    }
                    if (facebookUser.has("name")) {
                        val name = facebookUser.getString("name")
                        updateFirebaseValue("name", name)
                    }

                } else {
                    stopIndication()
                    var dialogBuilder = AlertDialog.Builder(this)
                    dialogBuilder.setIcon(R.drawable.main_appicon)
                    dialogBuilder.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                        dialog.dismiss()
                    })
                }
            }
    }

    private fun logIn(dialogBuilder: AlertDialog.Builder) {
        Log.d("MainActivity", "Logging in...")
        val email = userEmailRegisterField.text.toString()
        val password = userPasswordRegisterField.text.toString()
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Всі поля мають бути заповненими", Toast.LENGTH_SHORT).show()
            return
        }
        startIndication()
        getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener {
            if (!it.isSuccessful) return@addOnCompleteListener
            Log.d("MainActivity", "user successfully logged in")
            stopIndication()
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
            stopIndication()
        }
    }

    private fun register(dialogBuilder: AlertDialog.Builder) {
        Log.d("MainActivity", "Creating acc in...")
        val email = userEmailRegisterField.text.toString()
        val password = userPasswordRegisterField.text.toString()
        val userName = userNameRegisterField.text.toString()
        if (email.isEmpty() || password.isEmpty() || userName.isEmpty()) {
            Toast.makeText(this, "Всі поля мають бути заповненими", Toast.LENGTH_SHORT).show()
            return
        }
        Log.d("MainActivity", "UserName is: " + userName)
        Log.d("MainActivity", "Email is: " + email)
        Log.d("MainActivity", "Password is: $password")
        startIndication()
        getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener {
            if (!it.isSuccessful) return@addOnCompleteListener
            // else
            Log.d("MainActivity", "User with uid:${it.result!!.user!!.uid} is successfully created")

            getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener
                Log.d("MainActivity", "user successfully logged in")
                stopIndication()
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
            stopIndication()
        }
    }

    private fun regOrLogIn(){
        var dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setIcon(R.drawable.main_appicon)
        dialogBuilder.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
            dialog.dismiss()
        })
        if (!signUpStatus) {
            register(dialogBuilder)
        } else {
            logIn(dialogBuilder)
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

    private fun uploadImage(imageURL: Uri?) {
        val image = imageURL ?: return
        var userID = Firebase.auth.currentUser?.uid ?: return
        val ref = Firebase.storage.reference.child("avatars").child(userID)
        ref.putFile(image)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener {
                    updateFirebaseValue("avatarUrl", it.toString())
                }
            }
            .addOnFailureListener {
                Log.e("avatar", "upload exception: $it")
            }
    }

    private fun getImageUri(inContext: Activity, inImage: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path: String = MediaStore.Images.Media.insertImage(
            inContext.contentResolver,
            inImage,
            "Avatar",
            null
        )
        return Uri.parse(path)
    }

    private fun findMostSimilarGroup(group: String) {
        launch {
            val groups = GroupNetworkController().fetchData(true)
            var mostSimilarGroupName = group
            var similarity = 0.0
            for (groupName in groups) {
                val caseSimilarity = stringSimilatity(group, groupName.title)
                if (caseSimilarity > similarity) {
                    similarity = caseSimilarity
                    mostSimilarGroupName = groupName.title
                }
            }
            updateFirebaseValue("group", mostSimilarGroupName)
        }
    }
}