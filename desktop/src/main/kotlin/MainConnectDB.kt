import androidx.compose.ui.window.application
import util.DatabaseUtil

fun main() = application {
    try {
        val connection = DatabaseUtil.Connect()
        if (connection != null) {
            val stmt = connection.createStatement()
            // Prodobivanje podatkov iz tabele messages
            val rs = stmt.executeQuery("SELECT * FROM messages")
            while (rs.next()) {
                println(rs.getString("message"))
            }
        }
        DatabaseUtil.Disconnect()

    } catch (e: Exception) {
        e.printStackTrace()
    }
}