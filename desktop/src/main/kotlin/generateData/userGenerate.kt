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
                firstname = faker.name.firstName(),
                lastname = faker.name.lastName(),
                email = faker.internet.email(),
                password = faker.lorem.words(),
            )
        }
    }
}