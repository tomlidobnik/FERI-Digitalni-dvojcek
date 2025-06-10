package generateData

import io.github.serpro69.kfaker.Faker
import data.CreateUser

class userGenerate {
    private val faker = Faker()

    fun generateUser(Amount: Int): List<CreateUser> {
        return List(Amount) {
            CreateUser(
                username = faker.name.name(),
                firstname = faker.name.firstName(),
                lastname = faker.name.lastName(),
                email = faker.internet.email(),
                password = faker.lorem.words(),
            )
        }
    }
}