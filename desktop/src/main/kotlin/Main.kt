import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.window.*
import data.CreateUser
import data.PublicUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import requests.createUser
import requests.getAllUsers


enum class MenuState {
    USERS_LIST, ABOUT_APP, ADD_USER, ADD_EVENT, EVENTS_LIST
}


@Composable
@Preview
fun App() {
    val menuState = remember { mutableStateOf(MenuState.USERS_LIST) }
    MaterialTheme {
        Column(modifier = Modifier.fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
            Main(menuState)
        }
    }
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Compose Database Admin",
        state = rememberWindowState(
            position = WindowPosition.Aligned(Alignment.Center),
            size = DpSize(800.dp, 800.dp)
        ),
        undecorated = false,
        resizable = true
    ) {
        App()
    }
}

@Composable
fun Main(menuState: MutableState<MenuState>, modifier: Modifier = Modifier) {
    Row(
        modifier = Modifier
            .fillMaxHeight()
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Left-side menu
        Box(
            modifier = Modifier
                .weight(2f)
                .fillMaxSize()
                .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
                .shadow(2.dp, RoundedCornerShape(4.dp))
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Buttons
                AddUserButton(menuState)
                UsersListButton(menuState)
                DividerLine()
                AddEventButton(menuState)
                EventsListButton(menuState)
                DividerLine()
                AboutAppButton(menuState)
            }
        }
        // Main Content
        Box(
            modifier = Modifier
                .weight(5f)
                .fillMaxSize()
                .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
                .shadow(2.dp, RoundedCornerShape(4.dp))
                .background(Color.White)
        ) {
            when (menuState.value) {
                MenuState.ADD_USER -> AddUserTab()
                MenuState.USERS_LIST -> UsersListTab()
                MenuState.ADD_EVENT -> AddEventTab()
                MenuState.EVENTS_LIST -> EventsListTab()
                MenuState.ABOUT_APP -> AboutAppTab()
            }
        }
    }
}


// BUTTONS
@Composable
fun AddUserButton(menuState: MutableState<MenuState>, modifier: Modifier = Modifier) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(Color.LightGray, RoundedCornerShape(4.dp))
            .clickable { menuState.value = MenuState.ADD_USER },
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
                tint = Color.Black
            )
            Spacer(modifier = Modifier.width(20.dp))
            Text(
                text = "Add User",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

        }
    }
}

@Composable
fun UsersListButton(menuState: MutableState<MenuState>, modifier: Modifier = Modifier) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(Color.LightGray, RoundedCornerShape(4.dp))
            .clickable { menuState.value = MenuState.USERS_LIST },
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.List,
                contentDescription = "List",
                tint = Color.Black
            )
            Spacer(modifier = Modifier.width(20.dp))
            Text(
                text = "Users",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}

@Composable
fun AddEventButton(menuState: MutableState<MenuState>, modifier: Modifier = Modifier) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(Color.LightGray, RoundedCornerShape(4.dp))
            .clickable { menuState.value = MenuState.ADD_EVENT },
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
                tint = Color.Black
            )
            Spacer(modifier = Modifier.width(20.dp))
            Text(
                text = "Add Event",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

        }
    }
}

@Composable
fun EventsListButton(menuState: MutableState<MenuState>, modifier: Modifier = Modifier) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(Color.LightGray, RoundedCornerShape(4.dp))
            .clickable { menuState.value = MenuState.EVENTS_LIST },
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.List,
                contentDescription = "List",
                tint = Color.Black
            )
            Spacer(modifier = Modifier.width(20.dp))
            Text(
                text = "Events",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}

@Composable
fun AboutAppButton(menuState: MutableState<MenuState>, modifier: Modifier = Modifier) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(Color.LightGray, RoundedCornerShape(4.dp))
            .clickable { menuState.value = MenuState.ABOUT_APP },
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Info",
                tint = Color.Black
            )
            Spacer(modifier = Modifier.width(20.dp))
            Text(
                text = "About",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}

// TABS
@Composable
fun AddUserTab() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        var username by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var firstname by remember { mutableStateOf("") }
        var lastname by remember { mutableStateOf("") }
        var showValidationDialog by remember { mutableStateOf(false) }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Add a new User",
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
            )
            Spacer(modifier = Modifier.height(30.dp))
            TextInputField(
                value = username,
                onValueChange = { username = it },
                label = "Username",
                icon = Icons.Filled.AccountCircle
            )
            TextInputField(
                value = firstname,
                onValueChange = { firstname = it },
                label = "First name",
                icon = Icons.Filled.Person
            )
            TextInputField(
                value = lastname,
                onValueChange = { lastname = it },
                label = "Last name",
                icon = Icons.Filled.PersonOutline
            )
            TextInputField(
                value = email,
                onValueChange = { email = it },
                label = "E-mail",
                icon = Icons.Filled.Email
            )
            PasswordInputField(
                value = password,
                onValueChange = { password = it },
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                if (username.isBlank() || firstname.isBlank() || lastname.isBlank() || email.isBlank() || password.isBlank()) {
                    showValidationDialog = true
                } else {
                    val user = CreateUser(username, firstname, lastname, email, password)
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val response = createUser(user)
                            println("User created successfully: $response")
                        } catch (e: Exception) {
                            println("Failed to create user: ${e.message}")
                        }
                    }
                }
            }) {
                Text("Add User")
            }
        }

        if (showValidationDialog) {
            AlertBox(
                title = "Unfilled form",
                text = "You have to complete the whole user creation form.",
                onDismiss = { showValidationDialog = false }
            )
        }
    }
}

