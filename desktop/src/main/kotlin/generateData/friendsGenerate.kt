package generateData

import model.User

class friendsGenerate {
    private val faker = io.github.serpro69.kfaker.Faker()

    val statusOptions = listOf("pending", "accepted", "rejected")

    fun generateFriends(Amount: Int, listOfUsers: List<User>): List<model.Friends> {
        return List(Amount) {
            val user1 = listOfUsers.random()
            var user2 = listOfUsers.random()
            while (user1.id == user2.id) {
                user2 = listOfUsers.random()
            }
            model.Friends(
                id = it + 1,
                user1_fk = user1.id,
                user2_fk = user2.id,
                status = statusOptions.random()
            )
        }
    }
}