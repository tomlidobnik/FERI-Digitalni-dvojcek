package requests

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import data.User

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

suspend fun createUser(user: User): String = withContext(Dispatchers.IO) {
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
