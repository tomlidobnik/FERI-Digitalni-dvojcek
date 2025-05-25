package generateData

import com.github.javafaker.Faker
import java.time.LocalDateTime

class eventsGenerate {
    private val faker = io.github.serpro69.kfaker.Faker()
    private val fakerSentence = Faker()

    fun generateEvent(Amount: Int, listOfLocations: List<model.Location>, startDateRange: LocalDateTime, endDateRange: LocalDateTime): List<model.Event> {
        return List(Amount) {
            val startDate = startDateRange.plusDays((0..30).random().toLong())
            val endDate = endDateRange.plusDays((0..30).random().toLong())
            model.Event(
                id = it + 1,
                user_fk = (1..100).random(),
                title = fakerSentence.lorem().sentence(),
                description = fakerSentence.lorem().paragraph(),
                start_date = startDate,
                end_date = endDate,
                location_fk = listOfLocations.random().id,
                public = listOf(true, false).random()
            )
        }
    }
}