package requests

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import data.CreateUser
import data.PublicUser
import util.DatabaseUtil
import java.sql.SQLException

fun getUnsafeOkHttpClient(): OkHttpClient {
    val trustAllCerts = arrayOf<javax.net.ssl.TrustManager>(
        object : javax.net.ssl.X509TrustManager {
            override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> = arrayOf()
        }
    )

    val sslContext = javax.net.ssl.SSLContext.getInstance("SSL")
    sslContext.init(null, trustAllCerts, java.security.SecureRandom())

    val sslSocketFactory = sslContext.socketFactory

    return OkHttpClient.Builder()
        .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as javax.net.ssl.X509TrustManager)
        .hostnameVerifier { _, _ -> true }
        .build()
}

suspend fun createUser(user: CreateUser): String = withContext(Dispatchers.IO) {
    val client = getUnsafeOkHttpClient()
    val json = Json.encodeToString(user)
    val mediaType = "application/json".toMediaType()
    val body = json.toRequestBody(mediaType)

    val request = Request.Builder()
        .url("https://0.0.0.0:8000/api/user/create")
        .post(body)
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            throw Exception("Unexpected response: ${response.code}")
        }
        return@withContext response.body?.string() ?: "No response"
    }
}

fun getAllUsers(): List<PublicUser>? {
    val client = getUnsafeOkHttpClient()

    val request = Request.Builder()
        .url("https://0.0.0.0:8000/api/user/all")
        .get()
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            println("Failed to fetch users: ${response.code}")
            return null
        }

        val body = response.body?.string() ?: return null

        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val type = Types.newParameterizedType(List::class.java, PublicUser::class.java)
        val adapter = moshi.adapter<List<PublicUser>>(type)

        return adapter.fromJson(body)
    }
}

fun updateUser(user: PublicUser): Boolean {
    val connection = DatabaseUtil.Connect() ?: return false
    if (connection == null) {
        println("Failed to connect to DB")
        return false
    }
    val sql = """
        UPDATE users
        SET username = ?, firstname = ?, lastname = ?, email = ?
        WHERE id = ?
    """.trimIndent()

    return try {
        val stmt = connection.prepareStatement(sql)
        stmt.setString(1, user.username)
        stmt.setString(2, user.firstname)
        stmt.setString(3, user.lastname)
        stmt.setString(4, user.email)
        stmt.setInt(5, user.id ?: 0)

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

fun deleteUser(userId: Int): Boolean {
    val connection = DatabaseUtil.Connect() ?: return false

    val sql = "DELETE FROM users WHERE id = ?"

    return try {
        val stmt = connection.prepareStatement(sql)
        stmt.setInt(1, userId)

        val rowsAffected = stmt.executeUpdate()
        rowsAffected > 0
    } catch (e: SQLException) {
        println("Delete failed: ${e.message}")
        false
    } finally {
        DatabaseUtil.Disconnect()
    }
}


