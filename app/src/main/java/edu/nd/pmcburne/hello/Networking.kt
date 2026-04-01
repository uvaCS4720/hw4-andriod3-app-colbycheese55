package edu.nd.pmcburne.hello

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.Request
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

private val client = OkHttpClient().newBuilder()
    .callTimeout(10, TimeUnit.SECONDS)
    .connectTimeout(10, TimeUnit.SECONDS)
    .readTimeout(10, TimeUnit.SECONDS)
    .writeTimeout(10, TimeUnit.SECONDS)
    .build()

suspend fun fetchLandmarks(): List<Landmark> {
    val url = "https://www.cs.virginia.edu/~wxt4gm/placemarks.json"

    val request = Request.Builder()
        .header("User-Agent", "Android")
        .header("Accept", "application/json")
        .url(url)
        .build()

    val response = withContext(Dispatchers.IO) {
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("HTTP ${response.code}")
            response.body?.string() ?: throw Exception("Empty Body")
        }
    }

    val json = Json { ignoreUnknownKeys = true }
    val processedResponse = json.decodeFromString<List<ApiLandmarkWrapper>>(response)
    return processedResponse.map { l ->
        Landmark(
            id = l.id,
            name = l.name,
            description = l.description,
            tag_list = l.tag_list,
            latitude = l.visual_center.latitude,
            longitude = l.visual_center.longitude
        )
    }
}