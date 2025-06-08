package requests

import data.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.Request
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

suspend fun createEvent(event: Event, token: String): String = withContext(Dispatchers.IO) {
    val client = getUnsafeOkHttpClient()
    val json = Json.encodeToString(event)
    val mediaType = "application/json".toMediaType()
    val body = json.toRequestBody(mediaType)

    val request = Request.Builder()
        .url("https://0.0.0.0:8000/api/event/create")
        .post(body)
        .addHeader("Authorization", "Bearer $token")
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            throw Exception("Event creation failed: ${response.code}")
        }
        return@withContext response.body?.string() ?: "No response"
    }
}

fun getAllEvents(): List<Event>? {
    val client = getUnsafeOkHttpClient()

    val request = Request.Builder()
        .url("https://0.0.0.0:8000/api/event/all")
        .get()
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            println("Failed to fetch events: ${response.code}")
            return null
        }

        val body = response.body?.string() ?: return null

        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val type = Types.newParameterizedType(List::class.java, Event::class.java)
        val adapter = moshi.adapter<List<Event>>(type)

        return adapter.fromJson(body)
    }
}


