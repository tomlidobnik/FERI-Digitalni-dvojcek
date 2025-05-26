package generateData

import it.skrape.matchers.generalAssertion
import model.LocationOutline
import kotlin.random.Random
import com.github.javafaker.Faker

class locationsGenerate {
    private val faker = Faker()

    fun generateLocations(amount: Int, startRange: Double, endRange: Double, locationOutlines: List<LocationOutline>): List<model.Location> {
        require(amount > 0) { "Amount must be greater than 0" }
        require(startRange < endRange) { "Start range must be less than end range" }

        return List(amount) {
            model.Location(
                id = it + 1,
                info = faker.lorem().sentence(faker.random().nextInt(1, 30)),
                longitude = Random.nextDouble(startRange, endRange),
                latitude = Random.nextDouble(startRange, endRange),
                location_outline_fk = locationOutlines.random().id
            )
        }
    }
}