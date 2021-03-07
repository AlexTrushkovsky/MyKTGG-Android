package ua.pp.trushkovsky.MyKTGG.ui.home.weather

import android.util.Log
import com.google.gson.GsonBuilder
import com.paulrybitskyi.valuepicker.model.Item
import kotlinx.coroutines.*
import okhttp3.*
import ua.pp.trushkovsky.MyKTGG.ui.home.HomeFragment
import ua.pp.trushkovsky.MyKTGG.ui.timetable.model.TimetableRoot
import java.io.IOException
import java.lang.Exception
import java.text.SimpleDateFormat
//import java.util.*
//
//class WeatherController {
//    fun fetchData(): List<String> {
//        val url = "https://api.openweathermap.org/data/2.5/weather?q=kyiv&appid=5744b92815db0d211d94578419b57733&units=metric&lang=ua"
//        val request = Request.Builder().url(url).build()
//        val client = OkHttpClient()
//        client.newCall(request).enqueue(object : Callback {
//            override fun onResponse(call: Call, response: Response) {
//                    val body = response.body()?.string() ?: return
//                    val weatherList = emptyList<String>() as? MutableList<String> ?: return
//                    try {
//                        val gson = GsonBuilder().create() ?: return
//                        val weatherModel = gson.fromJson(body, WeatherModel::class.java)
//                        val weather = weatherModel.weather ?: return
//                        val main = weatherModel.main ?: return
//                        if (main.temp != null && weather[0].description != null && weather[0].icon != null) {
//                            weatherList.add(main.temp!!.toInt().toString())
//                            weatherList.add(weather[0].description!!.toInt().toString())
//                            weatherList.add(weather[0].icon!!.toInt().toString())
//                        }
//                    } catch (e: Exception) {
//                        Log.e("weather", "$e")
//                    }
//            }
//            override fun onFailure(call: Call, e: IOException) {
//                print("Failed to execute request")
//            }
//        })
//}