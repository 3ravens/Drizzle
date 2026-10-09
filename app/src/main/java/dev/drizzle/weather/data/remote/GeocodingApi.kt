package dev.drizzle.weather.data.remote

import dev.drizzle.weather.data.remote.dto.GeocodingResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodingApi {

    @GET("v1/search")
    suspend fun search(
        @Query("name") name: String,
        @Query("count") count: Int,
        @Query("language") language: String,
        @Query("format") format: String,
    ): GeocodingResponse
}
