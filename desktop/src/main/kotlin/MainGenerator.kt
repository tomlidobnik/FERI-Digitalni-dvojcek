import generateData.*

fun main() {
    val userGenerate = userGenerate()
    val locationOutlineGenerate = locationOutlineGenerate()
    val locationsGenerate = locationsGenerate()
    val friendsGenerate = friendsGenerate()
    val friendChatMessagesGenerate = friendChatMessagesGenerate()
    val eventsGenerate = eventsGenerate()
    val eventAllowedUsersGenerate = eventAllowedUsersGenerate()
    val chatMessagesGenerate = chatMessagesGenerate()

    val users = userGenerate.generateUser(10)
    users.forEach { user ->
        println("ID: ${user.id}, Username: ${user.username}, Firstname: ${user.firstname}, Lastname: ${user.lastname}, Email: ${user.email}")
    }

    val locationOutlines = locationOutlineGenerate.generateLocationOutline(10, 20.0, 30.0, 3, 10)
    locationOutlines.forEach { location ->
        println("ID: ${location.id}, Point: ${location.point}")
    }

    val locations = locationsGenerate.generateLocations(10, 20.0, 30.0, locationOutlines)
    locations.forEach { location ->
        println("ID: ${location.id}, Info: ${location.info}, Longitude: ${location.longitude}, Latitude: ${location.latitude}, Location Outline FK: ${location.location_outline_fk}")
    }

    val friends = friendsGenerate.generateFriends(5, users)
    friends.forEach { friend ->
        println("ID: ${friend.id}, User1 ID: ${friend.user1_fk}, User2 ID: ${friend.user2_fk}, Status: ${friend.status}")
    }

    val friendChatMessages = friendChatMessagesGenerate.generateFriendChatMessages(10, users, friends)
    friendChatMessages.forEach { message ->
        println("ID: ${message.id}, User FK: ${message.user_fk}, Message: ${message.message}, Created At: ${message.created_at}, Friend FK: ${message.friend_fk}")
    }

    val events = eventsGenerate.generateEvent(5, locations, java.time.LocalDateTime.now(), java.time.LocalDateTime.now().plusDays(30))
    events.forEach { event ->
        println("ID: ${event.id}, User FK: ${event.user_fk}, Title: ${event.title}, Description: ${event.description}, Start Date: ${event.start_date}, End Date: ${event.end_date}, Location FK: ${event.location_fk}, Public: ${event.public}")
    }

    val eventAllowedUsers = eventAllowedUsersGenerate.generateEventAllowedUsers(10, events, users)
    eventAllowedUsers.forEach { allowedUser ->
        println("Event ID: ${allowedUser.event_id}, User ID: ${allowedUser.user_id}")
    }

    val chatMessages = chatMessagesGenerate.generateChatMessages(10, users, events, java.time.LocalDateTime.now(), java.time.LocalDateTime.now().plusDays(30))
    chatMessages.forEach { message ->
        println("ID: ${message.id}, User FK: ${message.user_fk}, Event FK: ${message.event_fk}, Message: ${message.message}, Created At: ${message.created_at}")
    }
}
