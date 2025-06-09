package requests

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import util.DatabaseUtil
import java.sql.SQLException

fun getAllLocations(): List<Location>? {
    val client = getUnsafeOkHttpClient()

    val request = Request.Builder()
        .url("https://0.0.0.0:8000/api/location/all")
        .get()
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            println("Failed to fetch locations: ${response.code}")
            return null
        }

        val body = response.body?.string() ?: return null

        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val type = Types.newParameterizedType(List::class.java, Location::class.java)
        val adapter = moshi.adapter<List<Location>>(type)

        return adapter.fromJson(body)
    }
}

suspend fun createLocation(location: Location): String = withContext(Dispatchers.IO) {
    val client = getUnsafeOkHttpClient()
    val json = Json.encodeToString(location)
    val mediaType = "application/json".toMediaType()
    val body = json.toRequestBody(mediaType)

    val request = Request.Builder()
        .url("https://0.0.0.0:8000/api/location/create")
        .post(body)
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            throw Exception("Unexpected response: ${response.code}")
        }
        return@withContext response.body?.string() ?: "No response"
    }
}

suspend fun createLocationOutline(info: String, points: List<Coordinate>): String = withContext(Dispatchers.IO) {
    val client = getUnsafeOkHttpClient()
    val locationOutline = LocationOutline(info = info, points = points)
    val json = Json.encodeToString(locationOutline)
    println(json)
    val mediaType = "application/json".toMediaType()
    val body = json.toRequestBody(mediaType)

    val request = Request.Builder()
        .url("https://0.0.0.0:8000/api/location_outline/create")
        .post(body)
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            throw Exception("Unexpected response: ${response.code}")
        }
        return@withContext response.body?.string() ?: "No response"
    }
}

fun getAllLocationOutlines(): List<LocationOutline>? {
    val client = getUnsafeOkHttpClient()

    val request = Request.Builder()
        .url("https://0.0.0.0:8000/api/location_outline/all")
        .get()
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            println("Failed to fetch location outlines: ${response.code}")
            return null
        }

        val body = response.body?.string() ?: return null

        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val type = Types.newParameterizedType(List::class.java, LocationOutline::class.java)
        val adapter = moshi.adapter<List<LocationOutline>>(type)

        return adapter.fromJson(body)
    }
}

fun updateLocation(location: Location): Boolean {
    val connection = DatabaseUtil.Connect() ?: return false
    if (connection == null) {
        println("Failed to connect to DB")
        return false
    }
    val sql = """
        UPDATE locations
        SET info = ?, longitude = ?, latitude = ?, location_outline_fk = ?
        WHERE id = ?
    """.trimIndent()

    return try {
        val stmt = connection.prepareStatement(sql)
        stmt.setString(1, location.info)

        if (location.longitude != null && location.latitude != null) {
            stmt.setDouble(2, location.longitude)
            stmt.setDouble(3, location.latitude)
            stmt.setNull(4, java.sql.Types.INTEGER)
        } else if (location.location_outline_fk != null) {
            stmt.setNull(2, java.sql.Types.DOUBLE)
            stmt.setNull(3, java.sql.Types.DOUBLE)
            stmt.setInt(4, location.location_outline_fk)
        } else {
            throw SQLException("Invalid location data: does not satisfy constraint")
        }
        stmt.setInt(5, location.id ?: 0)

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

fun deleteLocation(locationId: Int): Boolean {
    val connection = DatabaseUtil.Connect() ?: return false

    val sql = "DELETE FROM locations WHERE id = ?"

    return try {
        val stmt = connection.prepareStatement(sql)
        stmt.setInt(1, locationId)
        val rowsAffected = stmt.executeUpdate()
        rowsAffected > 0
    } catch (e: SQLException) {
        println("Delete failed: ${e.message}")
        false
    } finally {
        DatabaseUtil.Disconnect()
    }
}
