package generateData

import kotlin.random.Random

class locationOutlineGenerate {
    private val faker = io.github.serpro69.kfaker.Faker()

    fun generateLocationOutline(amount: Int, startRange: Double, endRange: Double, minPoints: Int, maxPoints: Int): List<model.LocationOutline> {
        require(amount > 0) { "Amount must be greater than 0" }
        require(startRange < endRange) { "Start range must be less than end range" }
        require(minPoints > 0 && maxPoints >= minPoints) { "Invalid point range" }
        // Generate random points within the specified range

        return List(amount) {
            val points = List(Random.nextInt(minPoints, maxPoints + 1)) {
                """{"latitude": ${Random.nextDouble(startRange, endRange)}, "longitude": ${Random.nextDouble(startRange, endRange)}]}"""
            }.joinToString(", ")
            model.LocationOutline(
                id = it + 1,
                point = points
            )
        }
    }
}