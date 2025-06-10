import androidx.compose.ui.window.application
import util.DatabaseUtil

fun main() = application {
    try {
        val connection = DatabaseUtil.Connect()
        if (connection != null) {
            val stmt = connection.createStatement()

            val rs = stmt.executeQuery("SELECT * FROM users")
            while (rs.next()) {
                println(rs.getString("username"))
            }
        }
        DatabaseUtil.Disconnect()

    } catch (e: Exception) {
        e.printStackTrace()
    }
}