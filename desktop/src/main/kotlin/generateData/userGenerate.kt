package generateData

import io.github.serpro69.kfaker.Faker
import model.*

class userGenerate {
    private val faker = Faker()

    fun generateUser(Amount: Int): List<User> {
        return List(Amount) {
            User(
                id = it + 1,
                username = faker.name.name(),
                email = faker.internet.email(),
                password = faker.password(),
                created_at = faker.date.past().toLocalDateTime()
            )
        }
    }
}