package generateData

import com.github.javafaker.Faker
import java.time.LocalDateTime
import data.Event
import data.Location
import java.time.format.DateTimeFormatter

class eventsGenerate {
    private val faker = io.github.serpro69.kfaker.Faker()
    private val fakerSentence = Faker()

    fun generateEvent(Amount: Int, listOfLocations: List<Location>, startDateRange: LocalDateTime, endDateRange: LocalDateTime): List<Event> {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        return List(Amount) {
            val startDate = startDateRange.plusDays((0..30).random().toLong())
            val endDate = endDateRange.plusDays((0..30).random().toLong())
            Event(
                title = fakerSentence.lorem().sentence(),
                description = fakerSentence.lorem().paragraph(),
                start_date = startDate.format(formatter),
                end_date = endDate.format(formatter),
                location_fk = listOfLocations.random().id ?: 0,
                public = listOf(true, false).random(),
                tag = listOf("sport", "dogodek", "drugo", "sola").random()
            )
        }
    }
}