package ua.pp.trushkovsky.ktggauth.ui.timetable.api

import retrofit2.http.GET
import ua.pp.trushkovsky.ktggauth.ui.timetable.model.TimetableApiJSON

interface TimetableAPIRequest {
    @GET("/31-grs%28ro%29/")
    suspend fun TimetableApiJSON() : TimetableApiJSON
}