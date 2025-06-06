package generateData

import com.github.javafaker.Faker
import java.time.LocalDateTime

class chatMessagesGenerate {
    private val faker = io.github.serpro69.kfaker.Faker()
    private val fakerSentence = Faker()

    fun generateChatMessages(amount: Int, users: List<model.User>, chats: List<model.Event>, startDateRange: LocalDateTime, endDateRange: LocalDateTime): List<model.ChatMessage> {
        return List(amount) {
            model.ChatMessage(
                id = it + 1,
                user_fk = users.random().id,
                event_fk = chats.random().id,
                message = fakerSentence.lorem().sentence(),
                created_at = LocalDateTime.now().plusDays((startDateRange.toLocalDate().toEpochDay()..endDateRange.toLocalDate().toEpochDay()).random() - startDateRange.toLocalDate().toEpochDay()
            ))
        }
    }
}