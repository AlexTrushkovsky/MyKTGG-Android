package ua.pp.trushkovsky.ktggauth.ui.news.api

import retrofit2.http.GET
import ua.pp.trushkovsky.ktggauth.ui.news.model.NewsApiJSON

interface APIRequest {
    @GET("/uk/news.html?format=json")
    suspend fun getNews() : NewsApiJSON
}