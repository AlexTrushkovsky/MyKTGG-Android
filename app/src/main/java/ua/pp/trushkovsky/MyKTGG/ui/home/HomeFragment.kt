package ua.pp.trushkovsky.MyKTGG.ui.home

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeRecyclerView
import com.ernestoyaquello.dragdropswiperecyclerview.listener.OnItemDragListener
import com.ernestoyaquello.dragdropswiperecyclerview.listener.OnItemSwipeListener
import com.ernestoyaquello.dragdropswiperecyclerview.listener.OnListScrollListener
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.mahc.custombottomsheetbehavior.BottomSheetBehaviorGoogleMapsLike
import kotlinx.android.synthetic.main.bottom_sheet_content.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_news.bootomSheetRecycler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import ua.pp.trushkovsky.MyKTGG.R
import ua.pp.trushkovsky.MyKTGG.helpers.showDialogWith
import ua.pp.trushkovsky.MyKTGG.ui.home.model.Pushes
import ua.pp.trushkovsky.MyKTGG.ui.home.weather.WeatherModel
import java.io.IOException
import java.lang.reflect.Type
import kotlin.math.roundToInt


class HomeFragment : Fragment() {
    private var pushes = mutableListOf<Pushes>()

    private var temp = String()
    private var desk = String()
    private var icon = String()

    @SuppressLint("ResourceAsColor")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupWeather()
        setupBottomSheetData()
    }

    override fun onStart() {
        super.onStart()
        main_goto_ktgg_button.setOnClickListener {
            val intent = Intent(Intent(Intent.ACTION_VIEW))
            intent.data = Uri.parse("https://ktgg.kiev.ua/")
            context?.let { it1 ->
                ContextCompat.startActivity(it1, intent, null)
            }
        }
        main_chat_button.setOnClickListener {
            showDialogWith("Працюємо", "Наразі чат в розробці, очікуйте в наступних версіях", context, null)
        }

        val behavior = BottomSheetBehaviorGoogleMapsLike.from(bottom_sheet)
        behavior.isCollapsible = false

    }

    private fun addToList(
        title: String,
        text: String,
        image: String,
        date: String?
    ) {
        pushes.add(Pushes(title, text, image, date))
    }

    private fun setupBottomSheetData() {
        val pushes = getList("PushList")
        if (pushes == null) {
            main_no_pushes_title?.alpha = 1f
        } else {
            for (push in pushes) {
                val title = push.title
                val text = push.subtitle
                val image = push.image
                val date = push.date
                addToList(title, text, image, date)
            }
            if (pushes.isNotEmpty()) main_no_pushes_title?.alpha = 0f
            setUpRecyclerView()
        }
    }

    private fun setupWeather() {
            weather_desc.isVisible = false
            weather_field.isVisible = false
            weather_icon.isVisible = false
            weather_temp.isVisible = false
            val url = "https://api.openweathermap.org/data/2.5/weather?q=kyiv&appid=5744b92815db0d211d94578419b57733&units=metric&lang=ua"
            val request = Request.Builder().url(url).build()
            val client = OkHttpClient()
            client.newCall(request).enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    val body = response.body()?.string() ?: return
                    try {
                        GlobalScope.launch(Dispatchers.IO) {
                            val gson = GsonBuilder().create() ?: return@launch
                            val weatherModel = gson.fromJson(body, WeatherModel::class.java)
                            val weather = weatherModel.weather ?: return@launch
                            val main = weatherModel.main ?: return@launch
                            temp = main.temp!!.toInt().toString()
                            desk = weather[0].description!!.toString()
                            icon = weather[0].icon!!.toString()
                            withContext(Dispatchers.Main) {
                                if (temp != "" && desk != "" && icon != "") {
                                    if (weather_desc != null &&  weather_field != null && weather_icon != null && weather_temp != null) {
                                        weather_desc.text = desk
                                        weather_temp.text = "$temp°"
                                        val drawable = context?.let {
                                            ContextCompat.getDrawable(it, resources.getIdentifier(
                                                "weather_${icon}",
                                                "drawable",
                                                context?.packageName))
                                        }
                                        if (drawable != null) {
                                            weather_icon.setImageDrawable(drawable)
                                        } else return@withContext
                                        weather_desc.isVisible = true
                                        weather_field.isVisible = true
                                        weather_icon.isVisible = true
                                        weather_temp.isVisible = true
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("weather", "$e")
                    }
                }
                override fun onFailure(call: Call, e: IOException) {
                    print("Failed to execute request")
                }
            })
    }

    private val onItemSwipeListener = object : OnItemSwipeListener<Pushes> {
        override fun onItemSwiped(position: Int, direction: OnItemSwipeListener.SwipeDirection, item: Pushes): Boolean {
            val pushes = getList("PushList") as? MutableList<Pushes> ?: return true
            pushes.removeAt(position)
            if (pushes.isEmpty()) {
                main_no_pushes_title?.alpha = 1f
            }
            setList("PushList", pushes)
            return false
        }
    }

    private fun setUpRecyclerView() {
        bootomSheetRecycler.layoutManager = LinearLayoutManager(requireActivity().application.applicationContext)
        val recycler = bootomSheetRecycler as? DragDropSwipeRecyclerView ?: return
        recycler.adapter = MainRecyclerAdapter(pushes)
        recycler.orientation = DragDropSwipeRecyclerView.ListOrientation.VERTICAL_LIST_WITH_VERTICAL_DRAGGING
        recycler.swipeListener = onItemSwipeListener
        recycler.disableDragDirection(DragDropSwipeRecyclerView.ListOrientation.DirectionFlag.RIGHT)
        recycler.disableSwipeDirection(DragDropSwipeRecyclerView.ListOrientation.DirectionFlag.RIGHT)
        recycler.layoutManager = LinearLayoutManager(activity)
    }

    fun setList(key: String, list: List<Pushes>) {
        val json = Gson().toJson(list)
        set(key, json)
    }

    operator fun set(key: String?, value: String?) {
        val context = context ?: return
        val sharedPreferences = context.getSharedPreferences("default", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getList(key: String): List<Pushes>? {
        val context = context ?: return null
        val sharedPreferences = context.getSharedPreferences("default", Context.MODE_PRIVATE)
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