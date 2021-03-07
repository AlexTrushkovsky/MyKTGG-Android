package ua.pp.trushkovsky.MyKTGG.ui.settings

import android.util.Log
import com.paulrybitskyi.valuepicker.model.Item
import com.paulrybitskyi.valuepicker.model.PickerItem
import kotlinx.coroutines.*
import okhttp3.*

class GroupNetworkController {

    private fun formatJson(str: String, isStudent: Boolean) : List<String> {
        var result = str.replace("department", "")
        result = result.replace("]", "")
        result = result.replace("[", "")
        result = result.replace( "\"", "")
        result = result.replace( "}", "")
        result = result.replace( "{", "")
        var arr = result.split(",")
        var newArr = emptyList<String>().toMutableList()
        for (i in arr.indices) {
            if (!arr[i].contains(":")) {
                newArr.add(arr[i].trimStart())
            }
        }
        return newArr.sorted()
    }

    suspend fun fetchData(isStudent: Boolean): List<Item> {
            var mode = ""
            if (isStudent) {
                mode = "group"
            } else {
                mode = "teacher"
            }

            val url =
                "http://app.ktgg.kiev.ua/cgi-bin/timetable_export.cgi?req_type=obj_list&req_mode=$mode&req_format=json&coding_mode=WINDOWS-1251&bs=ok"
            Log.e("GroupChoose", "Trying to get $mode list")

            val request = Request.Builder().url(url).build()
            val client = OkHttpClient()

             val items = CoroutineScope(Dispatchers.IO).async {
                 val body = client.newCall(request).execute().body()?.string() ?: return@async emptyList<Item>()
                 val arr = formatJson(body, isStudent)
                 arr.sorted()
                 val items = mutableListOf<Item>().apply {
                     for(number in 0 until arr.size) {
                         add(PickerItem(id = number, title = arr[number]))
                     }
                 }
                 return@async items
            }
        val itemList = items.await()
        return itemList
    }
}