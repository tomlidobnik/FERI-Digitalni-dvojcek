package generateData

import it.skrape.matchers.generalAssertion
import kotlin.random.Random
import com.github.javafaker.Faker
import data.Location

class locationsGenerate {
    private val faker = Faker()

    fun generateLocations(amount: Int, startRange: Double, endRange: Double) : List<Location> {
        require(amount > 0) { "Amount must be greater than 0" }
        require(startRange < endRange) { "Start range must be less than end range" }

        return List(amount) {
            Location(
                info = faker.lorem().words(2).joinToString(" "),
                longitude = Random.nextDouble(startRange, endRange),
                latitude = Random.nextDouble(startRange, endRange)
            )
        }
    }
}