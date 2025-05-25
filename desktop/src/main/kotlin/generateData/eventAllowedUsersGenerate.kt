package generateData

class eventAllowedUsersGenerate {
    private val faker = io.github.serpro69.kfaker.Faker()

    fun generateEventAllowedUsers(amount: Int, events: List<model.Event>, users: List<model.User>): List<model.EventAllowedUser> {
        return List(amount) {
            model.EventAllowedUser(
                event_id = events.random().id,
                user_id = users.random().id
            )
        }
    }
}