@Composable
fun UsersListTab() {
    val users = remember { mutableStateOf<List<PublicUser>>(emptyList()) }
    val selectedUser = remember { mutableStateOf<PublicUser?>(null) }

    LaunchedEffect(Unit) {
        users.value = getAllUsers() ?: emptyList()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "All Users",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        users.value.forEach { user ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        selectedUser.value = user
                        println("Clicked on ${user.firstname} ${user.lastname}")

                    },
                elevation = 4.dp,
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Person Icon"
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("${user.firstname} ${user.lastname}")
                    }
                    Box(modifier = Modifier.align(Alignment.CenterVertically)) {
                        Text(user.username, color = Color.Gray)
                    }
                }
            }
        }
    }
    val userToEdit = selectedUser.value
    if (userToEdit != null) {
        EditUserModal(
            user = userToEdit,
            onDismiss = { selectedUser.value = null },
            onSave = { updatedUser ->
                // save logic
                println("Updated user: $updatedUser")
                selectedUser.value = null
            }
        )
    }

}

@Composable
fun AddEventTab() {
    Text("Add Event Tab")
}

@Composable
fun EventsListTab() {
    Text("Events List Tab")
}

@Composable
fun AboutAppTab() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "About application",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Application for database management")
            Text("Authors: Anej Bregant, Alen Kolmanič, Tom Li Dobnik")
        }
    }
}


// HELPER FUNCTIONS
@Composable
fun DividerLine() {
    Divider(
        color = Color.LightGray,
        thickness = 1.dp,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
fun TextInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = "$label Icon"
            )
        },
        modifier = Modifier
            .width(300.dp)
            .height(70.dp)
    )
}

@Composable
fun PasswordInputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var passwordVisibility by remember { mutableStateOf(false) }
    val icon = if (passwordVisibility) {
        Icons.Filled.Visibility
    } else {
        Icons.Filled.VisibilityOff
    }
    OutlinedTextField(
        value = value,
        label = { Text("Password") },
        onValueChange = onValueChange,
        singleLine = true,
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = "Lock Icon"
            )
        },
        trailingIcon = {
            IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                Icon(
                    imageVector = icon,
                    contentDescription = if (passwordVisibility) "Visibility Icon" else "VisibilityOff Icon"
                )
            }
        },
        visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
        modifier = Modifier
            .width(300.dp)
            .height(70.dp)
    )
}

@Composable
fun AlertBox(
    title: String,
    text: String,
    onDismiss: (() -> Unit)? = null,
) {
    AlertDialog(
        onDismissRequest = { onDismiss?.invoke() },
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                TextButton(
                    onClick = { onDismiss?.invoke() },
                    modifier = Modifier
                        .background(Color(0xFF6200EE), RoundedCornerShape(4.dp))
                ) {
                    Text("Ok", color = Color.White)
                }
            }

        }
    )
}

@Composable
fun EditUserModal(user: PublicUser, onDismiss: () -> Unit, onSave: (CreateUser) -> Unit) {
    var editedUsername by remember { mutableStateOf(user.username) }
    var editedFirstname by remember { mutableStateOf(user.firstname) }
    var editedLastname by remember { mutableStateOf(user.lastname) }
    var editedEmail by remember { mutableStateOf(user.email) }
    var editedPassword by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(8.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Edit User")

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = editedUsername,
                    onValueChange = { editedUsername = it },
                    label = { Text("Username") }
                )

                OutlinedTextField(
                    value = editedFirstname,
                    onValueChange = { editedFirstname = it },
                    label = { Text("First Name") }
                )

                OutlinedTextField(
                    value = editedLastname,
                    onValueChange = { editedLastname = it },
                    label = { Text("Last Name") }
                )

                OutlinedTextField(
                    value = editedEmail,
                    onValueChange = { editedEmail = it },
                    label = { Text("Email") }
                )

                OutlinedTextField(
                    value = editedPassword,
                    onValueChange = { editedPassword = it },
                    label = { Text("New Password") },
                    visualTransformation = PasswordVisualTransformation(),
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Button(onClick = {
                        if (editedPassword.isNotBlank()) {
                            onSave(
                                CreateUser(
                                    username = editedUsername,
                                    firstname = editedFirstname,
                                    lastname = editedLastname,
                                    email = editedEmail,
                                    password = editedPassword
                                )
                            )
                            onDismiss()
                        }
                    }) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

