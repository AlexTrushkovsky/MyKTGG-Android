package ua.pp.trushkovsky.MyKTGG.ui.news.api

import retrofit2.http.GET
import ua.pp.trushkovsky.MyKTGG.ui.news.model.NewsApiJSON

interface APIRequest {
    @GET("/uk/news.html?limit=9999999999999999&format=json")
    suspend fun getNews(): NewsApiJSON
}