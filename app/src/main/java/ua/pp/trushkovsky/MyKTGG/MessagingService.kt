package ua.pp.trushkovsky.MyKTGG

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type


class MessagingService: FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        var push = mutableListOf<String>()
        val body = remoteMessage.notification?.body
        var imageName = "default"
        var title = remoteMessage.notification?.title
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

        val pushes = getList("PushList") as? MutableList<List<String>>
        if (pushes != null) {
            pushes.add(push)
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