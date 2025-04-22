package util

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

@Serializable
data class Config(val url: String, val username: String, val password: String)

object DatabaseUtil {
    var connection: Connection? = null
    val jsonFile = File("db/config.json").readText()
    val config: Config = Json.decodeFromString(jsonFile)

    fun Connect(): Connection? {
        return try{
            DriverManager.getConnection(config.url, config.username, config.password);
        } catch (e: SQLException) {
            println("Napaka pri povezovanju: ${e.message}")
            null
        }
    }

    fun Disconnect() {
        try {
            connection?.close()
        } catch (e: SQLException) {
            println("Napaka pri zapiranju povezave: ${e.message}")
        }
    }

    fun printConfig(){
        println("URL: ${config.url}")
        println("Username: ${config.username}")
        println("Password: ${config.password}")
    }
}