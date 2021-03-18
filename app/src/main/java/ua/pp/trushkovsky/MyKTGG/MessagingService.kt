package ua.pp.trushkovsky.MyKTGG

import android.R
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type


class MessagingService: FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val data = remoteMessage.data

        var push = mutableListOf<String>()
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

        if (title != null) {
            if (title.contains("<in>") && title.contains("</in>")) {
                val superTitle = title.split("<in>").first()
                push.add(superTitle)
                if (body != null) {
                    push.add(body)
                }
                imageName = title.split("<in>").last().split("</in>").first()
                push.add(imageName)
            } else {
                push.add(title)
                if (body != null) {
                    push.add(body)
                }
                push.add("default")
            }
        }

        if (date != "") {
            val pushes = getList("PushList") as? MutableList<List<String>>
            if (pushes != null) {
                for (pushItem in pushes) {
                    if (pushItem.count() == 4) {
                        if (pushItem[3] == date && pushItem[1] == body) {
                            return
                        }
                    }
                }
            }
                push.add(date)
                //create in-app notification
                val CHANNEL_ID = "changes"
                val NOTIFICATION_ID = 100
                val icon = R.drawable.sym_def_app_icon
                val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(icon)
                    .setContentTitle(title.split("<in>").first())
                    .setContentText(body)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)

                with(NotificationManagerCompat.from(this)) {
                    notify(NOTIFICATION_ID, builder.build()) // посылаем уведомление
                    Log.i("notification", "sending local notification")
                }
        }

        val pushes = getList("PushList") as? MutableList<List<String>>
        if (pushes != null) {
            pushes.add(0, push)
            setList("PushList", pushes)
        } else {
            val pushes = listOf<List<String>>(push)
            setList("PushList", pushes)
        }
    }

    fun setList(key: String, list: List<List<String>>) {
        val json = Gson().toJson(list)
        set(key, json)
    }

    operator fun set(key: String?, value: String?) {
        val sharedPreferences = applicationContext.getSharedPreferences("default", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.commit()
    }

    fun getList(key: String): List<List<String>>? {
        val sharedPreferences = applicationContext.getSharedPreferences("default", Context.MODE_PRIVATE)
        val serializedObject = sharedPreferences.getString(key, null)
        val type: Type = object : TypeToken<List<List<String>>>() {}.type
        return Gson().fromJson<List<List<String>>>(serializedObject, type)
    }

}