package ua.pp.trushkovsky.MyKTGG

import ua.pp.trushkovsky.MyKTGG.R
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ua.pp.trushkovsky.MyKTGG.ui.home.model.Pushes


class MessagingService: FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val data = remoteMessage.data

        var body = ""
        var imageName = "default"
        var title = ""
        var date = ""

        if (data.count() >2) {
            title = data["title"] ?: return
            body = data["body"] ?: return
            date = data["date"] ?: return
         } else {
            body = remoteMessage.notification?.body ?: return
            title = remoteMessage.notification?.title ?: return
        }

        val icon = data["icon"]
        if (icon != null) {
            imageName = icon
        } else {
            imageName = ("default")
        }

        if (date != "") {
            val pushes = getList("PushList") as? MutableList<Pushes>
            if (pushes != null) {
                for (pushItem in pushes) {
                    if (pushItem.date == date && pushItem.subtitle == body) {
                        return
                    }
                }
            }

                //create in-app notification
                val CHANNEL_ID = "changes"
                val NOTIFICATION_ID = 100
                val icon = R.drawable.main_appicon
                val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(icon)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_MAX)

                with(NotificationManagerCompat.from(this)) {
                    notify(NOTIFICATION_ID, builder.build()) // посылаем уведомление
                    Log.i("notification", "sending local notification")
                }
        }
        val push = Pushes(title, body, imageName, date)
        val pushes = getList("PushList") as? MutableList<Pushes>
        if (pushes != null) {
            pushes.add(0, push)
            setList("PushList", pushes)
        } else {
            val pushes = listOf(push)
            setList("PushList", pushes)
        }
    }

    fun setList(key: String, list: List<Pushes>) {
        val json = Gson().toJson(list)
        set(key, json)
    }

    operator fun set(key: String?, value: String?) {
        val sharedPreferences = applicationContext.getSharedPreferences("default", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getList(key: String): List<Pushes>? {
        val sharedPreferences = applicationContext.getSharedPreferences("default", Context.MODE_PRIVATE)
        val serializedObject = sharedPreferences.getString(key, null) ?: return null
        val type = object : TypeToken<List<Pushes>>() {}.type ?: return null
        var list: List<Pushes>? = null
        try {
            list = Gson().fromJson<List<Pushes>>(serializedObject, type) ?: return null
        } catch (e: java.lang.Exception) {
            Log.e("getList", "Failed to get list: $e")
            return null
        }
        return list
    }
}