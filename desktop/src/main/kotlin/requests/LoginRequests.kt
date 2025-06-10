package requests

import kotlinx.serialization.json.Json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

suspend fun loginUser(username: String, password: String): String = withContext(Dispatchers.IO) {
    val client = getUnsafeOkHttpClient()

    val credentials = """
        {
            "username": "$username",
            "password": "$password"
        }
    """.trimIndent()

    val mediaType = "application/json".toMediaType()
    val body = credentials.toRequestBody(mediaType)

    val request = Request.Builder()
        .url("https://0.0.0.0:8000/api/user/token")
        .post(body)
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            throw Exception("Login failed: ${response.code}")
        }

        val responseBody = response.body?.string() ?: throw Exception("Empty response")
        println("Login response: $responseBody")

        return@withContext responseBody
    }
}
