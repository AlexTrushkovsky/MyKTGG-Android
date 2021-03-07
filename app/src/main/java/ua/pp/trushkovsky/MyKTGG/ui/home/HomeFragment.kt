package ua.pp.trushkovsky.MyKTGG.ui.home

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.mahc.custombottomsheetbehavior.BottomSheetBehaviorGoogleMapsLike
import kotlinx.android.synthetic.main.bottom_sheet_content.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_news.rv_main_recycler
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.coroutines.*
import okhttp3.*
import ua.pp.trushkovsky.MyKTGG.R
import ua.pp.trushkovsky.MyKTGG.helpers.showDialogWith
import ua.pp.trushkovsky.MyKTGG.ui.home.weather.WeatherModel
import ua.pp.trushkovsky.MyKTGG.ui.news.api.RecyclerAdapter
import java.io.IOException
import java.lang.Exception
import java.lang.reflect.Type

class HomeFragment : Fragment() {

    private var titleList = mutableListOf<String>()
    private var textList = mutableListOf<String>()
    private var imageList = mutableListOf<String>()

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

    @SuppressLint("CommitPrefEdits")
    fun getListOfPushes(): List<List<String>>? {
        val sharedPreferences: SharedPreferences = activity?.getSharedPreferences("default", Context.MODE_PRIVATE) ?: return null
        val editor = sharedPreferences.edit()
        val serializedObject = sharedPreferences.getString("PushList", null)
        val type: Type = object : TypeToken<List<List<String>>>() {}.type
        return Gson().fromJson<List<List<String>>>(serializedObject, type)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupWeather()
        setupBottomSheetData()
        val behavior = BottomSheetBehaviorGoogleMapsLike.from(bottom_sheet)
        behavior.isCollapsible = false
        behavior.addBottomSheetCallback(object : BottomSheetBehaviorGoogleMapsLike.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED -> Log.d(
                        "bottomsheet-",
                        "STATE_COLLAPSED"
                    )
                    BottomSheetBehaviorGoogleMapsLike.STATE_DRAGGING -> Log.d(
                        "bottomsheet-",
                        "STATE_DRAGGING"
                    )
                    BottomSheetBehaviorGoogleMapsLike.STATE_EXPANDED -> Log.d(
                        "bottomsheet-",
                        "STATE_EXPANDED"
                    )
                    BottomSheetBehaviorGoogleMapsLike.STATE_ANCHOR_POINT -> Log.d(
                        "bottomsheet-",
                        "STATE_ANCHOR_POINT"
                    )
                    BottomSheetBehaviorGoogleMapsLike.STATE_HIDDEN -> Log.d(
                        "bottomsheet-",
                        "STATE_HIDDEN"
                    )
                    else -> Log.d("bottomsheet-", "STATE_SETTLING")
                }
            }
        })
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
    }

    private fun addToList(
        title: String,
        text: String,
        image: String
    ) {
        titleList.add(title)
        textList.add(text)
        imageList.add(image)
    }

    fun setupBottomSheetData() {
        val pushes = getListOfPushes()
        if (pushes == null) {
            main_no_pushes_title.isVisible = true
        } else {
            for (push in pushes) {
                val title = push[0]
                val text = push[1]
                val image = push[2]
                addToList(title, text, image)
            }
            main_no_pushes_title.isVisible = false
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

    private fun setUpRecyclerView() {
        rv_main_recycler.layoutManager = LinearLayoutManager(requireActivity().application.applicationContext)
        rv_main_recycler.adapter = RecyclerAdapter(titleList, textList, imageList)
    }
}