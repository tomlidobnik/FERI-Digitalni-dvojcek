package generateData

import com.github.javafaker.Faker

class friendChatMessagesGenerate {
    private val faker = io.github.serpro69.kfaker.Faker()
    private val fakerSentence = Faker()

    fun generateFriendChatMessages(Amount: Int, listOfUsers: List<model.User>, listOfFriends: List<model.Friends>): List<model.FriendChatMessage> {
        return List(Amount) {
            val user = listOfUsers.random()
            val friend = listOfFriends.random().let { if (it.user1_fk == user.id) it.user2_fk else it.user1_fk }
            model.FriendChatMessage(
                id = it + 1,
                user_fk = user.id,
                message = fakerSentence.lorem().sentence(),
                created_at = java.time.LocalDateTime.now(),
                friend_fk = friend
            )
        }
    }
}