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
import data.Location
import util.DatabaseUtil
import java.sql.SQLException
import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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

fun updateEvent(event: Event): Boolean {
    val connection = DatabaseUtil.Connect() ?: return false
    val sql = """
        UPDATE events
        SET title = ?, description = ?, start_date = ?, end_date = ?, location_fk = ?, public = ?, tag = ?
        WHERE id = ?
    """.trimIndent()

    fun normalizeDateTime(input: String): String =
        if (input.length == 16) input + ":00" else input

    return try {
        val stmt = connection.prepareStatement(sql)
        stmt.setString(1, event.title)
        stmt.setString(2, event.description)
        stmt.setTimestamp(3, Timestamp.valueOf(normalizeDateTime(event.start_date).replace('T', ' ')))
        stmt.setTimestamp(4, Timestamp.valueOf(normalizeDateTime(event.end_date).replace('T', ' ')))
        stmt.setInt(5, event.location_fk)
        stmt.setBoolean(6, event.public)
        stmt.setString(7, event.tag)
        stmt.setInt(8, event.id ?: 0)

        val rowsAffected = stmt.executeUpdate()
        println("Rows affected: $rowsAffected")
        rowsAffected > 0
    } catch (e: SQLException) {
        println("Update failed??: ${e.message}")
        false
    } finally {
        DatabaseUtil.Disconnect()
    }
}

fun deleteEvent(eventId: Int): Boolean {
    val connection = DatabaseUtil.Connect() ?: return false

    val sql = "DELETE FROM events WHERE id = ?"

    return try {
        val stmt = connection.prepareStatement(sql)
        stmt.setInt(1, eventId)
        val rowsAffected = stmt.executeUpdate()
        rowsAffected > 0
    } catch (e: SQLException) {
        println("Delete failed: ${e.message}")
        false
    } finally {
        DatabaseUtil.Disconnect()
    }
}



